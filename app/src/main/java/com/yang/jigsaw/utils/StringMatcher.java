package com.yang.jigsaw.utils;

import java.io.UnsupportedEncodingException;

public class StringMatcher {
    static final int GB_SP_DIFF = 160;
    // 存放国标一级汉字不同读音的起始区位码
    static final int[] secPosValueList = {1601, 1637, 1833, 2078, 2274, 2302,
            2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
            4086, 4390, 4558, 4684, 4925, 5249, 5600};
    // 存放国标一级汉字不同读音的起始区位码对应读音
    static final char[] firstLetter = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'W', 'X',
            'Y', 'Z'};


    public static boolean match(String value, String keyword) {
        //非字母部分
        if (value.charAt(0) >> 7 != 0) {
            return String.valueOf(getFirstLetter(value.charAt(0))).contains(
                    keyword);
        }
        //小写字母部分
        else if (value.charAt(0) >= 'a' && value.charAt(0) <= 'z') {
            value = String.valueOf(Character.toUpperCase(value.charAt(0)));
        }
        //非字母部分(符号或数字部分)
       /* else if (value.charAt(0) < 'A' || value.charAt(0) > 'Z') {
            return false;
        }*/
        return value.contains(keyword);
    }

    public static String getSpells(String characters) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < characters.length(); i++) {

            char ch = characters.charAt(i);
            if ((ch >> 7) == 0) {
                // 判断是否为汉字，如果右移7位为0就不是汉字，否则是汉字
            } else {
                char spell = getFirstLetter(ch);
                buffer.append(String.valueOf(spell));
            }
        }
        return buffer.toString();
    }

    public static Character getFirstLetter(char ch) {

        byte[] uniCode;
        try {
            uniCode = String.valueOf(ch).getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        // 非汉字部分(byte占8字节,能表示0~255的范围,英文字母只用到前七位)
        if (uniCode[0] < 128 && uniCode[0] > 0) {
            //大写字母部分
            if (ch >= 'A' && ch <= 'Z') {
                return ch;
            }
            //小写字母部分(统一转化成大写字母)
            else if (ch >= 'a' && ch <= 'z') {
                return Character.toUpperCase(ch);
            }
            //非字母部分返回'#'符号(包括数字和一些特殊符号)
            else {
                return '#';
            }
        }
        //汉字部分
        else {
            return convert(uniCode);
        }
    }

    /**
     * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
     * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
     * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
     */
    static char convert(byte[] bytes) {
        char result = '#';
        int i;
        //获得高低字节各自的区位码
        for (i = 0; i < bytes.length; i++) {
            bytes[i] -= GB_SP_DIFF;
        }
        //计算区位码
        int secPosValue = bytes[0] * 100 + bytes[1];
        //根据区位码的值判断位于哪个声母的位置区域,并获得对应声母返回
        for (i = 0; i < firstLetter.length; i++) {
            if (secPosValue >= secPosValueList[i]
                    && secPosValue < secPosValueList[i + 1]) {
                result = firstLetter[i];
                break;
            }
        }
        return result;
    }
}
