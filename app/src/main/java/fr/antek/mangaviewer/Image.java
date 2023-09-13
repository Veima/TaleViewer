package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

public class Image extends File{
    public Image(String name, String path, DocumentFile docFile, File parentFile) {
        super(name, path, docFile, parentFile);
    }
}
