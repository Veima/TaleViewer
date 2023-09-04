package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class MangaLib {
    private ArrayList<Manga> mangaList;
    private AppCompatActivity context;
    private Uri uri;

    public MangaLib(AppCompatActivity context, Uri uri) {
        this.context =context;
        this.uri = uri;
        findManga();
    }

    private void findManga(){
        mangaList = new ArrayList<>();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
        if (pickedDir != null && pickedDir.isDirectory()) {
            DocumentFile[] files = pickedDir.listFiles();
            for (DocumentFile file : files) {
                if (file.isDirectory()) {
                    mangaList.add(createManga(file.getName()));

                }
            }

        }
        Collections.sort(mangaList);
    }

    private Manga createManga(String name){
        return new Manga(context, name, Uri.parse(uri.toString()+"%2F"+name));
    }

    public ArrayList<String> getListName(){
        ArrayList<String> listName = new ArrayList<>();
        for (Manga manga : mangaList){
            listName.add(manga.getName());
        }
        return listName;
    }

    public Manga getMangaWithName(String name){
        for (Manga manga : mangaList){
            if (manga.getName().equals(name)){
                return manga;
            }
        }
        return null;
    }

    public Manga getMangaWithPos(int pos){
        return mangaList.get(pos);
    }

    public AppCompatActivity getContext() {
        return context;
    }
}
