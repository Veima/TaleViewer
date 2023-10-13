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
    private int pageNumber;
    private Boolean isOpen = false;


    public PDFPage(PDF parentPDF, int pageNumber) {
        this.parentPDF = parentPDF;
        this.pageNumber = pageNumber;
        open();
    }

    public void open(){
        pageRenderer = parentPDF.getPdfRenderer().openPage(pageNumber);

        bitmapRaw = Bitmap.createBitmap(pageRenderer.getWidth()*4, pageRenderer.getHeight()*4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapRaw);
        canvas.drawColor(Color.WHITE);

        pageRenderer.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
        isOpen = true;
        pageRenderer.close();
    }

    public void close(){
        Log.d("MOI", "close" + pageNumber);
        bitmapRaw = null;
        isOpen = false;
    }

    public Bitmap getBitmapRaw() {
        return bitmapRaw;
    }

    public Boolean getWide() {
        return isWide;
    }

    public Boolean getOpen() {
        return isOpen;
    }
}
