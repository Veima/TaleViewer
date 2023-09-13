package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;

public class StoryLib extends Directory{
    private ArrayList<File> storyList;
    private AppCompatActivity context;
    private Uri uri;

    public StoryLib(AppCompatActivity context, Uri uri) {
        super("root", "/", DocumentFile.fromTreeUri(context, uri), null);
        this.context =context;
        this.uri = uri;
        super.listFile();
    }



}

