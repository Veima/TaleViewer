package fr.antek.mangaviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class BitmapUtility {
    public static Bitmap correctSize(Bitmap bitmap, View view) {
        float bmpW = bitmap.getWidth();
        float bmpH = bitmap.getHeight();
        float viewW = view.getWidth();
        float viewH = view.getHeight();
        float ratio = bmpW / bmpH;
        float factorX = bmpW / viewW;
        float factorY = bmpH / viewH;

        if ((viewW != 0) && (viewH != 0)) {
            if (factorX > factorY) {
                if (bmpW > viewW) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewW), Math.round(viewW / ratio), true);
                }
            } else {
                if (bmpH > viewH) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewH * ratio), Math.round(viewH), true);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap correctSize(Bitmap bitmap, float viewW, float viewH) {
        float bmpW = bitmap.getWidth();
        float bmpH = bitmap.getHeight();
        float ratio = bmpW / bmpH;
        float factorX = bmpW / viewW;
        float factorY = bmpH / viewH;

        if ((viewW != 0) && (viewH != 0)) {
            if (factorX > factorY) {
                if (bmpW > viewW) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewW), Math.round(viewW / ratio), true);
                }
            } else {
                if (bmpH > viewH) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewH * ratio), Math.round(viewH), true);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap correctRatio(Bitmap bitmap, View view){
        float viewW = view.getWidth();
        float viewH = view.getHeight();
        float bmpW = bitmap.getWidth();
        float bmpH = bitmap.getHeight();
        float factorX = bmpW / viewW;
        float factorY = bmpH / viewH;

        int newLeft;
        int newTop;
        int newRight;
        int newBottom;

        if (factorX < factorY){
            newLeft = Math.round((bmpW-factorY*viewW)/2);
            newTop = 1;
            newRight = Math.round(bmpW - newLeft);
            newBottom = Math.round(bmpH);
        }else{
            newLeft = 1;
            newTop = Math.round((bmpH-factorX*viewH)/2);
            newRight = Math.round(bmpW);
            newBottom = Math.round(bmpH - newTop);
        }

        Rect rect = new Rect(newLeft, newTop, newRight , newBottom );
        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(bitmap, -rect.left, -rect.top, null);

        return resultBmp;
    }


    public static Bitmap cropAndCheck(Bitmap bitmap, float offsetX, float offsetY, float currentScale, ImageActivity context){
        int newLeft;
        int newTop;

        if (offsetX > 0){
            newLeft = Math.round(offsetX);
        }else{
            newLeft = 0;
            context.setOffsetX(0);
        }
        if (offsetY > 0){
            newTop = Math.round(offsetY);
        }else{
            newTop = 0;
            context.setOffsetY(0);
        }

        int newW = Math.round(bitmap.getWidth()/currentScale);
        int newH = Math.round(bitmap.getHeight()/currentScale);

        int newRight;

        if (newLeft + newW < bitmap.getWidth()){
            newRight = newLeft + newW;
        }else{
            newRight = bitmap.getWidth();
            newLeft = newRight - newW;
            context.setOffsetX(newLeft);

        }

        int newBottom;

        if (newTop + newH < bitmap.getHeight()){
            newBottom = newTop + newH;
        }else{
            newBottom = bitmap.getHeight();
            newTop = newBottom - newH;
            context.setOffsetY(newTop);
        }

        Rect rect = new Rect(newLeft, newTop, newRight, newBottom);
        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(bitmap, -rect.left, -rect.top, null);

        return resultBmp;
    }

    public static Bitmap splitPage(Bitmap bitmapInput, boolean isRight, int overlap){
        Rect rect;
        if (isRight){
            float multi = (float) ((100.0-overlap)/200);
            rect = new Rect(Math.round(bitmapInput.getWidth()*multi), 0, bitmapInput.getWidth(), bitmapInput.getHeight());
        }else{
            float multi = (float) ((100.0+overlap)/200);
            rect = new Rect(0, 0, Math.round(bitmapInput.getWidth()*multi), bitmapInput.getHeight());
        }

        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(bitmapInput, -rect.left, -rect.top, null);

        return resultBmp;


    }
}
