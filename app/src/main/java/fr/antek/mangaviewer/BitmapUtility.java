package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class BitmapUtility {
    public static Bitmap adaptBitmap2View(Bitmap bitmap, View view){
        float bmpW = bitmap.getWidth();
        float bmpH = bitmap.getHeight();
        float viewW = view.getWidth();
        float viewH = view.getHeight();
        float ratio = bmpW/bmpH;
        float factorX = bmpW / viewW;
        float factorY = bmpH / viewH;

        if ((viewW !=0) && (viewH !=0)){
            if (factorX > factorY) {
                if (bmpW > viewW) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewW), Math.round(viewW/ratio), true);
                    bmpW = bitmap.getWidth();
                    bmpH = bitmap.getHeight();
                    factorX = bmpW / viewW;
                    factorY = bmpH / viewH;
                }
            }else{
                if (bmpH > viewH) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewH*ratio), Math.round(viewH), true);
                    bmpW = bitmap.getWidth();
                    bmpH = bitmap.getHeight();
                    factorX = bmpW / viewW;
                    factorY = bmpH / viewH;
                }
            }
        }


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

    public static Bitmap cropBitmap(Bitmap bitmap, float currentScale, float newScale, float offsetX, float offsetY, float currentFocusX, float newFocusX, float currentFocusY, float newFocusY){

        currentScale = Math.max(1.0f, Math.min(currentScale*newScale, 10.0f));

        offsetX = (offsetX-newFocusX)/newScale+newFocusX;
        offsetY = (offsetY-newFocusY)/newScale+newFocusY;

        offsetX = offsetX + (currentFocusX - newFocusX);
        offsetY = offsetY + (currentFocusY - newFocusY);

        currentFocusX = newFocusX;
        currentFocusY = newFocusY;

        return cropAndCheck(bitmap, offsetX, offsetY, currentScale);
    }

    public static Bitmap moveBitmap(Bitmap bitmap, float offsetX, float offsetY, float slideX, float slideY, float currentScale){

        offsetX = offsetX - slideX;
        offsetY = offsetY - slideY;
        return cropAndCheck(bitmap, offsetX, offsetY, currentScale);
    }

    public static Bitmap cropAndCheck(Bitmap bitmap, float offsetX, float offsetY, float currentScale){
        int newLeft;
        int newTop;

        if (offsetX > 0){
            newLeft = Math.round(offsetX);
        }else{
            newLeft = 0;
        }
        if (offsetY > 0){
            newTop = Math.round(offsetY);
        }else{
            newTop = 0;
        }

        int newW = Math.round(bitmap.getWidth()/currentScale);
        int newH = Math.round(bitmap.getHeight()/currentScale);

        int newRight;

        if (newLeft + newW < bitmap.getWidth()){
            newRight = newLeft + newW;
        }else{
            newRight = bitmap.getWidth();
            newLeft = newRight - newW;
        }

        Rect rect = new Rect(newLeft, newTop, newRight, newTop + newH);
        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(bitmap, -rect.left, -rect.top, null);

        return resultBmp;
    }
}
