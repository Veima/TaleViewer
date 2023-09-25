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
        if (activityAfter.equals("ImageActivity")){
            Intent intentToImageActivity = new Intent(ParameterActivity.this, ImageActivity.class);
            intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToImageActivity.putExtra("path", path);
            startActivity(intentToImageActivity);
        }

    }
}