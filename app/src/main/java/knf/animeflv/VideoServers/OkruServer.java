package knf.animeflv.VideoServers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import knf.animeflv.JsonFactory.Objects.Option;
import knf.animeflv.JsonFactory.Objects.VideoServer;

import static knf.animeflv.JsonFactory.Objects.VideoServer.Names.OKRU;

/**
 * Created by Jordy on 24/12/2017.
 */

public class OkruServer extends Server {
    public OkruServer(Context context, String baseLink) {
        super(context, baseLink);
    }

    @Override
    public boolean isValid() {
        return baseLink.contains("ok.ru");
    }

    @Override
    public String getName() {
        return VideoServer.Names.OKRU;
    }

    @Nullable
    @Override
    VideoServer getVideoServer() {
        try {
            String frame = baseLink.substring(baseLink.indexOf("'") + 1, baseLink.lastIndexOf("'"));
            String down_link = "http:" + Jsoup.parse(frame).select("iframe").first().attr("src");
            String e_json = Jsoup.connect(down_link).get().select("div[data-module='OKVideo']").first().attr("data-options");
            String cut_json = "{" + e_json.substring(e_json.lastIndexOf("\\\"videos"), e_json.indexOf(",\\\"metadataEmbedded")).replace("\\&quot;", "\"").replace("\\u0026", "&").replace("\\", "") + "}";
            JSONArray array = new JSONObject(cut_json).getJSONArray("videos");
            VideoServer videoServer = new VideoServer(OKRU);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                if (object.getString("name").equals("sd")) {
                    videoServer.addOption(new Option("SD", object.getString("url")));
                } else if (object.getString("name").equals("hd")) {
                    videoServer.addOption(new Option("HD", object.getString("url")));
                }
            }
            return videoServer;
        } catch (Exception e) {
            return null;
        }
    }
}
