package fr.antek.mangaviewer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;

public class File implements Comparable<File>{
    private final String name;
    private final String path;
    private final DocumentFile doc;
    private final Directory parentFile;
    private AppCompatActivity activity;

    public File(AppCompatActivity activity, String parentPath, DocumentFile doc, Directory parentFile) {
        this.activity = activity;
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
    public Directory getParentFile() {
        return parentFile;
    }

    public File getPrev(){
        if (parentFile.getParentFile() == null){
            return null;
        }else if (isFirst()){
            File beforeParent = parentFile.getPrev();

            if (beforeParent instanceof Directory) {
                return ((Directory) beforeParent).getLastNotDir();
            }else{
                return beforeParent;
            }
        }else{
            File beforeThis = parentFile.getFileWithPos(parentFile.getPos(this)-1);
            if (beforeThis instanceof Directory) {
                return ((Directory) beforeThis).getLastNotDir();
            }else{
                return beforeThis;
            }
        }
    }

    public File getNext(){
        if (parentFile.getParentFile() == null){
            return null;
        }else if (isLast()){
            File afterParent = parentFile.getNext();
            if (afterParent instanceof Directory) {
                return ((Directory) afterParent).getFirstNotDir();
            }else{
                return afterParent;
            }
        }else{
            File afterThis = parentFile.getFileWithPos(parentFile.getPos(this)+1);
            if (afterThis instanceof Directory) {
                return ((Directory) afterThis).getFirstNotDir();
            }else{
                return afterThis;
            }
        }
    }
    public int compareTo(File file){
        ArrayList<Object> splitTitleThis = this.splitIntAndString();
        ArrayList<Object> splitTitleOther = file.splitIntAndString();
        for (int i =0; i<splitTitleThis.size() ;i++) {
            if (splitTitleOther.size()<=i) {
                return 1;
            }
            if (!(splitTitleThis.get(i).equals(splitTitleOther.get(i)))) {
                if (i%2 == 0) {
                    return ((String)splitTitleThis.get(i)).compareTo((String)splitTitleOther.get(i));
                }else {
                    return (int) (((Double)splitTitleThis.get(i))-((Double)splitTitleOther.get(i)));
                }
            }
        }
        return -1;
    }


    public ArrayList<Object> splitIntAndString() {
        String nameWithoutExp = this.getNameWithoutExp();
        ArrayList<Object> outputList = new ArrayList<>();
        int i=0;
        char c=nameWithoutExp.charAt(i);
        while (i<nameWithoutExp.length()) {

            StringBuilder currentString = new StringBuilder();
            while (!Character.isDigit(c)) {
                currentString.append(c);
                i+=1;
                if (i>=nameWithoutExp.length()) {
                    outputList.add(currentString.toString());
                    return outputList;
                }else {
                    c=nameWithoutExp.charAt(i);
                }

            }
            outputList.add(currentString.toString());

            StringBuilder currentInt = new StringBuilder();
            while (Character.isDigit(c)) {
                currentInt.append(c);
                i+=1;
                if (i>=nameWithoutExp.length()) {
                    outputList.add(Double.parseDouble(currentInt.toString()));
                    return outputList;
                }else {
                    c=nameWithoutExp.charAt(i);
                }
            }
            outputList.add(Double.parseDouble(currentInt.toString()));

        }
        return outputList;
    }

    public String getNameWithoutExp() {
        if (this instanceof Directory){
            return name;
        }else{
            int lastPointIndex = name.lastIndexOf(".");

            if (lastPointIndex >= 0) {
                return name.substring(0, lastPointIndex);
            } else {
                return name;
            }
        }
    }
    public boolean equals(File file) {
        return (this.path.equals(file.getPath()) && this.name.equals(file.getName()));
    }
    public boolean isFirst(){
        return equals(parentFile.getFirst());
    }
    public boolean isLast(){
        return equals(parentFile.getLast());
    }

    public AppCompatActivity getActivity() {
        return activity;
    }
}