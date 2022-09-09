package net.doge.ui.componentui;

import net.doge.models.MusicPlayer;
import net.doge.ui.PlayerFrame;
import net.doge.ui.components.dialog.TipDialog;
import net.doge.ui.components.dialog.VideoDialog;
import net.doge.utils.StringUtils;
import net.doge.utils.TimeUtils;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @Author yzx
 * @Description 播放时间条自定义 UI
 * @Date 2020/12/13
 */
public class SliderUI extends BasicSliderUI {
    private Color thumbColor;
    private Color trackColor;
    private Color trackBgColor;
    private boolean isTimeBar;
    private PlayerFrame f;
    private MusicPlayer player;
    private MediaPlayer mp;
    private TipDialog dialog;
    private TipDialog lrcDialog;

    private boolean bigThumb;
    private boolean cursorOnSlider;

    // 音频播放器
    public SliderUI(JSlider slider, Color thumbColor, Color trackColor, PlayerFrame f, MusicPlayer player, boolean isTimeBar) {
        super(slider);
        this.thumbColor = thumbColor;
        this.trackColor = trackColor;
        this.trackBgColor = trackColor.darker();
        this.f = f;
        this.player = player;
        this.dialog = new TipDialog(f, 0);
        if (isTimeBar) this.lrcDialog = new TipDialog(f, 0);
        this.isTimeBar = isTimeBar;
    }

    // 视频播放器
    public SliderUI(JSlider slider, Color thumbColor, Color trackColor, PlayerFrame f, MediaPlayer mp, boolean isTimeBar) {
        super(slider);
        this.thumbColor = thumbColor;
        this.trackColor = trackColor;
        this.trackBgColor = trackColor.darker();
        this.f = f;
        this.mp = mp;
        this.dialog = new TipDialog(f, 0);
        dialog.setAlwaysOnTop(true);
        this.isTimeBar = isTimeBar;
    }

    /**
     * 自定义把手
     *
     * @param g
     */
    @Override
    public void paintThumb(Graphics g) {
        if (!bigThumb) return;
        Graphics2D g2d = (Graphics2D) g;
        // 避免锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2d.setColor(thumbColor);
        g2d.fillOval(thumbRect.x, thumbRect.y + 4, thumbRect.width, thumbRect.width);
//        else g2d.fillOval(thumbRect.x + 2, thumbRect.y + 6, thumbRect.width - 4, thumbRect.width - 4);
    }

    /**
     * 自定义滑道
     *
     * @param g
     */
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // 避免锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画未填充部分
        g2d.setColor(trackBgColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, isTimeBar ? 0.2f : 0.3f));
        int thx = Math.max(thumbRect.x, trackRect.x);
//        g2d.fillRect(
//                thx,
//                trackRect.y + 9,
//                trackRect.width - thx + trackRect.x,
//                trackRect.height - 18
//        );
        g2d.fillRoundRect(thx, trackRect.y + 8, trackRect.width - thx + trackRect.x, trackRect.height - 16, 4, 4);
        // 在时间条画出缓冲完成的部分
        if (isTimeBar) {
            int w = mp == null ? (int) (player.getBufferedSeconds() / player.getDurationSeconds() * trackRect.width + 0.5)
                    : (int) (mp.getBufferProgressTime() == null ? 0 : mp.getBufferProgressTime().toSeconds() / mp.getMedia().getDuration().toSeconds() * trackRect.width + 0.5);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.fillRoundRect(
                    thx,
                    trackRect.y + 8,
                    // 缓冲长度没超过已填充长度时可以不画；缓冲长度超出轨道长度时取轨道长度
                    Math.min(Math.max(w - thx + trackRect.x, 0), trackRect.width - thx + trackRect.x),
                    trackRect.height - 16, 4, 4
            );
        }
        // 画已填充部分
        g2d.setColor(trackColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2d.fillRoundRect(
                trackRect.x,
                trackRect.y + 8,
                thumbRect.x - trackRect.x + thumbRect.width / 2,
                trackRect.height - 16, 4, 4
        );
    }

    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new TrackListener() {
            private void update(MouseEvent e, boolean showDialog) {
                slider.setValueIsAdjusting(true);
                slider.setValue((int) ((double) (e.getX() - trackRect.x) / trackRect.width * slider.getMaximum()));
                if (showDialog) {
                    if (isTimeBar) {
                        String dStr = mp == null ? player.getDurationString() : TimeUtils.format(mp.getMedia().getDuration().toSeconds());
                        double dSec = mp == null ? player.getDurationSeconds() : mp.getMedia().getDuration().toSeconds();
                        double cSec = (double) slider.getValue() / slider.getMaximum() * dSec;
                        String lrc = mp == null ? f.getTimeLrc(cSec) : "";
                        dialog.setMessage(String.format("%s / %s", TimeUtils.format(cSec), dStr));
                        if (lrcDialog != null) {
                            lrcDialog.setMessage(StringUtils.textToHtml(lrc));
                            lrcDialog.updateSize();
                            // 将把手坐标转为屏幕上的坐标，确定歌词对话框位置
                            Point p = new Point(thumbRect.x, thumbRect.y);
                            SwingUtilities.convertPointToScreen(p, slider);
                            lrcDialog.setLocation(p.x - lrcDialog.getWidth() / 2 + thumbRect.width / 2, p.y - lrcDialog.getHeight() - 5);
                        }
                    } else
                        dialog.setMessage("音量：" + slider.getValue());
                    if (!dialog.isShowing()) dialog.showDialog();
                    if (lrcDialog != null && lrcDialog.isNotEmpty() && !lrcDialog.isShowing()) lrcDialog.showDialog();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                update(e, false);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                update(e, true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 鼠标在滑动条之外且松开时把手变小
                if (!cursorOnSlider) {
                    bigThumb = false;
                    slider.repaint();
                }
                if (dialog.isShowing()) dialog.close();
                if (lrcDialog != null && lrcDialog.isNotEmpty() && lrcDialog.isShowing()) lrcDialog.close();
                slider.setValueIsAdjusting(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cursorOnSlider = true;
                // 鼠标进入时画出大把手
                bigThumb = true;
                slider.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cursorOnSlider = false;
                // 鼠标退出时且不在拖拽状态把手变小
                if (!slider.getValueIsAdjusting()) {
                    bigThumb = false;
                    slider.repaint();
                }
            }
        };
    }
}
