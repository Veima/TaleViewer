package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

public class Image extends File{
    public Image(String parentPath, DocumentFile docFile, File parentFile) {
        super(parentPath, docFile, parentFile);
    }
}
