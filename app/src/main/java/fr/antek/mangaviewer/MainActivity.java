package fr.antek.mangaviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button buttonContinueUltime;
    private Button buttonContinuePenultieme;
    private Button buttonContinueAntepenultieme;
    private TextView textContinueUltime;
    private TextView textContinuePenultieme;
    private TextView textContinueAntepenultieme;
    private ListView listViewStory;
    private Uri storyFolderUri = null;
    private SharedPreferences memoire;
    private StoryLib storyLib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonContinueUltime = findViewById(R.id.buttonContinueUltime);
        buttonContinuePenultieme = findViewById(R.id.buttonContinuePenultieme);
        buttonContinueAntepenultieme = findViewById(R.id.buttonContinueAntepenultieme);
        textContinueUltime = findViewById(R.id.textContinueUltime);
        textContinuePenultieme = findViewById(R.id.textContinuePenultieme);
        textContinueAntepenultieme = findViewById(R.id.textContinueAntepenultieme);

        Button buttonCherche = findViewById(R.id.buttonCherche);
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        listViewStory = findViewById(R.id.listViewStory);


        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);
        recupLastStory();
        storyFolderUri = getStoredUri();

        if (storyFolderUri != null){
            updateListView(storyFolderUri);
        }

        buttonCherche.setOnClickListener(v -> pickDirectory());

        buttonUpdate.setOnClickListener(v -> updateListView(storyFolderUri));


    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK){
            Intent intent = result.getData();
            if (intent != null) {
                storyFolderUri = intent.getData();
                getContentResolver().takePersistableUriPermission(storyFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                saveUriToSharedPreferences(storyFolderUri);
                updateListView(storyFolderUri);
            }
        }
    });

    private void updateListView(Uri storyFolderUri){
        storyLib = new StoryLib(this, storyFolderUri);

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, storyLib.getListName());
        listViewStory.setAdapter(adapter);
        listViewStory.setOnItemClickListener((parent, view, position, id) -> {
            File selectedStory = storyLib.getFileWithPos(position);
            if (selectedStory instanceof Directory) {
                Intent intentToStoryActivity = new Intent(MainActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToStoryActivity.putExtra("path",selectedStory.getPath());
                startActivity(intentToStoryActivity);
            }else if(selectedStory instanceof Image){
                Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedStory.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }

    private void pickDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        activityResultLauncher.launch(intent);
    }

    private void saveUriToSharedPreferences(Uri uri) {
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString("mangaFolder", uri.toString());
        editor.apply();
    }

    private Uri getStoredUri() {
        String uriString = memoire.getString("mangaFolder", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }else{
            return null;
        }
    }


    private void recupLastStory(){

        String nameUltimeStory = memoire.getString("nameUltimeStory", null);
        if (nameUltimeStory != null){
            String pathLastImage = memoire.getString(nameUltimeStory + "lastImage", null);
            if (pathLastImage != null){
                textContinueUltime.setText(pathLastImage);
                String textButtonUltime = getString(R.string.buttonContinueText) + " " + nameUltimeStory;
                buttonContinueUltime.setText(textButtonUltime);

                buttonContinueUltime.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                textContinueUltime.setVisibility(View.GONE);
                buttonContinueUltime.setVisibility(View.GONE);
            }
        }else {
            textContinueUltime.setVisibility(View.GONE);
            buttonContinueUltime.setVisibility(View.GONE);
        }

        String namePenultiemeStory = memoire.getString("namePenultiemeStory", null);
        if (namePenultiemeStory != null){
            String pathLastImage = memoire.getString(namePenultiemeStory + "lastImage", null);
            if (pathLastImage != null){
                textContinuePenultieme.setText(pathLastImage);
                String textButtonPenultieme = getString(R.string.buttonContinueText) + " " + namePenultiemeStory;
                buttonContinuePenultieme.setText(textButtonPenultieme);

                buttonContinuePenultieme.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                textContinuePenultieme.setVisibility(View.GONE);
                buttonContinuePenultieme.setVisibility(View.GONE);
            }
        }else {
            textContinuePenultieme.setVisibility(View.GONE);
            buttonContinuePenultieme.setVisibility(View.GONE);
        }


        String nameAntepenultiemeStory = memoire.getString("nameAntepenultiemeStory", null);
        if (nameAntepenultiemeStory != null){
            String pathLastImage = memoire.getString(nameAntepenultiemeStory + "lastImage", null);
            if (pathLastImage != null){
                textContinueAntepenultieme.setText(pathLastImage);
                String textButtonAntepenultieme = getString(R.string.buttonContinueText) + " " + nameAntepenultiemeStory;
                buttonContinueAntepenultieme.setText(textButtonAntepenultieme);

                buttonContinueAntepenultieme.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                textContinueAntepenultieme.setVisibility(View.GONE);
                buttonContinueAntepenultieme.setVisibility(View.GONE);
            }
        }else {
            textContinueAntepenultieme.setVisibility(View.GONE);
            buttonContinueAntepenultieme.setVisibility(View.GONE);
        }


    }

}