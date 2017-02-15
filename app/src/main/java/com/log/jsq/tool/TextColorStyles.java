package com.log.jsq.tool;

import android.text.Html;
import android.text.Spanned;

import com.log.jsq.library.FuHao;

/**
 * 【colorArray的存储方式】
 * r0:符号是否变色的标记（如果有则为开始位置）
 * r1:变色符号在strs的位置（获得变色符号长度）
 */
public class TextColorStyles {
    private static String htmlStart = null;     //HTML标签头（指定文本颜色）
    private static String htmlEnd = null;       //HTML标签尾
    private static int COLOR_ARRAY_TEXT_START_HANG = 0; //文本变色开始的位置的行数（适用于colorArray）
    private static int COLOR_ARRAY_STRS_INDEX_HANG = 1; //变色符号对应的位置的行数（适用于colorArray）
    private static int NOT_CHANGE_COLOR = -1;           //不变色的标记（适用于colorArray）

    //文本变色（主）
    public static Spanned run(String text, int color) {
        StringBuffer textBuf = null;  //需要变色的算式
        String strs[] = null;        //需要变色的符号（字符串）
        final int colorArrayLie = text.length();        //colorArray的列数
        int[][] colorArray = new int[2][colorArrayLie]; //控制变色的数组
        textBuf = new StringBuffer(text);               //需要变色的文本

        //初始化符号数组
        htmlStart = "<font color=\"#" + Integer.toString(color, 16) + "\">";    //HTML标签头（指定文本颜色）
        htmlEnd = "</font>";    //HTML标签尾
        strs = new String[6];   //需要变色的符号（字符串数组）

        strs[0] = FuHao.jia;
        strs[1] = FuHao.jian;
        strs[2] = FuHao.cheng;
        strs[3] = FuHao.chu;
        strs[4] = FuHao.kuoHaoTou;
        strs[5] = FuHao.kuoHaoWei;

        //初始化colorArray，标记所有元素为“不变色”
        for (int i=0;i<colorArray.length;i++) {
            for (int j=0;j<colorArrayLie;j++) {
                colorArray[i][j] = NOT_CHANGE_COLOR;
            }
        }

        //标记colorArray，标记所有要变色的元素为它的位置
        //外循环 - 对符号
        for (int strsIndex = 0;strsIndex < strs.length;strsIndex++) {
            //内循环 - 对文本
            for (int textIndex = 0;true;) {
                textIndex = textBuf.indexOf(strs[strsIndex], textIndex);    //找到可变色符号的位置

                if (textIndex < 0) {
                    break;  //找不到则跳出循环，开始从下一个符号找
                } else {
                    //检测负号
                    if (strs[strsIndex].equals(FuHao.jian) && jianCeFu(textBuf.toString(),textIndex)) {
                        textIndex += FuHao.jian.length();
                        continue;
                    }

                    colorArray[COLOR_ARRAY_TEXT_START_HANG][textIndex] = textIndex; //标记当前变色符号在 textBuf 中的开始位置
                    colorArray[COLOR_ARRAY_STRS_INDEX_HANG][textIndex] = strsIndex; //标记当前变色符号在 strs 中的位置（获得变色符号长度）
                    textIndex += strs[strsIndex].length();
                }
            }
        }

        //文本变色处理（参照colorArray）
        for (int index=0;index<colorArrayLie;index++) {
            final int textStartIndex = colorArray[COLOR_ARRAY_TEXT_START_HANG][index];  //变色符号 textBuf 中的开始位置
            final int strsIndex = colorArray[COLOR_ARRAY_STRS_INDEX_HANG][index];       //变色符号在 strs 中的位置

            if (textStartIndex == NOT_CHANGE_COLOR || strsIndex == NOT_CHANGE_COLOR) {
                continue;   //跳过不变色的元素
            }

            textBuf.insert(textStartIndex, htmlStart);                                                  //添加HTML标签头
            textBuf.insert(textStartIndex + htmlStart.length() + strs[strsIndex].length(), htmlEnd);    //添加HTML标签尾

            //更新当前符号以后的所有符号的在 textBuf 中的位置
            for (int afterIndex=index+1;afterIndex<colorArrayLie;afterIndex++) {
                if (colorArray[COLOR_ARRAY_TEXT_START_HANG][afterIndex] != NOT_CHANGE_COLOR && colorArray[COLOR_ARRAY_STRS_INDEX_HANG][afterIndex] != NOT_CHANGE_COLOR) {
                    colorArray[COLOR_ARRAY_TEXT_START_HANG][afterIndex] += htmlStart.length() + htmlEnd.length();
                }
            }
        }

        return Html.fromHtml(textBuf.toString());   //返回HTML的表示
    }

    //检测减号是否为负号
    public static boolean jianCeFu(String text, int index) {
        final int indexUp = index - FuHao.kuoHaoTou.length();                       //假设负号存在时，用“负号”的位置推出“括号头+负号”的位置。
        return (text.indexOf(FuHao.kuoHaoTou + FuHao.jian, indexUp) == indexUp || index == 0);    //判断“括号头+负号”的位置是否匹配
    }
}
