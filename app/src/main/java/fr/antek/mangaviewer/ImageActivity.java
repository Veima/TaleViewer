package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

import fr.antek.mangaviewer.databinding.ActivityImageBinding;
public class ImageActivity extends AppCompatActivity {
    private final Handler mHideHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private View mContentView;
    private Image thisImage;
    private Uri storyFolderUri;
    private String path;
    private ImageView imageView;
    private boolean hide = false;
    private SharedPreferences memoire;
    private Image prevImage;
    private Image nextImage;
    private Boolean noOtherAction = true;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Bitmap bitmap;
    private Bitmap bitmapRaw;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float currentFocusX = 0f;
    private float currentFocusY = 0f;
    private float currentScale = 1.0f;
    private int currentOrientation;
    private float currentXSlide;
    private float currentYSlide;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fr.antek.mangaviewer.databinding.ActivityImageBinding binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        thisImage = (Image) storyLib.buildFromPath(path.split("/", 3)[2]);
        if (thisImage == null){
            Toast.makeText(this, getString(R.string.fileNotFound), R.integer.tempsToast).show();
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(thisImage.getName());


            mContentView = binding.imageView;
            imageView = findViewById(R.id.imageView);

            displayImage();
            currentOrientation = getResources().getConfiguration().orientation;

            if (hide) {
                hide();
            } else {
                show();
            }

            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
            gestureDetector = new GestureDetector(this, new DoubleTapListener());

            imageView.setOnTouchListener((v, event) -> {
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (currentScale != 1.0f) {
                        currentXSlide = event.getX();
                        currentYSlide = event.getY();
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if ((noOtherAction) && (currentScale != 1.0f) && (event.getPointerCount() == 1)) {
                        Bitmap cropedBitmap = BitmapUtility.moveBitmap(bitmap, offsetX,  offsetY,event.getX() - currentXSlide, event.getY() - currentYSlide, currentScale);
                        imageView.setImageBitmap(cropedBitmap);
                        currentXSlide = event.getX();
                        currentYSlide = event.getY();
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if ((noOtherAction) && (currentScale == 1.0f)) {
                        float x = event.getX();
                        float y = event.getY();

                        int width = imageView.getWidth();
                        int height = imageView.getHeight();

                        float relativeX = x / width;
                        float relativeY = y / height;

                        if ((relativeY < 0.15) || (relativeY > 0.85)) {
                            toggle();
                        } else if (relativeX < 0.5) {
                            goPrevImage();
                        } else {
                            goNextImage();
                        }
                    } else {
                        noOtherAction = true;
                    }
                }
                return true;

            });

            this.addOnOrientationChangeListener();
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);

        String[] menuOption = path.split("/");

        for (int i = 1; i < menuOption.length-1; i++) {
            String itemName;
            if (i==1){
                itemName = getString(R.string.home);
            }else {
                itemName = menuOption[i];
            }
            menu.add(Menu.NONE, i, Menu.NONE, itemName);
        }

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 1) {
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        } else {
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId + 1; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            if (itemId == 2) {
                Intent intentToStoryActivity = new Intent(ImageActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToStoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToStoryActivity);
            } else {
                Intent intentToDirectoryActivity = new Intent(ImageActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToDirectoryActivity);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addOnOrientationChangeListener() {
        this.getWindow().getDecorView().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if  (currentOrientation != getResources().getConfiguration().orientation){
                displayImage();
                currentOrientation = getResources().getConfiguration().orientation;
            }
        });
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            onNewImage();
            bitmap = BitmapUtility.adaptBitmap2View(bitmapRaw,imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            currentFocusX = detector.getFocusX();
            currentFocusY = detector.getFocusY();
            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float newFocusX = detector.getFocusX();
            float newFocusY = detector.getFocusY();

            noOtherAction = false;

            float newScale = detector.getScaleFactor();
            Bitmap cropedBitmap = BitmapUtility.cropBitmap(bitmap, currentScale, newScale, offsetX, offsetY, currentFocusX, newFocusX, currentFocusY, newFocusY);

            imageView.setImageBitmap(cropedBitmap);


            return true;
        }
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            noOtherAction = false;
            if (currentScale != 1.0f) {
                currentScale = 1.0f;
                offsetX = 0f;
                offsetY = 0f;
                imageView.setImageBitmap(bitmap);
            }
            return true;
        }
    }



    private void displayImage(){
        try {
            bitmapRaw = MediaStore.Images.Media.getBitmap(this.getContentResolver(), thisImage.getUri());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (bitmapRaw != null) {
            bitmap = BitmapUtility.adaptBitmap2View(bitmapRaw, imageView);
            imageView.setImageBitmap(bitmap);
        }

    }


    private void onNewImage(){
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        saveStoryLastImage();
        saveAppLastStory();
        path = thisImage.getPath();
        invalidateOptionsMenu();
        prevImage = (Image) thisImage.getPrev();
        nextImage = (Image) thisImage.getNext();
    }



    private void goPrevImage(){
        if (prevImage != null){
            thisImage = prevImage;

            Objects.requireNonNull(getSupportActionBar()).setTitle(thisImage.getName());
            displayImage();
            onNewImage();
        }else{
            Toast.makeText(this,getString(R.string.premiereImage) , R.integer.tempsToast).show();
        }

    }

    private void goNextImage(){
        if (nextImage != null){
            thisImage = nextImage;
            Objects.requireNonNull(getSupportActionBar()).setTitle(thisImage.getName());
            displayImage();
            onNewImage();
        }else{
            Toast.makeText(this, getString(R.string.derniereImage), R.integer.tempsToast).show();
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
        Handler handler = new Handler();
        handler.postDelayed(this::displayImage, 50);
    }




    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 1);
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
        mHideHandler.postDelayed(mShowPart2Runnable, 1);
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

    private void saveStoryLastImage(){
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString(path.split("/")[2] + "lastImage", thisImage.getPath());
        editor.apply();
    }

    private void saveAppLastStory(){
        String storyName = path.split("/")[2];
        String nameUltimeStory = memoire.getString("nameUltimeStory", null);
        String namePenultiemeStory = memoire.getString("namePenultiemeStory", null);
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString("nameUltimeStory", storyName);
        if (nameUltimeStory != null){
            if (! nameUltimeStory.equals(storyName)){
                editor.putString("namePenultiemeStory", nameUltimeStory);
                if (namePenultiemeStory != null) {
                    if (!namePenultiemeStory.equals(storyName)) {
                        editor.putString("nameAntepenultiemeStory", namePenultiemeStory);
                    }
                }
            }
        }
        editor.apply();
    }

}