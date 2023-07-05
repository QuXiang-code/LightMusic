package net.doge.ui.component.dialog;

import com.mpatric.mp3agic.ID3v1Genres;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import net.doge.constant.ui.Colors;
import net.doge.constant.player.Format;
import net.doge.model.entity.AudioFile;
import net.doge.model.entity.MediaInfo;
import net.doge.ui.MainFrame;
import net.doge.ui.component.button.DialogButton;
import net.doge.ui.component.combobox.CustomComboBox;
import net.doge.ui.component.dialog.factory.AbstractTitledDialog;
import net.doge.ui.component.label.CustomLabel;
import net.doge.ui.component.list.CustomScrollPane;
import net.doge.ui.component.panel.CustomPanel;
import net.doge.ui.component.textfield.CustomTextField;
import net.doge.ui.componentui.combobox.ComboBoxUI;
import net.doge.ui.componentui.list.ScrollBarUI;
import net.doge.util.media.MusicUtil;
import net.doge.util.system.FileUtil;
import net.doge.util.common.StringUtil;
import net.doge.util.common.TimeUtil;
import net.doge.util.ui.ImageUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @Author yzx
 * @Description 编辑歌曲信息的对话框
 * @Date 2020/12/15
 */
public class EditInfoDialog extends AbstractTitledDialog {
    private final int imgWidth = 120;
    private final int imgHeight = 120;

    // 文件正在使用提示
    private final String FILE_BEING_USED_MSG = "文件正在被占用，无法修改";

    private CustomPanel centerPanel = new CustomPanel();
    private CustomScrollPane centerScrollPane = new CustomScrollPane(centerPanel);
    private CustomPanel buttonPanel = new CustomPanel();

    private final CustomLabel[] labels = {
            new CustomLabel("文件名："),
            new CustomLabel("文件大小："),
            new CustomLabel("创建时间："),
            new CustomLabel("修改时间："),
            new CustomLabel("访问时间："),
            new CustomLabel("时长："),
            new CustomLabel("标题："),
            new CustomLabel("艺术家："),
            new CustomLabel("专辑："),
            new CustomLabel("流派："),
            new CustomLabel("注释："),
            new CustomLabel("版权："),
            new CustomLabel("封面图片：")
    };

    private CustomComboBox<String> comboBox = new CustomComboBox<>();
    private final int columns = 20;
    private final Component[] components = {
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomLabel(),
            new CustomTextField(columns),
            new CustomTextField(columns),
            new CustomTextField(columns),
            comboBox,
            new CustomTextField(columns),
            new CustomTextField(columns),
            new DialogButton("选择图片")
    };

    private DialogButton okButton;
    private DialogButton cancelButton;

    // 面板展示的文件
    private AudioFile file;

    private boolean confirmed = false;
    private Object[] results = new Object[components.length];

    // 父窗口，传入要展示的文件
    public EditInfoDialog(MainFrame f, AudioFile file) {
        super(f, "歌曲信息");
        this.file = file;

        Color textColor = f.currUIStyle.getTextColor();
        okButton = new DialogButton("保存", textColor);
        cancelButton = new DialogButton("取消", textColor);

        comboBox.addItem("");
        for (String genre : ID3v1Genres.GENRES) comboBox.addItem(genre);
    }

    public void showDialog() {
        setResizable(false);
        setSize(960, 750);

        globalPanel.setLayout(new BorderLayout());

        initTitleBar();
        initView();

        globalPanel.add(centerScrollPane, BorderLayout.CENTER);
        okButton.addActionListener(e -> {
            if (f.player.loadedAudioFile(file)) {
                new TipDialog(f, FILE_BEING_USED_MSG).showDialog();
                return;
            } else {
                for (int i = 0, size = labels.length; i < size; i++) {
                    if (components[i] instanceof CustomTextField) {
                        results[i] = ((CustomTextField) components[i]).getText();
                    }
                }
                try {
                    MediaInfo mediaInfo = new MediaInfo(
                            (String) results[6],
                            (String) results[7],
                            (String) results[8],
                            (String) results[9],
                            (String) results[10],
                            (String) results[11],
                            (BufferedImage) results[12]
                    );
                    MusicUtil.writeMP3Info(file.getAbsolutePath(), mediaInfo);
                    // 歌曲信息更改后重新填充
                    MusicUtil.fillAudioFileInfo(file);
                } catch (InvalidDataException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedTagException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (NotSupportedException ex) {
                    ex.printStackTrace();
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
        centerPanel.setLayout(new GridLayout(7, 2));

        // 获得传入的歌曲信息
        String fileName = StringUtil.wrapLineByWidth(file.getName(), 300);
        String fileSize = FileUtil.getUnitString(FileUtil.getDirOrFileSize(file));
        String creationTime = TimeUtil.msToDatetime(FileUtil.getCreationTime(file));
        String lastModifiedTime = TimeUtil.msToDatetime(file.lastModified());
        String lastAccessTime = TimeUtil.msToDatetime(FileUtil.getAccessTime(file));
        String duration = TimeUtil.format(file.getDuration());
        String title = file.getSongName();
        String artist = file.getArtist();
        String album = file.getAlbum();
        String genre = MusicUtil.getGenre(file);
        String comment = MusicUtil.getComment(file);
        String copyright = MusicUtil.getCopyright(file);
        BufferedImage albumImage = MusicUtil.getAlbumImage(file);

        results[0] = fileName;
        results[1] = fileSize;
        results[2] = creationTime;
        results[3] = lastModifiedTime;
        results[4] = lastAccessTime;
        results[5] = duration;
        results[6] = title;
        results[7] = artist;
        results[8] = album;
        results[9] = genre;
        results[10] = comment;
        results[11] = copyright;
        results[12] = albumImage;

        Border b = BorderFactory.createEmptyBorder(0, 20, 0, 20);

        Color textColor = f.currUIStyle.getTextColor();
        for (int i = 0, size = labels.length; i < size; i++) {
            // 左对齐容器
            CustomPanel panel = new CustomPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(b);
            // 添加标签
            labels[i].setForeground(textColor);
            panel.add(labels[i]);
            // 组件配置
            if (components[i] instanceof CustomLabel) {
                CustomLabel component = (CustomLabel) components[i];
                component.setForeground(textColor);
                component.setText(StringUtil.textToHtml((String) results[i]));
            } else if (components[i] instanceof CustomTextField) {
                CustomTextField component = (CustomTextField) components[i];
                component.setForeground(textColor);
                component.setCaretColor(textColor);
                component.setText((String) results[i]);
            } else if (components[i] instanceof CustomComboBox) {
                CustomComboBox component = (CustomComboBox) components[i];
                // 下拉框 UI
                component.setUI(new ComboBoxUI(component, f));

                int finalI = i;
                component.addItemListener(e -> {
                    results[finalI] = e.getItem().toString();
                });
                component.setSelectedItem(results[i]);
            } else if (components[i] instanceof DialogButton) {
                DialogButton component = (DialogButton) components[i];
                component.setForeColor(textColor);
                labels[i].setHorizontalTextPosition(SwingConstants.LEFT);
                // 加载封面图片(显示一个缩略图)
                if (results[i] != null) {
                    BufferedImage image = (BufferedImage) results[i];
                    if (image.getWidth() >= image.getHeight())
                        labels[i].setIcon(new ImageIcon(ImageUtil.width(image, imgWidth)));
                    else labels[i].setIcon(new ImageIcon(ImageUtil.height(image, imgHeight)));
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
                            BufferedImage image = ImageUtil.read(file);
                            results[finalI] = image;
                            if (image.getWidth() >= image.getHeight())
                                labels[finalI].setIcon(new ImageIcon(ImageUtil.width(image, imgWidth)));
                            else labels[finalI].setIcon(new ImageIcon(ImageUtil.height(image, imgHeight)));
                            setLocationRelativeTo(null);
                        }
                    });
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

        Color scrollBarColor = f.currUIStyle.getScrollBarColor();
        centerScrollPane.setHUI(new ScrollBarUI(scrollBarColor));
        centerScrollPane.setVUI(new ScrollBarUI(scrollBarColor));
        centerScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    public boolean getConfirmed() {
        return confirmed;
    }
}