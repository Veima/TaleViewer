package fr.antek.mangaviewer;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class StoryLib extends Directory{

    public StoryLib(AppCompatActivity context, Uri uri) {
        super(context,"", DocumentFile.fromTreeUri(context, uri), null);
    }
}

