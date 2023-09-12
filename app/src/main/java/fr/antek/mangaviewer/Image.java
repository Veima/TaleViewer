package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

public class Image extends File{
    public Image(String name, String path, DocumentFile root, DocumentFile docFile) {
        super(name, path, root, docFile);
    }
}
