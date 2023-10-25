package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private String currentAction = "none";
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Bitmap bitmap;
    private Bitmap bitmapToDisplay;
    private Bitmap bitmapScroll;
    private double offsetX = 0f;
    private double offsetY = 0f;
    private double currentFocusX = 0f;
    private double currentFocusY = 0f;
    private double currentScale = 1.0f;
    private int currentOrientation;
    private float currentXSlide;
    private float currentYSlide;
    private boolean firstLoad = true;
    private Settings settings;
    private double scrollOffset = 0;
    private Page thisPage;
    private String splitStep= null;
    private int pageNumber = 1;
    private final ImageActivity thisActivity = this;
    private int upH;
    private int downH;
    private int centerH;
    private int viewH;
    private ArrayList<Bitmap> bitmapUp;
    private ArrayList<Page> pageUp;
    private ArrayList<Bitmap> bitmapDown;
    private ArrayList<Page> pageDown;
    private Bitmap thisBitmap;

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
                    currentXSlide = event.getX();
                    currentYSlide = event.getY();
                }

                if ((event.getAction() == MotionEvent.ACTION_MOVE) && (!currentAction.equals("zoom"))) {
                    currentAction = "move";
                    if (settings.getScroll()) {
                        if (event.getPointerCount() == 1) {
                            if ((currentScale == 1.0f)){
                                scrollOffset = scrollOffset - (event.getY() - currentYSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                                currentYSlide = event.getY();
                                updateScrollBitmap();
                                displayBitmap();
                            }else{
                                scrollOffset = scrollOffset - (event.getY() - currentYSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                                offsetX = offsetX - (event.getX() - currentXSlide) / currentScale / imageView.getWidth() * bitmap.getWidth();
                                currentYSlide = event.getY();
                                updateScrollBitmap();
                                currentXSlide = event.getX();

                                Bitmap cropedBitmap = BitmapUtility.zoomScrollBitmap(bitmapScroll, offsetX, scrollOffset, currentScale, contextThis, imageView);
                                imageView.setImageBitmap(cropedBitmap);
                            }

                        }

                    }else {
                        if ((currentScale != 1.0f) && (event.getPointerCount() == 1)) {
                            offsetX = offsetX - (event.getX() - currentXSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                            offsetY = offsetY - (event.getY() - currentYSlide) / currentScale / imageView.getWidth() * bitmap.getWidth();
                            Bitmap cropedBitmap = BitmapUtility.zoomBitmap(bitmap, offsetX, offsetY, currentScale, this);
                            imageView.setImageBitmap(cropedBitmap);
                            currentXSlide = event.getX();
                            currentYSlide = event.getY();
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (currentAction.equals("none")){
                        if (currentScale == 1.0f){
                            double x = event.getX();
                            double y = event.getY();

                            int moveX = Math.toIntExact(Math.round(currentXSlide - x));
                            int moveY = Math.toIntExact(Math.round(currentYSlide - y));
                            int width = imageView.getWidth();
                            if (Math.sqrt(moveY*moveY+moveX*moveX)>width/4){
                                if (Math.abs(moveX) > Math.abs(moveY)){
                                    if ((moveX > 0) && (moveX > width/3)){
                                        goNextPage();
                                    }else if ((moveX < 0) && (moveX < width/(-3))){
                                        goPrevPage();
                                    }
                                }
                            }else{
                                int height = imageView.getHeight();

                                double relativeX = x / width;
                                double relativeY = y / height;

                                if ((relativeY < 0.15) || (relativeY > 0.85)) {
                                    toggle();
                                }else if (!settings.getScroll()){
                                    if (relativeX < 0.5) {
                                        goPrevPage();
                                    }else{
                                        goNextPage();
                                    }
                                }
                            }
                        }
                    }else{
                        currentAction = "none";
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
                if (settings.getScroll()){
                    bitmapScroll=generateScrollBitmap();
                }
                displayBitmap();
                currentOrientation = getResources().getConfiguration().orientation;
            }
        });
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (firstLoad){
                onNewPage();
                if (settings.getScroll()) {
                    bitmapScroll = generateScrollBitmap();
                }else{
                    prevPage = thisPage.getPrevPage();
                    nextPage = thisPage.getNextPage();
                }
                displayBitmap();

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
            double newFocusX = detector.getFocusX();
            double newFocusY = detector.getFocusY();

            currentAction = "zoom";

            double newScale = detector.getScaleFactor();

            currentScale = Math.max(1.0f, Math.min(currentScale*newScale, 10.0f));



            Bitmap cropedBitmap;
            if (settings.getScroll()){

                offsetX = (offsetX/newScale-newFocusX/newScale+newFocusX);
                offsetY = (offsetY/newScale-newFocusY/newScale+newFocusY);

                offsetX = offsetX + ((currentFocusX - newFocusX)/currentScale);
                scrollOffset = scrollOffset + ((currentFocusY - newFocusY))/currentScale;

                currentFocusX = newFocusX;
                currentFocusY = newFocusY;

                //scrollOffset = Math.round(scrollOffset + (offsetY - oldOffsetY));

                Log.d("MOI", "offsetY: " + offsetY);
                Log.d("MOI", "scrollOffset: " + scrollOffset);
                Log.d("MOI", "dif: " + (scrollOffset - offsetY));
                Log.d("MOI", "===================================");
                cropedBitmap = BitmapUtility.zoomScrollBitmap(bitmapScroll, offsetX, offsetY + scrollOffset, currentScale, contextThis, imageView);
                updateScrollBitmap();
            }else{
                offsetX = (offsetX/newScale-newFocusX/newScale+newFocusX);
                offsetY = (offsetY/newScale-newFocusY/newScale+newFocusY);

                offsetX = offsetX + ((currentFocusX - newFocusX)/currentScale);
                offsetY = offsetY + ((currentFocusY - newFocusY)/currentScale);

                currentFocusX = newFocusX;
                currentFocusY = newFocusY;
                cropedBitmap = BitmapUtility.zoomBitmap(bitmap, offsetX, offsetY, currentScale, contextThis);
            }

            imageView.setImageBitmap(cropedBitmap);


            return true;
        }
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            currentAction = "doubleTap";
            if (currentScale != 1.0f) {
                currentScale = 1.0f;
                if (settings.getScroll()){
                    scrollOffset = scrollOffset - offsetY;
                }
                offsetX = 0f;
                offsetY = 0f;
                displayBitmap();
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
            viewH = imageView.getHeight();

            createBitmapUp();

            createBitmapDown();
            thisBitmap = BitmapUtility.adaptWidth(thisPage.getBitmap(),imageView);
            centerH = thisBitmap.getHeight();
            scrollOffset = upH + (centerH - viewH)/2;

            if ((scrollOffset < upH - viewH/10 ) && (upH ==0)){
                scrollOffset = upH - viewH/10;
            }

            if ((viewH + scrollOffset > upH + centerH + downH + viewH/10) && (downH == 0)){
                scrollOffset = upH + centerH + downH - viewH + viewH/10;
            }

            return mergeBitmap();
        }
        return null;
    }

    public void createBitmapUp(){
        upH = 0;
        bitmapUp = new ArrayList<Bitmap>();
        pageUp = new ArrayList<Page>();
        Page page = thisPage;
        Boolean end = false;
        while ((upH < viewH) && !end){
            page = page.getPrevPage();
            if (page == null){
                end = true;
            }else {
                Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                upH = upH + prevBitmap.getHeight();
                pageUp.add(page);
                bitmapUp.add(prevBitmap);
            }
        }
    }

    private void createBitmapDown(){
        downH = 0;
        bitmapDown = new ArrayList<Bitmap>();
        pageDown = new ArrayList<Page>();
        Page page = thisPage;
        Boolean end = false;
        while ((downH < viewH) && !end) {
            page = page.getNextPage();
            if (page == null){
                end = true;
            }else{
                Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                downH = downH + prevBitmap.getHeight();
                pageDown.add(page);
                bitmapDown.add(prevBitmap);
            }
        }
    }

    public void updateScrollBitmap(){

        if ((scrollOffset < upH - viewH/10 ) && (upH ==0)){
            scrollOffset = upH - viewH/10;
        }

        if ((viewH + scrollOffset > upH + centerH + downH + viewH/10) && (downH == 0)){
            scrollOffset = upH + centerH + downH - viewH + viewH/10;
        }

        if (upH > scrollOffset +viewH/2){
            Log.d("Moi", "prev");

            bitmapDown.add(0, thisBitmap);
            pageDown.add(0,thisPage);

            thisBitmap = bitmapUp.get(0);
            thisPage = pageUp.get(0);
            thisFile = thisPage.getParentFile();

            bitmapUp.remove(0);
            pageUp.remove(0);

            cutDown();

            centerH = thisBitmap.getHeight();
            upH = upH - centerH;
            completeUp();

            bitmapScroll = mergeBitmap();

            onNewPage();
        }else if (upH + centerH < scrollOffset +viewH/2){
            Log.d("Moi", "next");
            bitmapUp.add(0, thisBitmap);
            pageUp.add(0,thisPage);

            thisBitmap = bitmapDown.get(0);
            thisPage = pageDown.get(0);
            thisFile = thisPage.getParentFile();

            bitmapDown.remove(0);
            pageDown.remove(0);

            cutUp();

            centerH = thisBitmap.getHeight();
            downH = downH - centerH;
            completeDown();

            bitmapScroll = mergeBitmap();
            onNewPage();
        }
    }

    public void completeUp(){
        Boolean end = false;
        if (pageUp.size() > 0){
            Page page = pageUp.get(pageUp.size()-1);
            while ((upH < viewH) && !end){
                page = page.getPrevPage();
                if (page == null){
                    end = true;
                }else {
                    Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                    upH = upH + prevBitmap.getHeight();

                    scrollOffset = scrollOffset + prevBitmap.getHeight();
                    pageUp.add(page);
                    bitmapUp.add(prevBitmap);
                }
            }
        }
    }

    public void completeDown(){
        Boolean end = false;
        if (pageDown.size() > 0){
            Page page = pageDown.get(pageDown.size()-1);
            while ((downH < viewH) && !end){
                page = page.getNextPage();
                if (page == null){
                    end = true;
                }else {
                    Bitmap nextBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                    downH = downH + nextBitmap.getHeight();
                    pageDown.add(page);
                    bitmapDown.add(nextBitmap);
                }
            }
        }

    }

    public void cutUp() {

        ArrayList<Bitmap> oldBitmapUp = bitmapUp;
        ArrayList<Page> oldPageUp = pageUp;
        bitmapUp = new ArrayList<Bitmap>();
        pageUp = new ArrayList<Page>();
        int i = 0;
        scrollOffset = scrollOffset - (upH + centerH);

        upH = 0;
        while ((upH < viewH) && (i < oldBitmapUp.size())) {
            bitmapUp.add(oldBitmapUp.get(i));
            pageUp.add(oldPageUp.get(i));
            upH = upH + oldBitmapUp.get(i).getHeight();

            i++;
        }
        scrollOffset = scrollOffset + upH;
    }

    public void cutDown(){
        ArrayList<Bitmap> oldBitmapDown = bitmapDown;
        ArrayList<Page> oldPageDown = pageDown;
        bitmapDown = new ArrayList<Bitmap>();
        pageDown = new ArrayList<Page>();
        int i = 0;
        downH = 0;
        while ((downH < viewH) && (i < oldBitmapDown.size())) {
            bitmapDown.add(oldBitmapDown.get(i));
            pageDown.add(oldPageDown.get(i));
            downH = downH + oldBitmapDown.get(i).getHeight();
            i++;
        }
    }

    public Bitmap mergeBitmap(){
        ArrayList<Bitmap> bitmapAll = new ArrayList<Bitmap>();

        for (int i = bitmapUp.size() - 1; i >= 0; i--) {
            bitmapAll.add(bitmapUp.get(i));
        }
        bitmapAll.add(thisBitmap);

        bitmapAll.addAll(bitmapDown);
        return joinBitmapsVertically(bitmapAll);
    }

    public static Bitmap joinBitmapsVertically(ArrayList<Bitmap> bitmaps) {
        int totalWidth = 0;
        int totalHeight = 0;

        for (Bitmap bitmap : bitmaps) {
            if (bitmap.getWidth() > totalWidth) {
                totalWidth = bitmap.getWidth();
            }
            totalHeight += bitmap.getHeight();
        }

        Bitmap resultBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(resultBitmap);
        int y = 0;

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
                    if ((nextFile instanceof PDF) && (nextPage.getPageNumber() != thisPage.getPageNumber())) {
                        ((PDF) nextFile).closePage(nextPage.getPageNumber());
                    }
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
                    if ((prevFile instanceof PDF) && (prevPage.getPageNumber() != thisPage.getPageNumber())) {
                        ((PDF) prevFile).closePage(prevPage.getPageNumber());
                    }
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
                        if (settings.getScroll()){
                            bitmapScroll = generateScrollBitmap();
                        }else{
                            bitmapToDisplay = thisPage.getBitmap();
                            prevPage = thisPage.getPrevPage();
                            nextPage = thisPage.getNextPage();
                        }
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

    public void setOffsetX(double offsetX){
        this.offsetX = offsetX;
    }

    public void setOffsetY(double offsetY){
        this.offsetY = offsetY;
    }

    public Settings getSettings(){
        return settings;
    }

}
