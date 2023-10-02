package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class ParameterActivity extends AppCompatActivity {
    private Uri storyFolderUri;
    private String path;
    private String activityAfter;
    private Switch switchSplit;
    private Switch switchFirstPage;
    private Switch switchFullBefore;
    private Switch switchFullBetween;
    private Switch switchFullAfter;
    private TextView textOverlap;
    private SeekBar sliderOverlap;
    private TextView overlapValue;
    private Switch switchScroll;
    private SharedPreferences memoire;
    private String textFirstPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");
        activityAfter = getIntent().getStringExtra("activityAfter");

        Button buttonValid = findViewById(R.id.buttonValid);
        buttonValid.setOnClickListener(v -> returnActivity());

        switchSplit = findViewById(R.id.switchSplit);
        switchFirstPage = findViewById(R.id.switchFirstPage);
        switchFullBefore = findViewById(R.id.switchFullBefore);
        switchFullBetween = findViewById(R.id.switchFullBetween);
        switchFullAfter = findViewById(R.id.switchFullAfter);
        textOverlap = findViewById(R.id.textOverlap);
        sliderOverlap = findViewById(R.id.sliderOverlap);
        overlapValue = findViewById(R.id.overlapValue);
        switchScroll = findViewById(R.id.switchScroll);

        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);
        SharedPreferences.Editor editor = memoire.edit();

        switchSplit.setChecked(memoire.getBoolean("switchSplit",false));
        if (memoire.getBoolean("switchSplit",false)) {
            changeDisplaySplit(View.VISIBLE);
        } else {
            changeDisplaySplit(View.GONE);
        }
        switchFirstPage.setChecked(memoire.getBoolean("switchFirstPage",true));
        if (memoire.getBoolean("switchFirstPage",true)) {
            textFirstPage = getString(R.string.FirstPage) + " "+ getString(R.string.Right);
        }else{
            textFirstPage = getString(R.string.FirstPage) + " "+ getString(R.string.Left);
        }
        switchFirstPage.setText(textFirstPage);
        switchFullBefore.setChecked(memoire.getBoolean("switchFullBefore",false));
        switchFullBetween.setChecked(memoire.getBoolean("switchFullBetween",false));
        switchFullAfter.setChecked(memoire.getBoolean("switchFullAfter",false));
        String textOverlapValue = memoire.getInt("overlap",0) + "%";
        overlapValue.setText(textOverlapValue);
        sliderOverlap.setProgress(memoire.getInt("overlap",0)*2);
        switchScroll.setChecked(memoire.getBoolean("switchScroll",false));

        switchSplit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchSplit", isChecked);
            editor.apply();
            if (isChecked) {
                changeDisplaySplit(View.VISIBLE);
            } else {
                changeDisplaySplit(View.GONE);
            }
        });
        switchFirstPage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchFirstPage", isChecked);
            editor.apply();
            if (isChecked) {
                textFirstPage = getString(R.string.FirstPage) + " "+ getString(R.string.Right);
            }else{
                textFirstPage = getString(R.string.FirstPage) + " "+ getString(R.string.Left);
            }
            switchFirstPage.setText(textFirstPage);
        });
        switchFullBefore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchFullBefore", isChecked);
            editor.apply();
        });
        switchFullBetween.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchFullBetween", isChecked);
            editor.apply();
        });
        switchFullAfter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchFullAfter", isChecked);
            editor.apply();
        });
        sliderOverlap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int overlap = Math.round(seekBar.getProgress()/2);
                editor.putInt("overlap", overlap);
                editor.apply();
                String textOverlapValue = overlap + "%";
                overlapValue.setText(textOverlapValue);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        switchScroll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("switchScroll", isChecked);
            editor.apply();
        });



    }

    public void changeDisplaySplit(int state){
        switchFirstPage.setVisibility(state);
        switchFullBefore.setVisibility(state);
        switchFullBetween.setVisibility(state);
        switchFullAfter.setVisibility(state);
        textOverlap.setVisibility(state);
        sliderOverlap.setVisibility(state);
        overlapValue.setVisibility(state);
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