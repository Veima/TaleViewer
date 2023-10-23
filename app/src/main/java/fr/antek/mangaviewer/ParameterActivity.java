package fr.antek.mangaviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import android.app.Activity;
import androidx.documentfile.provider.DocumentFile;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    private Button exportButton;
    private Button importButton;
    private static final String JSON_KEY = "data";
    public static final int FILE_PICKER_REQUEST_CODE = 1001;



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

        exportButton = findViewById(R.id.buttonExport);
        importButton = findViewById(R.id.buttonImport);

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

        exportButton.setOnClickListener(v -> showFileChooser(this));
        importButton.setOnClickListener(v -> chooseTextFile(this));



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



    public void showFileChooser(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, generateFileName());
        activity.startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        OutputStream outputStream = getContentResolver().openOutputStream(uri);

                        String yourData = sharedPreferencesToString(memoire);
                        outputStream.write(yourData.getBytes());
                        outputStream.close();

                        Toast.makeText(this, getString(R.string.SaveSucess), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.SaveFail), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else{
            String content = readSelectedTextFile(this, requestCode, resultCode, data);

            if (content != null) {
                Log.d("moi", content);
            }
        }
    }


    public String sharedPreferencesToString(SharedPreferences sharedPreferences) {
        String outputSting = "";

        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            outputSting = outputSting + key + "=" + value + "\n";

        }
        return outputSting;
    }

    public static void stringToSharedPreferences(SharedPreferences.Editor editor, String inputString) {
        String[] listPref = inputString.split("\n");
            for (String pref : listPref) {
                String key = pref.split("=",1)[0];
                String value = pref.split("=",1)[1];

                try {
                    boolean valueBoolean = Boolean.parseBoolean(value);
                    editor.putBoolean(key, valueBoolean);
                } catch (Exception e) {
                    editor.putString(key, value);
                }
            }
        editor.apply();
    }

    public String generateFileName() {
        String fileName;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm");
        String timestamp = formatter.format(LocalDateTime.now());
        fileName = "MW_Save_" + timestamp + ".txt";
        return fileName;
    }
    private static final int PICK_TXT_FILE = 1;

    public void chooseTextFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        activity.startActivityForResult(intent, PICK_TXT_FILE);
    }

    public String readSelectedTextFile(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_TXT_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            StringBuilder content = new StringBuilder();

            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }

                reader.close();
                return content.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }



}