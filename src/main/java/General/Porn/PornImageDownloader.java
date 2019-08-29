package General.Porn;

import General.Comment;
import General.Shortcuts;
import General.Tools;
import General.Internet.URLDataContainer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Random;

public class PornImageDownloader {
    public static PornImage getPicture(String domain, String searchTerm, String imageTemplate, boolean gifOnly) throws Throwable {
        searchTerm = URLEncoder.encode(searchTerm, "UTF-8");

        String url = "https://"+domain+"/index.php?page=dapi&s=post&q=index&tags="+searchTerm;
        String data = URLDataContainer.getInstance().getData(url, Instant.now().plusSeconds(60 * 60));

        int count = Math.min(200*100, Integer.parseInt(Tools.cutString(data,"count=\"","\"")));

        if (count == 0) {
            return null;
        }

        Random r = new Random();
        int page = r.nextInt(count)/100;
        if (searchTerm.length() == 0) page = 0;

        return getPictureOnPage(domain, searchTerm, page, imageTemplate, gifOnly);
    }

    private static PornImage getPictureOnPage(String domain, String searchTerm, int page, String imageTemplate, boolean gifOnly) throws Throwable {
        String url = "https://"+domain+"/index.php?page=dapi&s=post&q=index&json=1&tags="+searchTerm+"&pid="+page;
        String dataString = URLDataContainer.getInstance().getData(url, Instant.now().plusSeconds(60 * 60));

        if (dataString == null || dataString.length() == 0) return null;

        JSONArray data = new JSONArray(dataString);

        int count = Math.min(data.length(),100);

        if (count == 0) {
            return null;
        }

        Random r = new Random();
        ArrayList<Long> scoreList = new ArrayList<>();
        ArrayList<Integer> posList = new ArrayList<>();
        long totalScore = 0;
        for(int j=0; j<2; j++) {
            for (int i = 0; i < count; i++) {
                JSONObject postData = data.getJSONObject(i);
                String fileUrl;
                if (postData.has("file_url")) fileUrl = postData.getString("file_url");
                else fileUrl = postData.getString("image");
                String tags = postData.getString("tags");
                String rating = postData.getString("rating");
                //boolean noLoli = !tags.toLowerCase().contains("loli") && !tags.toLowerCase().contains("shota");
                long score;
                if (Tools.UrlContainsImage(fileUrl) && (rating.equalsIgnoreCase("e") || j==1) && (!gifOnly || fileUrl.endsWith("gif"))) {
                    score = (long) Math.pow(postData.getInt("score")+1, 2);
                } else {
                    score = 0;
                }
                scoreList.add(score);
                posList.add(i);
                totalScore += score;
            }

            if (totalScore > 0) break;
        }

        if (scoreList.size() == 0) return null;

        long pos = (long) (r.nextDouble()*totalScore);
        for(int i=0; i<scoreList.size(); i++) {
            pos -= scoreList.get(i);
            if (pos < 0) return getSpecificPictureOnPage(domain, data, posList.get(i), imageTemplate);
        }

        return null;
    }

    private static PornImage getSpecificPictureOnPage(String domain, JSONArray data, int pos, String imageTemplate) throws Throwable {
        JSONObject postData = data.getJSONObject(pos);

        String postURL = "https://"+domain+"/index.php?page=post&s=view&id=" + postData.getInt("id");
        String commentURL = "https://"+domain+"/index.php?page=dapi&s=comment&q=index&post_id=" + postData.getInt("id");

        String commentsDataString = URLDataContainer.getInstance().getData(commentURL , Instant.now().plusSeconds(60 * 60));

        ArrayList<Comment> comments = new ArrayList<>();
        while(commentsDataString.contains("creator=\"")) {
            String author = Shortcuts.decryptString(Tools.cutString(commentsDataString, "creator=\"", "\""));
            String content = Shortcuts.decryptString(Tools.cutString(commentsDataString, "body=\"", "\"")).replace("[spoiler]", "||").replace("[/spoiler]", "||");
            commentsDataString = commentsDataString.replaceFirst("creator=\"", "").replaceFirst("body=\"", "");
            comments.add(new Comment(author, content));
        }

        Instant instant;

        if (postData.has("created_at")) {
            String[] timeString = postData.getString("created_at").split(" ");

            int month = 0;
            String monthString = timeString[1];
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (int i = 0; i < 12; i++) {
                if (monthString.equalsIgnoreCase(monthNames[i])) {
                    month = i + 1;
                    break;
                }
            }

            LocalDateTime ldt1 = LocalDateTime.now()
                    .withYear(Integer.parseInt(timeString[5]))
                    .withMonth(month)
                    .withDayOfMonth(Integer.parseInt(timeString[2]))
                    .withHour(Integer.parseInt(timeString[3].split(":")[0]))
                    .withMinute(Integer.parseInt(timeString[3].split(":")[1]))
                    .withSecond(Integer.parseInt(timeString[3].split(":")[2]));

            instant = ldt1.atZone(ZoneOffset.UTC).toInstant();
        } else instant = Instant.now();

        String fileURL;
        if (postData.has("file_url")) fileURL = postData.getString("file_url");
        else fileURL = imageTemplate.replace("%d", postData.getString("directory")).replace("%f", postData.getString("image"));

        if (fileURL.contains("?")) fileURL = fileURL.split("\\?")[0];


        return new PornImage(fileURL, postURL, comments, postData.getInt("score"), comments.size(), instant);
    }
}
