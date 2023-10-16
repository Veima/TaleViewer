package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.util.Log;

public class PDFPage {
    private PDF parentPDF;
    private Bitmap bitmapRaw;
    private Boolean isWide;
    private PdfRenderer.Page pageRenderer;
    private final int pageId;
    private Boolean isOpen = false;


    public PDFPage(PDF parentPDF, int pageId) {
        this.parentPDF = parentPDF;
        this.pageId = pageId;
    }

    public void open(){
        pageRenderer = parentPDF.getPdfRenderer().openPage(pageId);

        bitmapRaw = Bitmap.createBitmap(pageRenderer.getWidth()*4, pageRenderer.getHeight()*4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapRaw);
        canvas.drawColor(Color.WHITE);

        pageRenderer.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
        isOpen = true;
        pageRenderer.close();
    }

    public void close(){
        bitmapRaw = null;
        isOpen = false;
    }

    public Bitmap getBitmapRaw() {
        if (!isOpen){
            open();
        }
        return bitmapRaw;
    }

    public Boolean getWide() {
        if (!isOpen){
            open();
        }
        return isWide;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public int getPageId() {
        return pageId;
    }
}
