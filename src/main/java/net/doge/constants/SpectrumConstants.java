package net.doge.constants;

/**
 * @Author yzx
 * @Description 频谱相关参数
 * @Date 2020/12/14
 */
public class SpectrumConstants {
    // 播放器更新频谱数量
    public static final int NUM_BANDS = 1024;
    // 播放器更新频谱时间间隔(s)
    public static final double PLAYER_INTERVAL = 0.09;
    // UI 更新频谱 Timer 时间间隔(ms)
    public static final int TIMER_INTERVAL = 10;
    // 频谱条数量
    public static final int BAR_NUM = 60;
    // 频谱每条宽度
    public static final int BAR_WIDTH = 7;
    // 频谱最大高度
    public static final int BAR_MAX_HEIGHT = 150;
    // 频谱条与条之间的间距
    public static final int BAR_GAP = 3;
    // 频谱阈值
    public static final int THRESHOLD = -80;
}
