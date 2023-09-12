package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class StoryLib {
    private ArrayList<File> storyList;
    private final AppCompatActivity context;
    private final Uri uri;

    public StoryLib(AppCompatActivity context, Uri uri) {
        this.context =context;
        this.uri = uri;
        findManga();
    }

    private void findManga(){
        storyList = new ArrayList<>();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
        if (pickedDir != null && pickedDir.isDirectory()) {
            DocumentFile[] files = pickedDir.listFiles();
            for (DocumentFile file : files) {
                if (file.isDirectory()) {
                    storyList.add(createStory(file));

                }
            }

        }
        Collections.sort(storyList);
    }

    public File createStory(DocumentFile file) {
        if (file.isDirectory()) {
            return new Directory(file.getName(), "root", DocumentFile.fromTreeUri(context, uri), file);
        }else {
            return new Image(file.getName(), "root", DocumentFile.fromTreeUri(context, uri), file);
        }
    }


    public ArrayList<String> getListName(){
        ArrayList<String> listName = new ArrayList<>();
        for (File file : storyList){
            listName.add(file.getName());
        }
        return listName;
    }

    public File getStoryWithPos(int pos){
        return storyList.get(pos);
    }

}
}
