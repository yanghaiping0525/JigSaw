package com.yang.jigsaw.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/6.
 */
public class NetworkImageResource {

    public static List<String> getUrlFromIndex(int index) {
        switch (index) {
            case 0:
                return getCartoon();
            case 1:
                return getHatsuneMiku();
            case 2:
                return getNaruto();
            case 3:
                return getOnePiece();
            default:
                return null;
        }
    }

    public static List<String> getThumbUrlFromIndex(int index) {
        switch (index) {
            case 0:
                return getCartoonThumb();
            case 1:
                return getHatsuneMiku_Thumb();
            case 2:
                return getNarutoThumb();
            case 3:
                return getOnePieceThumb();
            default:
                return null;
        }
    }


    static class Cycle {
        public static final String HATSUNEMIKU = "http://cdn.duitang.com/uploads/item/201507/27/20150727173958_NGLhe.jpeg";
        public static final String NARUTO = "http://img2.100bt.com/upload/ttq/20140413/1397366030661_middle.jpg";
        public static final String ONE_PIECE = "http://www.qqc5.com/uploads/allimg/150311/1-1503111G401.jpg";
        public static final String[] CYCLE = {HATSUNEMIKU, NARUTO, ONE_PIECE};
    }

    public static List<String> getCycle() {
        List<String> cycleList = new ArrayList<>();
        for (int i = 0; i < Cycle.CYCLE.length; i++) {
            cycleList.add(Cycle.CYCLE[i]);
        }
        return cycleList;
    }

    public static final String[] CycleTitle = {"初音未来", "火影忍者", "海贼王"};

    public static List<String> getCycleTitle() {
        List<String> cycleTitleList = new ArrayList<>();
        for (int i = 0; i < CycleTitle.length; i++) {
            cycleTitleList.add(CycleTitle[i]);
        }
        return cycleTitleList;
    }

    static class Cartoon {
        public static final String CARTOON_1 = "http://b.zol-img.com.cn/sjbizhi/images/9/640x960/1454571888244.jpg";
        public static final String CARTOON_2 = "http://b.zol-img.com.cn/sjbizhi/images/9/640x960/145457189492.jpg";
        public static final String CARTOON_3 = "http://b.zol-img.com.cn/sjbizhi/images/9/768x1280/1454571854382.jpg";
        public static final String CARTOON_4 = "http://b.zol-img.com.cn/sjbizhi/images/9/720x1280/1450427324404.png";
        public static final String CARTOON_5 = "http://b.zol-img.com.cn/sjbizhi/images/9/720x1280/1450427338403.png";
        public static final String CARTOON_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/800x1280/144612219426.jpg";
        public static final String CARTOON_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446122153987.jpg";
        public static final String CARTOON_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/1436167308882.jpg";
        public static final String CARTOON_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/14361672902.jpg";
        public static final String CARTOON_10 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/1367134018216.jpg";
        public static final String CARTOON_11 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/1367134010730.jpg";
        public static final String CARTOON_12 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/1367133991721.jpg";
        public static final String CARTOON_13 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/136713393479.jpg";
        public static final String CARTOON_14 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/1367133910719.jpg";
        public static final String CARTOON_15 = "http://b.zol-img.com.cn/sjbizhi/images/4/768x1280/1367133863593.jpg";
        public static final String CARTOON_16 = "http://b.zol-img.com.cn/sjbizhi/images/4/640x960/1367042394831.jpg";
        public static final String CARTOON_17 = "http://b.zol-img.com.cn/sjbizhi/images/4/640x960/1366706145779.jpg";
        public static final String CARTOON_18 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x1136/136187228187.jpg";
        public static final String CARTOON_19 = "http://b.zol-img.com.cn/sjbizhi/images/3/768x1280/1361775566683.jpg";
        public static final String CARTOON_20 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1359601344645.jpg";
        public static final String CARTOON_21 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1358836278766.jpg";
        public static final String CARTOON_22 = "http://b.zol-img.com.cn/sjbizhi/images/3/540x960/1358835290334.jpg";
        public static final String CARTOON_23 = "http://b.zol-img.com.cn/sjbizhi/images/3/540x960/1358835292537.jpg";
        public static final String CARTOON_24 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1358247773905.jpg";
        public static final String CARTOON_25 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1357617610417.jpg";
        public static final String CARTOON_26 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1358216173731.jpg";
        public static final String CARTOON_27 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1357783387794.jpg";
        public static final String CARTOON_28 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1357783403622.jpg";
        public static final String CARTOON_29 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/1357783419720.jpg";
        public static final String CARTOON_30 = "http://b.zol-img.com.cn/sjbizhi/images/3/640x960/135778341277.jpg";
        public static final String[] CARTOON = {CARTOON_1, CARTOON_2, CARTOON_3, CARTOON_4, CARTOON_5, CARTOON_6, CARTOON_7, CARTOON_8, CARTOON_9, CARTOON_10, CARTOON_11, CARTOON_12, CARTOON_13, CARTOON_14, CARTOON_15, CARTOON_16, CARTOON_17, CARTOON_18, CARTOON_19, CARTOON_20, CARTOON_21, CARTOON_22, CARTOON_23, CARTOON_24, CARTOON_25, CARTOON_26, CARTOON_27, CARTOON_28, CARTOON_29, CARTOON_30};
    }

    public static List<String> getCartoon() {
        List<String> cartoonList = new ArrayList<>();
        for (int i = 0; i < Cartoon.CARTOON.length; i++) {
            cartoonList.add(Cartoon.CARTOON[i]);
        }
        return cartoonList;
    }

    static class CartoonThumb {
        public static final String CARTOON_1 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/1454571888244.jpg";
        public static final String CARTOON_2 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/145457189492.jpg";
        public static final String CARTOON_3 = "http://b.zol-img.com.cn/sjbizhi/images/9/480x800/1454571854382.jpg";
        public static final String CARTOON_4 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/1450427324404.png";
        public static final String CARTOON_5 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/1450427338403.png";
        public static final String CARTOON_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/144612219426.jpg";
        public static final String CARTOON_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1446122153987.jpg";
        public static final String CARTOON_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1436167308882.jpg";
        public static final String CARTOON_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/14361672902.jpg";
        public static final String CARTOON_10 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367134018216.jpg";
        public static final String CARTOON_11 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367134010730.jpg";
        public static final String CARTOON_12 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367133991721.jpg";
        public static final String CARTOON_13 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/136713393479.jpg";
        public static final String CARTOON_14 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367133910719.jpg";
        public static final String CARTOON_15 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367133863593.jpg";
        public static final String CARTOON_16 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1367042394831.jpg";
        public static final String CARTOON_17 = "http://b.zol-img.com.cn/sjbizhi/images/4/240x320/1366706145779.jpg";
        public static final String CARTOON_18 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/136187228187.jpg";
        public static final String CARTOON_19 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1361775566683.jpg";
        public static final String CARTOON_20 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1359601344645.jpg";
        public static final String CARTOON_21 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1358836278766.jpg";
        public static final String CARTOON_22 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1358835290334.jpg";
        public static final String CARTOON_23 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1358835292537.jpg";
        public static final String CARTOON_24 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1358247773905.jpg";
        public static final String CARTOON_25 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1357617610417.jpg";
        public static final String CARTOON_26 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1358216173731.jpg";
        public static final String CARTOON_27 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1357783387794.jpg";
        public static final String CARTOON_28 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1357783403622.jpg";
        public static final String CARTOON_29 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/1357783419720.jpg";
        public static final String CARTOON_30 = "http://b.zol-img.com.cn/sjbizhi/images/3/240x320/135778341277.jpg";
        public static final String[] CARTOONTHUMB = {CARTOON_1, CARTOON_2, CARTOON_3, CARTOON_4, CARTOON_5, CARTOON_6, CARTOON_7, CARTOON_8, CARTOON_9, CARTOON_10, CARTOON_11, CARTOON_12, CARTOON_13, CARTOON_14, CARTOON_15, CARTOON_16, CARTOON_17, CARTOON_18, CARTOON_19, CARTOON_20, CARTOON_21, CARTOON_22, CARTOON_23, CARTOON_24, CARTOON_25, CARTOON_26, CARTOON_27, CARTOON_28, CARTOON_29, CARTOON_30};
    }

    public static List<String> getCartoonThumb() {
        List<String> cartoonThumbList = new ArrayList<>();
        for (int i = 0; i < CartoonThumb.CARTOONTHUMB.length; i++) {
            cartoonThumbList.add(CartoonThumb.CARTOONTHUMB[i]);
        }
        return cartoonThumbList;
    }

    static class Naruto {
        public static final String Naruto_1 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/1446003348608.jpg";
        public static final String Naruto_2 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446003379824.jpg";
        public static final String Naruto_3 = "http://b.zol-img.com.cn/sjbizhi/images/8/800x1280/1446003376376.jpg";
        public static final String Naruto_4 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003371852.jpg";
        public static final String Naruto_5 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003369195.jpg";
        public static final String Naruto_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003367873.jpg";
        public static final String Naruto_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003366566.jpg";
        public static final String Naruto_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003350621.jpg";
        public static final String Naruto_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x1136/1446003347921.jpg";
        public static final String Naruto_10 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/1446003344752.jpg";
        public static final String Naruto_11 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614307970.jpg";
        public static final String Naruto_12 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614367819.jpg";
        public static final String Naruto_13 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614360625.jpg";
        public static final String Naruto_14 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614353139.jpg";
        public static final String Naruto_15 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614338938.jpg";
        public static final String Naruto_16 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614325278.jpg";
        public static final String Naruto_17 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614313367.jpg";
        public static final String Naruto_18 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1441614374855.jpg";
        public static final String[] NARUTO = {Naruto_1, Naruto_2, Naruto_3, Naruto_4, Naruto_5, Naruto_6, Naruto_7, Naruto_8, Naruto_9, Naruto_10, Naruto_11, Naruto_12, Naruto_13, Naruto_14, Naruto_15, Naruto_16, Naruto_17, Naruto_18};
    }

    public static List<String> getNaruto() {
        List<String> NarutoList = new ArrayList<>();
        for (int i = 0; i < Naruto.NARUTO.length; i++) {
            NarutoList.add(Naruto.NARUTO[i]);
        }
        return NarutoList;
    }

    static class Naruto_Thumb {
        public static final String Naruto_1 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003348608.jpg";
        public static final String Naruto_2 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446003379824.jpg";
        public static final String Naruto_3 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446003376376.jpg";
        public static final String Naruto_4 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003371852.jpg";
        public static final String Naruto_5 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003369195.jpg";
        public static final String Naruto_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003367873.jpg";
        public static final String Naruto_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003366566.jpg";
        public static final String Naruto_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003350621.jpg";
        public static final String Naruto_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003347921.jpg";
        public static final String Naruto_10 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446003344752.jpg";
        public static final String Naruto_11 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614307970.jpg";
        public static final String Naruto_12 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614367819.jpg";
        public static final String Naruto_13 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614360625.jpg";
        public static final String Naruto_14 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614353139.jpg";
        public static final String Naruto_15 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614338938.jpg";
        public static final String Naruto_16 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614325278.jpg";
        public static final String Naruto_17 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614313367.jpg";
        public static final String Naruto_18 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1441614374855.jpg";
        public static final String[] NARUTOTHUMB = {Naruto_1, Naruto_2, Naruto_3, Naruto_4, Naruto_5, Naruto_6, Naruto_7, Naruto_8, Naruto_9, Naruto_10, Naruto_11, Naruto_12, Naruto_13, Naruto_14, Naruto_15, Naruto_16, Naruto_17, Naruto_18};
    }

    public static List<String> getNarutoThumb() {
        List<String> NarutoThumbList = new ArrayList<>();
        for (int i = 0; i < Naruto_Thumb.NARUTOTHUMB.length; i++) {
            NarutoThumbList.add(Naruto_Thumb.NARUTOTHUMB[i]);
        }
        return NarutoThumbList;
    }

    static class ONEPIECE {
        public static final String ONEPIECE_1 = "http://b.zol-img.com.cn/sjbizhi/images/9/800x1280/1451028430993.jpg";
        public static final String ONEPIECE_2 = "http://b.zol-img.com.cn/sjbizhi/images/9/768x1280/1451028471757.jpg";
        public static final String ONEPIECE_3 = "http://b.zol-img.com.cn/sjbizhi/images/9/768x1280/1451028457553.jpg";
        public static final String ONEPIECE_4 = "http://b.zol-img.com.cn/sjbizhi/images/9/768x1280/1451028450290.jpg";
        public static final String ONEPIECE_5 = "http://b.zol-img.com.cn/sjbizhi/images/9/480x854/1451028441400.jpg";
        public static final String ONEPIECE_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/800x1280/1446196324974.jpg";
        public static final String ONEPIECE_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x854/1446196304346.jpg";
        public static final String ONEPIECE_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446196389927.jpg";
        public static final String ONEPIECE_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446196374394.jpg";
        public static final String ONEPIECE_10 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446196365897.jpg";
        public static final String ONEPIECE_11 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446196348904.jpg";
        public static final String ONEPIECE_12 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446196402352.jpg";
        public static final String[] ONEPIECE = {ONEPIECE_1, ONEPIECE_2, ONEPIECE_3, ONEPIECE_4, ONEPIECE_5, ONEPIECE_6, ONEPIECE_7, ONEPIECE_8, ONEPIECE_9, ONEPIECE_10, ONEPIECE_11, ONEPIECE_12};
    }

    public static List<String> getOnePiece() {
        List<String> OnePiecebList = new ArrayList<>();
        for (int i = 0; i < ONEPIECE.ONEPIECE.length; i++) {
            OnePiecebList.add(ONEPIECE.ONEPIECE[i]);
        }
        return OnePiecebList;
    }

    static class ONEPIECE_Thumb {
        public static final String ONEPIECE_1 = "http://b.zol-img.com.cn/sjbizhi/images/9/320x480/1451028430993.jpg";
        public static final String ONEPIECE_2 = "http://b.zol-img.com.cn/sjbizhi/images/9/320x480/1451028471757.jpg";
        public static final String ONEPIECE_3 = "http://b.zol-img.com.cn/sjbizhi/images/9/480x800/1451028457553.jpg";
        public static final String ONEPIECE_4 = "http://b.zol-img.com.cn/sjbizhi/images/9/480x800/1451028450290.jpg";
        public static final String ONEPIECE_5 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/1451028441400.jpg";
        public static final String ONEPIECE_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446196324974.jpg";
        public static final String ONEPIECE_7 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446196304346.jpg";
        public static final String ONEPIECE_8 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446196389927.jpg";
        public static final String ONEPIECE_9 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1446196374394.jpg";
        public static final String ONEPIECE_10 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446196365897.jpg";
        public static final String ONEPIECE_11 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446196348904.jpg";
        public static final String ONEPIECE_12 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446196402352.jpg";
        public static final String[] ONEPIECETHUMB = {ONEPIECE_1, ONEPIECE_2, ONEPIECE_3, ONEPIECE_4, ONEPIECE_5, ONEPIECE_6, ONEPIECE_7, ONEPIECE_8, ONEPIECE_9, ONEPIECE_10, ONEPIECE_11, ONEPIECE_12};
    }

    public static List<String> getOnePieceThumb() {
        List<String> OnePiecebThumbList = new ArrayList<>();
        for (int i = 0; i < ONEPIECE_Thumb.ONEPIECETHUMB.length; i++) {
            OnePiecebThumbList.add(ONEPIECE_Thumb.ONEPIECETHUMB[i]);
        }
        return OnePiecebThumbList;
    }

    static class HatsuneMiku {
        public static final String HATSUNEMIKU_1 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x854/1446012483955.jpg";
        public static final String HATSUNEMIKU_2 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/1446012457679.jpg";
        public static final String HATSUNEMIKU_3 = "http://b.zol-img.com.cn/sjbizhi/images/8/768x1280/1446012478298.jpg";
        public static final String HATSUNEMIKU_4 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x800/1446012476328.jpg";
        public static final String HATSUNEMIKU_5 = "http://b.zol-img.com.cn/sjbizhi/images/8/640x960/1446012470935.jpg";
        public static final String HATSUNEMIKU_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/480x854/1446012485179.jpg";
        public static final String HATSUNEMIKU_7 = "http://b.zol-img.com.cn/sjbizhi/images/9/640x960/1450837922433.jpg";
        public static final String[] HATSUNEMIKU = {HATSUNEMIKU_1, HATSUNEMIKU_2, HATSUNEMIKU_3, HATSUNEMIKU_4, HATSUNEMIKU_5, HATSUNEMIKU_6, HATSUNEMIKU_7};
    }

    static List<String> getHatsuneMiku() {
        List<String> hatSuneMikuList = new ArrayList<>();
        for (int i = 0; i < HatsuneMiku.HATSUNEMIKU.length; i++) {
            hatSuneMikuList.add(HatsuneMiku.HATSUNEMIKU[i]);
        }
        return hatSuneMikuList;
    }

    static class HatsuneMiku_Thumb {
        public static final String HATSUNEMIKU_1 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446012483955.jpg";
        public static final String HATSUNEMIKU_2 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446012457679.jpg";
        public static final String HATSUNEMIKU_3 = "http://b.zol-img.com.cn/sjbizhi/images/8/320x480/1446012478298.jpg";
        public static final String HATSUNEMIKU_4 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446012476328.jpg";
        public static final String HATSUNEMIKU_5 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446012470935.jpg";
        public static final String HATSUNEMIKU_6 = "http://b.zol-img.com.cn/sjbizhi/images/8/240x320/1446012485179.jpg";
        public static final String HATSUNEMIKU_7 = "http://b.zol-img.com.cn/sjbizhi/images/9/240x320/1450837922433.jpg";
        public static final String[] HATSUNEMIKUTHUMB = {HATSUNEMIKU_1, HATSUNEMIKU_2, HATSUNEMIKU_3, HATSUNEMIKU_4, HATSUNEMIKU_5, HATSUNEMIKU_6, HATSUNEMIKU_7};
    }

    static List<String> getHatsuneMiku_Thumb() {
        List<String> hatSuneMikuThumbList = new ArrayList<>();
        for (int i = 0; i < HatsuneMiku_Thumb.HATSUNEMIKUTHUMB.length; i++) {
            hatSuneMikuThumbList.add(HatsuneMiku_Thumb.HATSUNEMIKUTHUMB[i]);
        }
        return hatSuneMikuThumbList;
    }
}
