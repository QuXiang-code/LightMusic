package net.doge.sdk.entity.radio.rcmd;

import cn.hutool.http.HttpRequest;
import net.doge.constant.async.GlobalExecutors;
import net.doge.constant.system.NetMusicSource;
import net.doge.model.entity.NetRadioInfo;
import net.doge.sdk.common.CommonResult;
import net.doge.sdk.common.SdkCommon;
import net.doge.sdk.util.SdkUtil;
import net.doge.util.collection.ListUtil;
import net.doge.util.common.StringUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class NewRadioReq {
    // 新晋电台 API
    private final String NEW_RADIO_API = SdkCommon.PREFIX + "/dj/toplist?type=new&limit=200";
    // 推荐个性电台 API
    private final String PERSONALIZED_RADIO_API = SdkCommon.PREFIX + "/personalized/djprogram";
    // 推荐电台 API
    private final String RECOMMEND_RADIO_API = SdkCommon.PREFIX + "/dj/recommend";
    // 付费精品电台 API
    private final String PAY_RADIO_API = SdkCommon.PREFIX + "/dj/toplist/pay?limit=100";
    // 付费精选电台 API
    private final String PAY_GIFT_RADIO_API = SdkCommon.PREFIX + "/dj/paygift?offset=%s&limit=%s";
    // 推荐电台 API (QQ)
    private final String RECOMMEND_RADIO_QQ_API = SdkCommon.PREFIX_QQ + "/radio/category";
    // 推荐广播剧 API (猫耳)
    private final String REC_RADIO_ME_API = "https://www.missevan.com/drama/site/recommend";
    // 夏日推荐 API (猫耳)
    private final String SUMMER_RADIO_ME_API = "https://www.missevan.com/dramaapi/summerdrama";
    // 频道 API (猫耳)
//    private final String CHANNEL_ME_API = "https://www.missevan.com/explore/channels?type=0";
    
    /**
     * 获取新晋电台
     */
    public CommonResult<NetRadioInfo> getNewRadios(int src, int limit, int page) {
        AtomicInteger total = new AtomicInteger();
        List<NetRadioInfo> radioInfos = new LinkedList<>();

        // 网易云(程序分页)
        // 新晋电台榜
        Callable<CommonResult<NetRadioInfo>> getNewRadios = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(NEW_RADIO_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONArray("toplist");
            t = radioArray.size();
            for (int i = (page - 1) * limit, len = Math.min(radioArray.size(), page * limit); i < len; i++) {
                JSONObject radioJson = radioArray.getJSONObject(i);

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
                String dj = radioJson.getJSONObject("dj").getString("nickname");
                String djId = radioJson.getJSONObject("dj").getString("userId");
                Long playCount = radioJson.getLong("playCount");
                Integer trackCount = radioJson.getIntValue("programCount");
                String category = radioJson.getString("category");
                String coverImgThumbUrl = radioJson.getString("picUrl");
//            String createTime = TimeUtils.msToDate(radioJson.getLong("createTime"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
                radioInfo.setDj(dj);
                radioInfo.setDjId(djId);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                radioInfo.setPlayCount(playCount);
                radioInfo.setTrackCount(trackCount);
                radioInfo.setCategory(category);
//            radioInfo.setCreateTime(createTime);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 推荐个性电台
        Callable<CommonResult<NetRadioInfo>> getPersonalizedRadios = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(PERSONALIZED_RADIO_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONArray("result");
            t = radioArray.size();
            for (int i = (page - 1) * limit, len = Math.min(radioArray.size(), page * limit); i < len; i++) {
                JSONObject infoJson = radioArray.getJSONObject(i);
                JSONObject programJson = infoJson.getJSONObject("program");
                JSONObject djJson = programJson.getJSONObject("dj");
                JSONObject radioJson = programJson.getJSONObject("radio");

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
                String dj = djJson.getString("nickname");
                String djId = djJson.getString("userId");
//                Long playCount = radioJson.getLong("playCount");
                Integer trackCount = radioJson.getIntValue("programCount");
                String category = radioJson.getString("category");
                String coverImgThumbUrl = radioJson.getString("picUrl");
//            String createTime = TimeUtils.msToDate(radioJson.getLong("createTime"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
                radioInfo.setDj(dj);
                radioInfo.setDjId(djId);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
//                radioInfo.setPlayCount(playCount);
                radioInfo.setTrackCount(trackCount);
                radioInfo.setCategory(category);
//            radioInfo.setCreateTime(createTime);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 推荐电台
        Callable<CommonResult<NetRadioInfo>> getRecommendRadios = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(RECOMMEND_RADIO_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONArray("djRadios");
            t = radioArray.size();
            for (int i = (page - 1) * limit, len = Math.min(radioArray.size(), page * limit); i < len; i++) {
                JSONObject radioJson = radioArray.getJSONObject(i);

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
                String dj = radioJson.getJSONObject("dj").getString("nickname");
                String djId = radioJson.getJSONObject("dj").getString("userId");
                Long playCount = radioJson.getLong("playCount");
                Integer trackCount = radioJson.getIntValue("programCount");
                String category = radioJson.getString("category");
                String coverImgThumbUrl = radioJson.getString("picUrl");
//            String createTime = TimeUtils.msToDate(radioJson.getLong("createTime"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
                radioInfo.setDj(dj);
                radioInfo.setDjId(djId);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                radioInfo.setPlayCount(playCount);
                radioInfo.setTrackCount(trackCount);
                radioInfo.setCategory(category);
//            radioInfo.setCreateTime(createTime);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 付费精品电台
        Callable<CommonResult<NetRadioInfo>> getPayRadios = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(PAY_RADIO_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONObject("data").getJSONArray("list");
            t = radioArray.size();
            for (int i = (page - 1) * limit, len = Math.min(radioArray.size(), page * limit); i < len; i++) {
                JSONObject radioJson = radioArray.getJSONObject(i);

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
                String dj = radioJson.getString("creatorName");
                Long playCount = radioJson.getLong("score");
//                Integer trackCount = radioJson.getIntValue("programCount");
//                String category = radioJson.getString("category");
                String coverImgThumbUrl = radioJson.getString("picUrl");
//            String createTime = TimeUtils.msToDate(radioJson.getLong("createTime"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
                radioInfo.setDj(dj);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                radioInfo.setPlayCount(playCount);
//                radioInfo.setTrackCount(trackCount);
//                radioInfo.setCategory(category);
//            radioInfo.setCreateTime(createTime);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 付费精选电台
        Callable<CommonResult<NetRadioInfo>> getPayGiftRadios = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(PAY_GIFT_RADIO_API, (page - 1) * limit, limit))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONObject("data").getJSONArray("list");
            t = radioArray.size();
            for (int i = 0, len = radioArray.size(); i < len; i++) {
                JSONObject radioJson = radioArray.getJSONObject(i);

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
//                String dj = radioJson.getString("creatorName");
//                Long playCount = radioJson.getLong("score");
                Integer trackCount = radioJson.getIntValue("programCount");
//                String category = radioJson.getString("category");
                String coverImgThumbUrl = radioJson.getString("picUrl");
//            String createTime = TimeUtils.msToDate(radioJson.getLong("createTime"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
//                radioInfo.setDj(dj);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
//                radioInfo.setPlayCount(playCount);
                radioInfo.setTrackCount(trackCount);
//                radioInfo.setCategory(category);
//            radioInfo.setCreateTime(createTime);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };

        // QQ(程序分页)
        Callable<CommonResult<NetRadioInfo>> getRecommendRadiosQq = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(RECOMMEND_RADIO_QQ_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray data = radioInfoJson.getJSONArray("data");
            for (int i = 0, len = data.size(); i < len; i++) {
                JSONArray radioArray = data.getJSONObject(i).getJSONArray("list");
                for (int j = 0, l = radioArray.size(); j < l; j++, t++) {
                    if (t >= (page - 1) * limit && t < page * limit) {
                        JSONObject radioJson = radioArray.getJSONObject(j);

                        String radioId = radioJson.getString("id");
                        String radioName = radioJson.getString("title");
                        String coverImgUrl = radioJson.getString("pic_url");
                        String coverImgThumbUrl = coverImgUrl;
                        Long playCount = radioJson.getLong("listenNum");

                        NetRadioInfo radioInfo = new NetRadioInfo();
                        radioInfo.setSource(NetMusicSource.QQ);
                        radioInfo.setId(radioId);
                        radioInfo.setName(radioName);
                        radioInfo.setPlayCount(playCount);
                        // QQ 需要提前写入电台图片 url，电台信息接口不提供！
                        radioInfo.setCoverImgUrl(coverImgUrl);
                        radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                        GlobalExecutors.imageExecutor.execute(() -> {
                            BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                            radioInfo.setCoverImgThumb(coverImgThumb);
                        });

                        res.add(radioInfo);
                    }
                }
            }
            return new CommonResult<>(res, t);
        };

        // 猫耳
        // 推荐广播剧
        Callable<CommonResult<NetRadioInfo>> getRecRadiosMe = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(REC_RADIO_ME_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONArray("info");
            t = radioArray.size();
            for (int i = (page - 1) * limit, len = Math.min(radioArray.size(), page * limit); i < len; i++) {
                JSONObject radioJson = radioArray.getJSONObject(i);

                String radioId = radioJson.getString("id");
                String radioName = radioJson.getString("name");
                String dj = radioJson.getString("author");
                String coverImgThumbUrl = "https:" + radioJson.getString("cover");
                String description = StringUtil.removeHTMLLabel(radioJson.getString("abstract"));

                NetRadioInfo radioInfo = new NetRadioInfo();
                radioInfo.setSource(NetMusicSource.ME);
                radioInfo.setId(radioId);
                radioInfo.setName(radioName);
                radioInfo.setDj(dj);
                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                radioInfo.setDescription(description);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    radioInfo.setCoverImgThumb(coverImgThumb);
                });

                res.add(radioInfo);
            }
            return new CommonResult<>(res, t);
        };
        // 夏日推荐
        Callable<CommonResult<NetRadioInfo>> getSummerRadiosMe = () -> {
            LinkedList<NetRadioInfo> res = new LinkedList<>();
            Integer t = 0;

            String radioInfoBody = HttpRequest.get(String.format(SUMMER_RADIO_ME_API))
                    .execute()
                    .body();
            JSONObject radioInfoJson = JSONObject.parseObject(radioInfoBody);
            JSONArray radioArray = radioInfoJson.getJSONArray("info");
            for (int i = 0, len = radioArray.size(); i < len; i++) {
                JSONArray array = radioArray.getJSONArray(i);
                for (int j = 0, s = array.size(); j < s; j++, t++) {
                    if (t >= (page - 1) * limit && t < page * limit) {
                        JSONObject radioJson = array.getJSONObject(j);

                        String radioId = radioJson.getString("id");
                        String radioName = radioJson.getString("name");
                        String dj = radioJson.getString("author");
                        String coverImgThumbUrl = radioJson.getString("cover");

                        NetRadioInfo radioInfo = new NetRadioInfo();
                        radioInfo.setSource(NetMusicSource.ME);
                        radioInfo.setId(radioId);
                        radioInfo.setName(radioName);
                        radioInfo.setDj(dj);
                        radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                        GlobalExecutors.imageExecutor.execute(() -> {
                            BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                            radioInfo.setCoverImgThumb(coverImgThumb);
                        });

                        res.add(radioInfo);
                    }
                }
            }

            return new CommonResult<>(res, t);
        };
        // 频道
//        Callable<CommonResult<NetRadioInfo>> getChannelsMe = () -> {
//            LinkedList<NetRadioInfo> res = new LinkedList<>();
//            Integer t = 0;
//
//            String radioInfoBody = HttpRequest.get(String.format(CHANNEL_ME_API))
//                    .execute()
//                    .body();
//            Document doc = Jsoup.parse(radioInfoBody);
//            Elements radios = doc.select(".item.blk > a");
//            t = radios.size();
//            for (int i = (page - 1) * limit, len = Math.min(radios.size(), page * limit); i < len; i++) {
//                Element radio = radios.get(i);
//
//                String radioId = radio.attr("href").replace("/explore/channel/","");
//                String radioName = radio.select("b").text();
//                String coverImgThumbUrl = "https:" + radioJson.getString("cover");
//                String description = StringUtils.removeHTMLLabel(radioJson.getString("abstract"));
//
//                NetRadioInfo radioInfo = new NetRadioInfo();
//                radioInfo.setSource(NetMusicSource.ME);
//                radioInfo.setId(radioId);
//                radioInfo.setName(radioName);
//                radioInfo.setCoverImgThumbUrl(coverImgThumbUrl);
//                radioInfo.setDescription(description);
//                GlobalExecutors.imageExecutor.execute(() -> {
//                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
//                    radioInfo.setCoverImgThumb(coverImgThumb);
//                });
//
//                res.add(radioInfo);
//            }
//            return new CommonResult<>(res, t);
//        };

        List<Future<CommonResult<NetRadioInfo>>> taskList = new LinkedList<>();

        if (src == NetMusicSource.NET_CLOUD || src == NetMusicSource.ALL) {
            taskList.add(GlobalExecutors.requestExecutor.submit(getNewRadios));
            taskList.add(GlobalExecutors.requestExecutor.submit(getPersonalizedRadios));
            taskList.add(GlobalExecutors.requestExecutor.submit(getRecommendRadios));
            taskList.add(GlobalExecutors.requestExecutor.submit(getPayRadios));
            taskList.add(GlobalExecutors.requestExecutor.submit(getPayGiftRadios));
        }

        if (src == NetMusicSource.QQ || src == NetMusicSource.ALL) {
            taskList.add(GlobalExecutors.requestExecutor.submit(getRecommendRadiosQq));
        }

        if (src == NetMusicSource.ME || src == NetMusicSource.ALL) {
            taskList.add(GlobalExecutors.requestExecutor.submit(getRecRadiosMe));
            taskList.add(GlobalExecutors.requestExecutor.submit(getSummerRadiosMe));
        }

        List<List<NetRadioInfo>> rl = new LinkedList<>();
        taskList.forEach(task -> {
            try {
                CommonResult<NetRadioInfo> result = task.get();
                rl.add(result.data);
                total.set(Math.max(total.get(), result.total));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        radioInfos.addAll(ListUtil.joinAll(rl));

        return new CommonResult<>(radioInfos, total.get());
    }
}
