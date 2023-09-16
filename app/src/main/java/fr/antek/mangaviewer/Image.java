package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class Image extends File{
    public Image(String parentPath, DocumentFile docFile, Directory parentFile) {
        super(parentPath, docFile, parentFile);
    }

    public Uri getUri(){
        return super.getDoc().getUri();
    }
}
