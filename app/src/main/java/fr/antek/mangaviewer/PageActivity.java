package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

import fr.antek.mangaviewer.databinding.ActivityPageBinding;

//TODO:Changer comment marche le changement de page
//TODO:ajouter des toast pour quand a la fin ou au debut d'un manga
//TODO:ajouter zoom

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PageActivity extends AppCompatActivity {
    private final Handler mHideHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private View mContentView;
    private Page page;
    private Uri mangaFolderUri;
    private String mangaName;
    private String chapitreName;
    private String pageName;
    private ImageView pageView;
    private String prevChapitreName;
    private String prevPageName;
    private String nextChapitreName;
    private String nextPageName;
    private boolean hide;
    private SharedPreferences memoire;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        fr.antek.mangaviewer.databinding.ActivityPageBinding binding = ActivityPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mangaFolderUri = Uri.parse(getIntent().getStringExtra("mangaFolderUri"));
        mangaName = getIntent().getStringExtra("mangaName");
        chapitreName = getIntent().getStringExtra("chapitreName");
        pageName = getIntent().getStringExtra("pageName");
        hide = getIntent().getBooleanExtra("hide",false);
        Objects.requireNonNull(getSupportActionBar()).setTitle(pageName);


        mContentView = binding.pageView;
        pageView = findViewById(R.id.pageView);
        // Set up the user interaction to manually show or hide the system UI.

        if (hide){
            hide();
        }else{
            show();
        }

        pageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                int width = pageView.getWidth();
                int height = pageView.getHeight();

                float relativeX = x / width;
                float relativeY = y / height;

                if ((relativeY < 0.15) || (relativeY > 0.85)) {
                    toggle();
                } else if (relativeX < 0.5) {
                    goPrevPage();
                } else {
                    goNextPage();
                }
            }
            return true;
        });

        Manga manga = new Manga(this, mangaName, mangaFolderUri);

        Chapitre chapitre = manga.getChapitreWithName(chapitreName);
        page = chapitre.getPageWithName(pageName);

        Uri pageUri = page.getPageFile().getUri();




        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (bitmap != null) {
            pageView.setImageBitmap(bitmap);
        }

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
            saveMangaLastPage();
            saveAppLastManga();

            String[] prevEtNextPage = page.getPrevEtNextPage();
            prevChapitreName = prevEtNextPage[0];
            prevPageName = prevEtNextPage[1];
            nextChapitreName = prevEtNextPage[2];
            nextPageName = prevEtNextPage[3];
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main) {
            Intent intentToMain = new Intent(PageActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        } else if (id == R.id.action_manga) {

            Intent intentToMangaActivity = new Intent(PageActivity.this, MangaActivity.class);
            intentToMangaActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
            intentToMangaActivity.putExtra("mangaName",mangaName);
            startActivity(intentToMangaActivity);
            return true;
        } else if (id == R.id.action_chapitre) {
            Intent intentToChapitreActivity = new Intent(PageActivity.this, ChapitreActivity.class);
            intentToChapitreActivity.putExtra("mangaFolderUri",mangaFolderUri.toString());
            intentToChapitreActivity.putExtra("mangaName",mangaName);
            intentToChapitreActivity.putExtra("chapitreName",chapitreName);
            startActivity(intentToChapitreActivity);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goPrevPage(){
        if ((prevChapitreName!=null) && (prevPageName!=null)) {
            Intent intentToPageActivity = new Intent(PageActivity.this, PageActivity.class);
            intentToPageActivity.putExtra("mangaFolderUri", mangaFolderUri.toString());
            intentToPageActivity.putExtra("mangaName", mangaName);
            intentToPageActivity.putExtra("chapitreName", prevChapitreName);
            intentToPageActivity.putExtra("pageName", prevPageName);
            intentToPageActivity.putExtra("from","right");
            intentToPageActivity.putExtra("hide",hide);
            startActivity(intentToPageActivity);
        }
    }

    private void goNextPage(){
        if ((nextChapitreName!=null) && (nextPageName!=null)) {
            Intent intentToPageActivity = new Intent(PageActivity.this, PageActivity.class);
            intentToPageActivity.putExtra("mangaFolderUri", mangaFolderUri.toString());
            intentToPageActivity.putExtra("mangaName", mangaName);
            intentToPageActivity.putExtra("chapitreName", nextChapitreName);
            intentToPageActivity.putExtra("pageName", nextPageName);
            intentToPageActivity.putExtra("from","left");
            intentToPageActivity.putExtra("hide",hide);
            startActivity(intentToPageActivity);
        }
    }


    private void toggle() {
        if (hide) {
            hide = false;
            show();
        } else {
            hide = true;
            hide();

        }
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 0);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            Objects.requireNonNull(mContentView.getWindowInsetsController()).show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, 0);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                Objects.requireNonNull(mContentView.getWindowInsetsController()).hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };

    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };

    private void saveMangaLastPage(){
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString(mangaName + "lastChapitre", chapitreName);
        editor.putString(mangaName + "lastPage", pageName);
        editor.apply();

    }

    private void saveAppLastManga(){
        String nameUltimeManga = memoire.getString("nameUltimeManga", null);
        String namePenultiemeManga = memoire.getString("namePenultiemeManga", null);
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString("nameUltimeManga", mangaName);
        if (nameUltimeManga != null){
            if (! nameUltimeManga.equals(mangaName)){
                editor.putString("namePenultiemeManga", nameUltimeManga);
                if (namePenultiemeManga != null) {
                    if (!namePenultiemeManga.equals(mangaName)) {
                        editor.putString("nameAntepenultiemeManga", namePenultiemeManga);
                    }
                }
            }
        }
        editor.apply();

    }

}