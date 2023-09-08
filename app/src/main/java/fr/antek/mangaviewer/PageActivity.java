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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

import fr.antek.mangaviewer.databinding.ActivityPageBinding;

import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.view.MotionEvent;

import android.graphics.Matrix;
import android.graphics.Canvas;

public class PageActivity extends AppCompatActivity {
    private final Handler mHideHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private View mContentView;
    private Page page;
    private Uri mangaFolderUri;
    private String mangaName;
    private String chapitreName;
    private ImageView pageView;
    private boolean hide = false;
    private SharedPreferences memoire;
    private Page prevPage;
    private Page nextPage;
    private Boolean noOtherAction = true;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    private boolean isZoomed = false;
    private Bitmap bitmap;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        fr.antek.mangaviewer.databinding.ActivityPageBinding binding = ActivityPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mangaFolderUri = Uri.parse(getIntent().getStringExtra("mangaFolderUri"));
        mangaName = getIntent().getStringExtra("mangaName");
        chapitreName = getIntent().getStringExtra("chapitreName");
        String pageName = getIntent().getStringExtra("pageName");
        Objects.requireNonNull(getSupportActionBar()).setTitle(pageName);


        mContentView = binding.pageView;
        pageView = findViewById(R.id.pageView);
        // Set up the user interaction to manually show or hide the system UI.

        Manga manga = new Manga(this, mangaName, mangaFolderUri);

        Chapitre chapitre = manga.getChapitreWithName(chapitreName);
        page = chapitre.getPageWithName(pageName);

        displayPage();

        if (hide){
            hide();
        }else{
            show();
        }

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
        gestureDetector = new GestureDetector(this, new DoubleTapListener());

        pageView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (noOtherAction){
                    Objects.requireNonNull(getSupportActionBar()).setTitle("pong");
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
                }else{
                    noOtherAction = true;
                }

            }

            return true;
        });



    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Gestion du geste de zoom ici
            noOtherAction = false;

            scaleFactor *= detector.getScaleFactor();
            // Limitez l'échelle minimale et maximale si nécessaire
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 10.0f));

            // Appliquez la transformation à la matrice
            Bitmap zoomedBitmapbitmap = getZoomedBitmap(scaleFactor, bitmap, 50f, 50f);
            pageView.setImageBitmap(zoomedBitmapbitmap);

            isZoomed = true;

            return true;
        }
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            noOtherAction = false;
            // Double clic détecté, effectuez le zoom à 50% centré sur le point de clic
            if (isZoomed) {
                matrix.setScale(1.0f, 1.0f);
                scaleFactor = 1.0f;
                pageView.setImageMatrix(matrix);
                isZoomed = false;
            } else {
                // Vous pouvez également implémenter le centrage du zoom ici en fonction de e.getX() et e.getY()
                matrix.setScale(0.5f, 0.5f, e.getX(), e.getY());
                scaleFactor = 0.5f;
                pageView.setImageMatrix(matrix);
                isZoomed = true;
            }
            //invalidate();
            return true;
        }
    }

    private void displayPage(){
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), page.getPageFile().getUri());
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
            onNewPage();
        }
    }

    private void onNewPage(){
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        saveMangaLastPage();
        saveAppLastManga();

        prevPage = page.getPrevPage();
        nextPage = page.getNextPage();
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
        if (prevPage != null){
            page = prevPage;
            Objects.requireNonNull(getSupportActionBar()).setTitle(page.getName());
            displayPage();
            onNewPage();
        }else{
            Toast.makeText(this, getString(R.string.premierePage), R.integer.tempsToast).show();
        }

    }

    private void goNextPage(){
        if (nextPage != null){
            page = nextPage;
            Objects.requireNonNull(getSupportActionBar()).setTitle(page.getName());
            displayPage();
            onNewPage();
        }else{
            Toast.makeText(this, getString(R.string.dernierePage), R.integer.tempsToast).show();
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
        editor.putString(mangaName + "lastChapitre", page.getChapitre().getName());
        editor.putString(mangaName + "lastPage", page.getName());
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

    private Bitmap getZoomedBitmap(float zoomScale, Bitmap bmp, float xPercentage, float yPercentage){

        if (bmp != null) {
            bmp.setDensity(Bitmap.DENSITY_NONE);

            //Set the default values in case of bad input
            zoomScale = (zoomScale < 0.0f || zoomScale > 10.0f) ? 2.0f : zoomScale;
            xPercentage = (xPercentage < 0.0f || xPercentage > 100.0f) ? 50.0f : xPercentage;
            yPercentage = (yPercentage < 0.0f || yPercentage > 100.0f) ? 50.0f : yPercentage;

            float originalWidth = bmp.getWidth();
            float originalHeight = bmp.getHeight();

            //Get the new sizes based on zoomScale
            float newWidth = originalWidth / zoomScale;
            float newHeight = originalHeight / zoomScale;

            //get the new X/Y positions based on x/yPercentage
            float newX = (originalWidth * xPercentage / 100) - (newWidth / 2);
            float newY = (originalHeight * yPercentage / 100) - (newHeight / 2);

            //Make sure the x/y values are not lower than 0
            newX = (newX < 0) ? 0 : newX;
            newY = (newY < 0) ? 0 : newY;

            //make sure the image does not go over the right edge
            while ((newX + newWidth) > originalWidth) {
                newX -= 2;
            }

            //make sure the image does not go over the bottom edge
            while ((newY + newHeight) > originalHeight) {
                newY -= 2;
            }

            return Bitmap.createBitmap(bmp, Math.round(newX), Math.round(newY), Math.round(newWidth), Math.round(newHeight));
        }

        return null;
    }
}

