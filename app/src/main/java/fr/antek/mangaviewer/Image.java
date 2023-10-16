package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Image extends File{
    private Bitmap miniature = null;
    private Bitmap bitmapRaw;
    private Boolean isWide;
    private Boolean isOpen = false;

    public Image(AppCompatActivity activity, String parentPath, DocumentFile docFile, Directory parentFile) {
        super(activity, parentPath, docFile, parentFile);
        open();

    }

    public void open() {
        try {
            bitmapRaw = MediaStore.Images.Media.getBitmap(super.getActivity().getContentResolver(), super.getDoc().getUri());
            isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
            isOpen = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        bitmapRaw = null;
        isOpen = false;
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

    public Boolean getWide() {
        return isWide;
    }

    public Bitmap getBitmapRaw() {
        if (!isOpen){
            open();
        }
        return bitmapRaw;
    }

    public Boolean getOpen() {
        return isOpen;
    }
}
