package MySQL.Modules.FisheryUsers;

import Constants.CodeBlockColor;
import Constants.FisheryCategoryInterface;
import Constants.LogStatus;
import Constants.Settings;
import Core.*;
import Core.Utils.StringUtil;
import Core.Utils.TimeUtil;
import MySQL.BeanWithServer;
import MySQL.Modules.Server.ServerBean;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FisheryUserBean extends BeanWithServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(FisheryUserBean.class);

    private final long userId;
    private FisheryServerBean fisheryServerBean = null;
    private final HashMap<Instant, FisheryHourlyIncomeBean> fisheryHourlyIncomeMap;
    private final HashMap<Integer, FisheryUserPowerUpBean> powerUpMap;
    private long fish, coins, dailyStreak;
    private LocalDate dailyReceived;
    private int upvoteStack, lastMessagePeriod = -1, lastMessageHour = -1, vcMinutes = 0;
    private boolean reminderSent, changed = false, banned = false;
    private Boolean onServer = null;
    private Long fishIncome = null;
    private Instant fishIncomeUpdateTime = null;
    private long hiddenCoins = 0, messagesThisHour = 0;
    private String lastContent = null;

    FisheryUserBean(ServerBean serverBean, long userId, long fish, long coins, LocalDate dailyReceived, long dailyStreak, boolean reminderSent, int upvoteStack, HashMap<Instant, FisheryHourlyIncomeBean> fisheryHourlyIncomeMap, HashMap<Integer, FisheryUserPowerUpBean> powerUpMap) {
        super(serverBean);
        this.userId = userId;
        this.fish = fish;
        this.coins = coins;
        this.dailyReceived = dailyReceived;
        this.dailyStreak = dailyStreak;
        this.reminderSent = reminderSent;
        this.upvoteStack = upvoteStack;
        this.fisheryHourlyIncomeMap = fisheryHourlyIncomeMap;
        this.powerUpMap = powerUpMap;

        for(int i = 0; i < 6; i++) this.powerUpMap.putIfAbsent(i, new FisheryUserPowerUpBean(serverBean.getServerId(), userId, i, 0));
    }

    public FisheryUserBean(ServerBean serverBean, long userId, FisheryServerBean fisheryServerBean, long fish, long coins, LocalDate dailyReceived, int dailyStreak, boolean reminderSent, int upvoteStack, HashMap<Instant, FisheryHourlyIncomeBean> fisheryHourlyIncomeMap, HashMap<Integer, FisheryUserPowerUpBean> powerUpMap) {
        this(serverBean, userId, fish, coins, dailyReceived, dailyStreak, reminderSent, upvoteStack, fisheryHourlyIncomeMap, powerUpMap);
        setFisheryServerBean(fisheryServerBean);
    }


    /* Getters */

    public long getUserId() { return userId; }

    public Optional<User> getUser() { return getServer().flatMap(server -> server.getMemberById(userId)); }

    public FisheryServerBean getFisheryServerBean() { return fisheryServerBean; }

    public HashMap<Integer, FisheryUserPowerUpBean> getPowerUpMap() { return powerUpMap; }

    public FisheryUserPowerUpBean getPowerUp(int powerUpId) { return powerUpMap.computeIfAbsent(powerUpId, k -> new FisheryUserPowerUpBean(getServerId(), userId, powerUpId, 0)); }

    public List<FisheryHourlyIncomeBean> getAllFishHourlyIncomeChanged() {
        return fisheryHourlyIncomeMap.values().stream()
                .filter(FisheryHourlyIncomeBean::checkChanged)
                .collect(Collectors.toList());
    }

    public long getFish() { return fish; }

    public long getCoins() { return coins - hiddenCoins; }

    public long getCoinsRaw() { return coins; }

    public int getRank() {
        try {
            return (int) (fisheryServerBean.getUsers().values().stream()
                    .filter(user -> user.isOnServer() && userIsRankedHigherThanMe(user))
                    .count() + 1);
        } catch (ConcurrentModificationException e) {
            LOGGER.error("Concurrent modification exception", e);
            return 0;
        }
    }

    private boolean userIsRankedHigherThanMe(FisheryUserBean user) {
        return (user.getFishIncome() > getFishIncome()) ||
                (user.getFishIncome() == getFishIncome() && user.getFish() > getFish()) ||
                (user.getFishIncome() == getFishIncome() && user.getFish() == getFish() && user.getCoins() > getCoins());
    }

    public long getFishIncome() {
        Instant currentHourInstance = TimeUtil.instantRoundDownToHour(Instant.now());
        if (fishIncome == null || fishIncomeUpdateTime == null || fishIncomeUpdateTime.isBefore(currentHourInstance)) {
            try {
                long n = 0;

                Instant effectiveInstant = currentHourInstance.minus(7, ChronoUnit.DAYS);
                for (Iterator<FisheryHourlyIncomeBean> iterator = fisheryHourlyIncomeMap.values().iterator(); iterator.hasNext(); ) {
                    FisheryHourlyIncomeBean fisheryHourlyIncomeBean = iterator.next();
                    if (fisheryHourlyIncomeBean.getTime().isBefore(effectiveInstant)) iterator.remove();
                    else n += fisheryHourlyIncomeBean.getFishIncome();
                }

                fishIncome = n;
                fishIncomeUpdateTime = currentHourInstance;
                checkValuesBound();
            } catch (Throwable e) {
                LOGGER.error("Exception", e);
            }
        }

        return fishIncome;
    }

    private FisheryHourlyIncomeBean getCurrentFisheryHourlyIncome() {
        Instant currentTimeHour = TimeUtil.instantRoundDownToHour(Instant.now());
        return fisheryHourlyIncomeMap.computeIfAbsent(currentTimeHour, k -> new FisheryHourlyIncomeBean(getServerId(), userId, currentTimeHour, 0));
    }

    public LocalDate getDailyReceived() { return dailyReceived; }

    public long getDailyStreak() { return dailyStreak; }

    public int getUpvoteStack() { return upvoteStack; }

    public boolean isReminderSent() { return reminderSent; }

    public boolean isBanned() { return banned; }


    /* Setters */

    void setFisheryServerBean(FisheryServerBean fisheryServerBean) {
        if (this.fisheryServerBean == null) {
            this.fisheryServerBean = fisheryServerBean;
        }
    }

    public boolean registerMessage(Message message, ServerTextChannel channel) throws ExecutionException, InterruptedException {
        if (banned) return false;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        messagesThisHour++;
        if (messagesThisHour >= 3400) {
            banned = true;
            LOGGER.warn("### User temporarely banned with id " + userId);
            return false;
        }

        if (lastMessageHour != hour) {
            lastMessageHour = hour;
            messagesThisHour = 0;
        }

        if (!message.getContent().equalsIgnoreCase(lastContent)) {
            lastContent = message.getContent();
            int currentMessagePeriod = (Calendar.getInstance().get(Calendar.SECOND) + Calendar.getInstance().get(Calendar.MINUTE) * 60) / 20;
            if (currentMessagePeriod != lastMessagePeriod) {
                lastMessagePeriod = currentMessagePeriod;
                long effect = getPowerUp(FisheryCategoryInterface.PER_MESSAGE).getEffect();

                fish += effect;
                if (fishIncome != null) fishIncome += effect;
                getCurrentFisheryHourlyIncome().add(effect);
                checkValuesBound();
                setChanged();

                Optional<User> userOpt = getUser();
                if (fish >= 100 &&
                        !reminderSent &&
                        getServerBean().isFisheryReminders() &&
                        channel.canYouWrite() &&
                        channel.canYouEmbedLinks() &&
                        userOpt.isPresent()
                ) {
                    reminderSent = true;
                    User user = userOpt.get();
                    Locale locale = getServerBean().getLocale();
                    String prefix = getServerBean().getPrefix();

                    Message message1 = channel.sendMessage(user.getMentionTag(), EmbedFactory.getEmbed()
                            .setAuthor(user)
                            .setTitle(TextManager.getString(locale, TextManager.GENERAL, "hundret_joule_collected_title"))
                            .setDescription(TextManager.getString(locale, TextManager.GENERAL, "hundret_joule_collected_description").replace("%PREFIX", prefix))
                            .setFooter(TextManager.getString(locale, TextManager.GENERAL, "hundret_joule_collected_footer").replace("%PREFIX", prefix))).get();

                    new CustomThread(() -> {
                        try {
                            Thread.sleep(Settings.FISHERY_DESPAWN_MINUTES * 60 * 1000);
                            message1.delete();
                        } catch (InterruptedException e) {
                            LOGGER.error("Interrupted", e);
                        }
                    }, "fishery_100_countdown", 1).start();
                }

                return true;
            }
        }
        return false;
    }

    public void registerVC(int minutes) throws ExecutionException {
        if (!banned) {
            Optional<Integer> limitOpt = getServerBean().getFisheryVcHoursCap();
            if (limitOpt.isPresent() && ServerPatreonBoostCache.getInstance().get(getServerId()))
                minutes = Math.min(minutes, limitOpt.get() * 60 - vcMinutes);

            if (minutes > 0) {
                long effect = getPowerUp(FisheryCategoryInterface.PER_VC).getEffect() * minutes;

                fish += effect;
                if (fishIncome != null) fishIncome += effect;
                getCurrentFisheryHourlyIncome().add(effect);
                vcMinutes += minutes;

                checkValuesBound();
                setChanged();
            }
        }
    }

    public EmbedBuilder getAccountEmbed() {
        return changeValues(0, 0);
    }

    public void setFish(long fish) {
        if (this.fish != fish) {
            this.fish = fish;
            checkValuesBound();
            setChanged();
        }
    }

    public void addFish(long fish) {
        if (fish != 0) {
            this.fish += fish;
            if (fish > 0) {
                if (fishIncome != null) fishIncome += fish;
                getCurrentFisheryHourlyIncome().add(fish);
            }
            checkValuesBound();
            setChanged();
        }
    }

    public void setCoinsRaw(long coins) {
        if (this.coins != coins) {
            this.coins = coins;
            checkValuesBound();
            setChanged();
        }
    }

    public void addCoins(long coins) {
        if (coins != 0) {
            this.coins += coins;
            checkValuesBound();
            setChanged();
        }
    }

    public void setDailyStreak(long dailyStreak) {
        if (this.dailyStreak != dailyStreak) {
            this.dailyStreak = dailyStreak;
            checkValuesBound();
            setChanged();
        }
    }

    public EmbedBuilder changeValues(long fishAdd, long coinsAdd) {
        return changeValues(fishAdd, coinsAdd, null);
    }

    public synchronized EmbedBuilder changeValues(long fishAdd, long coinsAdd, Long newDailyStreak) {
        /* Collect Current Data */
        long fishIncomePrevious = getFishIncome();
        long fishPrevious = getFish();
        long coinsPrevious = getCoins();
        long rankPrevious = getRank();
        long dailyStreakPrevious = getDailyStreak();

        /* Update Changes */
        addFish(fishAdd);
        addCoins(coinsAdd);
        if (newDailyStreak != null) dailyStreak = newDailyStreak;

        long rank = getRank();

        /* Generate Account Embed */
        Optional<Server> serverOpt = getServer();
        Optional<User> userOpt = serverOpt.flatMap(server -> server.getMemberById(userId));
        Locale locale = getServerBean().getLocale();

        return userOpt
                .map(user -> generateUserChangeEmbed(serverOpt.get(), user, locale, fishAdd, coinsAdd, rank, rankPrevious, fishIncomePrevious, fishPrevious, coinsPrevious, newDailyStreak, dailyStreakPrevious))
                .orElse(null);
    }

    private EmbedBuilder generateUserChangeEmbed(Server server, User user, Locale locale, long fishAdd, long coinsAdd,
                                                 long rank, long rankPrevious, long fishIncomePrevious, long fishPrevious, long coinsPrevious, Long newDailyStreak, long dailyStreakPrevious
    ) {
        boolean patron = PatreonCache.getInstance().getPatreonLevel(userId) >= 1;

        String patreonEmoji = "👑";
        String displayName = user.getDisplayName(server);
        while (displayName.length() > 0 && displayName.startsWith(patreonEmoji)) displayName = displayName.substring(patreonEmoji.length());

        EmbedBuilder eb = EmbedFactory.getEmbed()
                .setAuthor(TextManager.getString(locale, TextManager.GENERAL, "rankingprogress_title", patron, displayName, patreonEmoji), "", user.getAvatar())
                .setThumbnail(user.getAvatar());

        if (patron) eb.setColor(Color.YELLOW);
        if (fishAdd > 0 || (fishAdd == 0 && coinsAdd > 0))
            eb.setColor(Color.GREEN);
        else if (coinsAdd <= 0 && (fishAdd < 0 || coinsAdd < 0))
            eb.setColor(Color.RED);

        String codeBlock = CodeBlockColor.WHITE;
        if (rank < rankPrevious)
            codeBlock = CodeBlockColor.GREEN;
        else if (rank > rankPrevious)
            codeBlock = CodeBlockColor.RED;

        eb.setDescription(TextManager.getString(locale, TextManager.GENERAL, "rankingprogress_desription",
                getEmbedSlot(locale, fishIncome, fishIncomePrevious, false),
                getEmbedSlot(locale, getFish(), fishPrevious, false),
                getEmbedSlot(locale, getCoins(), coinsPrevious, false),
                getEmbedSlot(locale, getDailyStreak(), newDailyStreak != null ? dailyStreakPrevious : getDailyStreak(), false),
                getEmbedSlot(locale, rank, rankPrevious, true),
                codeBlock
        ));

        if (banned) EmbedFactory.addLog(eb, LogStatus.FAILURE, TextManager.getString(locale, TextManager.GENERAL, "banned"));

        return eb;
    }

    private String getEmbedSlot(Locale locale, long numberNow, long numberPrevious, boolean rankSlot) {
        long diff = numberNow - numberPrevious;
        String diffSign = diff >= 0 ? "+" : "";
        return TextManager.getString(locale, TextManager.GENERAL, rankSlot ? "rankingprogress_update2" : "rankingprogress_update", diff != 0,
                StringUtil.numToString(locale, numberPrevious),
                StringUtil.numToString(locale, numberNow),
                diffSign + StringUtil.numToString(locale, diff)
        );
    }

    private void checkValuesBound() {
        if (fish > Settings.MAX) fish = Settings.MAX;
        else if (fish < 0) fish = 0;

        if (coins > Settings.MAX) coins = Settings.MAX;
        else if (coins < 0) coins = 0;

        if (fishIncome != null) {
            if (fishIncome > Settings.MAX) fishIncome = Settings.MAX;
            else if (fishIncome < 0) fishIncome = 0L;
        }

        if (dailyStreak > Settings.MAX) dailyStreak = Settings.MAX;
        if (dailyStreak < 0) dailyStreak = 0;
    }

    public void levelUp(int powerUpId) {
        getPowerUp(powerUpId).setLevel(getPowerUp(powerUpId).getLevel() + 1);
        setChanged();
    }

    public void setLevel(int powerUpId, int level) {
        getPowerUp(powerUpId).setLevel(level);
        setChanged();
    }

    public void updateDailyReceived() {
        if (!LocalDate.now().equals(dailyReceived)) {
            dailyReceived = LocalDate.now();
            checkValuesBound();
            setChanged();
        }
    }

    public void increaseDailyStreak() {
        dailyStreak++;
        setChanged();
    }

    public void resetDailyStreak() {
        dailyStreak = 0;
        setChanged();
    }

    public void addUpvote(int upvotes) {
        if (upvotes > 0) {
            upvoteStack += upvotes;
            setChanged();
        }
    }

    public void addHiddenCoins(long amount) {
        hiddenCoins = Math.max(0, Math.min(coins, hiddenCoins + amount));
    }

    public void clearUpvoteStack() {
        if (upvoteStack > 0) {
            upvoteStack = 0;
            setChanged();
        }
    }

    public boolean isOnServer() {
        if (onServer == null) {
            onServer = getServer().get().getMembers().stream().anyMatch(user -> user.getId() == userId);
        }

        return onServer;
    }

    public void setOnServer(boolean onServer) {
        this.onServer = onServer;
    }

    public void remove() {
        getFisheryServerBean().getUsers().remove(userId);
        DBFishery.getInstance().removeFisheryUserBean(this);
    }

    public boolean checkChanged() {
        boolean changedTemp = changed;
        changed = false;
        return changedTemp;
    }

    public void setChanged() {
        fisheryServerBean.update();
        changed = true;
    }

}