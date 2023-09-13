package fr.antek.mangaviewer;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Objects;

public class File implements Comparable<File>{
    private final String name;
    private final String path;
    private final DocumentFile doc;
    private final File parentFile;

    public File(String parentPath, DocumentFile doc, File parentFile) {
        this.name = doc.getName();
        this.path = parentPath + "/" + name;
        this.doc = doc;
        this.parentFile = parentFile;
    }



    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public DocumentFile getDoc() {
        return doc;
    }
    public int compareTo(File file){
        return this.name.compareTo(file.getName());
    }
    public boolean equals(File file) {
        return (this.path.equals(file.getPath()) && this.name.equals(file.getName()));
    }




}
