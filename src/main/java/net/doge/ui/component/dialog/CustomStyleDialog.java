package net.doge.ui.component.dialog;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import net.doge.constant.ui.Colors;
import net.doge.constant.player.Format;
import net.doge.constant.system.SimplePath;
import net.doge.model.ui.UIStyle;
import net.doge.ui.MainFrame;
import net.doge.ui.component.button.DialogButton;
import net.doge.ui.component.dialog.factory.AbstractTitledDialog;
import net.doge.ui.component.label.CustomLabel;
import net.doge.ui.component.list.CustomScrollPane;
import net.doge.ui.component.panel.CustomPanel;
import net.doge.ui.component.textfield.CustomTextField;
import net.doge.ui.componentui.list.ScrollBarUI;
import net.doge.util.system.FileUtil;
import net.doge.util.ui.ImageUtil;
import net.doge.util.common.StringUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @Author yzx
 * @Description 自定义样式的对话框
 * @Date 2020/12/15
 */
public class CustomStyleDialog extends AbstractTitledDialog implements DocumentListener {
    private final int imgWidth = 150;
    private final int imgHeight = 120;
    private final int rectWidth = 170;
    private final int rectHeight = 30;
    private final String STYLE_NAME_NOT_NULL_MSG = "emmm~~主题名称不能为无名氏哦";
    private final String STYLE_NAME_DUPLICATE_MSG = "emmm~该主题名称已存在，换一个吧";
    private final String IMG_FILE_NOT_EXIST_MSG = "选定的图片路径无效";
    private final String IMG_NOT_VALID_MSG = "不是有效的图片文件";

    private CustomPanel centerPanel = new CustomPanel();
    private CustomScrollPane centerScrollPane = new CustomScrollPane(centerPanel);
    private CustomPanel buttonPanel = new CustomPanel();

    private final CustomLabel[] labels = {
            new CustomLabel("主题名称："),
            new CustomLabel("背景图片："),
            new CustomLabel("列表悬停框颜色："),
            new CustomLabel("列表选中框颜色："),
            new CustomLabel("歌词文字颜色："),
            new CustomLabel("歌词高亮颜色："),
            new CustomLabel("界面文字颜色："),
            new CustomLabel("时间条颜色："),
            new CustomLabel("图标颜色："),
            new CustomLabel("滚动条颜色："),
            new CustomLabel("音量条颜色："),
            new CustomLabel("频谱颜色：")
    };

    private final Component[] components = {
            new CustomTextField(15),
            new DialogButton("选择图片"),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel()
    };
    private DialogButton pureColor;

    private DialogButton okButton;
    private DialogButton cancelButton;

    // 面板展示的样式
    private UIStyle showedStyle;

    private boolean confirmed = false;
    private Object[] results = new Object[components.length];

    // 父窗口，传入 OK 按钮文字，要展示的样式(添加则用当前样式，编辑则用选中样式)
    public CustomStyleDialog(MainFrame f, String okButtonText, UIStyle showedStyle) {
        super(f, "自定义主题");
        this.showedStyle = showedStyle;

        Color textColor = f.currUIStyle.getTextColor();
        okButton = new DialogButton(okButtonText, textColor);
        cancelButton = new DialogButton("取消", textColor);
        pureColor = new DialogButton("纯色", textColor);
    }

    public void showDialog() {
        setResizable(false);
        setSize(960, 750);

        globalPanel.setLayout(new BorderLayout());

        initTitleBar();
        initView();

        globalPanel.add(centerScrollPane, BorderLayout.CENTER);
        okButton.addActionListener(e -> {
            // 风格名称不为空
            if (results[0].equals("")) {
                new TipDialog(f, STYLE_NAME_NOT_NULL_MSG).showDialog();
                return;
            }
            // 风格名称不重复
            List<UIStyle> styles = f.styles;
            for (UIStyle style : styles) {
                // 添加时，名称一定不相等；编辑时，只允许同一样式名称相等
                if (style.getStyleName().equals(results[0])
                        && (style != showedStyle || okButton.getPlainText().contains("添加"))) {
                    new TipDialog(f, STYLE_NAME_DUPLICATE_MSG).showDialog();
                    return;
                }
            }
            // 图片路径
            if (results[1] instanceof String) {
                // 复制图片(如果有)
                File imgFile = new File(((String) results[1]));
                // 图片存在且是文件
                if (imgFile.exists() && imgFile.isFile()) {
                    try {
                        // 文件夹不存在就创建
                        File dir = new File(SimplePath.CUSTOM_STYLE_IMG_PATH);
                        FileUtil.makeSureDir(dir);
                        String newPath = SimplePath.CUSTOM_STYLE_IMG_PATH + imgFile.getName();
                        Files.copy(
                                Paths.get(imgFile.getPath()),
                                Paths.get(newPath),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                        // 设置新的路径
                        results[1] = newPath;
                        // 更新时删除原来的图片
                        String imgPath = f.currUIStyle.getStyleImgPath();
                        if (StringUtil.isNotEmpty(imgPath)) {
                            File sf = new File(imgPath);
                            File df = new File(newPath);
                            if (f.currUIStyle == showedStyle && !sf.equals(df) && sf.getParentFile().equals(dir))
                                sf.delete();
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    new TipDialog(f, IMG_FILE_NOT_EXIST_MSG).showDialog();
                    return;
                }
            }
            confirmed = true;
            dispose();
            f.currDialogs.remove(this);
        });
        cancelButton.addActionListener(e -> close());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        globalPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(globalPanel, BorderLayout.CENTER);
        setUndecorated(true);
        setBackground(Colors.TRANSLUCENT);
        setLocationRelativeTo(null);

        updateBlur();

        f.currDialogs.add(this);
        setVisible(true);
    }

    private void initView() {
        centerPanel.setLayout(new GridLayout(6, 2));

        // 获得传入的界面样式
        results[0] = showedStyle.getStyleName();
        String styleImgPath = showedStyle.getStyleImgPath();
        results[1] = StringUtil.isEmpty(styleImgPath) ? showedStyle.getBgColor() : styleImgPath;
        results[2] = showedStyle.getForeColor();
        results[3] = showedStyle.getSelectedColor();
        results[4] = showedStyle.getLrcColor();
        results[5] = showedStyle.getHighlightColor();
        results[6] = showedStyle.getTextColor();
        results[7] = showedStyle.getTimeBarColor();
        results[8] = showedStyle.getIconColor();
        results[9] = showedStyle.getScrollBarColor();
        results[10] = showedStyle.getSliderColor();
        results[11] = showedStyle.getSpectrumColor();

        Border eb = BorderFactory.createEmptyBorder(0, 20, 0, 20);

        Color textColor = f.currUIStyle.getTextColor();
        for (int i = 0, size = labels.length; i < size; i++) {
            // 左对齐容器
            CustomPanel panel = new CustomPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(eb);
            // 添加标签
            labels[i].setForeground(textColor);
            panel.add(labels[i]);
            // 组件配置
            if (components[i] instanceof CustomTextField) {
                CustomTextField component = (CustomTextField) components[i];
                component.setForeground(textColor);
                component.setCaretColor(textColor);
                // 加载风格名称
                component.setText((String) results[i]);
                Document document = component.getDocument();
                // 添加文本改变监听器
                document.addDocumentListener(this);
            } else if (components[i] instanceof DialogButton) {
                DialogButton component = (DialogButton) components[i];
                component.setForeColor(textColor);
                labels[i].setHorizontalTextPosition(SwingConstants.LEFT);
                // 加载当前样式背景图(显示一个缩略图)
                if (results[i] != null) {
                    if (results[i] instanceof String) {
                        BufferedImage image = ImageUtil.read((String) results[i]);
                        if (image != null) {
                            if (image.getWidth() >= image.getHeight())
                                labels[i].setIcon(new ImageIcon(ImageUtil.width(image, imgWidth)));
                            else labels[i].setIcon(new ImageIcon(ImageUtil.height(image, imgHeight)));
                        }
                    } else {
                        labels[i].setIcon(new ImageIcon(ImageUtil.width(ImageUtil.dyeRect(2, 1, (Color) results[i]), imgWidth)));
                    }
                }
                int finalI = i;
                // 图片文件选择
                component.addActionListener(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("选择图片");
                    ObservableList<FileChooser.ExtensionFilter> filters = fileChooser.getExtensionFilters();
                    // 添加可读取的图片格式
                    String allSuffix = "";
                    for (String suffix : Format.READ_IMAGE_TYPE_SUPPORTED) {
                        filters.add(new FileChooser.ExtensionFilter(suffix.toUpperCase(), "*." + suffix));
                        allSuffix += "*." + suffix + ";";
                    }
                    filters.add(0, new FileChooser.ExtensionFilter("图片文件", allSuffix));
                    Platform.runLater(() -> {
                        File file = fileChooser.showOpenDialog(null);
                        if (file != null) {
                            results[finalI] = file.getPath();
                            BufferedImage img = ImageUtil.read((String) results[finalI]);
                            if (img == null) {
                                new TipDialog(f, IMG_NOT_VALID_MSG).showDialog();
                                return;
                            }
                            if (img.getWidth() >= img.getHeight())
                                labels[finalI].setIcon(new ImageIcon(ImageUtil.width(img, imgWidth)));
                            else labels[finalI].setIcon(new ImageIcon(ImageUtil.height(img, imgHeight)));
                            setLocationRelativeTo(null);
                        }
                    });
                });
            } else if (components[i] instanceof CustomLabel) {
                CustomLabel component = (CustomLabel) components[i];
                // 鼠标光标
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // 获取风格颜色并显示成小方格
                component.setIcon(ImageUtil.dyeRoundRect(rectWidth, rectHeight, ((Color) results[i])));
                int finalI = i;
                // 颜色选择
                component.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            ColorChooserDialog d = new ColorChooserDialog(f, (Color) results[finalI]);
                            d.showDialog();
                            if (!d.isConfirmed()) return;
                            Color color = d.getResult();
                            // 更改方框内颜色并保存
                            component.setIcon(ImageUtil.dyeRoundRect(rectWidth, rectHeight, color));
                            results[finalI] = color;
                        }
                    }
                });
            }
            panel.add(components[i]);

            CustomPanel outer = new CustomPanel();
            outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
            outer.add(Box.createVerticalGlue());
            outer.add(panel);
            outer.add(Box.createVerticalGlue());

            centerPanel.add(outer);
        }

        // 纯色按钮
        pureColor.addActionListener(e -> {
            ColorChooserDialog d = new ColorChooserDialog(f, results[1] instanceof Color ? (Color) results[1] : Colors.THEME);
            d.showDialog();
            if (!d.isConfirmed()) return;
            Color color = d.getResult();
            // 更改方框内颜色并保存
            labels[1].setIcon(new ImageIcon(ImageUtil.width(ImageUtil.dyeRect(2, 1, color), imgWidth)));
            results[1] = color;
            setLocationRelativeTo(null);
        });
        ((CustomPanel) ((CustomPanel) centerPanel.getComponent(1)).getComponent(1)).add(pureColor);

        Color scrollBarColor = f.currUIStyle.getScrollBarColor();
        centerScrollPane.setHUI(new ScrollBarUI(scrollBarColor));
        centerScrollPane.setVUI(new ScrollBarUI(scrollBarColor));
        centerScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        results[0] = ((CustomTextField) components[0]).getText();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        results[0] = ((CustomTextField) components[0]).getText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        results[0] = ((CustomTextField) components[0]).getText();
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public Object[] getResults() {
        return results;
    }
}