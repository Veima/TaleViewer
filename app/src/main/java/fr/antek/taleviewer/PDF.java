package fr.antek.taleviewer;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a PDF document file, providing functionality to open, navigate, and manage PDF pages.
 */
public class PDF extends File{
    private Bitmap miniature = null; // The miniature representation of the PDF file.
    private PdfRenderer pdfRenderer; // The PdfRenderer used for rendering PDF pages.
    private PDFPage[] listPage; // An array of PDFPage objects to manage PDF pages.
    private boolean open = false; // Flag indicating whether the PDF file is open.

    /**
     * Constructs a PDF object.
     * @param activity The parent AppCompatActivity associated with this PDF.
     * @param parentPath The path of the parent directory.
     * @param doc The DocumentFile representing the PDF file.
     * @param parentFile The parent directory containing this PDF file.
     */
    public PDF(AppCompatActivity activity, String parentPath, DocumentFile doc, Directory parentFile) {
        super(activity, parentPath, doc, parentFile);
    }

    /**
     * Opens the PDF file for rendering and initializes the PdfRenderer.
     */
    private void open(){
        try {
            ParcelFileDescriptor fileDescriptor = super.getActivity().getContentResolver().openFileDescriptor(super.getDoc().getUri(), "r");
            pdfRenderer = new PdfRenderer(Objects.requireNonNull(fileDescriptor));
            listPage = new PDFPage[pdfRenderer.getPageCount()];
        } catch (IOException e) {
            pdfRenderer = null;
            listPage = new PDFPage[1];
        }
        open = true;
    }

    /**
     * Closes the PDF file and releases resources.
     */
    public void close(){
        if (pdfRenderer != null){
            pdfRenderer.close();
        }
        listPage = null;
        open = false;
    }

    /**
     * Opens a specific page within the PDF file.
     * @param pageNumber The page number to open.
     */
    public void openPage(int pageNumber){
        if (listPage == null){
            open();
        }
        listPage[pageNumber-1] = new PDFPage(this, pageNumber-1);
    }

    /**
     * Closes a specific page within the PDF file.
     * @param pageNumber The page number to close.
     */
    public void closePage(int pageNumber){
        listPage[pageNumber-1].close();
    }

    /**
     * Retrieves the PdfRenderer used for rendering PDF pages.
     * If the PDF is not open, it opens it first.
     * @return The PdfRenderer for the PDF file.
     */
    public PdfRenderer getPdfRenderer() {
        if (!open){
            open();
        }
        return pdfRenderer;
    }

    /**
     * Retrieves a specific page from the PDF file.
     * @param pageNumber The page number to retrieve.
     * @return The PDFPage object representing the specified page.
     */
    public PDFPage getPage(int pageNumber){
        return listPage[pageNumber-1];
    }

    public int getPageCount(){
        if (!open){
            open();
        }
        return listPage.length;
    }

    public Uri getUri(){
        return super.getDoc().getUri();
    }

    public void setMiniature(Bitmap miniature) {
        this.miniature = miniature;
    }

    public Bitmap getMiniature() {
        return miniature;
    }
}
