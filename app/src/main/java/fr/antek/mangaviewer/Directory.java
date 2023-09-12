package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;

public class Directory extends File{
    private ArrayList<File> fileList = null;

    public Directory(String name, String path, DocumentFile root, DocumentFile docFile) {
        super(name, path, root, docFile);
    }
}
