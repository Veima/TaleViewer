package fr.antek.mangaviewer;


import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Directory extends File{
    private ArrayList<File> fileList = null;
    private boolean isScan= false;

    public Directory(String parentPath, DocumentFile docFile, Directory parentFile) {
        super(parentPath, docFile, parentFile);
    }

    public void listFile(){
        DocumentFile[] files = super.getDoc().listFiles();
        ArrayList<Thread> listThread = new ArrayList<>();
        List fileListNotArray =  Collections.synchronizedList(new ArrayList<File>());
        for (DocumentFile file : files) {

            Thread thread = new Thread(() -> {
                File newFile = createFile(file);
                if (newFile != null){
                    fileListNotArray.add(newFile);
                }
            });

            listThread.add(thread);
            thread.start();
        }

        for (Thread thread : listThread) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fileList = new ArrayList<>(fileListNotArray);

        Collections.sort(fileList);
        isScan = true;
    }

    public File createFile(DocumentFile file) {
        if (file.isDirectory()) {
            return new Directory(super.getPath(), file, this);
        }else if(isImage(file)){
            return new Image(super.getPath(), file, this);
        }else{
            return null;
        }
    }

    public boolean isImage(DocumentFile file){
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

        for (String extension : extensions) {
            if (file.getName().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<File> getListFile() {
        if (!isScan){
            listFile();
        }
        return fileList;
    }

    public File buildFromPath(String endPath){
        fileList = new ArrayList<>();
        String[] splitPath = endPath.split("/", 2);
        DocumentFile childDoc = super.getDoc().findFile(splitPath[0]);
        if (childDoc == null) {
            return null;
        }else{
            if (childDoc.isDirectory()) {
                Directory childFile = new Directory(super.getPath(), childDoc, this);
                fileList.add(childFile);
                if (splitPath.length == 2) {
                    return childFile.buildFromPath(splitPath[1]);
                } else {
                    return childFile;
                }
            } else {
                Image childFile = new Image(super.getPath(), childDoc, this);
                fileList.add(childFile);
                return childFile;
            }
        }
    }

    public File getFileWithPos(int pos){
        return fileList.get(pos);
    }

    public File getFirst(){
        if (!isScan){
            listFile();
        }
        return getFileWithPos(0);
    }
    public File getLast(){
        if (!isScan){
            listFile();
        }
        return getFileWithPos(fileList.size()-1);
    }
    public int getPos(File file){
        if (!isScan){
            listFile();
        }
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).equals(file)) {
                return i;
            }
        }
        return 0;
    }

    public File getFirstNotDir(){
        File first = getFirst();
        if (first instanceof Directory) {
            return ((Directory) first).getFirstNotDir();
        }else{
            return first;
        }
    }
    public File getLastNotDir(){
        File last = getLast();
        if (last instanceof Directory) {
            return ((Directory) last).getLastNotDir();
        }else{
            return last;
        }
    }

}
