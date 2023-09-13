package fr.antek.mangaviewer;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class Directory extends File{
    private ArrayList<File> fileList = null;

    public Directory(String parentPath, DocumentFile docFile, File parentFile) {
        super(parentPath, docFile, parentFile);
    }

    public void listFile(){
        fileList = new ArrayList<>();
        DocumentFile[] files = super.getDoc().listFiles();
        for (DocumentFile file : files) {
            fileList.add(createStory(file));
        }
        Collections.sort(fileList);
    }

    public File createStory(DocumentFile file) {
        if (file.isDirectory()) {
            return new Directory(super.getPath(), file, this);
        }else {
            return new Image(super.getPath(), file, this);
        }
    }

    public ArrayList<String> getListName(){
        if (fileList==null){
            listFile();
        }
        ArrayList<String> listName = new ArrayList<>();
        for (File file : fileList){
            listName.add(file.getName());
        }
        return listName;
    }

    public File buildFromPath(String endPath){
        String[] splitPath = endPath.split("/", 2);
        DocumentFile childDoc = super.getDoc().findFile(splitPath[0]);
        if (childDoc.isDirectory()){
            Directory childFile = new Directory(super.getPath(),childDoc,this);
            fileList.add(childFile);
            if (splitPath.length==2){
                return childFile.buildFromPath(splitPath[1]);
            }else{
                return childFile;
            }
        }else{
            Image childFile = new Image(super.getPath(),childDoc,this);
            fileList.add(childFile);
            return childFile;
        }
    }

    public File getFileWithPos(int pos){
        return fileList.get(pos);
    }

}
