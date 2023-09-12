package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;

public class File {
    private final String name;
    private final String path;
    private final DocumentFile root;
    private final DocumentFile docFile;

    public File(String name, String path, DocumentFile root, DocumentFile docFile) {
        this.name = name;
        this.path = path;
        this.root = root;
        this.docFile = docFile;
    }

}
