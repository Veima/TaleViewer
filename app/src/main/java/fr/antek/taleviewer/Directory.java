package fr.antek.taleviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a directory in the application's file system, extending the File class.
 */
public class Directory extends File{
    private ArrayList<File> fileList = null; // List of files within this directory.
    private boolean isScan = false; // Flag to indicate whether the directory has been scanned.

    /**
     * Constructs a Directory object.
     * @param activity The parent AppCompatActivity.
     * @param parentPath The path of the parent directory.
     * @param docFile The DocumentFile representing this directory.
     * @param parentFile The parent directory.
     */
    public Directory(AppCompatActivity activity, String parentPath, DocumentFile docFile, Directory parentFile) {
        super(activity, parentPath, docFile, parentFile);
    }

    /**
     * Lists the files within this directory and populates the fileList.
     */
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

    /**
     * Creates a File object from a DocumentFile.
     * @param file The DocumentFile to create a File from.
     * @return The created File object or null if the file format is not supported.
     */
    public File createFile(DocumentFile file) {
        if (file.isDirectory()) {
            return new Directory(super.getActivity(), super.getPath(), file, this);
        }else if(isImage(file)){
            return new Image(super.getActivity(), super.getPath(), file, this);
        }else if(Objects.requireNonNull(file.getName()).endsWith(".pdf")) {
            return new PDF(super.getActivity(), super.getPath(), file, this);
        }else{
            return null;
        }
    }

    /**
     * Checks if a DocumentFile is an image by its file extension.
     * @param file The DocumentFile to check.
     * @return True if the file is an image, otherwise false.
     */
    public boolean isImage(DocumentFile file){
        String[] extensions = {".jpg", ".jpeg", ".png", ".bmp", ".webp"};

        for (String extension : extensions) {
            if (Objects.requireNonNull(file.getName()).endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a File object from a path relative to this directory.
     * @param endPath The path relative to this directory.
     * @return The constructed File object or null if it does not exist.
     */
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
            }else if(Objects.requireNonNull(childDoc.getName()).endsWith(".pdf")) {

                PDF childFile = new PDF(super.getActivity(), super.getPath(), childDoc, this);
                fileList.add(childFile);
                return childFile;
            }else{
                return null;
            }
        }
    }

    /**
     * Get a list of files within this directory.
     * If the directory has not been scanned, it scans it first.
     * @return A list of files within this directory.
     */
    public ArrayList<File> getListFile() {
        if (!isScan){
            listFile();
        }
        return fileList;
    }

    /**
     * Get a file at a specific position within the directory.
     * If the directory has not been scanned, it scans it first.
     * @param pos The position of the file.
     * @return The file at the specified position or null if it does not exist.
     */
    public File getFileWithPos(int pos){
        if (!isScan){
            listFile();
        }
        if (fileList.size() > 0){
            return fileList.get(pos);
        }else{
            return null;
        }

    }

    /**
     * Get the first file in the directory.
     * @return The first file or the next file if the first one is a directory.
     */
    public File getFirst(){
        File first = getFileWithPos(0);
        if (first == null){
            return this.getNext();
        }else{
            return first;
        }
    }

    /**
     * Get the last file in the directory.
     * @return The last file or the previous file if the last one is a directory.
     */
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

    /**
     * Get the position of a file within the directory.
     * @param file The file to find the position of.
     * @return The position of the file or 0 if not found.
     */
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

    /**
     * Get the first non-directory file in the directory.
     * @return The first non-directory file or the next one if it is a directory.
     */
    public File getFirstNotDir(){
        File first = getFirst();
        if (first instanceof Directory) {
            return ((Directory) first).getFirstNotDir();
        }else{
            return first;
        }
    }

    /**
     * Get the last non-directory file in the directory.
     * @return The last non-directory file or the previous one if it is a directory.
     */
    public File getLastNotDir(){
        File last = getLast();
        if (last instanceof Directory) {
            return ((Directory) last).getLastNotDir();
        }else{
            return last;
        }
    }

}
