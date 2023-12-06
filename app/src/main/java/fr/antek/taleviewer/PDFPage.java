package fr.antek.taleviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;

/**
 * Represents a page within a PDF document, providing functionality to open, close, and access the page's raw bitmap content.
 */
public class PDFPage {
    private final PDF parentPDF; // The parent PDF document containing this page.
    private Bitmap bitmapRaw; // The raw bitmap of the page's content.
    private Boolean isWide; // Flag indicating whether the page is wide.
    private final int pageId; // The page ID within the PDF document.
    private Boolean isOpen = false; // Flag indicating whether the page is open.

    /**
     * Constructs a PDFPage object.
     * @param parentPDF The parent PDF document containing this page.
     * @param pageId The page ID within the PDF document.
     */
    public PDFPage(PDF parentPDF, int pageId) {
        this.parentPDF = parentPDF;
        this.pageId = pageId;
    }

    /**
     * Opens and renders the PDF page, creating a raw bitmap representation of the page's content.
     */
    public void open(){
        if (parentPDF.getPdfRenderer() != null){
            PdfRenderer.Page pageRenderer = parentPDF.getPdfRenderer().openPage(pageId);

            bitmapRaw = Bitmap.createBitmap(pageRenderer.getWidth()*4, pageRenderer.getHeight()*4, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapRaw);
            canvas.drawColor(Color.WHITE);

            pageRenderer.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            pageRenderer.close();
        }else{
            bitmapRaw = BitmapUtility.generateTextBitmap(parentPDF.getName() + " " + parentPDF.getActivity().getString(R.string.ErrorOpen), 720, 1280);
        }

        isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
        isOpen = true;

    }

    /**
     * Closes the PDF page, releasing the resources associated with it.
     */
    public void close(){
        bitmapRaw = null;
        isOpen = false;
    }

    /**
     * Retrieves the raw bitmap representation of the page's content.
     * If the PDFPage is not open, it opens it first.
     * @return The Bitmap representing the raw content of the page.
     */
    public Bitmap getBitmapRaw() {
        if (!isOpen){
            open();
        }
        return bitmapRaw;
    }

    /**
     * Checks if the page is wide, indicating its width is greater than its height.
     * If the PDFPage is not open, it opens it first.
     * @return true if the page is wide, false otherwise.
     */
    public Boolean getWide() {
        if (!isOpen){
            open();
        }
        return isWide;
    }
}
