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
    private ArrayList<PDFPage> listPage;

    public PDF(AppCompatActivity activity, String parentPath, DocumentFile doc, Directory parentFile) {
        super(activity, parentPath, doc, parentFile);
        open();
    }

    private void open(){
        try {
            ParcelFileDescriptor fileDescriptor = super.getActivity().getContentResolver().openFileDescriptor(super.getDoc().getUri(), "r");
            pdfRenderer = new PdfRenderer(fileDescriptor);
            listPage = new ArrayList<PDFPage>();
            for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                listPage.add(null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        pdfRenderer.close();
        listPage = null;
    }

    public void openPage(int pageNumber){
        listPage.add(pageNumber-1,new PDFPage(this, pageNumber-1));
    }

    public void closePage(int pageNumber){
        listPage.get(pageNumber-1).close();
    }

    public PDFPage getPage(int pageNumber){
        if (listPage.get(pageNumber-1) == null){
            openPage(pageNumber);
        }
        return listPage.get(pageNumber-1);
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
        return pdfRenderer;
    }
}
