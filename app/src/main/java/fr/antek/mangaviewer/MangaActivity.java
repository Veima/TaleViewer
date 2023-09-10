package fr.antek.mangaviewer;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;

public class MangaActivity extends AppCompatActivity {


    private Manga manga;
    private TextView textContinueManga;
    private Button buttonContinueManga;
    private Uri mangaFolderUri;
    private String mangaName;
    private SharedPreferences memoire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga);
        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);

        ListView listViewChapitre = findViewById(R.id.listViewChapitre);

        buttonContinueManga = findViewById(R.id.buttonContinuManga);
        textContinueManga = findViewById(R.id.textContinuManga);

        mangaFolderUri = Uri.parse(getIntent().getStringExtra("mangaFolderUri"));
        mangaName = getIntent().getStringExtra("mangaName");

        recupLastManga();

        String textButtonUltime = getString(R.string.buttonContinueText) + " " + mangaName;
        buttonContinueManga.setText(textButtonUltime);

        Objects.requireNonNull(getSupportActionBar()).setTitle(mangaName);

        manga = new Manga(this, mangaName, mangaFolderUri);
        manga.findChapitre();

        ArrayList<String> chapitreNamesList = manga.getListName();

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,chapitreNamesList);
        listViewChapitre.setAdapter(adapter);
        listViewChapitre.setOnItemClickListener((parent, view, position, id) -> {
            Chapitre selectedChapitre = manga.getChapitreWithPos(position);
            Intent intentToChapitreActivity = new Intent(MangaActivity.this, ChapitreActivity.class);
            intentToChapitreActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
            intentToChapitreActivity.putExtra("mangaName",mangaName);
            intentToChapitreActivity.putExtra("chapitreName",selectedChapitre.getName());
            startActivity(intentToChapitreActivity);
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manga, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main) {
            Intent intentToMain = new Intent(MangaActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recupLastManga(){
        String nameLastChapitre = memoire.getString(mangaName + "lastChapitre", null);
        String nameLastPage = memoire.getString(mangaName + "lastPage", null);
        if ((nameLastChapitre != null) && (nameLastPage != null)){
            String textInfo = nameLastChapitre + " | " + nameLastPage;
            textContinueManga.setText(textInfo);

            buttonContinueManga.setOnClickListener(v -> {
                Intent intentToPageActivity = new Intent(MangaActivity.this, PageActivity.class);
                intentToPageActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
                intentToPageActivity.putExtra("mangaName",mangaName);
                intentToPageActivity.putExtra("chapitreName",nameLastChapitre);
                intentToPageActivity.putExtra("pageName",nameLastPage);
                intentToPageActivity.putExtra("from","left");
                intentToPageActivity.putExtra("hide",false);
                startActivity(intentToPageActivity);
            });

        }else{
            textContinueManga.setVisibility(View.GONE);
            buttonContinueManga.setVisibility(View.GONE);
        }
    }
}