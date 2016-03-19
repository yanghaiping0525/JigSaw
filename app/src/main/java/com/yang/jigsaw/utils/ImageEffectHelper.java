package com.yang.jigsaw.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageEffectHelper {
    /*
     * hue色相 saturation饱和度 lightness明度
	 */
    public static Bitmap handleImageEffect(Bitmap bitmap, float hue,
                                           float saturation, float lightness) {
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        // 在画布操作，不是在原图操作
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ColorMatrix hueMatrix = new ColorMatrix();
        // 0红1绿2蓝
        hueMatrix.setRotate(0, hue);
        hueMatrix.setRotate(1, hue);
        hueMatrix.setRotate(2, hue);

        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        ColorMatrix lightnessMatrix = new ColorMatrix();
        lightnessMatrix.setScale(lightness, lightness, lightness, 1);

        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(hueMatrix);
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(lightnessMatrix);
        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bm;
    }

    public static Bitmap antiColorEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int colorElement;
        int r, g, b, a;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[width * height];
        int[] newPx = new int[width * height];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++) {
            colorElement = oldPx[i];
            r = Color.red(colorElement);
            g = Color.green(colorElement);
            b = Color.blue(colorElement);
            a = Color.alpha(colorElement);
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            if (g > 255) {
                g = 255;
            } else if (g < 0) {
                g = 0;
            }
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            newPx[i] = Color.argb(a, r, g, b);
        }
        bm.setPixels(newPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap oldPhotoEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int colorElement = 0;
        int r, g, b, a, r1, b1, g1;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[width * height];
        int[] newPx = new int[width * height];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++) {
            colorElement = oldPx[i];
            r = Color.red(colorElement);
            g = Color.green(colorElement);
            b = Color.blue(colorElement);
            a = Color.alpha(colorElement);
            r1 = (int) (0.393 * r + 0.769 * g + 0.189 * b);
            g1 = (int) (0.349 * r + 0.686 * g + 0.168 * b);
            b1 = (int) (0.272 * r + 0.534 * g + 0.131 * b);
            if (r1 > 255) {
                r1 = 255;
            }
            if (g1 > 255) {
                g1 = 255;
            }
            if (b1 > 255) {
                b1 = 255;
            }
            newPx[i] = Color.argb(a, r1, g1, b1);
        }
        bm.setPixels(newPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap reliefEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int colorElement, colorBefore;
        int r, g, b, a, r1, b1, g1;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[width * height];
        int[] newPx = new int[width * height];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int i = 1; i < width * height; i++) {
            colorBefore = oldPx[i - 1];
            r = Color.red(colorBefore);
            g = Color.green(colorBefore);
            b = Color.blue(colorBefore);
            a = Color.alpha(colorBefore);
            colorElement = oldPx[i];
            r1 = Color.red(colorElement);
            g1 = Color.green(colorElement);
            b1 = Color.blue(colorElement);
            r = (r - r1 + 127);
            g = (g - g1 + 127);
            b = (b - b1 + 127);
            if (r > 255) {
                r = 255;
            }
            if (g > 255) {
                g = 255;
            }
            if (b > 255) {
                b = 255;
            }
            newPx[i] = Color.argb(a, r, g, b);
        }
        bm.setPixels(newPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap oilPaintingEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tatalPixels = width * height;
        int colorOrigin, colorCompare;
        int r, g, b, a, rOrigin, bOrigin, gOrigin;
        int accuracy = 30;
        int rGrad, gGrad, bGrad;
        float border = 0.1f;
        float deep = 0.8f;
        float shallow = 1.1f;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[tatalPixels];
        int[] newPx = new int[tatalPixels];
        int[] finalPx = new int[tatalPixels];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                a = Color.alpha(colorOrigin);
                r = (int) (rOrigin * shallow);
                g = (int) (gOrigin * shallow);
                b = (int) (bOrigin * shallow);

                if (r >= 0 && r < 25) {
                    r = 24;
                } else if (r >= 25 && r < 50) {
                    r = 49;
                } else if (r >= 50 && r < 75) {
                    r = 74;
                } else if (r >= 75 && r < 100) {
                    r = 99;
                } else if (r >= 100 && r < 125) {
                    r = 124;
                } else if (r >= 125 && r < 150) {
                    r = 149;
                } else if (r >= 150 && r < 175) {
                    r = 174;
                } else if (r >= 175 && r < 200) {
                    r = 199;
                } else if (r >= 200 && r < 225) {
                    r = 224;
                } else if (r >= 225 && r <= 255) {
                    r = 255;
                }

                if (g >= 0 && g < 25) {
                    g = 24;
                } else if (g >= 25 && g < 50) {
                    g = 49;
                } else if (g >= 50 && g < 75) {
                    g = 74;
                } else if (g >= 75 && g < 100) {
                    g = 99;
                } else if (g >= 100 && g < 125) {
                    g = 124;
                } else if (g >= 125 && g < 150) {
                    g = 149;
                } else if (g >= 150 && g < 175) {
                    g = 174;
                } else if (g >= 175 && g < 200) {
                    g = 199;
                } else if (g >= 200 && g < 225) {
                    g = 224;
                } else if (g >= 225 && g <= 255) {
                    g = 255;
                }

                if (b >= 0 && b < 25) {
                    b = 24;
                } else if (b >= 25 && b < 50) {
                    b = 49;
                } else if (b >= 50 && b < 75) {
                    b = 74;
                } else if (b >= 75 && b < 100) {
                    b = 99;
                } else if (b >= 100 && b < 125) {
                    b = 124;
                } else if (b >= 125 && b < 150) {
                    b = 149;
                } else if (b >= 150 && b < 175) {
                    b = 174;
                } else if (b >= 175 && b < 200) {
                    b = 199;
                } else if (b >= 200 && b < 225) {
                    b = 224;
                } else if (b >= 225 && b <= 255) {
                    b = 255;
                }

                if (r > 255)
                    r = 255;
                if (g > 255)
                    g = 255;
                if (b > 255)
                    b = 255;
                newPx[y * width + x] = Color.argb(a, r, g, b);
            }
        }


        for (int i = 0; i < tatalPixels; i++) {
            finalPx[i] = newPx[i];
        }
        bm.setPixels(finalPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap colorSketchEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tatalPixels = width * height;
        int colorOrigin, colorCompare;
        int r, g, b, a, rOrigin, bOrigin, gOrigin;
        int accuracy = 20;
        int rGrad, gGrad, bGrad;
        float border = 1.0f;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[tatalPixels];
        int[] newPx_horz_right = new int[tatalPixels];
        int[] newPx_vert_bottom = new int[tatalPixels];
        int[] finalPx = new int[tatalPixels];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                colorCompare = oldPx[y * width + x - 1];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;
                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = (int) (rOrigin * border);
                    g = (int) (gOrigin * border);
                    b = (int) (bOrigin * border);
                } else {
                    if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                        r = rOrigin;
                        g = gOrigin;
                        b = bOrigin;
                    } else {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                }
                newPx_horz_right[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colorCompare = oldPx[y * width + x - width];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;

                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = (int) (rOrigin * border);
                    g = (int) (gOrigin * border);
                    b = (int) (bOrigin * border);
                } else {
                    if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                        r = rOrigin;
                        g = gOrigin;
                        b = bOrigin;
                    } else {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                }

                newPx_vert_bottom[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int i = 0; i < tatalPixels; i++) {
            finalPx[i] = Math.min(newPx_horz_right[i], newPx_vert_bottom[i]);
        }
        bm.setPixels(finalPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap blackAndWhiteSketchEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tatalPixels = width * height;
        int colorOrigin, colorCompare;
        int r, g, b, a, rOrigin, bOrigin, gOrigin;
        int accuracy = 20;
        int rGrad, gGrad, bGrad;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[tatalPixels];
        int[] newPx_horz_right = new int[tatalPixels];
        int[] newPx_vert_bottom = new int[tatalPixels];
        int[] finalPx = new int[tatalPixels];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                colorCompare = oldPx[y * width + x - 1];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;
                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                        r = 0;
                        g = 0;
                        b = 0;
                    } else {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                }
                newPx_horz_right[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colorCompare = oldPx[y * width + x - width];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;

                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                        r = 0;
                        g = 0;
                        b = 0;
                    } else {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                }

                newPx_vert_bottom[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int i = 0; i < tatalPixels; i++) {
            finalPx[i] = Math.min(newPx_horz_right[i], newPx_vert_bottom[i]);
        }
        bm.setPixels(finalPx, 0, width, 0, 0, width, height);
        newPx_horz_right = null;
        newPx_vert_bottom = null;
        return bm;
    }

    public static Bitmap comicEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tatalPixels = width * height;
        int colorOrigin, colorCompare;
        int r, g, b, a, rOrigin, bOrigin, gOrigin;
        int accuracy = 15;
        int rGrad, gGrad, bGrad;
        float lightAndGray = 1.8f;
        float divide = 1.8f;
        float bright = 1.8f;
        float deep = 1.6f;
        float border = 1.20f;
        int exposureValue = 255;
        int exposureValueOfBorder = 10;
        boolean style = true;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[tatalPixels];
        int[] newPx_horz_right = new int[tatalPixels];
        int[] newPx_vert_bottom = new int[tatalPixels];
        int[] finalPx = new int[tatalPixels];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                colorCompare = oldPx[y * width + x - 1];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;
                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = (int) (rOrigin * border);
                    g = (int) (gOrigin * border);
                    b = (int) (bOrigin * border);
                    if (r > exposureValueOfBorder)
                        r = exposureValueOfBorder;
                    if (g > exposureValueOfBorder)
                        g = exposureValueOfBorder;
                    if (b > exposureValueOfBorder)
                        b = exposureValueOfBorder;

                } else {

                    // 浅色区域(各种浅颜色和浅灰色的过渡边界至白色的过渡区域)(明亮)
                    if ((int) (rOrigin * divide) > exposureValue
                            && (int) (gOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {

                        r = (int) (rOrigin * lightAndGray);
                        g = (int) (gOrigin * lightAndGray);
                        b = (int) (bOrigin * lightAndGray);

                    }
                    // 浅灰黄渐变至鲜艳黄色的区域(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue
                            && (int) (gOrigin * divide) > exposureValue) {

                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);

                    }
                    // 浅灰紫渐变至鲜艳紫色的区域(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅灰蓝渐变至鲜艳蓝色的区域(明亮)
                    else if ((int) (gOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅灰黄至红色的渐变区域(浅黄-橙色-红色)(明亮)
                    // 浅灰紫至红色的渐变区域(浅紫-粉色-红色)(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue) {

                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);

                    }
                    // 浅蓝至艳绿色的渐变区域(浅绿-蓝绿-蓝色)(明亮)
                    else if ((int) (gOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅紫至蓝色的渐变区域(浅紫-紫罗兰-蓝色)(明亮)
                    else if ((int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 深色区域(各种浅颜色和浅灰色的过渡边界至黑色的过渡区域)(暗沉)
                    else {
                        // 深色区域特殊处理
                        if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                            r = (int) (deep * rOrigin);
                            g = (int) (deep * gOrigin);
                            b = (int) (deep * bOrigin);

                        } else {
                            r = (int) (divide * rOrigin);
                            g = (int) (divide * gOrigin);
                            b = (int) (divide * bOrigin);
                        }

                    }

                    if (r > exposureValue)
                        r = exposureValue;
                    if (g > exposureValue)
                        g = exposureValue;
                    if (b > exposureValue)
                        b = exposureValue;
                }
                newPx_horz_right[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colorCompare = oldPx[y * width + x - width];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;

                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = (int) (rOrigin * border);
                    g = (int) (gOrigin * border);
                    b = (int) (bOrigin * border);
                    if (r > exposureValueOfBorder)
                        r = exposureValueOfBorder;
                    if (g > exposureValueOfBorder)
                        g = exposureValueOfBorder;
                    if (b > exposureValueOfBorder)
                        b = exposureValueOfBorder;

                } else {

                    // 浅色区域(各种浅颜色和浅灰色的过渡边界至白色的过渡区域)(明亮)
                    if ((int) (rOrigin * divide) > exposureValue
                            && (int) (gOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {

                        r = (int) (rOrigin * lightAndGray);
                        g = (int) (gOrigin * lightAndGray);
                        b = (int) (bOrigin * lightAndGray);

                    }
                    // 浅灰黄渐变至鲜艳黄色的区域(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue
                            && (int) (gOrigin * divide) > exposureValue) {

                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅灰紫渐变至鲜艳紫色的区域(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅灰蓝渐变至鲜艳蓝色的区域(明亮)
                    else if ((int) (gOrigin * divide) > exposureValue
                            && (int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅灰黄至红色的渐变区域(浅黄-橙色-红色)(明亮)
                    // 浅灰紫至红色的渐变区域(浅紫-粉色-红色)(明亮)
                    else if ((int) (rOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅蓝至艳绿色的渐变区域(浅绿-蓝绿-蓝色)(明亮)
                    else if ((int) (gOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 浅紫至蓝色的渐变区域(浅紫-紫罗兰-蓝色)(明亮)
                    else if ((int) (bOrigin * divide) > exposureValue) {
                        r = (int) (bright * rOrigin);
                        g = (int) (bright * gOrigin);
                        b = (int) (bright * bOrigin);
                    }
                    // 深色区域(各种浅颜色和浅灰色的过渡边界至黑色的过渡区域)(暗沉)
                    else {
                        // 深色区域特殊处理
                        if (rOrigin < 68 && gOrigin < 68 && bOrigin < 68) {
                            r = (int) (deep * rOrigin);
                            g = (int) (deep * gOrigin);
                            b = (int) (deep * bOrigin);

                        } else {
                            r = (int) (divide * rOrigin);
                            g = (int) (divide * gOrigin);
                            b = (int) (divide * bOrigin);
                        }

                    }
                    if (r > exposureValue)
                        r = exposureValue;
                    if (g > exposureValue)
                        g = exposureValue;
                    if (b > exposureValue)
                        b = exposureValue;
                }

                newPx_vert_bottom[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int i = 0; i < tatalPixels; i++) {
                finalPx[i] = Math
                        .min(newPx_horz_right[i], newPx_vert_bottom[i]);
//            finalPx[i] = newPx_horz_right[i];
        }
        bm.setPixels(finalPx, 0, width, 0, 0, width, height);
        return bm;
    }

    public static Bitmap sketchEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int pixel = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        // The horizontal of Sobel matrix
        int horiz_matrix[][] = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        // The vertical of Sobel matrix
        int vert_matrix[][] = {{-1, 0, -1}, {-2, 0, 2}, {1, 0, 1}};
        // The ordered dither of the matrix
        int order_matrix[][] = {{28, 255, 57}, {142, 113, 227},
                {170, 198, 85}};
        float gray = 0.0f;
        int sobel = 0;
        int matrix_offset = 1;// 3 x 3 半径为1 , 5 x 5半径为2
        int matrix_size = 3;
        int horz_value = 0;
        int vert_value = 0;
        int sketchValue = 0;
        // For a horizontal index of Sobel matrix
        int xIndex = 0;
        // For a vertical index of Sobel matrix
        int yIndex = 0;
        // Sobel filter of image processing
        // The height of the image
        for (int y = matrix_offset; y < height - 1; y++) {
            // The width of the image
            for (int x = matrix_offset; x < width - 1; x++) {
                yIndex = 0;
                horz_value = 0;
                vert_value = 0;
                // The height of Sobel matrix
                for (int y_sobel = y - matrix_offset; y_sobel <= y
                        + matrix_offset; y_sobel++) {
                    xIndex = 0;
                    // The width of Sobel matrix
                    for (int x_sobel = x - matrix_offset; x_sobel <= x
                            + matrix_offset; x_sobel++) {
                        // Get value of pixel
                        pixel = bitmap.getPixel(x_sobel, y_sobel);
                        // To get value of the red channel.
                        r = Color.red(pixel);
                        // To get value of the green channel.
                        g = Color.green(pixel);
                        // To get value of the blue channel.
                        b = Color.blue(pixel);
                        // Compute value of gray.
                        gray = (0.299f * r) + (0.587f * g) + (0.114f * b);
                        // To compute Sobel matrix, we transform float to
                        // integer.
                        sobel = (int) gray;
                        // Convolution computing horizontal of Sobel matrix.
                        horz_value += sobel * horiz_matrix[xIndex][yIndex];
                        // Convolution computing vertical of Sobel matrix.
                        vert_value += sobel * vert_matrix[xIndex][yIndex];
                        xIndex++;
                    }
                    yIndex++;
                    xIndex = 0;
                }
                // Choice maximum value.
                sketchValue = Math.max(horz_value, vert_value);
                // The twenty-four is a magic number.
                if (sketchValue > 24) {
                    // Set the pixel is black.
                    bm.setPixel(x, y, Color.rgb(0, 0, 0));
                } else {
                    // Set the pixel is white.
                    bm.setPixel(x, y, Color.rgb(255, 255, 255));
                }
            }
        }
        // The height of the image. But we are stepping to three once.
        for (int y = 0; y < height - 3; y += 3) {
            // The width of the image. But we are stepping to three once.
            for (int x = 0; x < width - 3; x += 3) {
                yIndex = 0;
                // The height of the matrix.
                for (int y_sobel = y; y_sobel < y + matrix_size; y_sobel++) {
                    xIndex = 0;
                    // The width of the matrix.
                    for (int x_sobel = x; x_sobel < x + matrix_size; x_sobel++) {
                        // Get value of pixel
                        pixel = bitmap.getPixel(x_sobel, y_sobel);
                        // To get value of the red channel.
                        r = Color.red(pixel);
                        // To get value of the green channel.
                        g = Color.green(pixel);
                        // To get value of the blue channel.
                        b = Color.blue(pixel);
                        // Compute value of gray.
                        gray = (0.299f * r) + (0.587f * g) + (0.114f * b);
                        // To compute Sobel matrix, we transform float to
                        // integer.
                        sobel = (int) gray;
                        // If the gray depth more than the matrix, it should be
                        // white.
                        if (sobel >= order_matrix[xIndex][yIndex]) {
                            bm.setPixel(x, y, Color.rgb(255, 255, 255));
                        }
                        // Otherwise, it should be black.
                        else {
                            bm.setPixel(x, y, Color.rgb(0, 0, 0));
                        }
                        xIndex++;
                    }
                    yIndex++;
                    xIndex = 0;
                }
            }
        }
        return bm;
    }

    public static Bitmap frescoEffect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tatalPixels = width * height;
        int colorOrigin, colorCompare;
        int r, g, b, a, rOrigin, bOrigin, gOrigin;
        int accuracy = 30;
        int rGrad = 0, gGrad = 0, bGrad = 0;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] oldPx = new int[tatalPixels];
        int[] newPx_horz_right = new int[tatalPixels];
        int[] newPx_vert_bottom = new int[tatalPixels];
        int[] finalPx = new int[tatalPixels];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                colorCompare = oldPx[y * width + x - 1];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;
                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = 255;
                    g = 153;
                    b = 51;
                    newPx_horz_right[y * width + x] = Color.argb(a, r, g, b);
                    newPx_horz_right[y * width + x - 1] = Color.argb(a, r, g, b);
                } else {
                    r = 51;
                    g = 25;
                    b = 0;
                }
                newPx_horz_right[y * width + x] = Color.argb(a, r, g, b);
            }
        }
        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colorCompare = oldPx[y * width + x - width];
                r = Color.red(colorCompare);
                g = Color.green(colorCompare);
                b = Color.blue(colorCompare);
                a = Color.alpha(colorCompare);
                colorOrigin = oldPx[y * width + x];
                rOrigin = Color.red(colorOrigin);
                gOrigin = Color.green(colorOrigin);
                bOrigin = Color.blue(colorOrigin);
                rGrad = r - rOrigin;
                gGrad = g - gOrigin;
                bGrad = b - bOrigin;

                if ((Math.abs(rGrad) >= accuracy)
                        || (Math.abs(gGrad) >= accuracy)
                        || (Math.abs(bGrad) >= accuracy)) {
                    r = 255;
                    g = 153;
                    b = 51;
                    newPx_vert_bottom[y * width + x] = Color.argb(a, r, g, b);
                    newPx_vert_bottom[y * width + x - width] = Color.argb(a, r, g, b);
                    continue;
                } else {
                    r = 51;
                    g = 25;
                    b = 0;
                }

                newPx_vert_bottom[y * width + x] = Color.argb(a, r, g, b);
            }
        }

        for (int i = 0; i < tatalPixels; i++) {
            finalPx[i] = Math.max(newPx_horz_right[i], newPx_vert_bottom[i]);
        }
        bm.setPixels(finalPx, 0, width, 0, 0, width, height);
        return bm;
    }

    //灰度效果（黑白照片）
    public static Bitmap grayEffect(Bitmap bmp) {
        int height = bmp.getHeight();
        int width = bmp.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmp, 0, 0, paint);
        return bmpGrayscale;
    }

    //冰冻特效
    public static Bitmap ice(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int dst[] = new int[width * height];
        bmp.getPixels(dst, 0, width, 0, 0, width, height);
        int R, G, B, pixel;
        int pos, pixColor;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pos = y * width + x;
                pixColor = dst[pos]; // 获取图片当前点的像素值
                R = Color.red(pixColor); // 获取RGB三原色
                G = Color.green(pixColor);
                B = Color.blue(pixColor);
                pixel = R - G - B;
                pixel = pixel * 3 / 2;

                if (pixel < 0)
                    pixel = -pixel;
                if (pixel > 255)
                    pixel = 255;

                R = pixel; // 计算后重置R值，以下类同
                pixel = G - B - R;
                pixel = pixel * 3 / 2;

                if (pixel < 0)
                    pixel = -pixel;
                if (pixel > 255)
                    pixel = 255;

                G = pixel;
                pixel = B - R - G;
                pixel = pixel * 3 / 2;

                if (pixel < 0)
                    pixel = -pixel;
                if (pixel > 255)
                    pixel = 255;
                B = pixel;
                dst[pos] = Color.rgb(R, G, B); // 重置当前点的像素值
            } // x
        } // y
        bitmap.setPixels(dst, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
