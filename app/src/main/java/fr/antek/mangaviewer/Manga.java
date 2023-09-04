package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class Manga implements Comparable<Manga>{
    private AppCompatActivity context;
    private String name;
    private Uri mangaFolderUri;
    private ArrayList<Chapitre> chapitreList = null;
    private DocumentFile root;
    private DocumentFile mangaFile;

    public Manga(AppCompatActivity context,String name, Uri mangaFolderUri) {
        this.context = context;
        this.name = name;
        this.mangaFolderUri = mangaFolderUri;
        this.root = DocumentFile.fromTreeUri(context, mangaFolderUri);
        this.mangaFile = root.findFile(name);
    }

    public void findChapitre(){
        chapitreList = new ArrayList<>();

        DocumentFile[] chapitreFileList = mangaFile.listFiles();
        for (DocumentFile chapitreFile : chapitreFileList) {
            chapitreList.add(new Chapitre(chapitreFile,this));
        }
        Collections.sort(chapitreList);

    }

    public ArrayList<String> getListName(){
        if (chapitreList == null){
            findChapitre();
        }
        ArrayList<String> nameList = new ArrayList<>();
        for (Chapitre chapitre : chapitreList){
            nameList.add(chapitre.getName());
        }
        return nameList;
    }

    public Chapitre getChapitreWithName(String name){
        if (chapitreList != null){
            for (Chapitre chapitre : chapitreList){
                if (chapitre.getName().equals(name)){
                    return chapitre;
                }
            }
            return null;
        }else{
            return new Chapitre(mangaFile.findFile(name),this);
        }

    }

    public String getName() {
        return name;
    }

    public DocumentFile getRoot() {
        return root;
    }

    public Chapitre getPrev(Chapitre chapitre){
        if (chapitreList == null){
            findChapitre();
        }
        for (int i = 1; i<chapitreList.size(); i++) {
            if (chapitreList.get(i).equals(chapitre)) {
                return chapitreList.get(i - 1);
            }
        }
        return null;
    }

    public Chapitre getNext(Chapitre chapitre){
        if (chapitreList == null){
            findChapitre();
        }
        for (int i = 0; i<chapitreList.size()-1; i++) {
            if (chapitreList.get(i).equals(chapitre)) {
                return chapitreList.get(i + 1);
            }
        }
        return null;
    }



    public Chapitre getChapitreWithPos(int pos){
        if (chapitreList == null){
            findChapitre();
        }
        return chapitreList.get(pos);
    }

    public int compareTo(Manga manga){
        return this.name.compareTo(manga.getName());
    }

    public boolean equals(@Nullable Manga manga) {
        return (this.name.equals(manga.getName()) && this.root.getName().equals(manga.getRoot().getName()));
    }
}
