package fr.antek.mangaviewer;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class Page implements Comparable<Page>{
    private final Chapitre chapitre;
    private final String name;
    private final DocumentFile pageFile;

    public Page(@NonNull DocumentFile pageFile, Chapitre chapitre) {
        this.pageFile = pageFile;
        this.name = pageFile.getName();
        this.chapitre = chapitre;
    }

    public Page getPrevPage(){
        if (chapitre.isFirst(this)){
            Chapitre prevChapitre = chapitre.getPrevChap();
            if (prevChapitre == null){
                return null;
            }else{
                return prevChapitre.getLastPage();
            }
        }else{
            return chapitre.getPrevPage(this);
        }
    }

    public Page getNextPage(){
        if (chapitre.isLast(this)){
            Chapitre nextChapitre = chapitre.getNextChap();
            if (nextChapitre == null){
                return null;
            }else {
                return nextChapitre.getFirstPage();
            }
        }else{
            return chapitre.getNextPage(this);
        }
    }


    public DocumentFile getPageFile() {
        return pageFile;
    }

    public String getName() {
        return name;
    }

    public Chapitre getChapitre() {
        return chapitre;
    }

    public int compareTo(@NonNull Page page){
        return this.name.compareTo(page.getName());
    }

    public boolean equals(@NonNull Page page){
        return (this.name.equals(page.getName()) && this.chapitre.equals(page.getChapitre()));
    }
}
