package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class Directory extends File{
    private ArrayList<File> fileList = null;

    public Directory(String name, String path, DocumentFile docFile, File parentFile) {
        super(name, path, docFile, parentFile);
    }

    public void listFile(){
        fileList = new ArrayList<>();
        DocumentFile[] files = super.getDocFile().listFiles();
        for (DocumentFile file : files) {
            fileList.add(createStory(file));
        }
        Collections.sort(fileList);
    }

    public File createStory(DocumentFile file) {
        if (file.isDirectory()) {
            return new Directory(file.getName(), super.getPath() + "/" + file.getName(), file, this);
        }else {
            return new Image(file.getName(), super.getPath() + "/" + file.getName(), file, this);
        }
    }

    public ArrayList<String> getListName(){
        ArrayList<String> listName = new ArrayList<>();
        for (File file : fileList){
            listName.add(file.getName());
        }
        return listName;
    }
    public File getFileWithPos(int pos){
        return fileList.get(pos);
    }

}
