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

    public String[] getPrevEtNextPage(){
        String[] sortie = new String[4];
        if (chapitre.isFirst(this)){
            Chapitre prevChapitre = chapitre.getPrevChap();
            if (prevChapitre == null){
                sortie[0] = null;
                sortie[1] = null;
            }else{
                sortie[0] = prevChapitre.getName();
                sortie[1] = prevChapitre.getLastPage().getName();
            }

        }else{
            sortie[0] = chapitre.getName();
            sortie[1] = chapitre.getPrevPage(this).getName();
        }

        if (chapitre.isLast(this)){
            Chapitre nextChapitre = chapitre.getNextChap();
            if (nextChapitre == null){
                sortie[2] = null;
                sortie[3] = null;
            }else {
                sortie[2] = nextChapitre.getName();
                sortie[3] = nextChapitre.getFirstPage().getName();
            }

        }else{
            sortie[2] = chapitre.getName();
            sortie[3] = chapitre.getNextPage(this).getName();
        }
        return sortie;
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
