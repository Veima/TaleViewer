package fr.antek.mangaviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;

//TODO:ajouter une preview des pages

public class ChapitreActivity extends AppCompatActivity {
    private Chapitre chapitre;
    private Uri mangaFolderUri;
    private String mangaName;
    private String chapitreName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapitre);

        ListView listViewPage = findViewById(R.id.listViewPage);

        mangaFolderUri = Uri.parse(getIntent().getStringExtra("mangaFolderUri"));
        mangaName = getIntent().getStringExtra("mangaName");
        chapitreName = getIntent().getStringExtra("chapitreName");

        Objects.requireNonNull(getSupportActionBar()).setTitle(chapitreName);

        Manga manga = new Manga(this, mangaName, mangaFolderUri);
        manga.findChapitre();

        chapitre = manga.getChapitreWithName(chapitreName);
        chapitre.findPage();

        ArrayList<String> pageNamesList = chapitre.getListName();

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,pageNamesList);
        listViewPage.setAdapter(adapter);
        
        listViewPage.setOnItemClickListener((parent, view, position, id) -> {
            Page page = chapitre.getPageWithPos(position);
            Intent intentToPageActivity = new Intent(ChapitreActivity.this, PageActivity.class);
            intentToPageActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
            intentToPageActivity.putExtra("mangaName",mangaName);
            intentToPageActivity.putExtra("chapitreName",chapitreName);
            intentToPageActivity.putExtra("pageName",page.getName());
            intentToPageActivity.putExtra("from","left");
            startActivity(intentToPageActivity);
        });
        
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapitre, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main) {
            Intent intentToMain = new Intent(ChapitreActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        } else if (id == R.id.action_manga) {

            Intent intentToMangaActivity = new Intent(ChapitreActivity.this, MangaActivity.class);
            intentToMangaActivity.putExtra("mangaFolderUri", mangaFolderUri.toString());
            intentToMangaActivity.putExtra("mangaName", mangaName);
            startActivity(intentToMangaActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}