package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class Image extends File{
    private Bitmap miniature = null;
    public Image(String parentPath, DocumentFile docFile, Directory parentFile) {
        super(parentPath, docFile, parentFile);
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
