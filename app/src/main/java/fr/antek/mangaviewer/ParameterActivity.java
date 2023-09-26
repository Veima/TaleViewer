package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

public class ParameterActivity extends AppCompatActivity {
    private Uri storyFolderUri;
    private String path;
    private String activityAfter;
    private Button buttonValid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");
        activityAfter = getIntent().getStringExtra("activityAfter");

        buttonValid = findViewById(R.id.buttonValid);
        buttonValid.setOnClickListener(v -> returnActivity());
    }

    private void returnActivity(){
        if (activityAfter.equals("MainActivity")){
            Intent intentToMainActivity = new Intent(ParameterActivity.this, MainActivity.class);
            startActivity(intentToMainActivity);
        }else if (activityAfter.equals("StoryActivity")){
            Intent intentToStoryActivity = new Intent(ParameterActivity.this, StoryActivity.class);
            intentToStoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToStoryActivity.putExtra("path", path);
            startActivity(intentToStoryActivity);
        }else if (activityAfter.equals("DirectoryActivity")){
            Intent intentToDirectoryActivity = new Intent(ParameterActivity.this, DirectoryActivity.class);
            intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToDirectoryActivity.putExtra("path", path);
            startActivity(intentToDirectoryActivity);
        }else if (activityAfter.equals("ImageActivity")){
            Intent intentToImageActivity = new Intent(ParameterActivity.this, ImageActivity.class);
            intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToImageActivity.putExtra("path", path);
            startActivity(intentToImageActivity);
        }

    }
}