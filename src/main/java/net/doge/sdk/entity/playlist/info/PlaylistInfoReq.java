package net.doge.sdk.entity.playlist.info;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import net.doge.constant.async.GlobalExecutors;
import net.doge.constant.system.NetMusicSource;
import net.doge.model.entity.NetMusicInfo;
import net.doge.model.entity.NetPlaylistInfo;
import net.doge.sdk.common.CommonResult;
import net.doge.sdk.common.SdkCommon;
import net.doge.sdk.util.SdkUtil;
import net.doge.util.common.StringUtil;
import net.doge.util.common.TimeUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaylistInfoReq {
    // 歌单信息 API
    private final String PLAYLIST_DETAIL_API = SdkCommon.prefix + "/playlist/detail?id=%s";
    // 歌单歌曲 API
    private final String PLAYLIST_SONGS_API = SdkCommon.prefix + "/playlist/track/all?id=%s&offset=%s&limit=%s";
    // 歌单信息 API (酷狗)
    private final String PLAYLIST_DETAIL_KG_API = "https://mobiles.kugou.com/api/v5/special/info_v2?appid=1058&specialid=0&global_specialid=%s&format=jsonp&srcappid=2919&clientver=20000&clienttime=1586163242519&mid=1586163242519&uuid=1586163242519&dfid=-&signature=%s";
    private final String PLAYLIST_SONGS_KG_API = "https://mobiles.kugou.com/api/v5/special/song_v2?appid=1058&global_specialid=%s&specialid=0&plat=0&version=8000&page=%s&pagesize=%s&srcappid=2919&clientver=20000&clienttime=1586163263991&mid=1586163263991&uuid=1586163263991&dfid=-&signature=%s";
    //    private final String PLAYLIST_DETAIL_KG_API = "https://m.kugou.com/plist/list/%s?json=true&page=%s";
    // 歌单信息 API (QQ)
    private final String PLAYLIST_DETAIL_QQ_API = SdkCommon.prefixQQ33 + "/songlist?id=%s";
    // 歌单信息 API (酷我)
    private final String PLAYLIST_DETAIL_KW_API = "http://www.kuwo.cn/api/www/playlist/playListInfo?pid=%s&pn=%s&rn=%s&httpsStatus=1";
    // 歌单信息 API (咪咕)
//    private final String PLAYLIST_DETAIL_MG_API = prefixMg + "/playlist?id=%s";
    private final String PLAYLIST_DETAIL_MG_API = "https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do?needSimple=00&resourceType=2021&resourceId=%s";
    // 歌单歌曲 API (咪咕)
    private final String PLAYLIST_SONGS_MG_API = "https://app.c.nf.migu.cn/MIGUM2.0/v1.0/user/queryMusicListSongs.do?musicListId=%s&pageNo=%s&pageSize=%s";
    // 歌单信息 API (千千)
    private final String PLAYLIST_DETAIL_QI_API = "https://music.91q.com/v1/tracklist/info?appid=16073360&id=%s&pageNo=%s&pageSize=%s&timestamp=%s";
    // 歌单信息 API (5sing)
    private final String PLAYLIST_DETAIL_FS_API = "http://5sing.kugou.com/%s/dj/%s.html";
    // 歌单信息 API (猫耳)
    private final String PLAYLIST_DETAIL_ME_API = "https://www.missevan.com/sound/soundAllList?albumid=%s";
    // 歌单信息 API (哔哩哔哩)
    private final String PLAYLIST_DETAIL_BI_API = "https://www.bilibili.com/audio/music-service-c/web/menu/info?sid=%s";
    // 歌单歌曲 API (哔哩哔哩)
    private final String PLAYLIST_SONGS_BI_API = "https://www.bilibili.com/audio/music-service-c/web/song/of-menu?sid=%s&pn=%s&ps=%s";

    /**
     * 根据歌单 id 和 source 预加载歌单信息
     */
    public void preloadPlaylistInfo(NetPlaylistInfo playlistInfo) {
        // 信息完整直接跳过
        if (playlistInfo.isIntegrated()) return;

        GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImgThumb(SdkUtil.extractCover(playlistInfo.getCoverImgThumbUrl())));
    }

    /**
     * 根据歌单 id 获取歌单
     */
    public CommonResult<NetPlaylistInfo> getPlaylistInfo(int source, String id) {
        List<NetPlaylistInfo> res = new LinkedList<>();
        Integer t = 1;

        // 网易云
        if (source == NetMusicSource.NET_CLOUD) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("playlist");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                JSONObject ct = playlistJson.optJSONObject("creator");

                String playlistId = playlistJson.getString("id");
                String name = playlistJson.getString("name");
                String creator = ct != null ? ct.getString("nickname") : "";
                String creatorId = ct != null ? ct.getString("userId") : "";
                Integer trackCount = playlistJson.getInt("trackCount");
                Long playCount = playlistJson.getLong("playCount");
                String coverImgThumbUrl = playlistJson.getString("coverImgUrl");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setCreatorId(creatorId);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 酷狗
        else if (source == NetMusicSource.KG) {
            HttpResponse resp = HttpRequest.get(String.format(PLAYLIST_DETAIL_KG_API, id,
                            StringUtil.toMD5("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwtappid=1058clienttime=1586163242519clientver=20000dfid=-format=jsonpglobal_specialid="
                                    + id + "mid=1586163242519specialid=0srcappid=2919uuid=1586163242519NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt")))
                    .header("mid", "1586163242519")
                    .header("Referer", "https://m3ws.kugou.com/share/index.php")
                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
                    .header("dfid", "-")
                    .header("clienttime", "1586163242519")
                    .execute();
            if (resp.getStatus() == HttpStatus.HTTP_OK) {
                String playlistInfoBody = resp.body();
                JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
                JSONObject playlistJson = playlistInfoJson.optJSONObject("data");
                if (playlistJson != null && !playlistJson.isEmpty()) {
                    String playlistId = playlistJson.getString("specialid");
                    String name = playlistJson.getString("specialname");
                    String creator = playlistJson.getString("nickname");
                    Integer trackCount = playlistJson.getInt("songcount");
                    Long playCount = playlistJson.getLong("playcount");
                    String coverImgThumbUrl = playlistJson.getString("imgurl").replace("/{size}", "");

                    NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                    playlistInfo.setSource(NetMusicSource.KG);
                    playlistInfo.setId(playlistId);
                    playlistInfo.setName(name);
                    playlistInfo.setCreator(creator);
                    playlistInfo.setTrackCount(trackCount);
                    playlistInfo.setPlayCount(playCount);
                    playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                    GlobalExecutors.imageExecutor.execute(() -> {
                        BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                        playlistInfo.setCoverImgThumb(coverImgThumb);
                    });

                    res.add(playlistInfo);
                }
            }
        }

        // QQ
        else if (source == NetMusicSource.QQ) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_QQ_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("data");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                String playlistId = playlistJson.getString("disstid");
                String name = playlistJson.getString("dissname");
                String creator = playlistJson.getString("nickname");
                Long playCount = playlistJson.getLong("visitnum");
                Integer trackCount = playlistJson.getInt("songnum");
                String coverImgThumbUrl = playlistJson.getString("logo");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.QQ);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 酷我
        else if (source == NetMusicSource.KW) {
            String playlistInfoBody = SdkCommon.kwRequest(String.format(PLAYLIST_DETAIL_KW_API, id, 1, 1))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("data");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                String playlistId = playlistJson.getString("id");
                String name = playlistJson.getString("name");
                String creator = playlistJson.getString("userName");
                Long playCount = playlistJson.getLong("listencnt");
                Integer trackCount = playlistJson.getInt("total");
                String coverImgThumbUrl = playlistJson.getString("img");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.KW);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 咪咕
        else if (source == NetMusicSource.MG) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_MG_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONArray resource = playlistInfoJson.optJSONArray("resource");
            if (resource != null) {
                JSONObject playlistJson = resource.getJSONObject(0);

                String playlistId = playlistJson.getString("musicListId");
                String name = playlistJson.getString("title");
                String creator = playlistJson.getString("ownerName");
                String creatorId = playlistJson.getString("ownerId");
                Long playCount = playlistJson.getJSONObject("opNumItem").getLong("playNum");
                Integer trackCount = playlistJson.getInt("musicNum");
                String coverImgThumbUrl = playlistJson.getJSONObject("imgItem").getString("img");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.MG);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setCreatorId(creatorId);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 千千
        else if (source == NetMusicSource.QI) {
            String playlistInfoBody = HttpRequest.get(SdkCommon.buildQianUrl(String.format(PLAYLIST_DETAIL_QI_API, id, 1, 1, System.currentTimeMillis())))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("data");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                String playlistId = playlistJson.getString("id");
                String name = playlistJson.getString("title");
                Integer trackCount = playlistJson.getInt("trackCount");
                String coverImgThumbUrl = playlistJson.getString("pic");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.QI);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 猫耳
        else if (source == NetMusicSource.ME) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_ME_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("info");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                JSONObject album = playlistJson.getJSONObject("album");

                String playlistId = album.getString("id");
                String name = album.getString("title");
                String creator = album.getString("username");
                String creatorId = album.getString("user_id");
                Integer trackCount = album.getInt("music_count");
                Long playCount = album.getLong("view_count");
                String coverImgThumbUrl = album.getString("front_cover");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.ME);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setCreatorId(creatorId);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        // 哔哩哔哩
        else if (source == NetMusicSource.BI) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_BI_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.optJSONObject("data");
            if (playlistJson != null && !playlistJson.isEmpty()) {
                JSONObject stat = playlistJson.getJSONObject("statistic");

                String playlistId = playlistJson.getString("menuId");
                String name = playlistJson.getString("title");
                String creator = playlistJson.getString("uname");
                String creatorId = playlistJson.getString("uid");
                Integer trackCount = playlistJson.optInt("snum", -1);
                Long playCount = stat.getLong("play");
                String coverImgThumbUrl = playlistJson.getString("cover");

                NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
                playlistInfo.setSource(NetMusicSource.BI);
                playlistInfo.setId(playlistId);
                playlistInfo.setName(name);
                playlistInfo.setCreator(creator);
                playlistInfo.setCreatorId(creatorId);
                playlistInfo.setTrackCount(trackCount);
                playlistInfo.setPlayCount(playCount);
                playlistInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    playlistInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(playlistInfo);
            }
        }

        return new CommonResult<>(res, t);
    }

    /**
     * 根据歌单 id 补全歌单信息(包括封面图、描述)
     */
    public void fillPlaylistInfo(NetPlaylistInfo playlistInfo) {
        // 信息完整直接跳过
        if (playlistInfo.isIntegrated()) return;

        int source = playlistInfo.getSource();
        String id = playlistInfo.getId();
        String creatorId = playlistInfo.getCreatorId();

        // 网易云
        if (source == NetMusicSource.NET_CLOUD) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.getJSONObject("playlist");
            JSONObject ct = playlistJson.optJSONObject("creator");

            String coverImgUrl = playlistJson.getString("coverImgUrl");
            String description = playlistJson.getString("description");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description.equals("null") ? "" : description);
            if (!playlistInfo.hasCreator())
                playlistInfo.setCreator(ct != null ? ct.getString("nickname") : "");
            if (!playlistInfo.hasCreatorId())
                playlistInfo.setCreatorId(ct != null ? ct.getString("userId") : "");
            if (!playlistInfo.hasTag())
                playlistInfo.setTag(SdkUtil.parseTags(playlistJson, NetMusicSource.NET_CLOUD));
            if (!playlistInfo.hasTrackCount())
                playlistInfo.setTrackCount(playlistJson.getInt("trackCount"));
        }

        // 酷狗
        else if (source == NetMusicSource.KG) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_KG_API, id,
                            StringUtil.toMD5("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwtappid=1058clienttime=1586163242519clientver=20000dfid=-format=jsonpglobal_specialid="
                                    + id + "mid=1586163242519specialid=0srcappid=2919uuid=1586163242519NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt")))
                    .header("mid", "1586163242519")
                    .header("Referer", "https://m3ws.kugou.com/share/index.php")
                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
                    .header("dfid", "-")
                    .header("clienttime", "1586163242519")
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");

            String coverImgUrl = data.getString("imgurl").replace("/{size}", "");
            String description = data.getString("intro");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            playlistInfo.setTag("");
            if (!playlistInfo.hasTrackCount()) playlistInfo.setTrackCount(data.getInt("songcount"));
        }

        // QQ
        else if (source == NetMusicSource.QQ) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_QQ_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");

            String coverImgUrl = data.getString("logo");
            String description = data.getString("desc").replace("<br>", "\n");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(SdkUtil.parseTags(data, NetMusicSource.QQ));
            if (!playlistInfo.hasTrackCount()) playlistInfo.setTrackCount(data.getInt("songnum"));
        }

        // 酷我
        else if (source == NetMusicSource.KW) {
            String playlistInfoBody = SdkCommon.kwRequest(String.format(PLAYLIST_DETAIL_KW_API, id, 1, 1))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");

            String coverImgUrl = data.getString("img500");
            String description = data.getString("info");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(data.getString("tag").replace(",", "、"));
        }

        // 咪咕
        else if (source == NetMusicSource.MG) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_MG_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONArray("resource").getJSONObject(0);

            String coverImgUrl = data.getJSONObject("imgItem").getString("img");
            String description = data.getString("summary");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(SdkUtil.parseTags(data, NetMusicSource.MG));
        }

        // 千千
        else if (source == NetMusicSource.QI) {
            String playlistInfoBody = HttpRequest.get(SdkCommon.buildQianUrl(String.format(PLAYLIST_DETAIL_QI_API, id, 1, 1, System.currentTimeMillis())))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject playlistJson = playlistInfoJson.getJSONObject("data");

            String coverImgUrl = playlistJson.getString("pic");
            String description = playlistJson.getString("desc");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(SdkUtil.parseTags(playlistJson, NetMusicSource.QI));
            if (!playlistInfo.hasTrackCount()) playlistInfo.setTrackCount(playlistJson.getInt("trackCount"));
        }

        // 5sing
        else if (source == NetMusicSource.FS) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_FS_API, creatorId, id))
                    .setFollowRedirects(true)
                    .execute()
                    .body();
            Document doc = Jsoup.parse(playlistInfoBody);

            String coverImgUrl = doc.select(".lt.w_30 img").attr("src");
            String description = doc.select("#normalIntro").first().ownText();
            StringJoiner sj = new StringJoiner("、");
            Elements elems = doc.select(".c_wap.tag_box label");
            elems.forEach(elem -> sj.add(elem.text()));
            String tag = sj.toString();

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(tag);
        }

        // 猫耳
        else if (source == NetMusicSource.ME) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_ME_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject info = playlistInfoJson.optJSONObject("info");
            if (info == null) return;
            JSONObject album = info.getJSONObject("album");

            String coverImgUrl = album.getString("front_cover");
            String description = StringUtil.removeHTMLLabel(album.getString("intro"));

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            if (!playlistInfo.hasTag()) playlistInfo.setTag(SdkUtil.parseTags(info, NetMusicSource.ME));
            if (!playlistInfo.hasTrackCount()) playlistInfo.setTrackCount(album.getInt("music_count"));
        }

        // 哔哩哔哩
        else if (source == NetMusicSource.BI) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_BI_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");

            String coverImgUrl = data.getString("cover");
            String description = data.getString("intro");

            if (!playlistInfo.hasCoverImgUrl()) playlistInfo.setCoverImgUrl(coverImgUrl);
            GlobalExecutors.imageExecutor.submit(() -> playlistInfo.setCoverImg(SdkUtil.getImageFromUrl(coverImgUrl)));
            playlistInfo.setDescription(description);
            playlistInfo.setTag("");
        }
    }

    public CommonResult<NetMusicInfo> getMusicInfoInPlaylist(String id, int source, int limit, int page) {
        NetPlaylistInfo playlistInfo = new NetPlaylistInfo();
        playlistInfo.setSource(source);
        playlistInfo.setId(id);
        return getMusicInfoInPlaylist(playlistInfo, limit, page);
    }

    /**
     * 根据歌单 id 获取里面歌曲的 id 并获取每首歌曲粗略信息，分页，返回 NetMusicInfo
     */
    public CommonResult<NetMusicInfo> getMusicInfoInPlaylist(NetPlaylistInfo playlistInfo, int limit, int page) {
        AtomicInteger total = new AtomicInteger();
        List<NetMusicInfo> netMusicInfos = new LinkedList<>();

        int source = playlistInfo.getSource();
        String id = playlistInfo.getId();
        String creatorId = playlistInfo.getCreatorId();

        // 网易云
        if (source == NetMusicSource.NET_CLOUD) {
            LinkedList<Future<?>> taskList = new LinkedList<>();

            taskList.add(GlobalExecutors.requestExecutor.submit(() -> {
                String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_SONGS_API, id, (page - 1) * limit, limit))
                        .execute()
                        .body();
                JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
                JSONArray songArray = playlistInfoJson.getJSONArray("songs");
                for (int i = 0, len = songArray.size(); i < len; i++) {
                    JSONObject songJson = songArray.getJSONObject(i);

                    String songId = songJson.getString("id");
                    String name = songJson.getString("name").trim();
                    String artists = SdkUtil.parseArtists(songJson, NetMusicSource.NET_CLOUD);
                    String artistId = songJson.getJSONArray("ar").getJSONObject(0).getString("id");
                    String albumName = songJson.getJSONObject("al").getString("name");
                    String albumId = songJson.getJSONObject("al").getString("id");
                    Double duration = songJson.getDouble("dt") / 1000;
                    String mvId = songJson.getString("mv");

                    NetMusicInfo netMusicInfo = new NetMusicInfo();
                    netMusicInfo.setId(songId);
                    netMusicInfo.setName(name);
                    netMusicInfo.setArtist(artists);
                    netMusicInfo.setArtistId(artistId);
                    netMusicInfo.setAlbumName(albumName);
                    netMusicInfo.setAlbumId(albumId);
                    netMusicInfo.setDuration(duration);
                    netMusicInfo.setMvId(mvId);

                    netMusicInfos.add(netMusicInfo);
                }
            }));

            taskList.add(GlobalExecutors.requestExecutor.submit(() -> {
                // 网易云获取歌单歌曲总数需要额外请求歌单详情接口！
                String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_API, id))
                        .execute()
                        .body();
                JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
                total.set(playlistInfoJson.getJSONObject("playlist").getInt("trackCount"));
            }));

            taskList.forEach(task -> {
                try {
                    task.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        // 酷狗
        else if (source == NetMusicSource.KG) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_SONGS_KG_API, id, page, limit,
                            StringUtil.toMD5("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwtappid=1058clienttime=1586163263991" +
                                    "clientver=20000dfid=-global_specialid=" + id + "mid=1586163263991page=" + page + "pagesize=" + limit +
                                    "plat=0specialid=0srcappid=2919uuid=1586163263991version=8000NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt")))
                    .header("mid", "1586163263991")
                    .header("Referer", "https://m3ws.kugou.com/share/index.php")
                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
                    .header("dfid", "-")
                    .header("clienttime", "1586163263991")
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");
            total.set(data.getInt("total"));
            JSONArray songArray = data.getJSONArray("info");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String hash = songJson.getString("hash");
                String songId = songJson.getString("album_audio_id");
                String[] s = songJson.getString("filename").split(" - ");
                String name = s[1];
                String artist = s[0];
//                String albumName = songJson.getString("remark");
                String albumId = songJson.getString("album_id");
                Double duration = songJson.getDouble("duration");
                String mvId = songJson.getString("mvhash");

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.KG);
                netMusicInfo.setHash(hash);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artist);
//                netMusicInfo.setAlbumName(albumName);
                netMusicInfo.setAlbumId(albumId);
                netMusicInfo.setDuration(duration);
                netMusicInfo.setMvId(mvId);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // QQ
        else if (source == NetMusicSource.QQ) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_QQ_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");
            total.set(data.getInt("songnum"));
            JSONArray songArray = data.getJSONArray("songlist");
            for (int i = (page - 1) * limit, len = Math.min(songArray.size(), page * limit); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("songmid");
                String name = songJson.getString("songname");
                String artists = SdkUtil.parseArtists(songJson, NetMusicSource.QQ);
                String artistId = songJson.getJSONArray("singer").getJSONObject(0).getString("mid");
                String albumName = songJson.getString("albumname");
                String albumId = songJson.getString("albummid");
                Double duration = songJson.getDouble("interval");
                String mvId = songJson.getString("vid");

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.QQ);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artists);
                netMusicInfo.setArtistId(artistId);
                netMusicInfo.setAlbumName(albumName);
                netMusicInfo.setAlbumId(albumId);
                netMusicInfo.setDuration(duration);
                netMusicInfo.setMvId(mvId);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 酷我
        else if (source == NetMusicSource.KW) {
            String playlistInfoBody = SdkCommon.kwRequest(String.format(PLAYLIST_DETAIL_KW_API, id, page, limit))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");
            JSONArray songArray = data.getJSONArray("musicList");
            total.set(data.getInt("total"));
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("rid");
                String name = songJson.getString("name");
                String artists = songJson.getString("artist");
                String artistId = songJson.getString("artistid");
                String albumName = songJson.getString("album");
                String albumId = songJson.getString("albumid");
                Double duration = songJson.getDouble("duration");
                String mvId = songJson.getInt("hasmv") == 0 ? "" : songId;

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.KW);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artists);
                netMusicInfo.setArtistId(artistId);
                netMusicInfo.setAlbumName(albumName);
                netMusicInfo.setAlbumId(albumId);
                netMusicInfo.setDuration(duration);
                netMusicInfo.setMvId(mvId);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 咪咕
        else if (source == NetMusicSource.MG) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_SONGS_MG_API, id, page, limit))
                    .header(Header.USER_AGENT, "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                    .header(Header.REFERER, "https://m.music.migu.cn/")
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONArray songArray = playlistInfoJson.getJSONArray("list");
            total.set(playlistInfoJson.getInt("totalCount"));
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("copyrightId");
                String name = songJson.getString("songName");
                String artists = SdkUtil.parseArtists(songJson, NetMusicSource.MG);
                String artistId = songJson.getJSONArray("artists").getJSONObject(0).getString("id");
                String albumName = songJson.getString("album");
                String albumId = songJson.getString("albumId");
                Double duration = TimeUtil.toSeconds(songJson.getString("length"));
                // 咪咕音乐没有 mv 时，该字段不存在！
                String mvId = songJson.optString("mvId");

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.MG);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artists);
                netMusicInfo.setArtistId(artistId);
                netMusicInfo.setAlbumName(albumName);
                netMusicInfo.setAlbumId(albumId);
                netMusicInfo.setDuration(duration);
                netMusicInfo.setMvId(mvId);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 千千
        else if (source == NetMusicSource.QI) {
            String playlistInfoBody = HttpRequest.get(SdkCommon.buildQianUrl(String.format(PLAYLIST_DETAIL_QI_API, id, page, limit, System.currentTimeMillis())))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");
            total.set(data.getInt("trackCount"));
            JSONArray songArray = data.getJSONArray("trackList");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("TSID");
                String name = songJson.getString("title");
                String artists = SdkUtil.parseArtists(songJson, NetMusicSource.QI);
                JSONArray artistArray = songJson.optJSONArray("artist");
                String artistId = artistArray != null && !artistArray.isEmpty() ? artistArray.getJSONObject(0).getString("artistCode") : "";
                String albumName = songJson.getString("albumTitle");
                String albumId = songJson.getString("albumAssetCode");
                Double duration = songJson.getDouble("duration");

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.QI);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artists);
                netMusicInfo.setArtistId(artistId);
                netMusicInfo.setAlbumName(albumName);
                netMusicInfo.setAlbumId(albumId);
                netMusicInfo.setDuration(duration);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 5sing
        else if (source == NetMusicSource.FS) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_FS_API, creatorId, id))
                    .setFollowRedirects(true)
                    .execute()
                    .body();
            Document doc = Jsoup.parse(playlistInfoBody);
            total.set(Integer.parseInt(ReUtil.get("（(\\d+)）", doc.select("span.number").text(), 1)));
            Elements songArray = doc.select("li.p_rel");
            for (int i = (page - 1) * limit, len = Math.min(page * limit, songArray.size()); i < len; i++) {
                Element elem = songArray.get(i);
                Elements na = elem.select(".s_title.lt a");
                Elements aa = elem.select(".s_soner.lt a");

                String songId = ReUtil.get("http://5sing.kugou.com/(.*?).html", na.attr("href"), 1).replaceFirst("/", "_");
                String name = na.text();
                String artists = aa.text();
                String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", aa.attr("href"), 1);

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.FS);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artists);
                netMusicInfo.setArtistId(artistId);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 猫耳
        else if (source == NetMusicSource.ME) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_DETAIL_ME_API, id))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("info");
            JSONArray songArray = data.getJSONArray("sounds");
            total.set(songArray.size());
            for (int i = (page - 1) * limit, len = Math.min(songArray.size(), page * limit); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("id");
                String name = songJson.getString("soundstr");
                Double duration = songJson.getDouble("duration") / 1000;

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.ME);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setDuration(duration);

                netMusicInfos.add(netMusicInfo);
            }
        }

        // 哔哩哔哩
        else if (source == NetMusicSource.BI) {
            String playlistInfoBody = HttpRequest.get(String.format(PLAYLIST_SONGS_BI_API, id, page, limit))
                    .execute()
                    .body();
            JSONObject playlistInfoJson = JSONObject.fromObject(playlistInfoBody);
            JSONObject data = playlistInfoJson.getJSONObject("data");
            total.set(data.getInt("totalSize"));
            JSONArray songArray = data.getJSONArray("data");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String songId = songJson.getString("id");
                String name = songJson.getString("title");
                String artist = songJson.getString("uname");
                String artistId = songJson.getString("uid");
                Double duration = songJson.getDouble("duration");

                NetMusicInfo netMusicInfo = new NetMusicInfo();
                netMusicInfo.setSource(NetMusicSource.BI);
                netMusicInfo.setId(songId);
                netMusicInfo.setName(name);
                netMusicInfo.setArtist(artist);
                netMusicInfo.setArtistId(artistId);
                netMusicInfo.setDuration(duration);

                netMusicInfos.add(netMusicInfo);
            }
        }

        return new CommonResult<>(netMusicInfos, total.get());
    }
}