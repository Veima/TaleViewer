package fr.antek.taleviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * ParameterActivity is responsible for managing the application's settings and providing options for exporting and importing settings.
 */
public class ParameterActivity extends AppCompatActivity {
    private Uri storyFolderUri;
    private String path;
    private String activityAfter;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchFirstPage;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchFullBefore;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchFullBetween;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchFullAfter;
    private TextView textOverlap;
    private SeekBar sliderOverlap;
    private TextView overlapValue;
    private SharedPreferences memoire;
    private String textFirstPage;
    public static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int PICK_TXT_FILE = 2;


    /**
     * Called when the activity is created. Initializes UI elements, handles user interactions, and manages settings.
     *
     * @param savedInstanceState The saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");
        activityAfter = getIntent().getStringExtra("activityAfter");

        Button buttonValid = findViewById(R.id.buttonValid);
        buttonValid.setOnClickListener(v -> returnActivity());

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchSplit = findViewById(R.id.switchSplit);
        switchFirstPage = findViewById(R.id.switchFirstPage);
        switchFullBefore = findViewById(R.id.switchFullBefore);
        switchFullBetween = findViewById(R.id.switchFullBetween);
        switchFullAfter = findViewById(R.id.switchFullAfter);
        textOverlap = findViewById(R.id.textOverlap);
        sliderOverlap = findViewById(R.id.sliderOverlap);
        overlapValue = findViewById(R.id.overlapValue);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchScroll = findViewById(R.id.switchScroll);

        Button exportButton = findViewById(R.id.buttonExport);
        Button importButton = findViewById(R.id.buttonImport);

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
                int overlap = Math.toIntExact(Math.round(seekBar.getProgress() / 2.0));
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

    /**
     * Toggles the visibility of UI elements related to splitting settings.
     *
     * @param state The visibility state to set for the elements.
     */
    public void changeDisplaySplit(int state){
        switchFirstPage.setVisibility(state);
        switchFullBefore.setVisibility(state);
        switchFullBetween.setVisibility(state);
        switchFullAfter.setVisibility(state);
        textOverlap.setVisibility(state);
        sliderOverlap.setVisibility(state);
        overlapValue.setVisibility(state);
    }

    /**
     * Navigates back to the previous activity.
     */
    private void returnActivity(){
        switch (activityAfter) {
            case "MainActivity" -> {
                Intent intentToMainActivity = new Intent(ParameterActivity.this, MainActivity.class);
                startActivity(intentToMainActivity);
            }
            case "StoryActivity" -> {
                Intent intentToStoryActivity = new Intent(ParameterActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToStoryActivity.putExtra("path", path);
                startActivity(intentToStoryActivity);
            }
            case "DirectoryActivity" -> {
                Intent intentToDirectoryActivity = new Intent(ParameterActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", path);
                startActivity(intentToDirectoryActivity);
            }
            case "ImageActivity" -> {
                Intent intentToImageActivity = new Intent(ParameterActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", path);
                startActivity(intentToImageActivity);
            }
        }
    }

    /**
     * Displays the file chooser dialog to export settings to a file.
     *
     * @param activity The activity context in which to display the dialog.
     */
    public void showFileChooser(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, generateFileName());
        activity.startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    /**
     * Called when an activity launched by this activity returns a result.
     *
     * @param requestCode The code that was sent to the launched activity.
     * @param resultCode The result code returned by the launched activity.
     * @param data The intent data returned by the launched activity.
     */
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
                        Objects.requireNonNull(outputStream).write(yourData.getBytes());
                        outputStream.close();

                        Toast.makeText(this, getString(R.string.SaveSucess), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.SaveFail), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else if(requestCode == PICK_TXT_FILE && resultCode == Activity.RESULT_OK){
            String content = readSelectedTextFile(this, requestCode, resultCode, data);

            if (content != null) {
                stringToSharedPreferences(memoire.edit(), content);
            }
        }
    }

    /**
     * Converts the application's settings stored in SharedPreferences to a string representation.
     *
     * @param sharedPreferences The SharedPreferences object containing the settings.
     * @return A string representation of the settings.
     */
    public String sharedPreferencesToString(SharedPreferences sharedPreferences) {
        StringBuilder outputSting = new StringBuilder();

        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            outputSting.append(key).append("=").append(value).append("\n");

        }
        return outputSting.toString();
    }

    /**
     * Imports settings from a text file into the application's SharedPreferences.
     *
     * @param editor The SharedPreferences.Editor used to apply the imported settings.
     * @param inputString The string containing the settings to import.
     */
    public static void stringToSharedPreferences(SharedPreferences.Editor editor, String inputString) {
        String[] listPref = inputString.split("\n");
            for (String pref : listPref) {
                String[] prefSplit = pref.split("=", 2);
                if (prefSplit.length>1) {
                    String key = prefSplit[0];
                    String value = prefSplit[1];
                    if ((value.equals("true")) || (value.equals("false"))){
                        editor.putBoolean(key, Boolean.parseBoolean(value));
                    }else if (value.matches("[0-9]+")){
                        editor.putInt(key, Integer.parseInt(value));
                    }else{
                        editor.putString(key, value);
                    }
                }
            }
        editor.apply();
    }

    /**
     * Generates a unique file name for exported settings.
     *
     * @return A unique file name for exported settings.
     */
    public String generateFileName() {
        String fileName;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm");
        String timestamp = formatter.format(LocalDateTime.now());
        fileName = "TV_Save_" + timestamp + ".txt";
        return fileName;
    }

    /**
     * Displays a file picker dialog to choose a text file for importing settings.
     *
     * @param activity The activity context in which to display the dialog.
     */
    public void chooseTextFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        activity.startActivityForResult(intent, PICK_TXT_FILE);
    }

    /**
     * Reads the content of the selected text file.
     *
     * @param activity The activity context.
     * @param requestCode The request code.
     * @param resultCode The result code.
     * @param data The intent data containing the selected text file's URI.
     * @return The content of the selected text file, or null if an error occurs.
     */
    public String readSelectedTextFile(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_TXT_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            StringBuilder content = new StringBuilder();

            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(Objects.requireNonNull(uri));
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

    /**
     * Inflate the options menu for the activity. It includes an item for app settings.
     * @param menu The menu to be inflated.
     * @return True if the menu is successfully inflated.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.home));
        return true;
    }

    /**
     * Handle menu item selection. In this case, it navigates to the app's settings activity.
     * @param item The selected menu item.
     * @return True if the item is successfully handled.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 0) {
            Intent intentToMain = new Intent(ParameterActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }

        return super.onOptionsItemSelected(item);
    }
}