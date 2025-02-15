package modules.fishery;

import java.util.List;
import constants.Settings;
import core.utils.MentionUtil;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryMemberData;

public class FisheryManage {

    public enum ValueProcedure { ABSOLUTE, ADD, SUB }

    public static boolean updateValues(List<FisheryMemberData> fisheryMemberList, int type, String inputString) {
        boolean success = false;
        ValueProcedure valueProcedure = ValueProcedure.ABSOLUTE;
        inputString = inputString
                .replace(" ", "")
                .replaceAll("(?i)lv\\.", "");

        if (inputString.startsWith("+")) {
            valueProcedure = ValueProcedure.ADD;
            inputString = inputString.substring(1);
        } else if (inputString.startsWith("-")) {
            valueProcedure = ValueProcedure.SUB;
            inputString = inputString.substring(1);
        }

        if (inputString.length() == 0 || !Character.isDigit(inputString.charAt(0))) {
            return false;
        }

        for (FisheryMemberData fisheryMemberData : fisheryMemberList) {
            long baseValue = getBaseValueByType(fisheryMemberData, type);
            long newValue = MentionUtil.getAmountExt(inputString, baseValue);
            if (newValue == -1) {
                continue;
            }

            newValue = calculateNewValue(fisheryMemberData.getGuildId(), baseValue, newValue, valueProcedure, type);
            setNewValues(fisheryMemberData, newValue, type);
            success = true;
        }

        return success;
    }

    private static void setNewValues(FisheryMemberData fisheryMemberBean, long newValue, int type) {
        switch (type) {
            /* fish */
            case 0 -> fisheryMemberBean.setFish(newValue);

            /* coins */
            case 1 -> fisheryMemberBean.setCoinsRaw(newValue + fisheryMemberBean.getCoinsHidden());

            /* daily streak */
            case 2 -> fisheryMemberBean.setDailyStreak(newValue);

            /* gear */
            default -> {
                fisheryMemberBean.setLevel(FisheryGear.values()[type - 3], (int) newValue);
                if (type == FisheryGear.ROLE.ordinal() + 3) {
                    Fishery.synchronizeRoles(fisheryMemberBean.getMember().get());
                }
            }
        }
    }

    private static long calculateNewValue(long guildId, long baseValue, long newValue, ValueProcedure valueProcedure, int type) {
        switch (valueProcedure) {
            case ADD:
                newValue = baseValue + newValue;
                break;

            case SUB:
                newValue = baseValue - newValue;
                break;

            default:
        }
        long maxValue = maxValueOfProperty(guildId, type);
        if (newValue < 0) newValue = 0;
        if (newValue > maxValue) newValue = maxValue;

        return newValue;
    }

    private static long getBaseValueByType(FisheryMemberData fisheryMemberBean, int type) {
        return switch (type) {
            case 0 -> fisheryMemberBean.getFish();
            case 1 -> fisheryMemberBean.getCoins();
            case 2 -> fisheryMemberBean.getDailyStreak();
            default -> fisheryMemberBean.getMemberGear(FisheryGear.values()[type - 3]).getLevel();
        };
    }

    private static long maxValueOfProperty(long guildId, int i) {
        if (i <= 2) {
            return Settings.FISHERY_MAX;
        } else if (i == FisheryGear.ROLE.ordinal() + 3) {
            return DBFishery.getInstance().retrieve(guildId).getRoles().size();
        } else {
            return Settings.FISHERY_GEAR_MAX;
        }
    }

}
