package fr.antek.mangaviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button buttonCherche;
    private Button buttonUpdate;
    private Button buttonContinueUltime;
    private Button buttonContinuePenultieme;
    private Button buttonContinueAntepenultieme;
    private TextView textContinueUltime;
    private TextView textContinuePenultieme;
    private TextView textContinueAntepenultieme;
    private ListView listViewManga;
    private Uri mangaFolderUri = null;
    private SharedPreferences memoire;
    private SharedPreferences.Editor memoireEditor;
    private MangaLib mangaLib;
    private static final String TAG = "MyActivity";



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

        buttonCherche = findViewById(R.id.buttoncherche);
        buttonUpdate = findViewById(R.id.buttonupdate);
        listViewManga = findViewById(R.id.listViewManga);


        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);
        recupLastManga();
        mangaFolderUri = getStoredUri();

        if (mangaFolderUri != null){
            updateListView(mangaFolderUri);
        }

        buttonCherche.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickDirectory();
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateListView(mangaFolderUri);
            }
        });


    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent intent = result.getData();
                mangaFolderUri = intent.getData();

                getContentResolver().takePersistableUriPermission(mangaFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                saveUriToSharedPreferences(mangaFolderUri);
                updateListView(mangaFolderUri);
            }
        }
    });

    private void updateListView(Uri mangaFolderUri){
        mangaLib = new MangaLib(this, mangaFolderUri);

        ListAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mangaLib.getListName());
        listViewManga.setAdapter(adapter);
        listViewManga.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manga selectedManga = mangaLib.getMangaWithPos(position);
                Intent intentToMangaActivity = new Intent(MainActivity.this, MangaActivity.class);
                intentToMangaActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
                intentToMangaActivity.putExtra("mangaName",selectedManga.getName());
                startActivity(intentToMangaActivity);
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
            Uri storedUri = Uri.parse(uriString);
            return storedUri;
        }else{
            return null;
        }
    }

    private void recupLastManga(){
        String nameUltimeManga = memoire.getString("nameUltimeManga", null);
        if (nameUltimeManga != null){
            String nameLastChapitre = memoire.getString(nameUltimeManga + "lastChapitre", null);
            String nameLastPage = memoire.getString(nameUltimeManga + "lastPage", null);
            buttonContinueUltime.setText("Continué " + nameUltimeManga);
            if ((nameLastChapitre != null) && (nameLastPage != null)){
                textContinueUltime.setText(nameLastChapitre + " | " + nameLastPage);

                buttonContinueUltime.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intentToPageActivity = new Intent(MainActivity.this, PageActivity.class);
                        intentToPageActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
                        intentToPageActivity.putExtra("mangaName",nameUltimeManga);
                        intentToPageActivity.putExtra("chapitreName",nameLastChapitre);
                        intentToPageActivity.putExtra("pageName",nameLastPage);
                        intentToPageActivity.putExtra("from","left");
                        intentToPageActivity.putExtra("hide",false);
                        startActivity(intentToPageActivity);
                    }
                });

            }else{
                textContinueUltime.setVisibility(View.GONE);
                buttonContinueUltime.setVisibility(View.GONE);
            }
        }else {
            textContinueUltime.setVisibility(View.GONE);
            buttonContinueUltime.setVisibility(View.GONE);
        }

        String namePenultiemeManga = memoire.getString("namePenultiemeManga", null);
        if (namePenultiemeManga != null){
            String nameLastChapitre = memoire.getString(namePenultiemeManga + "lastChapitre", null);
            String nameLastPage = memoire.getString(namePenultiemeManga + "lastPage", null);
            buttonContinuePenultieme.setText("Continué " + namePenultiemeManga);
            if ((nameLastChapitre != null) && (nameLastPage != null)){
                textContinuePenultieme.setText(nameLastChapitre + " | " + nameLastPage);

                buttonContinuePenultieme.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intentToPageActivity = new Intent(MainActivity.this, PageActivity.class);
                        intentToPageActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
                        intentToPageActivity.putExtra("mangaName",namePenultiemeManga);
                        intentToPageActivity.putExtra("chapitreName",nameLastChapitre);
                        intentToPageActivity.putExtra("pageName",nameLastPage);
                        intentToPageActivity.putExtra("from","left");
                        intentToPageActivity.putExtra("hide",false);
                        startActivity(intentToPageActivity);
                    }
                });

            }else{
                textContinuePenultieme.setVisibility(View.GONE);
                buttonContinuePenultieme.setVisibility(View.GONE);
            }
        }else {
            textContinuePenultieme.setVisibility(View.GONE);
            buttonContinuePenultieme.setVisibility(View.GONE);
        }

        String nameAntepenultiemeManga = memoire.getString("nameAntepenultiemeManga", null);
        if (nameAntepenultiemeManga != null){
            String nameLastChapitre = memoire.getString(nameAntepenultiemeManga + "lastChapitre", null);
            String nameLastPage = memoire.getString(nameAntepenultiemeManga + "lastPage", null);
            buttonContinueAntepenultieme.setText("Continué " + nameAntepenultiemeManga);
            if ((nameLastChapitre != null) && (nameLastPage != null)){
                textContinueAntepenultieme.setText(nameLastChapitre + " | " + nameLastPage);

                buttonContinueAntepenultieme.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intentToPageActivity = new Intent(MainActivity.this, PageActivity.class);
                        intentToPageActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
                        intentToPageActivity.putExtra("mangaName",nameAntepenultiemeManga);
                        intentToPageActivity.putExtra("chapitreName",nameLastChapitre);
                        intentToPageActivity.putExtra("pageName",nameLastPage);
                        intentToPageActivity.putExtra("from","left");
                        intentToPageActivity.putExtra("hide",false);
                        startActivity(intentToPageActivity);
                    }
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