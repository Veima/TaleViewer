package fr.antek.mangaviewer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class Chapitre implements Comparable<Chapitre>{
    private AppCompatActivity context;
    private Manga manga;
    private String name;
    private ArrayList<Page> pageList = null;
    private DocumentFile chapitreFile;
    public Chapitre(DocumentFile chapitreFile, Manga manga) {
        this.chapitreFile = chapitreFile;
        this.name = chapitreFile.getName();
        this.manga = manga;
    }

    public void findPage(){
        pageList= new ArrayList<>();
        DocumentFile[] pageFileList = chapitreFile.listFiles();
        for (DocumentFile pageFile : pageFileList) {
            pageList.add(new Page(pageFile, this));
        }
        Collections.sort(pageList);
    }

    public ArrayList<String> getListName(){
        if (pageList == null){
            findPage();
        }
        ArrayList<String> listName = new ArrayList<>();
        for (Page page : pageList){
            listName.add(page.getName());
        }
        return listName;
    }

    public boolean isFirst(Page page){
        if (pageList == null){
            findPage();
        }
        return pageList.get(0).equals(page);
    }

    public boolean isLast(Page page){
        if (pageList == null){
            findPage();
        }
        return pageList.get(pageList.size()-1).equals(page);
    }

    public Page getPrevPage(Page page){
        if (pageList == null){
            findPage();
        }
        for (int i = 1; i<pageList.size(); i++) {
            if (pageList.get(i).equals(page)) {
                return pageList.get(i - 1);
            }
        }
        return page;
    }

    public Page getNextPage(Page page){
        if (pageList == null){
            findPage();
        }
        for (int i = 0; i < pageList.size() - 1; i++) {
            if (pageList.get(i).equals(page)) {
                return pageList.get(i + 1);
            }
        }
        return page;
    }

    public Page getFirstPage(){
        if (pageList == null){
            findPage();
        }
        return pageList.get(0);
    }

    public Page getLastPage(){
        if (pageList == null){
            findPage();
        }
        return pageList.get(pageList.size()-1);
    }

    public Chapitre getPrevChap(){
        return manga.getPrev(this);
    }

    public Chapitre getNextChap(){
        return manga.getNext(this);
    }

    public Page getPageWithName(String name){
        if (pageList != null) {
            for (Page page : pageList){
                if (page.getName().equals(name)){
                    return page;
                }
            }
            return null;
        }else{
            return new Page(chapitreFile.findFile(name), this);
        }

    }


    public Manga getManga() {
        return manga;
    }

    public String getName() {
        return name;
    }

    public Page getPageWithPos(int pos){
        return pageList.get(pos);
    }

    public int compareTo(@NonNull Chapitre chapitre){
        return this.name.compareTo(chapitre.getName());
    }

    public boolean equals(@Nullable Chapitre chapitre) {
        return (this.name.equals(chapitre.getName()) && this.manga.equals(chapitre.getManga()));
    }
}
