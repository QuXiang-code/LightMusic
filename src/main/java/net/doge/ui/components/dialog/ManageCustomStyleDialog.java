package net.doge.ui.components.dialog;

import net.coobird.thumbnailator.Thumbnails;
import net.doge.constants.BlurType;
import net.doge.constants.Colors;
import net.doge.constants.UIStyleConstants;
import net.doge.models.UIStyle;
import net.doge.ui.PlayerFrame;
import net.doge.ui.components.*;
import net.doge.ui.componentui.ScrollBarUI;
import net.doge.ui.listeners.ButtonMouseListener;
import net.doge.ui.renderers.DefaultStyleListRenderer;
import net.doge.utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @Author yzx
 * @Description 管理自定义样式的对话框
 * @Date 2020/12/15
 */
public class ManageCustomStyleDialog extends JDialog {
    private final String TITLE = "管理主题";
    private final String IMG_LOST_MSG = "主题背景图片丢失，请重新编辑主题";
    private final String EDIT_DENIED_MSG = "不能编辑预设的主题";
    private final String REMOVE_DENIED_MSG = "不能删除预设的主题";
    private final String ASK_REMOVE_MSG = "确定删除选中的主题？";
    private final String SINGLE_SELECT_MSG = "需要编辑的主题一次只能选择一个";
    private ManageCustomStyleDialogPanel globalPanel = new ManageCustomStyleDialogPanel();

    // 最大阴影透明度
    private final int TOP_OPACITY = 30;
    // 阴影大小像素
    private final int pixels = 10;

    private CustomPanel centerPanel = new CustomPanel();

    private CustomPanel topPanel = new CustomPanel();
    private CustomLabel titleLabel = new CustomLabel();
    private CustomPanel windowCtrlPanel = new CustomPanel();
    private CustomButton closeButton = new CustomButton();

    private CustomPanel northPanel = new CustomPanel();
    private CustomPanel tipPanel = new CustomPanel();
    private CustomLabel tipLabel = new CustomLabel("应用、添加、编辑或删除主题（预设主题不能修改），主界面右下角可设置主题背景附加效果");
    private CustomPanel customOnlyPanel = new CustomPanel();
    private CustomCheckBox customOnlyCheckBox = new CustomCheckBox("仅显示自定义主题");
    private CustomList<UIStyle> styleList = new CustomList<>();
    private CustomScrollPane styleListScrollPane = new CustomScrollPane(styleList);
    private DefaultListModel<UIStyle> styleListModel = new DefaultListModel<>();
    private DefaultListModel<UIStyle> emptyListModel = new DefaultListModel<>();
    private DialogButton allSelectButton;
    private DialogButton nonSelectButton;
    private DialogButton applyButton;
    private DialogButton addButton;
    private DialogButton editButton;
    private DialogButton removeButton;

    // 底部盒子
    private Box bottomBox = new Box(BoxLayout.X_AXIS);
    // 右部按钮盒子
    private Box rightBox = new Box(BoxLayout.Y_AXIS);

    private PlayerFrame f;
    private UIStyle style;

    // 父窗口和是否是模态
    public ManageCustomStyleDialog(PlayerFrame f, boolean isModel) {
        super(f, isModel);
        this.f = f;
        this.style = f.currUIStyle;

        Color textColor = style.getTextColor();
        allSelectButton = new DialogButton("全选", textColor);
        nonSelectButton = new DialogButton("反选", textColor);
        applyButton = new DialogButton("应用", textColor);
        addButton = new DialogButton("添加", textColor);
        editButton = new DialogButton("编辑", textColor);
        removeButton = new DialogButton("删除", textColor);
    }

    public void showDialog() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // 解决 setUndecorated(true) 后窗口不能拖动的问题
        Point origin = new Point();
        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                origin.x = e.getX();
                origin.y = e.getY();
            }
        });
        topPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // mouseDragged 不能正确返回 button 值，需要借助此方法
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                Point p = getLocation();
                setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
            }
        });

        setTitle(TITLE);
        setResizable(false);
        setSize(800, 700);

        globalPanel.setLayout(new BorderLayout());

        initTitleBar();
        // 组装界面
        initView();
        // 初始化数据
        initStyles();

        add(globalPanel, BorderLayout.CENTER);
        setUndecorated(true);
        setBackground(Colors.TRANSLUCENT);
        setLocationRelativeTo(null);

        updateBlur();

        f.currDialogs.add(this);
        setVisible(true);
    }

    public void updateBlur() {
        BufferedImage bufferedImage;
        if (f.blurType != BlurType.OFF && f.player.loadedMusic()) {
            bufferedImage = f.player.getMusicInfo().getAlbumImage();
            if (bufferedImage == f.defaultAlbumImage) bufferedImage = ImageUtils.eraseTranslucency(bufferedImage);
            if (f.blurType == BlurType.MC)
                bufferedImage = ImageUtils.dyeRect(1, 1, ImageUtils.getAvgRGB(bufferedImage));
            else if (f.blurType == BlurType.LG)
                bufferedImage = ImageUtils.toGradient(bufferedImage);
        } else {
            UIStyle style = f.currUIStyle;
            bufferedImage = style.getImg();
        }
        doBlur(bufferedImage);
    }

    // 初始化标题栏
    private void initTitleBar() {
        titleLabel.setForeground(style.getTextColor());
        titleLabel.setText(TITLE);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        closeButton.setIcon(ImageUtils.dye(f.closeWindowIcon, style.getIconColor()));
        closeButton.setPreferredSize(new Dimension(f.closeWindowIcon.getIconWidth() + 2, f.closeWindowIcon.getIconHeight()));
        // 关闭窗口
        closeButton.addActionListener(e -> {
            dispose();
            f.currDialogs.remove(this);
        });
        // 鼠标事件
        closeButton.addMouseListener(new ButtonMouseListener(closeButton, f));
        FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
        windowCtrlPanel.setLayout(fl);
        windowCtrlPanel.setMinimumSize(new Dimension(40, 30));
        windowCtrlPanel.add(closeButton);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(windowCtrlPanel);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        globalPanel.add(topPanel, BorderLayout.NORTH);
    }

    // 组装界面
    private void initView() {
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        globalPanel.add(centerPanel, BorderLayout.CENTER);
        // 添加标签
        tipLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Color textColor = style.getTextColor();
        Color iconColor = style.getIconColor();
        tipLabel.setForeground(textColor);
        tipPanel.add(tipLabel);
        customOnlyCheckBox.setSelected(f.customOnly);
        customOnlyCheckBox.setForeground(textColor);
        customOnlyCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        customOnlyCheckBox.setIcon(ImageUtils.dye(f.uncheckedIcon, iconColor));
        customOnlyCheckBox.setSelectedIcon(ImageUtils.dye(f.checkedIcon, iconColor));
        customOnlyCheckBox.addActionListener(e -> {
            f.customOnly = customOnlyCheckBox.isSelected();
            initStyles();
        });
        customOnlyPanel.add(customOnlyCheckBox);

        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(tipPanel);
        northPanel.add(customOnlyPanel);
        centerPanel.add(northPanel, BorderLayout.NORTH);
        // 全选事件
        allSelectButton.addActionListener(e -> {
            // 选择开始到结束(包含)的节点！
            styleList.getSelectionModel().setSelectionInterval(0, styleListModel.getSize() - 1);
        });
        // 取消全选事件
        nonSelectButton.addActionListener(e -> {
            styleList.clearSelection();
        });
        // 应用事件
        applyButton.addActionListener(e -> {
            UIStyle style = styleList.getSelectedValue();
            if (style == null) return;
            if (!style.hasImg()) {
                new TipDialog(f, IMG_LOST_MSG).showDialog();
                return;
            }
            f.changeUIStyle(style);
            updateStyle();
        });
        // 添加事件
        addButton.addActionListener(e -> {
            UIStyle value = styleList.getSelectedValue();
            CustomStyleDialog customStyleDialog = new CustomStyleDialog(f, true, "添加", value != null ? value : f.currUIStyle);
            customStyleDialog.showDialog();
            if (customStyleDialog.getConfirmed()) {
                // 创建自定义样式并更换
                Object[] results = customStyleDialog.getResults();
                UIStyle customStyle = new UIStyle(
                        UIStyleConstants.CUSTOM,
                        ((String) results[0]),
                        "", ((Color) results[2]), ((Color) results[3]),
                        ((Color) results[4]), ((Color) results[5]), ((Color) results[6]),
                        ((Color) results[7]), ((Color) results[8]), ((Color) results[9]),
                        ((Color) results[10]), ((Color) results[11])
                );
                customStyle.setInvokeLater(() -> updateRenderer(styleList));
                if (results[1] instanceof Color) customStyle.setBgColor((Color) results[1]);
                else customStyle.setStyleImgPath((String) results[1]);
                // 添加风格菜单项、按钮组，但不切换风格
                f.addStyle(customStyle);
                // 最后别忘了到列表中添加
                styleListModel.addElement(customStyle);
            }
        });
        // 删除事件
        removeButton.addActionListener(e -> {
            UIStyle value = styleList.getSelectedValue();
            if (value == null) return;
            if (value.isPreDefined()) {
                new TipDialog(f, REMOVE_DENIED_MSG).showDialog();
                return;
            }
            ConfirmDialog d = new ConfirmDialog(f, ASK_REMOVE_MSG, "是", "否");
            d.showDialog();
            if (d.getResponse() == JOptionPane.YES_OPTION) {
                List<UIStyle> selectedStyles = styleList.getSelectedValuesList();
                List<UIStyle> styles = f.styles;
                UIStyle currUIStyle = f.currUIStyle;
                selectedStyles.forEach(style -> {
                    // 删除正在使用的样式，先换回默认样式，再删除
                    if (style == currUIStyle) {
                        f.changeUIStyle(styles.get(0));
                        updateStyle();
                    }
                    styles.remove(style);
                    // 删除图片文件
                    File file = new File(style.getStyleImgPath());
                    // 确保要删除的文件不被其他主题使用
                    boolean canDelete = true;
                    for (UIStyle st : styles) {
                        if (file.equals(new File(st.getStyleImgPath()))) {
                            canDelete = false;
                            break;
                        }
                    }
                    if (canDelete) file.delete();
                    // 最后别忘了从列表中删除
                    styleListModel.removeElement(style);
                });
            }
        });
        // 编辑事件
        editButton.addActionListener(e -> {
            UIStyle value = styleList.getSelectedValue();
            if (value == null) return;
            if (value.isPreDefined()) {
                new TipDialog(f, EDIT_DENIED_MSG).showDialog();
                return;
            }
            CustomStyleDialog dialog = new CustomStyleDialog(f, true, "更新", value);
            int length = styleList.getSelectedIndices().length;
            if (length == 0) return;
            // 只能单选
            if (length > 1) {
                new TipDialog(f, SINGLE_SELECT_MSG).showDialog();
                return;
            }
            dialog.showDialog();
            if (dialog.getConfirmed()) {
                Object[] results = dialog.getResults();
                UIStyle selectedStyle = styleList.getSelectedValue();
                selectedStyle.setStyleName((String) results[0]);
                selectedStyle.setInvokeLater(() -> updateRenderer(styleList));
                if (results[1] instanceof Color) {
                    selectedStyle.setStyleImgPath("");
                    selectedStyle.setBgColor((Color) results[1]);
                } else {
                    selectedStyle.setStyleImgPath((String) results[1]);
                    selectedStyle.setBgColor(null);
                }
                selectedStyle.setForeColor((Color) results[2]);
                selectedStyle.setSelectedColor((Color) results[3]);
                selectedStyle.setLrcColor((Color) results[4]);
                selectedStyle.setHighlightColor((Color) results[5]);
                selectedStyle.setTextColor((Color) results[6]);
                selectedStyle.setTimeBarColor((Color) results[7]);
                selectedStyle.setIconColor((Color) results[8]);
                selectedStyle.setScrollBarColor((Color) results[9]);
                selectedStyle.setSliderColor((Color) results[10]);
                selectedStyle.setSpectrumColor((Color) results[11]);
                // 若编辑的样式正在使用，则更换
                if (f.currUIStyle == selectedStyle) {
                    f.changeUIStyle(selectedStyle);
                    updateStyle();
                }
            }
        });
        // 添加右部按钮
        rightBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        Dimension area = new Dimension(1, 10);
        rightBox.add(Box.createVerticalGlue());
        rightBox.add(allSelectButton);
        rightBox.add(Box.createRigidArea(area));
        rightBox.add(nonSelectButton);
        rightBox.add(Box.createRigidArea(area));
        rightBox.add(applyButton);
        rightBox.add(Box.createRigidArea(area));
        rightBox.add(addButton);
        rightBox.add(Box.createRigidArea(area));
        rightBox.add(editButton);
        rightBox.add(Box.createRigidArea(area));
        rightBox.add(removeButton);
        rightBox.add(Box.createVerticalGlue());
        // 添加列表和右部按钮整体
        DefaultStyleListRenderer r = new DefaultStyleListRenderer(f);
        r.setForeColor(style.getForeColor());
        r.setSelectedColor(style.getSelectedColor());
        r.setTextColor(style.getTextColor());
        styleList.setCellRenderer(r);
        styleList.setModel(styleListModel);
        styleList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = styleList.locationToIndex(e.getPoint());
                Rectangle bounds = styleList.getCellBounds(index, index);
                if (bounds == null) return;
                setHoverIndex(bounds.contains(e.getPoint()) ? index : -1);
            }

            private void setHoverIndex(int index) {
                DefaultStyleListRenderer renderer = (DefaultStyleListRenderer) styleList.getCellRenderer();
                if (renderer == null) return;
                int hoverIndex = renderer.getHoverIndex();
                if (hoverIndex == index) return;
                renderer.setHoverIndex(index);
                // 奇怪的黑背景解决
                repaint();
            }
        });
        styleList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                DefaultStyleListRenderer renderer = (DefaultStyleListRenderer) styleList.getCellRenderer();
                if (renderer == null) return;
                renderer.setHoverIndex(-1);
                repaint();
            }
        });
        styleList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 鼠标左键双击应用风格
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    applyButton.doClick();
                }
            }
        });
        Color scrollBarColor = style.getScrollBarColor();
        styleListScrollPane.setHUI(new ScrollBarUI(scrollBarColor));
        styleListScrollPane.setVUI(new ScrollBarUI(scrollBarColor));
        styleListScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottomBox.add(styleListScrollPane);
        bottomBox.add(rightBox);
        centerPanel.add(bottomBox, BorderLayout.CENTER);
    }

    // 初始化数据
    private void initStyles() {
        List<UIStyle> styles = f.styles;
        styleList.setModel(emptyListModel);
        styleListModel.clear();
        if (f.customOnly) {
            styles.forEach(style -> {
                if (style.isCustom()) styleListModel.addElement(style);
            });
        } else {
            styles.forEach(style -> styleListModel.addElement(style));
        }
        styleList.setModel(styleListModel);
    }

    private void updateRenderer(JList list) {
        ListCellRenderer renderer = list.getCellRenderer();
        list.setCellRenderer(null);
        list.setCellRenderer(renderer);
    }

    // 主题更换时更新窗口主题
    private void updateStyle() {
        UIStyle st = f.currUIStyle;
        Color textColor = st.getTextColor();
        Color iconColor = st.getIconColor();
        Color scrollBarColor = st.getScrollBarColor();

        titleLabel.setForeground(textColor);
        closeButton.setIcon(ImageUtils.dye((ImageIcon) closeButton.getIcon(), iconColor));
        tipLabel.setForeground(textColor);
        customOnlyCheckBox.setForeground(textColor);
        customOnlyCheckBox.setIcon(ImageUtils.dye(f.uncheckedIcon, iconColor));
        customOnlyCheckBox.setSelectedIcon(ImageUtils.dye(f.checkedIcon, iconColor));
        allSelectButton.setForeColor(textColor);
        nonSelectButton.setForeColor(textColor);
        applyButton.setForeColor(textColor);
        addButton.setForeColor(textColor);
        editButton.setForeColor(textColor);
        removeButton.setForeColor(textColor);
        DefaultStyleListRenderer r = (DefaultStyleListRenderer) styleList.getCellRenderer();
        r.setForeColor(st.getForeColor());
        r.setSelectedColor(st.getSelectedColor());
        r.setTextColor(textColor);
        styleList.repaint();

        styleListScrollPane.setHUI(new ScrollBarUI(scrollBarColor));
        styleListScrollPane.setVUI(new ScrollBarUI(scrollBarColor));
    }

    private void doBlur(BufferedImage bufferedImage) {
        int dw = getWidth() - 2 * pixels, dh = getHeight() - 2 * pixels;
        try {
            boolean loadedMusic = f.player.loadedMusic();
            // 截取中间的一部分(有的图片是长方形)
            if (loadedMusic && f.blurType == BlurType.CV) bufferedImage = ImageUtils.cropCenter(bufferedImage);
            // 处理成 100 * 100 大小
            if (f.gsOn) bufferedImage = ImageUtils.width(bufferedImage, 100);
            // 消除透明度
            bufferedImage = ImageUtils.eraseTranslucency(bufferedImage);
            // 高斯模糊并暗化
            if (f.gsOn) bufferedImage = ImageUtils.doBlur(bufferedImage);
            if (f.darkerOn) bufferedImage = ImageUtils.darker(bufferedImage);
            // 放大至窗口大小
            bufferedImage = ImageUtils.width(bufferedImage, dw);
            if (dh > bufferedImage.getHeight())
                bufferedImage = ImageUtils.height(bufferedImage, dh);
            // 裁剪中间的一部分
            if (!loadedMusic || f.blurType == BlurType.CV || f.blurType == BlurType.OFF) {
                int iw = bufferedImage.getWidth(), ih = bufferedImage.getHeight();
                bufferedImage = Thumbnails.of(bufferedImage)
                        .scale(1f)
                        .sourceRegion(iw > dw ? (iw - dw) / 2 : 0, iw > dw ? 0 : (ih - dh) / 2, dw, dh)
                        .outputQuality(0.1)
                        .asBufferedImage();
            } else {
                bufferedImage = ImageUtils.forceSize(bufferedImage, dw, dh);
            }
            // 设置圆角
            bufferedImage = ImageUtils.setRadius(bufferedImage, 10);
            globalPanel.setBackgroundImage(bufferedImage);
            repaint();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private class ManageCustomStyleDialogPanel extends JPanel {
        private BufferedImage backgroundImage;

        public ManageCustomStyleDialogPanel() {
            // 阴影边框
            Border border = BorderFactory.createEmptyBorder(pixels, pixels, pixels, pixels);
            setBorder(BorderFactory.createCompoundBorder(getBorder(), border));
        }

        public void setBackgroundImage(BufferedImage backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            // 避免锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (backgroundImage != null) {
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2d.drawImage(backgroundImage, pixels, pixels, getWidth() - 2 * pixels, getHeight() - 2 * pixels, this);
            }

            // 画边框阴影
            for (int i = 0; i < pixels; i++) {
                g2d.setColor(new Color(0, 0, 0, ((TOP_OPACITY / pixels) * i)));
                g2d.drawRoundRect(i, i, getWidth() - ((i * 2) + 1), getHeight() - ((i * 2) + 1), 10, 10);
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }
}
