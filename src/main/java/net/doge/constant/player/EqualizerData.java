package net.doge.constant.player;

/**
 * @Author yzx
 * @Description 均衡器参数
 * @Date 2020/12/7
 */
public class EqualizerData {
    public static final int BAND_NUM = 10;
    public static final int MAX_GAIN = 12;
    public static final int MIN_GAIN = -12;

    public static final double[] OFF = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final double[] POP = {4, 2, 0, -3, -6, -6, -3, 0, 1, 3};
    public static final double[] DANCE = {7, 6, 3, 0, 0, -4, -6, -6, 0, 0};
    public static final double[] PRESENT = {-5, -4, 2, 2, 2, 2, 2, 2, 2, 2};
    public static final double[] BLUE = {3, 6, 8, 3, -2, 0, 4, 7, 9, 10};
    public static final double[] CLASSIC = {0, 0, 0, 0, 0, 0, -6, -6, -6, -8};
    public static final double[] JAZZ = {0, 0, 1, 4, 4, 4, 0, 1, 3, 3};
    public static final double[] SLOW = {5, 4, 2, 0, -2, 0, 3, 6, 7, 8};
    public static final double[] ELECTRONIC = {6, 5, 0, -5, -4, 0, 6, 8, 8, 7};
    public static final double[] ROCK = {7, 4, -4, 7, -2, 1, 5, 7, 9, 9};
    public static final double[] COUNTRY = {5, 6, 2, -5, 1, 1, -5, 3, 8, 5};
    public static final double[] VOICE = {-2, -1, -1, 0, 3, 4, 3, 0, 0, 1};
    public static final double[] ACG = {4, 6, 3, -1, -1, 2, 5, 1, 1, 4};
    public static final double[] ACG_WOMAN = {1, 1, 0, 0, 1, 1, -10, 5, 3, 7};
    public static final double[] CHINESE_STYLE = {4, 2, 2, -1, -1, 3, 4, 1, 1, 3};
    public static final double[] FOLK = {0, 3, 0, -1, 1, 4, 5, 3, 0, 2};
    public static final double[] RAP = {5, 5, 4, 0, -2, 1, 3, 0, 3, 4};
    public static final double[] SOFT = {1, 3, 1, -3, -5, -11, -12, -7, -11, -7};
    public static final double[] SOFT_BASS = {3, 3, 3, 0, -3, -3, 0, 0, 0, 0};
    public static final double[] SOFT_TONE = {0, 0, -5, -5, -5, -5, -3, 0, 5, 5};
    public static final double[] TONE = {-3, -3, -3, -3, -2, -2, 0, 6, 10, 12};
    public static final double[] MEDIANT = {-5, -5, -2, 2, 5, 5, 2, 0, -5, -6};
    public static final double[] BASS = {6, 6, 3, 0, -2, -2, 0, 0, 0, 0};
    public static final double[] BASS_AND_TONE = {6, 6, 3, 0, -2, -2, 0, 2, 6, 6};
    public static final double[] MEGA_BASS = {6, 4, -5, 2, 3, 4, 4, 5, 5, 6};
    public static final double[] SUPER_MEGA_BASS = {6, 6, 5, 0, -2, -2, 0, 0, 0, 0};
    public static final double[] SPEAKER = {-3, -3, 3, 4, 5, 5, 6, 6, 3, 3};
    public static final double[] BIG_HORN = {-11, -12, -10, -8, -8, 4, 10, 7, 4, -10};
    public static final double[] IPHONE4S = {11, 8, 7, 5, 3, 0, 0, 0, 2, 4};
    public static final double[] HIGH_PARSE = {1, 0, 0, 0, 0, 0, 0, 1, 4, 8};
    public static final double[] TREBLE_ATTENUATION = {8, 9, 7, 5, 3, 1, 0, 0, 1, 4};
    public static final double[] HIGH_VOICE_PRESSURE = {12, 12, 12, -2, -10, -12, -12, -12, -12, -12};
    public static final double[] OLD_PHONE = {7, -7, 7, -7, 7, -7, 7, -7, 7, -7};

    public static final String[] names = {"关闭", "流行", "舞曲", "现场", "蓝调", "古典", "爵士", "慢歌", "电子", "摇滚", "乡村", "人声", "ACG", "ACG 女声", "国风", "民谣", "说唱",
            "柔和", "柔和低音", "柔和高音", "高音", "中音", "低音", "低音 & 高音", "重低音", "超重低音",
            "扬声器（响亮）", "村口大喇叭", "iPhone4s 赠送耳机专用", "高解析", "高音衰减", "喇叭炸机专用", "老年机/儿童玩具"};
    public static final double[][] data = {
            OFF, POP, DANCE, PRESENT, BLUE, CLASSIC, JAZZ, SLOW, ELECTRONIC, ROCK, COUNTRY, VOICE, ACG, ACG_WOMAN, CHINESE_STYLE, FOLK, RAP,
            SOFT, SOFT_BASS, SOFT_TONE, TONE, MEDIANT, BASS, BASS_AND_TONE, MEGA_BASS, SUPER_MEGA_BASS,
            SPEAKER, BIG_HORN, IPHONE4S, HIGH_PARSE, TREBLE_ATTENUATION, HIGH_VOICE_PRESSURE, OLD_PHONE
    };
}