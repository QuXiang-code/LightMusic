package net.doge.utils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

/**
 * @Author yzx
 * @Description 字体工具类
 * @Date 2020/12/15
 */
public class FontUtils {
    /**
     * 加载外部字体
     *
     * @param source
     * @param fontSize
     * @return
     */
    public static Font loadFont(String source, float fontSize) {
        try {
            File file = new File(source);
            FileInputStream fis = new FileInputStream(file);
            Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, fis);
            Font dynamicFontPt = dynamicFont.deriveFont(fontSize);
            fis.close();
            return dynamicFontPt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}