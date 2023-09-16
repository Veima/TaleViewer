package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

public class DirectoryActivity extends AppCompatActivity {

    private Uri storyFolderUri;
    private String path;
    private Directory thisDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        thisDirectory = (Directory) storyLib.buildFromPath(path.split("/", 3)[2]);

        ListView listViewFile = findViewById(R.id.listViewFile);

        Objects.requireNonNull(getSupportActionBar()).setTitle(thisDirectory.getName());

        ArrayList<String> fileNamesList = thisDirectory.getListName();

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,fileNamesList);
        listViewFile.setAdapter(adapter);
        listViewFile.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = thisDirectory.getFileWithPos(position);
            if (selectedFile instanceof Directory) {
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToDirectoryActivity);
            }else if(selectedFile instanceof Image){
                Intent intentToImageActivity = new Intent(DirectoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToImageActivity);
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);

        String[] menuOption = path.split("/");

        for (int i = 1; i < menuOption.length-1; i++) {
            String itemName;
            if (i==1){
                itemName = getString(R.string.home);
            }else {
                itemName = menuOption[i];
            }
            menu.add(Menu.NONE, i, Menu.NONE, itemName);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 1){
            Intent intentToMain = new Intent(DirectoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }else{
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId+1; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            if (itemId == 2) {
                Intent intentToStoryActivity = new Intent(DirectoryActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToStoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToStoryActivity);
            }else {
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToDirectoryActivity);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}