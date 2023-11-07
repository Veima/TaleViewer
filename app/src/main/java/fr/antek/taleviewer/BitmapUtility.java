package fr.antek.taleviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;

/**
 * Utility class for manipulating Bitmap images and performing various operations on them.
 */
public class BitmapUtility {

    /**
     * Resizes a bitmap to fit within a specified view while maintaining its aspect ratio.
     * @param bitmap The input Bitmap to be resized.
     * @param viewW The width of the target view.
     * @param viewH The height of the target view.
     * @return The resized Bitmap.
     */
    public static Bitmap correctSize(Bitmap bitmap, float viewW, float viewH) {
        float bmpW = bitmap.getWidth();
        float bmpH = bitmap.getHeight();
        float ratio = bmpW / bmpH;
        float factorX = bmpW / viewW;
        float factorY = bmpH / viewH;

        if ((viewW != 0) && (viewH != 0)) {
            if (factorX > factorY) {
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewW), Math.round(viewW / ratio), true);
            } else {
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(viewH * ratio), Math.round(viewH), true);
            }
        }
        return bitmap;
    }

    /**
     * Adjusts the aspect ratio of a bitmap to match the aspect ratio of a specified view for a better control when zooming.
     * @param bitmap The input Bitmap to be adjusted.
     * @param view The target view with the desired aspect ratio.
     * @return The adjusted Bitmap with the matching aspect ratio.
     */
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

    /**
     * Zooms in on a specific region of a bitmap and returns the zoomed portion.
     * @param bitmap The input Bitmap to be zoomed.
     * @param offsetX The horizontal offset of the zoomed region.
     * @param offsetY The vertical offset of the zoomed region.
     * @param currentScale The zoom level.
     * @param context The context where the zoom operation is performed.
     * @param imageView The view containing the zoomed image.
     * @return The zoomed portion of the Bitmap.
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, double offsetX, double offsetY, double currentScale, ImageActivity context, View imageView){
        int newLeft;
        int newTop;

        if (offsetX > 0){
            newLeft = Math.toIntExact(Math.round(offsetX));
        }else{
            newLeft = 0;
            context.setOffsetX(0);
        }
        if (offsetY > 0){
            newTop = Math.toIntExact(Math.round(offsetY));
        }else{
            newTop = 0;
            context.setOffsetY(0);
        }

        int newW = Math.toIntExact(Math.round(bitmap.getWidth() / currentScale));
        int newH = Math.toIntExact(Math.round(imageView.getHeight() / currentScale));

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

    /**
     * Splits a bitmap into two parts (left or right) with an optional overlap.
     * @param bitmapInput The input Bitmap to be split.
     * @param isRight Indicates whether to split the right or left portion.
     * @param overlap The percentage of overlap (0 for no overlap).
     * @return The split portion of the Bitmap.
     */
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

    /**
     * Adapts a bitmap to match the visible region of a ScrollView.
     * @param bitmapScroll The input Bitmap to be adapted.
     * @param view The ScrollView view containing the bitmap.
     * @param scrollOffset The vertical offset of the ScrollView.
     * @return The adapted Bitmap matching the ScrollView's visible region.
     */
    public static Bitmap adaptScrollView(Bitmap bitmapScroll, View view,double scrollOffset){
        int viewW = view.getWidth();
        int viewH = view.getHeight();

        if ((viewW != 0) && (viewH != 0)) {
            int offset = Math.toIntExact(Math.round(scrollOffset));

            Rect rect = new Rect(0, offset, viewW, viewH+offset);
            assert(rect.left < rect.right && rect.top < rect.bottom);
            Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
            new Canvas(resultBmp).drawBitmap(bitmapScroll, -rect.left, -rect.top, null);

            return resultBmp;
        }else{
            return bitmapScroll;
        }


    }

    /**
     * Adapts a bitmap to match a specified width while maintaining its aspect ratio.
     * @param bitmapInput The input Bitmap to be adapted.
     * @param view The view with the desired width.
     * @return The adapted Bitmap with the matching width.
     */
    public static Bitmap adaptWidth(Bitmap bitmapInput, View view){
        int viewW = view.getWidth();
        if (viewW != 0){
            float btmW = bitmapInput.getWidth();
            float btmH = bitmapInput.getHeight();

            return Bitmap.createScaledBitmap(bitmapInput, viewW, Math.round(viewW * btmH / btmW) , true);
        }else{
            return bitmapInput;
        }

    }

    /**
     * Merges a list of bitmaps (top, center, and bottom) vertically into a single bitmap.
     * @param bitmapUp List of bitmaps to be placed above the central bitmap.
     * @param thisBitmap The central bitmap.
     * @param bitmapDown List of bitmaps to be placed below the central bitmap.
     * @return The merged bitmap containing all specified bitmaps vertically.
     */
    public static Bitmap mergeBitmap(ArrayList<Bitmap> bitmapUp,Bitmap thisBitmap,ArrayList<Bitmap> bitmapDown){
        ArrayList<Bitmap> bitmapAll = new ArrayList<>();

        for (int i = bitmapUp.size() - 1; i >= 0; i--) {
            bitmapAll.add(bitmapUp.get(i));
        }
        bitmapAll.add(thisBitmap);

        bitmapAll.addAll(bitmapDown);
        return joinBitmapsVertically(bitmapAll);
    }

    /**
     * Joins a list of bitmaps vertically to create a single combined bitmap.
     * @param bitmaps List of bitmaps to be joined vertically.
     * @return The combined bitmap containing all specified bitmaps.
     */
    public static Bitmap joinBitmapsVertically(ArrayList<Bitmap> bitmaps) {
        int totalWidth = 0;
        int totalHeight = 0;

        for (Bitmap bitmap : bitmaps) {
            if (bitmap.getWidth() > totalWidth) {
                totalWidth = bitmap.getWidth();
            }
            totalHeight += bitmap.getHeight();
        }

        Bitmap resultBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(resultBitmap);
        int y = 0;

        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, 0, y, null);
            y += bitmap.getHeight();
        }

        return resultBitmap;
    }

}
