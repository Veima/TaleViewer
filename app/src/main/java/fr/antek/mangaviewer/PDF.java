package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.util.ArrayList;

public class PDF extends File{
    private Bitmap miniature = null;
    private PdfRenderer pdfRenderer;
    private PDFPage[] listPage;
    private boolean open= false;

    public PDF(AppCompatActivity activity, String parentPath, DocumentFile doc, Directory parentFile) {
        super(activity, parentPath, doc, parentFile);
        open();
    }

    private void open(){
        try {
            ParcelFileDescriptor fileDescriptor = super.getActivity().getContentResolver().openFileDescriptor(super.getDoc().getUri(), "r");
            pdfRenderer = new PdfRenderer(fileDescriptor);
            listPage = new PDFPage[pdfRenderer.getPageCount()];
            open = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        pdfRenderer.close();
        listPage = null;
        open = false;
    }

    public void openPage(int pageNumber){
        if (listPage == null){
            open();
        }
        listPage[pageNumber-1] = new PDFPage(this, pageNumber-1);
    }

    public void closePage(int pageNumber){
        listPage[pageNumber-1].close();
    }

    public PDFPage getPage(int pageNumber){
        return listPage[pageNumber-1];
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

    public PdfRenderer getPdfRenderer() {
        if (!open){
            open();
        }
        return pdfRenderer;
    }
}
