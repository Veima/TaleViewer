package fr.antek.historyviewer;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

/**
 * Represents a story library directory for managing and organizing stories.
 */
public class StoryLib extends Directory{

    /**
     * Constructs a StoryLib object using the provided context and URI.
     * @param context The application context.
     * @param uri The URI for the story library directory.
     */
    public StoryLib(AppCompatActivity context, Uri uri) {
        super(context,"", DocumentFile.fromTreeUri(context, uri), null);
    }
}

