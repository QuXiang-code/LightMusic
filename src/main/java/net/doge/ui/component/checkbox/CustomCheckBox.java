package net.doge.ui.component.checkbox;

import net.doge.constant.ui.Fonts;

import javax.swing.*;

public class CustomCheckBox extends JCheckBox {
    public CustomCheckBox() {
        super();
        init();
    }

    public CustomCheckBox(String text) {
        super(text);
        init();
    }

    private void init() {
        setOpaque(false);
        setFocusPainted(false);
        setFont(Fonts.NORMAL);
        setIconTextGap(10);
    }
}