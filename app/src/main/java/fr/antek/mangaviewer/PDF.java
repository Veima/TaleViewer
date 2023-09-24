package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class PDF extends File{
    private Bitmap miniature = null;
    public PDF(String parentPath, DocumentFile doc, Directory parentFile) {
        super(parentPath, doc, parentFile);
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
