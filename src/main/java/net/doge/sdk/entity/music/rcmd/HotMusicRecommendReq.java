package net.doge.sdk.entity.music.rcmd;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import net.doge.constant.async.GlobalExecutors;
import net.doge.constant.system.NetMusicSource;
import net.doge.sdk.common.Tags;
import net.doge.model.entity.NetMusicInfo;
import net.doge.sdk.common.CommonResult;
import net.doge.sdk.common.SdkCommon;
import net.doge.sdk.entity.playlist.info.PlaylistInfoReq;
import net.doge.sdk.util.SdkUtil;
import net.doge.util.collection.ListUtil;
import net.doge.util.common.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class HotMusicRecommendReq {
    // 曲风歌曲(最热) API
    private final String STYLE_HOT_SONG_API = SdkCommon.prefix + "/style/song?tagId=%s&sort=0&cursor=%s&size=%s";
    // 飙升榜 API (酷狗)
    private final String UP_MUSIC_KG_API
            = "http://mobilecdnbj.kugou.com/api/v3/rank/song?volid=35050&rankid=6666&page=%s&pagesize=%s";
    // TOP500 API (酷狗)
    private final String TOP500_KG_API
            = "http://mobilecdnbj.kugou.com/api/v3/rank/song?volid=35050&rankid=8888&page=%s&pagesize=%s";
    // 流行指数榜 API (QQ)
    private final String POPULAR_MUSIC_QQ_API = SdkCommon.prefixQQ33 + "/top?id=4&pageNo=%s&pageSize=%s";
    // 热歌榜 API (QQ)
    private final String HOT_MUSIC_QQ_API = SdkCommon.prefixQQ33 + "/top?id=26&pageNo=%s&pageSize=%s";
    // 飙升榜 API (酷我)
    private final String UP_MUSIC_KW_API = "http://www.kuwo.cn/api/www/bang/bang/musicList?bangId=93&pn=%s&rn=%s&httpsStatus=1";
    // 热歌榜 API (酷我)
    private final String HOT_MUSIC_KW_API = "http://www.kuwo.cn/api/www/bang/bang/musicList?bangId=16&pn=%s&rn=%s&httpsStatus=1";
    // 尖叫热歌榜 API (咪咕)
    private final String HOT_MUSIC_MG_API = "https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/querycontentbyId.do?columnId=27186466";
    // 热歌 API (音乐磁场)
    private final String HOT_MUSIC_HF_API = "https://www.hifini.com/%s-%s.htm";
    // 热歌 API (咕咕咕音乐)
    private final String HOT_MUSIC_GG_API = "http://www.gggmusic.com/%s-%s.htm";
    // 传播最快(原唱) API (5sing)
    private final String SPREAD_YC_MUSIC_FS_API = "http://5sing.kugou.com/yc/spread/more_%s.shtml";
    // 分享最多(原唱) API (5sing)
    private final String SHARE_YC_MUSIC_FS_API = "http://5sing.kugou.com/yc/share/more_%s.shtml";
    // 传播最快(翻唱) API (5sing)
    private final String SPREAD_FC_MUSIC_FS_API = "http://5sing.kugou.com/fc/spread/more_%s.shtml";
    // 分享最多(翻唱) API (5sing)
    private final String SHARE_FC_MUSIC_FS_API = "http://5sing.kugou.com/fc/share/more_%s.shtml";
    // 热门伴奏(伴奏) API (5sing)
    private final String HOT_BZ_MUSIC_FS_API = "http://5sing.kugou.com/bz/rmsong/more_%s.shtml";
    // 下载排行(伴奏) API (5sing)
    private final String RANK_BZ_MUSIC_FS_API = "http://5sing.kugou.com/bz/xzsong/more_%s.shtml";

    /**
     * 获取飙升歌曲
     */
    public CommonResult<NetMusicInfo> getHotMusicRecommend(int src, String tag, int limit, int page) {
        AtomicInteger total = new AtomicInteger();
        List<NetMusicInfo> musicInfos = new LinkedList<>();

        final String defaultTag = "默认";
        String[] s = Tags.hotSongTag.get(tag);

        // 网易云(榜单就是歌单，固定榜单 id 直接请求歌单音乐接口，接口分页)
        PlaylistInfoReq playlistInfoReq = new PlaylistInfoReq();
        // 飙升榜
        Callable<CommonResult<NetMusicInfo>> getUpMusic = () -> playlistInfoReq.getMusicInfoInPlaylist(String.valueOf(19723756), NetMusicSource.NET_CLOUD, limit, page);
        // 热歌榜
        Callable<CommonResult<NetMusicInfo>> getHotMusic = () -> playlistInfoReq.getMusicInfoInPlaylist(String.valueOf(3778678), NetMusicSource.NET_CLOUD, limit, page);
        // 曲风歌曲(最热)
        Callable<CommonResult<NetMusicInfo>> getStyleHotSong = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            if (StringUtil.isNotEmpty(s[0])) {
                String musicInfoBody = HttpRequest.get(String.format(STYLE_HOT_SONG_API, s[0], (page - 1) * limit, limit))
                        .execute()
                        .body();
                JSONObject musicInfoJson = JSONObject.fromObject(musicInfoBody);
                JSONObject data = musicInfoJson.getJSONObject("data");
                JSONArray songsArray = data.getJSONArray("songs");
                t = data.getJSONObject("page").getInt("total");
                for (int i = 0, len = songsArray.size(); i < len; i++) {
                    JSONObject songJson = songsArray.getJSONObject(i);

                    String songId = songJson.getString("id");
                    String songName = songJson.getString("name").trim();
                    String artist = SdkUtil.parseArtists(songJson, NetMusicSource.NET_CLOUD);
                    String artistId = songJson.getJSONArray("ar").getJSONObject(0).getString("id");
                    String albumName = songJson.getJSONObject("al").getString("name");
                    String albumId = songJson.getJSONObject("al").getString("id");
                    Double duration = songJson.getDouble("dt") / 1000;
                    String mvId = songJson.getString("mv");

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);
                    musicInfo.setAlbumName(albumName);
                    musicInfo.setAlbumId(albumId);
                    musicInfo.setDuration(duration);
                    musicInfo.setMvId(mvId);
                    res.add(musicInfo);
                }
            }

            return new CommonResult<>(res, t);
        };

        // 酷狗
        // 飙升榜
        Callable<CommonResult<NetMusicInfo>> getUpMusicKg = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String rankingInfoBody = HttpRequest.get(String.format(UP_MUSIC_KG_API, page, limit))
                    .execute()
                    .body();
            JSONObject rankingInfoJson = JSONObject.fromObject(rankingInfoBody);
            JSONObject data = rankingInfoJson.getJSONObject("data");
            t = data.getInt("total");
            JSONArray songArray = data.getJSONArray("info");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String hash = songJson.getString("hash");
                String songId = songJson.getString("album_audio_id");
                String name = songJson.getString("songname");
                String artists = SdkUtil.parseArtists(songJson, NetMusicSource.KG);
                JSONArray artistArray = songJson.optJSONArray("authors");
                String artistId = artistArray != null && !artistArray.isEmpty() ? artistArray.getJSONObject(0).getString("author_id") : "";
//                String albumName = songJson.getString("remark");
                String albumId = songJson.getString("album_id");
                Double duration = songJson.getDouble("duration");
                JSONArray mvdata = songJson.optJSONArray("mvdata");
                String mvId = mvdata == null ? songJson.getString("mvhash") : mvdata.getJSONObject(0).getString("hash");

                NetMusicInfo musicInfo = new NetMusicInfo();
                musicInfo.setSource(NetMusicSource.KG);
                musicInfo.setHash(hash);
                musicInfo.setId(songId);
                musicInfo.setName(name);
                musicInfo.setArtist(artists);
                musicInfo.setArtistId(artistId);
//                musicInfo.setAlbumName(albumName);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setMvId(mvId);

                res.add(musicInfo);
            }
            return new CommonResult<>(res, t);
        };
        // TOP500
        Callable<CommonResult<NetMusicInfo>> getTop500Kg = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String rankingInfoBody = HttpRequest.get(String.format(TOP500_KG_API, page, limit))
                    .execute()
                    .body();
            JSONObject rankingInfoJson = JSONObject.fromObject(rankingInfoBody);
            JSONObject data = rankingInfoJson.getJSONObject("data");
            t = data.getInt("total");
            JSONArray songArray = data.getJSONArray("info");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String hash = songJson.getString("hash");
                String songId = songJson.getString("album_audio_id");
                String name = songJson.getString("songname");
                String artists = SdkUtil.parseArtists(songJson, NetMusicSource.KG);
                JSONArray artistArray = songJson.optJSONArray("authors");
                String artistId = artistArray != null && !artistArray.isEmpty() ? artistArray.getJSONObject(0).getString("author_id") : "";
//                String albumName = songJson.getString("remark");
                String albumId = songJson.getString("album_id");
                Double duration = songJson.getDouble("duration");
                JSONArray mvdata = songJson.optJSONArray("mvdata");
                String mvId = mvdata == null ? songJson.getString("mvhash") : mvdata.getJSONObject(0).getString("hash");

                NetMusicInfo musicInfo = new NetMusicInfo();
                musicInfo.setSource(NetMusicSource.KG);
                musicInfo.setHash(hash);
                musicInfo.setId(songId);
                musicInfo.setName(name);
                musicInfo.setArtist(artists);
                musicInfo.setArtistId(artistId);
//                musicInfo.setAlbumName(albumName);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setMvId(mvId);

                res.add(musicInfo);
            }
            return new CommonResult<>(res, t);
        };

        // QQ
        // 流行指数榜
        Callable<CommonResult<NetMusicInfo>> getPopularMusicQq = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(POPULAR_MUSIC_QQ_API, page, limit))
                    .execute()
                    .body();
            JSONObject musicInfoJson = JSONObject.fromObject(musicInfoBody);
            JSONObject data = musicInfoJson.getJSONObject("data");
            t = data.getInt("total");
            JSONArray songArray = data.getJSONArray("list");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String id = songJson.getString("mid");
                String name = songJson.getString("name");
                String artist = SdkUtil.parseArtists(songJson, NetMusicSource.QQ);
                String artistId = songJson.getJSONArray("singer").getJSONObject(0).getString("mid");
                String albumName = songJson.getJSONObject("album").getString("name");
                String albumId = songJson.getJSONObject("album").getString("mid");
                Double duration = songJson.getDouble("interval");
                String mvId = songJson.getJSONObject("mv").getString("vid");

                NetMusicInfo musicInfo = new NetMusicInfo();
                musicInfo.setSource(NetMusicSource.QQ);
                musicInfo.setId(id);
                musicInfo.setName(name);
                musicInfo.setArtist(artist);
                musicInfo.setArtistId(artistId);
                musicInfo.setAlbumName(albumName);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setMvId(mvId);

                res.add(musicInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 热歌榜
        Callable<CommonResult<NetMusicInfo>> getHotMusicQq = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(HOT_MUSIC_QQ_API, page, limit))
                    .execute()
                    .body();
            JSONObject musicInfoJson = JSONObject.fromObject(musicInfoBody);
            JSONObject data = musicInfoJson.getJSONObject("data");
            t = data.getInt("total");
            JSONArray songArray = data.getJSONArray("list");
            for (int i = 0, len = songArray.size(); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i);

                String id = songJson.getString("mid");
                String name = songJson.getString("name");
                String artist = SdkUtil.parseArtists(songJson, NetMusicSource.QQ);
                String artistId = songJson.getJSONArray("singer").getJSONObject(0).getString("mid");
                String albumName = songJson.getJSONObject("album").getString("name");
                String albumId = songJson.getJSONObject("album").getString("mid");
                Double duration = songJson.getDouble("interval");
                String mvId = songJson.getJSONObject("mv").getString("vid");

                NetMusicInfo musicInfo = new NetMusicInfo();
                musicInfo.setSource(NetMusicSource.QQ);
                musicInfo.setId(id);
                musicInfo.setName(name);
                musicInfo.setArtist(artist);
                musicInfo.setArtistId(artistId);
                musicInfo.setAlbumName(albumName);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setMvId(mvId);

                res.add(musicInfo);
            }
            return new CommonResult<>(res, t);
        };

        // 酷我
        // 飙升榜
        Callable<CommonResult<NetMusicInfo>> getUpMusicKw = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = SdkCommon.kwRequest(String.format(UP_MUSIC_KW_API, page, limit)).execute();
            if (resp.getStatus() == HttpStatus.HTTP_OK) {
                String musicInfoBody = resp.body();
                JSONObject musicInfoJson = JSONObject.fromObject(musicInfoBody);
                JSONObject data = musicInfoJson.getJSONObject("data");
                t = data.getInt("num");
                JSONArray songArray = data.getJSONArray("musicList");
                for (int i = 0, len = songArray.size(); i < len; i++) {
                    JSONObject songJson = songArray.getJSONObject(i);

                    String id = songJson.getString("rid");
                    String name = songJson.getString("name");
                    String artist = songJson.getString("artist");
                    String artistId = songJson.getString("artistid");
                    String albumName = songJson.getString("album");
                    String albumId = songJson.getString("albumid");
                    Double duration = songJson.getDouble("duration");
                    String mvId = songJson.getInt("hasmv") == 0 ? "" : id;

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.KW);
                    musicInfo.setId(id);
                    musicInfo.setName(name);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);
                    musicInfo.setAlbumName(albumName);
                    musicInfo.setAlbumId(albumId);
                    musicInfo.setDuration(duration);
                    musicInfo.setMvId(mvId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 热歌榜
        Callable<CommonResult<NetMusicInfo>> getHotMusicKw = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = SdkCommon.kwRequest(String.format(HOT_MUSIC_KW_API, page, limit)).execute();
            if (resp.getStatus() == HttpStatus.HTTP_OK) {
                String musicInfoBody = resp.body();
                JSONObject musicInfoJson = JSONObject.fromObject(musicInfoBody);
                JSONObject data = musicInfoJson.getJSONObject("data");
                t = data.getInt("num");
                JSONArray songArray = data.getJSONArray("musicList");
                for (int i = 0, len = songArray.size(); i < len; i++) {
                    JSONObject songJson = songArray.getJSONObject(i);

                    String id = songJson.getString("rid");
                    String name = songJson.getString("name");
                    String artist = songJson.getString("artist");
                    String artistId = songJson.getString("artistid");
                    String albumName = songJson.getString("album");
                    String albumId = songJson.getString("albumid");
                    Double duration = songJson.getDouble("duration");
                    String mvId = songJson.getInt("hasmv") == 0 ? "" : id;

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.KW);
                    musicInfo.setId(id);
                    musicInfo.setName(name);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);
                    musicInfo.setAlbumName(albumName);
                    musicInfo.setAlbumId(albumId);
                    musicInfo.setDuration(duration);
                    musicInfo.setMvId(mvId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        // 咪咕
        // 尖叫热歌榜
        Callable<CommonResult<NetMusicInfo>> getHotMusicMg = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String rankingInfoBody = HttpRequest.get(String.format(HOT_MUSIC_MG_API))
                    .execute()
                    .body();
            JSONObject rankingInfoJson = JSONObject.fromObject(rankingInfoBody);
            JSONObject data = rankingInfoJson.getJSONObject("columnInfo");
            t = data.getInt("contentsCount");
            JSONArray songArray = data.getJSONArray("contents");
            for (int i = (page - 1) * limit, len = Math.min(songArray.size(), page * limit); i < len; i++) {
                JSONObject songJson = songArray.getJSONObject(i).getJSONObject("objectInfo");

                String songId = songJson.optString("copyrightId");
                // 过滤掉不是歌曲的 objectInfo
                if (StringUtil.isEmpty(songId)) continue;
                String name = songJson.getString("songName");
                String artists = songJson.getString("singer");
                String artistId = songJson.getString("singerId");
                String albumName = songJson.getString("album");
                String albumId = songJson.getString("albumId");

                NetMusicInfo musicInfo = new NetMusicInfo();
                musicInfo.setSource(NetMusicSource.MG);
                musicInfo.setId(songId);
                musicInfo.setName(name);
                musicInfo.setArtist(artists);
                musicInfo.setArtistId(artistId);
                musicInfo.setAlbumName(albumName);
                musicInfo.setAlbumId(albumId);

                res.add(musicInfo);
            }
            return new CommonResult<>(res, t);
        };

        // 音乐磁场
        Callable<CommonResult<NetMusicInfo>> getHotMusicHf = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            if (StringUtil.isNotEmpty(s[1])) {
                String musicInfoBody = HttpRequest.get(String.format(HOT_MUSIC_HF_API, s[1], page))
                        .cookie(SdkCommon.HF_COOKIE)
                        .execute()
                        .body();
                Document doc = Jsoup.parse(musicInfoBody);
                Elements songs = doc.select(".media.thread.tap");
                Elements ap = doc.select("a.page-link");
                String ts = ReUtil.get("(\\d+)", ap.isEmpty() ? "" : ap.get(ap.size() - 1).text(), 1);
                if (StringUtil.isEmpty(ts))
                    ts = ReUtil.get("(\\d+)", ap.isEmpty() ? "" : ap.get(ap.size() - 2).text(), 1);
                boolean hasTs = StringUtil.isNotEmpty(ts);
                if (hasTs) t = Integer.parseInt(ts) * limit;
                else t = songs.size();
                for (int i = hasTs ? 0 : (page - 1) * limit, len = hasTs ? songs.size() : Math.min(songs.size(), page * limit); i < len; i++) {
                    Element song = songs.get(i);

                    Elements a = song.select(".subject.break-all a");
                    Element span = song.select(".username.text-grey.mr-1").first();

                    String songId = ReUtil.get("thread-(.*?)\\.htm", a.attr("href"), 1);
                    String songName = a.text();
                    String artist = span.text();
                    String artistId = span.attr("uid");

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.HF);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        // 咕咕咕音乐
        Callable<CommonResult<NetMusicInfo>> getHotMusicGg = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            if (StringUtil.isNotEmpty(s[2])) {
                String musicInfoBody = HttpRequest.get(String.format(HOT_MUSIC_GG_API, s[2], page))
                        .execute()
                        .body();
                Document doc = Jsoup.parse(musicInfoBody);
                Elements songs = doc.select(".media.thread.tap");
                Elements ap = doc.select("a.page-link");
                String ts = ReUtil.get("(\\d+)", ap.isEmpty() ? "" : ap.get(ap.size() - 1).text(), 1);
                if (StringUtil.isEmpty(ts))
                    ts = ReUtil.get("(\\d+)", ap.isEmpty() ? "" : ap.get(ap.size() - 2).text(), 1);
                boolean hasTs = StringUtil.isNotEmpty(ts);
                if (hasTs) t = Integer.parseInt(ts) * limit;
                else t = songs.size();
                for (int i = hasTs ? 0 : (page - 1) * limit, len = hasTs ? songs.size() : Math.min(songs.size(), page * limit); i < len; i++) {
                    Element song = songs.get(i);

                    Elements a = song.select(".subject.break-all a");

                    String songId = ReUtil.get("thread-(.*?)\\.htm", a.attr("href"), 1);
                    String songName = a.text();

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.GG);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        // 5sing
        // 传播最快(原唱)
        Callable<CommonResult<NetMusicInfo>> getSpreadYcSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(SPREAD_YC_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select(".lists dl dd.l_info");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(em.text()) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements a = song.select("h3 a");
                    Elements pa = song.select("p.m_z a");

                    String songId = ReUtil.get("/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = ReUtil.get("音乐人：(.*)", pa.text(), 1);
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 分享最多(原唱)
        Callable<CommonResult<NetMusicInfo>> getShareYcSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(SHARE_YC_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select(".lists dl dd.l_info");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(em.text()) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements a = song.select("h3 a");
                    Elements pa = song.select("p.m_z a");

                    String songId = ReUtil.get("/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = ReUtil.get("音乐人：(.*)", pa.text(), 1);
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 传播最快(翻唱)
        Callable<CommonResult<NetMusicInfo>> getSpreadFcSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(SPREAD_FC_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select(".lists dl dd.l_info");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(em.text()) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements a = song.select("h3 a");
                    Elements pa = song.select("p.m_z a");

                    String songId = ReUtil.get("/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = ReUtil.get("音乐人：(.*)", pa.text(), 1);
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 分享最多(翻唱)
        Callable<CommonResult<NetMusicInfo>> getShareFcSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(SHARE_FC_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select(".lists dl dd.l_info");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(em.text()) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements a = song.select("h3 a");
                    Elements pa = song.select("p.m_z a");

                    String songId = ReUtil.get("/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = ReUtil.get("音乐人：(.*)", pa.text(), 1);
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 热门伴奏(伴奏)
        Callable<CommonResult<NetMusicInfo>> getHotBzSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(HOT_BZ_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select("tr");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(ReUtil.get("\\d+/(\\d+)", em.text(), 1)) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements td = song.select("td");
                    // 排除表头
                    if (td.isEmpty()) continue;

                    Elements a = song.select(".aleft a");
                    Elements pa = td.get(2).select("a");

                    String songId = ReUtil.get("http://5sing.kugou.com/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = pa.text();
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };
        // 下载排行(伴奏)
        Callable<CommonResult<NetMusicInfo>> getRankBzSongFs = () -> {
            LinkedList<NetMusicInfo> res = new LinkedList<>();
            Integer t = 0;

            String musicInfoBody = HttpRequest.get(String.format(RANK_BZ_MUSIC_FS_API, page))
                    .execute()
                    .body();
            Document doc = Jsoup.parse(musicInfoBody);
            Elements songs = doc.select("tr");
            if (!songs.isEmpty()) {
                Elements em = doc.select(".page_num em");
                t = Integer.parseInt(ReUtil.get("\\d+/(\\d+)", em.text(), 1)) * limit;
                for (int i = 0, len = songs.size(); i < len; i++) {
                    Element song = songs.get(i);
                    Elements td = song.select("td");
                    // 排除表头
                    if (td.isEmpty()) continue;

                    Elements a = song.select(".aleft a");
                    Elements pa = td.get(2).select("a");

                    String songId = ReUtil.get("http://5sing.kugou.com/(.*?/.*?).html", a.attr("href"), 1).replaceFirst("/", "_");
                    String songName = a.text();
                    String artist = pa.text();
                    String artistId = ReUtil.get("http://5sing.kugou.com/(\\d+)", pa.attr("href"), 1);

                    NetMusicInfo musicInfo = new NetMusicInfo();
                    musicInfo.setSource(NetMusicSource.FS);
                    musicInfo.setId(songId);
                    musicInfo.setName(songName);
                    musicInfo.setArtist(artist);
                    musicInfo.setArtistId(artistId);

                    res.add(musicInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        List<Future<CommonResult<NetMusicInfo>>> taskList = new LinkedList<>();

        boolean dt = defaultTag.equals(tag);

        if (dt) {
            if (src == NetMusicSource.NET_CLOUD || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getUpMusic));
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusic));
            }
            if (src == NetMusicSource.KG || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getUpMusicKg));
                taskList.add(GlobalExecutors.requestExecutor.submit(getTop500Kg));
            }
            if (src == NetMusicSource.QQ || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getPopularMusicQq));
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicQq));
            }
            if (src == NetMusicSource.KW || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getUpMusicKw));
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicKw));
            }
            if (src == NetMusicSource.MG || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicMg));
            }
            if (src == NetMusicSource.HF || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicHf));
            }
            if (src == NetMusicSource.GG || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicGg));
            }
            if (src == NetMusicSource.FS || src == NetMusicSource.ALL) {
                taskList.add(GlobalExecutors.requestExecutor.submit(getSpreadYcSongFs));
                taskList.add(GlobalExecutors.requestExecutor.submit(getShareYcSongFs));
                taskList.add(GlobalExecutors.requestExecutor.submit(getSpreadFcSongFs));
                taskList.add(GlobalExecutors.requestExecutor.submit(getShareFcSongFs));
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotBzSongFs));
                taskList.add(GlobalExecutors.requestExecutor.submit(getRankBzSongFs));
            }
        } else {
            if (src == NetMusicSource.NET_CLOUD || src == NetMusicSource.ALL)
                taskList.add(GlobalExecutors.requestExecutor.submit(getStyleHotSong));
            if (src == NetMusicSource.HF || src == NetMusicSource.ALL)
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicHf));
            if (src == NetMusicSource.GG || src == NetMusicSource.ALL)
                taskList.add(GlobalExecutors.requestExecutor.submit(getHotMusicGg));
        }

        List<List<NetMusicInfo>> rl = new LinkedList<>();
        taskList.forEach(task -> {
            try {
                CommonResult<NetMusicInfo> result = task.get();
                rl.add(result.data);
                total.set(Math.max(total.get(), result.total));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        musicInfos.addAll(ListUtil.joinAll(rl));

        return new CommonResult<>(musicInfos, total.get());
    }
}
