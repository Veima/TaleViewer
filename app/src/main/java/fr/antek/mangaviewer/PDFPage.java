package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;

public class PDFPage {
    private PDF parentPDF;
    private Bitmap bitmapRaw;
    private Boolean isWide;
    private PdfRenderer.Page pageRenderer;
    private int pageNumber;


    public PDFPage(PDF parentPDF, int pageNumber) {
        this.parentPDF = parentPDF;
        this.pageNumber = pageNumber;
        open();
    }

    public void open(){
        pageRenderer = parentPDF.getPdfRenderer().openPage(pageNumber-1);

        bitmapRaw = Bitmap.createBitmap(pageRenderer.getWidth()*4, pageRenderer.getHeight()*4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapRaw);
        canvas.drawColor(Color.WHITE);

        pageRenderer.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
    }

    public void close(){
        bitmapRaw = null;
        pageRenderer.close();
    }

    public Bitmap getBitmapRaw() {
        return bitmapRaw;
    }

    public Boolean getWide() {
        return isWide;
    }
}
