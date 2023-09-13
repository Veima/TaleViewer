package fr.antek.mangaviewer;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Objects;

public class File implements Comparable<File>{
    private final String name;
    private final String path;
    private final DocumentFile docFile;
    private final File parentFile;

    public File(String name, String path, DocumentFile docFile, File parentFile) {
        this.name = name;
        this.path = path;
        this.docFile = docFile;
        this.parentFile = parentFile;
    }



    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public DocumentFile getDocFile() {
        return docFile;
    }
    public int compareTo(File file){
        return this.name.compareTo(file.getName());
    }
    public boolean equals(File file) {
        return (this.path.equals(file.getPath()) && this.name.equals(file.getName()));
    }


}
