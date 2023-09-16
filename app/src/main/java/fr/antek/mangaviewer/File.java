package fr.antek.mangaviewer;


import androidx.documentfile.provider.DocumentFile;

public class File implements Comparable<File>{
    private final String name;
    private final String path;
    private final DocumentFile doc;
    private final Directory parentFile;

    public File(String parentPath, DocumentFile doc, Directory parentFile) {
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
        return this.name.compareTo(file.getName());
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
}