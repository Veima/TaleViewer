package fr.antek.mangaviewer;


import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Directory extends File{
    private ArrayList<File> fileList = null;
    private boolean isScan= false;

    public Directory(AppCompatActivity activity, String parentPath, DocumentFile docFile, Directory parentFile) {
        super(activity, parentPath, docFile, parentFile);
    }

    public void listFile(){
        DocumentFile[] files = super.getDoc().listFiles();
        ArrayList<Thread> listThread = new ArrayList<>();
        List<File> fileListNotArray =  Collections.synchronizedList(new ArrayList<>());
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
            return new Directory(super.getActivity(), super.getPath(), file, this);
        }else if(isImage(file)){
            return new Image(super.getActivity(), super.getPath(), file, this);
        }else if(file.getName().endsWith(".pdf")) {
            return new PDF(super.getActivity(), super.getPath(), file, this);
        }else{
            return null;
        }
    }

    public boolean isImage(DocumentFile file){
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

        for (String extension : extensions) {
            if (Objects.requireNonNull(file.getName()).endsWith(extension)) {
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
                Directory childFile = new Directory(super.getActivity(), super.getPath(), childDoc, this);
                fileList.add(childFile);
                if (splitPath.length == 2) {
                    return childFile.buildFromPath(splitPath[1]);
                } else {
                    return childFile;
                }
            }else if(isImage(childDoc)){
                Image childFile = new Image(super.getActivity(), super.getPath(), childDoc, this);
                fileList.add(childFile);
                return childFile;
            }else if(childDoc.getName().endsWith(".pdf")) {

                PDF childFile = new PDF(super.getActivity(), super.getPath(), childDoc, this);
                fileList.add(childFile);
                return childFile;
            }else{
                return null;
            }
        }
    }

    public File getFileWithPos(int pos){
        if (fileList.size() > 0){
            return fileList.get(pos);
        }else{
            return null;
        }

    }

    public File getFirst(){
        if (!isScan){
            listFile();
        }
        File first = getFileWithPos(0);
        if (first == null){
            return this.getNext();
        }else{
            return first;
        }
    }
    public File getLast(){
        if (!isScan){
            listFile();
        }
        File last = getFileWithPos(fileList.size()-1);
        if (last == null){
            return this.getPrev();
        }else{
            return last;
        }

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
