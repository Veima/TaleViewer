package fr.antek.historyviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;

/**
 * Represents a file or directory in the application's file system.
 */
public class File implements Comparable<File>{
    private final String name;      // The name of the file or directory.
    private final String path;      // The full path of the file or directory.
    private final DocumentFile doc; // A reference to the corresponding DocumentFile.
    private final Directory parentFile; // The parent directory of this file.
    private final AppCompatActivity activity; // The parent activity associated with this file.


    /**
     * Constructs a File object.
     * @param activity The parent AppCompatActivity.
     * @param parentPath The path of the parent directory.
     * @param doc The DocumentFile representing this file or directory.
     * @param parentFile The parent directory.
     */
    public File(AppCompatActivity activity, String parentPath, DocumentFile doc, Directory parentFile) {
        this.activity = activity;
        this.name = doc.getName();
        this.path = parentPath + "/" + name;
        this.doc = doc;
        this.parentFile = parentFile;
    }

    /**
     * Get the previous file in the parent directory.
     * @return The previous file or null if this is the first file.
     */
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

    /**
     * Get the next file in the parent directory.
     * @return The next file or null if this is the last file.
     */
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

    /**
     * Compare this file to another file based on their names.
     * @param file The file to compare to.
     * @return Negative value if this file is smaller, positive if larger, 0 if equal.
     */
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

    /**
     * Splits the name of the file into a list of strings and integers.
     * @return An ArrayList containing strings and doubles extracted from the name.
     */
    public ArrayList<Object> splitIntAndString() {
        String nameWithoutExp = this.getNameWithoutExt();
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

    /**
     * Get the name of the file without its file extension.
     * @return The name of the file without its extension.
     */
    public String getNameWithoutExt() {
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

    /**
     * Check if this file is equal to another file based on path and name.
     * @param file The file to compare to.
     * @return True if the files are equal, otherwise false.
     */
    public boolean equals(File file) {
        return (this.path.equals(file.getPath()) && this.name.equals(file.getName()));
    }

    /**
     * Check if this file is the first file in the parent directory.
     * @return True if this is the first file, otherwise false.
     */
    public boolean isFirst(){
        return equals(parentFile.getFirst());
    }

    /**
     * Check if this file is the last file in the parent directory.
     * @return True if this is the last file, otherwise false.
     */
    public boolean isLast(){
        return equals(parentFile.getLast());
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
    public AppCompatActivity getActivity() {
        return activity;
    }
}