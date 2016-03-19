package com.yang.jigsaw.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.yang.jigsaw.bean.ImagePiece;

public class ImageSplitterUtil {
    public static List<ImagePiece> splitImage(Bitmap bitmap, int piecesLineCount) {
        List<ImagePiece> imagePieces = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bm;
        int pieceWidth = Math.min(width, height) / piecesLineCount;
        int shorter = Math.min(width, height);
        int longer = Math.max(width, height);

        if (width > height) {
            bm = Bitmap.createBitmap(bitmap, (longer - shorter) / 2, 0, shorter, shorter);
        } else {
            bm = Bitmap.createBitmap(bitmap, 0, (longer - shorter) / 2, shorter, shorter);
        }
        if(bm == null){
            throw  new RuntimeException("拼图创建失败");
        }
        for (int i = 0; i < piecesLineCount; i++) {
            for (int j = 0; j < piecesLineCount; j++) {
                ImagePiece piece = new ImagePiece();
                piece.setIndex(j + i * piecesLineCount);
                int x = j * pieceWidth;
                int y = i * pieceWidth;
                piece.setBitmap(Bitmap.createBitmap(bm, x, y, pieceWidth, pieceWidth));
                imagePieces.add(piece);
            }
        }
        return imagePieces;
    }
}
