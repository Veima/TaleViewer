package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import fr.antek.mangaviewer.databinding.ActivityImageBinding;
public class ImageActivity extends AppCompatActivity {
    private final Handler mHideHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private final ImageActivity contextThis = this;
    private View mContentView;
    private File thisFile;
    private Uri storyFolderUri;
    private String path;
    private ImageView imageView;
    private boolean hide = false;
    private SharedPreferences memoire;
    private Page prevPage;
    private Page nextPage;
    private Boolean noOtherAction = true;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Bitmap bitmap;
    private Bitmap bitmapRaw;
    private Bitmap bitmapToDisplay;
    private Bitmap bitmapScroll;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float currentFocusX = 0f;
    private float currentFocusY = 0f;
    private float currentScale = 1.0f;
    private int currentOrientation;
    private float currentXSlide;
    private float currentYSlide;
    private boolean firstLoad = true;
    private Settings settings;
    private String parameter;
    private float scrollOffset = 0;
    private Page thisPage;
    private String splitStep= null;
    private int pageNumber = 1;
    private final ImageActivity thisActivity = this;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fr.antek.mangaviewer.databinding.ActivityImageBinding binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);

        settings = new Settings();

        settings.setSplitPage(memoire.getBoolean("switchSplit",false));
        settings.setFirstPage(memoire.getBoolean("switchFirstPage",true));
        settings.setFullBefore(memoire.getBoolean("switchFullBefore",false));
        settings.setFullBetween(memoire.getBoolean("switchFullBetween",false));
        settings.setFullAfter(memoire.getBoolean("switchFullAfter",false));
        settings.setOverlap(memoire.getInt("overlap",0));
        settings.setScroll(memoire.getBoolean("switchScroll",false));

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        String imagePath = path.split("/", 3)[2];
        String[] pathSplit = imagePath.split(":");
        thisFile = storyLib.buildFromPath(pathSplit[0]);

        if (pathSplit.length >1){
            for (int i=1; i<pathSplit.length; i++){
                stringToParameter(pathSplit[i]);
            }
        }
        if (thisFile == null){
            Toast.makeText(this, getString(R.string.fileNotFound), R.integer.tempsToast).show();
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else {
            thisPage = new Page(thisFile, this, splitStep, pageNumber);

            mContentView = binding.imageView;
            imageView = findViewById(R.id.imageView);
            currentOrientation = getResources().getConfiguration().orientation;

            Objects.requireNonNull(getSupportActionBar()).setTitle(title());

            if (settings.getScroll()){
                bitmapScroll=generateScrollBitmap();
            }else{
                bitmapToDisplay=thisPage.getBitmap();
            }

            displayBitmap();

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
                    if (settings.getScroll()) {
                        scrollOffset = scrollOffset - (event.getX() - currentXSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                        currentXSlide = event.getX();
                        //update scroll
                    }else {
                        if ((noOtherAction) && (currentScale != 1.0f) && (event.getPointerCount() == 1)) {
                            offsetX = offsetX - (event.getX() - currentXSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                            offsetY = offsetY - (event.getY() - currentYSlide) / currentScale / imageView.getWidth() * bitmap.getWidth();
                            Bitmap cropedBitmap = BitmapUtility.cropAndCheck(bitmap, offsetX, offsetY, currentScale, this);
                            imageView.setImageBitmap(cropedBitmap);
                            currentXSlide = event.getX();
                            currentYSlide = event.getY();
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!settings.getScroll()){
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
                                goPrevPage();
                            } else {
                                goNextPage();
                            }
                        } else {
                            noOtherAction = true;
                        }
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

        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.parameter));

        for (int i = 10; i < menuOption.length+8; i++) {
            String itemName;
            if (i==10){
                itemName = getString(R.string.home);
            }else {
                itemName = menuOption[i-9];
            }
            menu.add(Menu.NONE, i, Menu.NONE, itemName);
        }
        if (thisFile instanceof PDF){
            menu.add(Menu.NONE, 9, Menu.NONE, getString(R.string.goToPage));
        }

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 0) {
            Intent intentToParameterActivity = new Intent(ImageActivity.this, ParameterActivity.class);
            intentToParameterActivity.putExtra("activityAfter", "ImageActivity");
            intentToParameterActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToParameterActivity.putExtra("path", thisFile.getPath() + ":" + parameterToString());
            startActivity(intentToParameterActivity);
        }else if (itemId == 9) {
            pageSelectorDialog();
        }else if(itemId == 10){
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else{
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId -8; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            if (itemId == 11) {
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
                displayBitmap();
                currentOrientation = getResources().getConfiguration().orientation;
            }
        });
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (firstLoad){
                prevPage = thisPage.getPrevPage();
                nextPage = thisPage.getNextPage();
                onNewPage();
                if (settings.getScroll()){
                    bitmap = BitmapUtility.adaptScrollView(bitmapScroll,imageView,scrollOffset);
                }else {
                    bitmap = BitmapUtility.correctSize(bitmapToDisplay, imageView);
                    bitmap = BitmapUtility.correctRatio(bitmap, imageView);
                }
                imageView.setImageBitmap(bitmap);
                firstLoad = false;
            }
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

            currentScale = Math.max(1.0f, Math.min(currentScale*newScale, 10.0f));

            offsetX = (offsetX/newScale-newFocusX/newScale+newFocusX);
            offsetY = (offsetY/newScale-newFocusY/newScale+newFocusY);

            offsetX = offsetX + ((currentFocusX - newFocusX)/currentScale)/imageView.getWidth()*bitmap.getWidth();
            offsetY = offsetY + ((currentFocusY - newFocusY)/currentScale)/imageView.getHeight()*bitmap.getHeight();

            currentFocusX = newFocusX;
            currentFocusY = newFocusY;

            Bitmap cropedBitmap = BitmapUtility.cropAndCheck(bitmap, offsetX, offsetY, currentScale, contextThis);

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

    private void displayBitmap(){
            if (settings.getScroll()){
                if (bitmapScroll != null) {
                    bitmap = BitmapUtility.adaptScrollView(bitmapScroll, imageView, scrollOffset);
                    imageView.setImageBitmap(bitmap);
                }
            }else {
                if (bitmapToDisplay != null) {
                    bitmap = BitmapUtility.correctSize(bitmapToDisplay, imageView);
                    bitmap = BitmapUtility.correctRatio(bitmap, imageView);
                    imageView.setImageBitmap(bitmap);
                }
            }

    }

    private void onNewPage(){
        pageNumber = thisPage.getPageNumber();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title());
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        saveStoryLastPage();
        saveAppLastStory();

        invalidateOptionsMenu();
    }

    public Bitmap generateScrollBitmap(){
        if (imageView != null){
            int viewH = imageView.getHeight();
            int upH = 0;
            ArrayList<Bitmap> bitmapUp = new ArrayList<Bitmap>();
            Page page = thisPage;
            while (upH < viewH){
                page = page.getPrevPage();
                Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(),imageView);
                upH = upH + prevBitmap.getHeight();
                bitmapUp.add(prevBitmap);
                Log.d("moi", "up: " + upH);
            }

            int downH = 0;
            ArrayList<Bitmap> bitmapDown = new ArrayList<Bitmap>();
            page = thisPage;
            while (downH < viewH){
                page = page.getPrevPage();
                Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(),imageView);
                downH = downH + prevBitmap.getHeight();
                bitmapDown.add(prevBitmap);
                Log.d("moi", "down: " + downH);
            }

            ArrayList<Bitmap> bitmapAll = new ArrayList<Bitmap>();

            for (int i = bitmapUp.size() - 1; i >= 0; i--) {
                bitmapAll.add(bitmapUp.get(i));
            }

            bitmapAll.add(thisPage.getBitmap());

            bitmapAll.addAll(bitmapDown);

            return joinBitmapsVertically(bitmapAll);
        }
        return null;
    }

    public static Bitmap joinBitmapsVertically(ArrayList<Bitmap> bitmaps) {
        int totalWidth = 0;
        int totalHeight = 0;

        // Calculer la largeur totale et la hauteur totale des bitmaps
        for (Bitmap bitmap : bitmaps) {
            if (bitmap.getWidth() > totalWidth) {
                totalWidth = bitmap.getWidth();
            }
            totalHeight += bitmap.getHeight();
        }

        // Créer une nouvelle bitmap avec les dimensions calculées
        Bitmap resultBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);

        // Créer un canvas pour dessiner sur la nouvelle bitmap
        Canvas canvas = new Canvas(resultBitmap);
        int y = 0;

        // Dessiner chaque bitmap sur la nouvelle bitmap, l'une en dessous de l'autre
        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, 0, y, null);
            y += bitmap.getHeight();
        }

        return resultBitmap;
    }



    private void goPrevPage(){
        if (prevPage == null){
            Toast.makeText(contextThis, getString(R.string.premiereImage), Toast.LENGTH_SHORT).show();
        }else{
            if (nextPage != null) {
                File nextFile = nextPage.getParentFile();
                if (nextFile.equals(thisFile)) {
                    /*
                    if ((nextFile instanceof PDF) && (nextPage.getPageNumber() != thisPage.getPageNumber())) {
                        ((PDF) nextFile).closePage(nextPage.getPageNumber());
                    }
                     */
                } else {
                    if (nextFile instanceof PDF) {
                        ((PDF) nextFile).closePage(nextPage.getPageNumber());
                        ((PDF) nextFile).close();
                    } else if (nextFile instanceof Image) {
                        ((Image) nextFile).close();
                    }
                }
            }

            nextPage = thisPage;
            thisPage = prevPage;
            thisFile = thisPage.getParentFile();
            bitmapToDisplay = thisPage.getBitmap();
            displayBitmap();
            onNewPage();
            prevPage = thisPage.getPrevPage();
        }
    }

    private void goNextPage(){
        if (nextPage == null){
            Toast.makeText(contextThis, getString(R.string.derniereImage), Toast.LENGTH_SHORT).show();
        }else{
            if (prevPage != null) {
                File prevFile = prevPage.getParentFile();
                if (prevFile.equals(thisFile)) {
                    /*
                    if ((prevFile instanceof PDF) && (prevPage.getPageNumber() != thisPage.getPageNumber())) {
                        ((PDF) prevFile).closePage(prevPage.getPageNumber());
                    }
                    */
                } else {
                    if (prevFile instanceof PDF) {
                        ((PDF) prevFile).closePage(prevPage.getPageNumber());
                        ((PDF) prevFile).close();
                    } else if (prevFile instanceof Image) {
                        ((Image) prevFile).close();
                    }
                }
            }

            prevPage = thisPage;
            thisPage = nextPage;
            thisFile = thisPage.getParentFile();
            bitmapToDisplay = thisPage.getBitmap();
            displayBitmap();
            onNewPage();
            nextPage = thisPage.getNextPage();
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
        handler.postDelayed(this::displayBitmap, 50);
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

    private void saveStoryLastPage(){
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString(path.split("/")[2].split(":")[0] + "lastImage", thisFile.getPath() + ":" + parameterToString());
        editor.apply();
    }

    private void saveAppLastStory(){
        String storyName = path.split("/")[2].split(":")[0];
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

    public String title(){
        if (thisFile instanceof Image){
            int imagePos = thisFile.getParentFile().getPos(thisFile)+1;
            return "(" + imagePos + "/" + thisFile.getParentFile().getListFile().size() + ") " + thisFile.getName();
        } else if (thisFile instanceof PDF) {
            int pageNum = thisPage.getPageNumber();
            int lastPage = ((PDF) thisFile).getPdfRenderer().getPageCount();
            return "(" + pageNum + "/" + lastPage + ") " + thisFile.getName();
        }else{
            return thisFile.getName();
        }
    }

    private void pageSelectorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.askNumber) + " " + ((PDF) thisFile).getPdfRenderer().getPageCount());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.validationButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                try {
                    int number = Integer.parseInt(inputText);
                    if (number >= 1 && number <= ((PDF) thisFile).getPdfRenderer().getPageCount()) {
                        pageNumber = number;
                        thisPage = new Page(thisFile,thisActivity,"firstPossible",pageNumber);
                        bitmapToDisplay = thisPage.getBitmap();
                        prevPage = thisPage.getPrevPage();
                        nextPage = thisPage.getNextPage();
                        displayBitmap();
                        onNewPage();
                    } else {
                        Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + ((PDF) thisFile).getPdfRenderer().getPageCount(), Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + ((PDF) thisFile).getPdfRenderer().getPageCount(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public String parameterToString(){
        return "nPage=" + thisPage.getPageNumber() + ":splitStep=" + thisPage.getSplitStep();
    }

    public void stringToParameter(String parameter){
        String[] paraSplit = parameter.split("=");
        if (paraSplit[0].equals("nPage")){
            pageNumber = Integer.parseInt(paraSplit[1]);
        }else if (paraSplit[0].equals("splitStep")){
            splitStep = paraSplit[1];
        }
    }

    public void setOffsetX(float offsetX){
        this.offsetX = offsetX;
    }

    public void setOffsetY(float offsetY){
        this.offsetY = offsetY;
    }

    public Settings getSettings(){
        return settings;
    }

}