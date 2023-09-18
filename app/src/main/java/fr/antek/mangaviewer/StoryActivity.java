package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class StoryActivity extends AppCompatActivity {
    private TextView textContinueStory;
    private Button buttonContinueStory;
    private Uri storyFolderUri;
    private String path;
    private SharedPreferences memoire;
    private Directory thisStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);

        ListView listViewChapitre = findViewById(R.id.listViewFile);

        buttonContinueStory = findViewById(R.id.buttonContinuStory);
        textContinueStory = findViewById(R.id.textContinuStory);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        recupLastStory();

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        thisStory = (Directory) storyLib.buildFromPath(path.split("/", 3)[2]);

        Objects.requireNonNull(getSupportActionBar()).setTitle(thisStory.getName());

        ArrayList<String> fileNamesList = thisStory.getListName();

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,fileNamesList);
        listViewChapitre.setAdapter(adapter);
        listViewChapitre.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = thisStory.getFileWithPos(position);
            if (selectedFile instanceof Directory) {
                Intent intentToDirectoryActivity = new Intent(StoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path",selectedFile.getPath());
                startActivity(intentToDirectoryActivity);
            }else if(selectedFile instanceof Image){
                Intent intentToImageActivity = new Intent(StoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_story, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main) {
            Intent intentToMain = new Intent(StoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recupLastStory(){
        String storyName = path.split("/")[2];
        String pathLastImage = memoire.getString(storyName + "lastImage", null);
        if (pathLastImage != null){
            textContinueStory.setText(pathLastImage.split("/",4)[3]);
            String textButton = getString(R.string.buttonContinueText) + " " + storyName;
            buttonContinueStory.setText(textButton);

            buttonContinueStory.setOnClickListener(v -> {
                Intent intentToImageActivity = new Intent(StoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", pathLastImage);
                startActivity(intentToImageActivity);
            });
        }else{
            textContinueStory.setVisibility(View.GONE);
            buttonContinueStory.setVisibility(View.GONE);
        }
    }
}