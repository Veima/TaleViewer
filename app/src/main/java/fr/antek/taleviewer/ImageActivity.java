package fr.antek.taleviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.antek.taleviewer.databinding.ActivityImageBinding;

/**
 * ImageActivity is an Android activity class responsible for displaying and interacting with image
 * or PDF files within a story folder. It allows users to navigate through pages, zoom in and out,
 * and perform other actions based on user interactions.
 */
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
    private boolean updateInProgress = false;
    double newScrollOffset;

    /**
     * Constructor for ImageActivity. Initializes and configures the activity.
     * @param savedInstanceState The saved instance state, if any.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this activity
        fr.antek.taleviewer.databinding.ActivityImageBinding binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Retrieve essential data from intent extras and shared preferences
        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        // Checks if there is a saved instance state (e.g., during activity recreation) and retrieves the path
        if (savedInstanceState != null) {
            path = savedInstanceState.getString("path");
        }

        // Create and configure the settings object based on user preferences
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        settings = new Settings();
        settings.setSplitPage(memoire.getBoolean("switchSplit",false));
        settings.setFirstPageRight(memoire.getBoolean("switchFirstPage",true));
        settings.setFullBefore(memoire.getBoolean("switchFullBefore",false));
        settings.setFullBetween(memoire.getBoolean("switchFullBetween",false));
        settings.setFullAfter(memoire.getBoolean("switchFullAfter",false));
        settings.setOverlap(memoire.getInt("overlap",0));
        settings.setScroll(memoire.getBoolean("switchScroll",false));

        // Create a StoryLib instance to manage the story folder
        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        // Extract the image path and any parameters
        String imagePath = path.split("/", 3)[2];
        String[] pathSplit = imagePath.split(":");
        thisFile = storyLib.buildFromPath(pathSplit[0]);

        // Parse and apply any additional parameters to the file
        if (pathSplit.length >1){
            for (int i=1; i<pathSplit.length; i++){
                stringToParameter(pathSplit[i]);
            }
        }

        // Check if the specified file exists in the story folder
        if (thisFile == null){
            Toast.makeText(this, getString(R.string.fileNotFound), R.integer.tempsToast).show();
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else {
            thisPage = new Page(thisFile, this, splitStep, pageNumber);

            // Set the main content view and initialize UI components
            mContentView = binding.imageView;
            imageView = findViewById(R.id.imageView);
            currentOrientation = getResources().getConfiguration().orientation;

            Objects.requireNonNull(getSupportActionBar()).setTitle(title());

            // Initialize the bitmap for display (scroll or page mode)
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

            // Configure touch and gesture detectors for user interactions
            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
            gestureDetector = new GestureDetector(this, new DoubleTapListener());

            // Handle touch events on the image view
            imageView.setOnTouchListener((v, event) -> {

                // Process scaling and double-tap gestures
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Store the starting position for tracking user movements
                    currentXSlide = event.getX();
                    currentYSlide = event.getY();
                }

                if ((event.getAction() == MotionEvent.ACTION_MOVE) && (!currentAction.equals("zoom"))) {
                    // Handle image panning (scroll or page mode) based on user input
                    // Update the display and trigger any necessary actions

                    double x = event.getX();
                    double y = event.getY();
                    int moveX = Math.toIntExact(Math.round(currentXSlide - x));
                    int moveY = Math.toIntExact(Math.round(currentYSlide - y));
                    int width = imageView.getWidth();

                    if ((Math.sqrt(moveY * moveY + moveX * moveX) > (width / 20.0)) || currentAction.equals("move")) {
                        currentAction = "move";
                        if (settings.getScroll()) {
                            if (event.getPointerCount() == 1) {
                                if ((currentScale == 1.0f)) {
                                    scrollOffset = scrollOffset - (event.getY() - currentYSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                                    currentYSlide = event.getY();
                                    if (!updateInProgress) {
                                        updateInProgress = true;

                                        // Update the scroll bitmap in a separate thread
                                        ExecutorService executor = Executors.newSingleThreadExecutor();
                                        executor.execute(() -> {
                                            updateScrollBitmap();
                                            updateInProgress = false;
                                        });
                                    }
                                    displayBitmap();
                                    onNewPage();


                                } else {
                                    // Handle zooming and scrolling simultaneously
                                    scrollOffset = scrollOffset - (event.getY() - currentYSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                                    offsetX = offsetX - (event.getX() - currentXSlide) / currentScale / imageView.getWidth() * bitmap.getWidth();
                                    currentYSlide = event.getY();
                                    if (!updateInProgress) {
                                        updateInProgress = true;

                                        ExecutorService executor = Executors.newSingleThreadExecutor();
                                        executor.execute(() -> {
                                            updateScrollBitmap();
                                            updateInProgress = false;
                                        });
                                    }
                                    displayBitmap();
                                    onNewPage();

                                    currentXSlide = event.getX();

                                    Bitmap cropedBitmap = BitmapUtility.zoomBitmap(bitmapScroll, offsetX, scrollOffset, currentScale, contextThis, imageView);
                                    imageView.setImageBitmap(cropedBitmap);
                                }
                            }
                        }

                    }else {
                        if ((currentScale != 1.0f) && (event.getPointerCount() == 1)) {

                            offsetX = offsetX - (event.getX() - currentXSlide) / currentScale / imageView.getHeight() * bitmap.getHeight();
                            offsetY = offsetY - (event.getY() - currentYSlide) / currentScale / imageView.getWidth() * bitmap.getWidth();
                            Bitmap cropedBitmap = BitmapUtility.zoomBitmap(bitmap, offsetX, offsetY, currentScale, this, imageView);
                            imageView.setImageBitmap(cropedBitmap);
                            currentXSlide = event.getX();
                            currentYSlide = event.getY();
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    // Handle touch-up events, gestures, and page navigation
                    if (currentScale == 1.0f) {
                        double x = event.getX();
                        double y = event.getY();

                        int moveX = Math.toIntExact(Math.round(currentXSlide - x));
                        int moveY = Math.toIntExact(Math.round(currentYSlide - y));
                        int width = imageView.getWidth();

                        if (currentAction.equals("none")) {

                            int height = imageView.getHeight();

                            double relativeX = x / width;
                            double relativeY = y / height;

                            if ((relativeY < 0.15) || (relativeY > 0.85)) {
                                toggle();
                            } else if (!settings.getScroll()) {
                                if (relativeX < 0.5) {
                                    goPrevPage();
                                } else {
                                    goNextPage();
                                }
                            }
                        } else if ((currentAction.equals("move")) && (!settings.getScroll())) {
                            if (Math.sqrt(moveY * moveY + moveX * moveX) > (width / 4.0)) {
                                if (Math.abs(moveX) > Math.abs(moveY)) {
                                    // Detect horizontal swipe for page navigation
                                    if ((moveX > 0) && (moveX > width / 3)) {
                                        goNextPage();
                                    } else if ((moveX < 0) && (moveX < width / (-3))) {
                                        goPrevPage();
                                    }
                                }
                            }
                            currentAction = "none";
                        } else {
                            currentAction = "none";
                        }
                    }else {
                        currentAction = "none";
                    }
                }
                return true;
            });
            // Add an orientation change listener to handle screen rotation
            this.addOnOrientationChangeListener();
        }
    }

    /**
     * Called to save the current instance state of the activity. It stores the 'path' variable's value
     * in the provided Bundle to ensure that the state can be restored when needed.
     *
     * @param outState A Bundle in which the current instance state is saved.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", path);
    }

    /**
     * Create the options menu for the activity, dynamically adding items based on the current context.
     * @param menu The options menu in which items are placed.
     * @return `true` to display the menu, `false` to prevent it from being shown.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);

        String[] menuOption = path.split("/");

        // Add the 'Parameter' option to the menu
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.parameter));

        // Dynamically add items to the menu based on the folder structure
        for (int i = 10; i < menuOption.length+8; i++) {
            String itemName;
            if (i==10){
                itemName = getString(R.string.home);
            }else {
                itemName = menuOption[i-9];
            }
            menu.add(Menu.NONE, i, Menu.NONE, itemName);
        }

        // Add a 'Go to Page' option to the menu if the current file is a PDF
        if (thisFile instanceof PDF){
            menu.add(Menu.NONE, 9, Menu.NONE, getString(R.string.goToPage));
        }

        return true;
    }

    /**
     * Handle options menu item selection, triggering appropriate actions based on the selected menu item.
     * @param item The selected menu item.
     * @return `true` if the item selection was handled, `false` otherwise.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 0) {
            // Handle the 'Parameter' menu item
            Intent intentToParameterActivity = new Intent(ImageActivity.this, ParameterActivity.class);
            intentToParameterActivity.putExtra("activityAfter", "ImageActivity");
            intentToParameterActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToParameterActivity.putExtra("path", thisFile.getPath() + ":" + parameterToString());
            startActivity(intentToParameterActivity);
        }else if (itemId == 9) {
            // Handle the 'Go to Page' menu item
            pageSelectorDialog();
        }else if(itemId == 10){
            // Handle the 'Home' menu item
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else{
            // Handle dynamically added folder-specific menu items
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId -8; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            // Determine whether to navigate to a StoryActivity or DirectoryActivity
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

    /**
     * Adds an orientation change listener to the activity's window decor view.
     * This listener detects changes in the device's orientation and responds accordingly.
     */
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

    /**
     * Called when the focus of the activity's window changes, typically when the activity
     * gains or loses focus.
     * @param hasFocus `true` if the activity gains focus, `false` if it loses focus.
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (firstLoad){
                // If it's the first time loading the activity, perform initialization steps
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

    /**
     * A custom scale gesture listener to handle zooming actions on the image view.
     */
    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // Store the initial focus point of the gesture
            currentFocusX = detector.getFocusX();
            currentFocusY = detector.getFocusY();

            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Get the new focus point of the gesture
            double newFocusX = detector.getFocusX();
            double newFocusY = detector.getFocusY();

            currentAction = "zoom";

            double newScale = detector.getScaleFactor();

            currentScale = Math.max(1.0f, Math.min(currentScale*newScale, 10.0f));

            //handling zoom on without moving
            offsetX = ((offsetX-newFocusX)/newScale+newFocusX);
            offsetY = ((offsetY-newFocusY)/newScale+newFocusY);

            //handling focus movement on X
            offsetX = offsetX + ((currentFocusX - newFocusX)/currentScale);
            currentFocusX = newFocusX;


            Bitmap cropedBitmap;
            if (settings.getScroll()){
                //handling focus movement on Y
                scrollOffset = scrollOffset + ((currentFocusY - newFocusY))/currentScale;
                currentFocusY = newFocusY;

                cropedBitmap = BitmapUtility.zoomBitmap(bitmapScroll, offsetX, offsetY + scrollOffset, currentScale, contextThis, imageView);
                if (!updateInProgress){
                    updateInProgress = true;

                    // Asynchronously update the scroll bitmap
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        updateScrollBitmap();
                        updateInProgress = false;
                    });
                }
                displayBitmap();
                onNewPage();

            }else{
                //handling focus movement on Y
                offsetY = offsetY + ((currentFocusY - newFocusY)/currentScale);
                currentFocusY = newFocusY;

                cropedBitmap = BitmapUtility.zoomBitmap(bitmap, offsetX, offsetY, currentScale, contextThis, imageView);
            }

            imageView.setImageBitmap(cropedBitmap);


            return true;
        }
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * This method is triggered when a double-tap gesture is detected on the view.
         * @param e The MotionEvent associated with the double-tap gesture.
         * @return `true` to indicate that the double-tap gesture has been successfully handled.
         */
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

    /**
     * Displays the appropriate bitmap on the imageView based on the current viewing mode.
     * If the app is in scrolling mode, it adapts and displays the scrollable bitmap; otherwise, it displays a bitmap with the correct size and aspect ratio.
     */
    private void displayBitmap(){
        if (settings.getScroll()){
            if (bitmapScroll != null) {
                bitmap = BitmapUtility.adaptScrollView(bitmapScroll, imageView, scrollOffset);
                imageView.setImageBitmap(bitmap);
            }
        }else {
            if (bitmapToDisplay != null) {
                bitmap = BitmapUtility.correctSize(bitmapToDisplay, imageView.getWidth(), imageView.getHeight());
                bitmap = BitmapUtility.correctRatio(bitmap, imageView);
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    /**
     * Handles the transition to a new page within the current file.
     * Updates the page number, path, and ActionBar title to reflect the new page.
     * Saves the current page's information to memory for future reference.
     * Invalidates the options menu to update its state.
     */
    private void onNewPage(){
        pageNumber = thisPage.getPageNumber();
        path = thisFile.getPath();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title());
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        saveStoryLastPage();
        saveAppLastStory();

        invalidateOptionsMenu();
    }

    /**
     * Generates a composite bitmap for the scroll view.
     * Combines three separate bitmaps: one for the top section, one for the visible page content,
     * and one for the bottom section.
     * Calculates and sets the scroll offset to position the visible content correctly.
     * @return The composite bitmap for the scroll view.
     */
    public Bitmap generateScrollBitmap(){
        if (imageView != null){
            viewH = imageView.getHeight();

            createBitmapUp();

            createBitmapDown();
            thisBitmap = BitmapUtility.adaptWidth(thisPage.getBitmap(),imageView);
            centerH = thisBitmap.getHeight();
            scrollOffset = upH + (centerH - viewH)/2.0;

            if ((scrollOffset < upH - viewH/10.0 ) && (upH ==0)){
                scrollOffset = upH - viewH/10.0;
            }

            if ((viewH + scrollOffset > upH + centerH + downH + viewH/10.0) && (downH == 0)){
                scrollOffset = upH + centerH + downH - viewH + viewH/10.0;
            }

            return BitmapUtility.mergeBitmap(bitmapUp,thisBitmap,bitmapDown);
        }
        return null;
    }

    /**
     * Creates bitmaps for the top section of the scroll view and accumulates the total height (upH).
     * It generates bitmaps for the pages above the currently visible page until the top of the ImageView
     * is reached or there are no more pages to display.
     * The method collects the pages and their respective bitmaps in the 'pageUp' and 'bitmapUp' lists.
     */
    public void createBitmapUp(){
        upH = 0;
        bitmapUp = new ArrayList<>();
        pageUp = new ArrayList<>();
        Page page = thisPage;
        boolean end = false;
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

    /**
     * Creates bitmaps for the bottom section of the scroll view and accumulates the total height (downH).
     * It generates bitmaps for the pages below the currently visible page until the bottom of the ImageView
     * is reached or there are no more pages to display.
     * The method collects the pages and their respective bitmaps in the 'pageDown' and 'bitmapDown' lists.
     */
    private void createBitmapDown(){
        downH = 0;
        bitmapDown = new ArrayList<>();
        pageDown = new ArrayList<>();
        Page page = thisPage;
        boolean end = false;
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

    /**
     * Updates the scroll view bitmap based on the new scroll position (scrollOffset).
     * The method checks if the new scroll position reaches the top or bottom of the available pages
     * and adjusts the content accordingly. It reassembles the composite scroll view bitmap to display
     * the newly visible content.
     */
    public void updateScrollBitmap(){

        double oldScrollOffset = scrollOffset;
        newScrollOffset = scrollOffset;

        if ((newScrollOffset < upH - viewH/10.0) && (upH ==0)){
            newScrollOffset = upH - viewH/10.0;
        }

        if ((viewH + newScrollOffset > upH + centerH + downH + viewH/10.0) && (downH == 0)){
            newScrollOffset = upH + centerH + downH - viewH + viewH/10.0;
        }

        if (upH > newScrollOffset +viewH/2.0){
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

            bitmapScroll = BitmapUtility.mergeBitmap(bitmapUp,thisBitmap,bitmapDown);

        }else if (upH + centerH < newScrollOffset +viewH/2.0){
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

            bitmapScroll = BitmapUtility.mergeBitmap(bitmapUp,thisBitmap,bitmapDown);
        }

        scrollOffset = scrollOffset + (newScrollOffset - oldScrollOffset);
    }

    /**
     * Completes the content above the current scroll view by adding more pages and bitmaps.
     * This method ensures that the scroll view content extends upwards until it fills the view.
     */
    public void completeUp(){
        boolean end = false;
        if (pageUp.size() > 0){
            Page page = pageUp.get(pageUp.size()-1);
            while ((upH < viewH) && !end){

                page = page.getPrevPage();
                if (page == null){
                    end = true;
                }else {
                    Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                    upH = upH + prevBitmap.getHeight();

                    newScrollOffset = newScrollOffset + prevBitmap.getHeight();
                    pageUp.add(page);
                    bitmapUp.add(prevBitmap);
                }
            }
        }
        if (pageUp.size() == 0){
            Page page = thisPage.getPrevPage();
            Bitmap prevBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
            upH = upH + prevBitmap.getHeight();

            newScrollOffset = newScrollOffset + prevBitmap.getHeight();
            pageUp.add(page);
            bitmapUp.add(prevBitmap);

        }
    }

    /**
     * Completes the content below the current scroll view by adding more pages and bitmaps.
     * This method ensures that the scroll view content extends downwards until it fills the view.
     */
    public void completeDown(){
        boolean end = false;
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
        if (pageDown.size() == 0){
            Page page = thisPage.getNextPage();
            if (page != null){
                Bitmap nextBitmap = BitmapUtility.adaptWidth(page.getBitmap(), imageView);
                downH = downH + nextBitmap.getHeight();
                pageDown.add(page);
                bitmapDown.add(nextBitmap);
            }


        }
    }

    /**
     * Cuts the content above the current scroll view to optimize memory usage and performance.
     * This method removes the bitmaps and pages that are no longer visible above the scroll view.
     */
    public void cutUp() {
        ArrayList<Bitmap> oldBitmapUp = bitmapUp;
        ArrayList<Page> oldPageUp = pageUp;
        bitmapUp = new ArrayList<>();
        pageUp = new ArrayList<>();
        int i = 0;
        newScrollOffset = newScrollOffset - (upH + centerH);

        upH = 0;
        while ((upH < viewH) && (i < oldBitmapUp.size())) {
            bitmapUp.add(oldBitmapUp.get(i));
            pageUp.add(oldPageUp.get(i));
            upH = upH + oldBitmapUp.get(i).getHeight();

            i++;
        }
        newScrollOffset = newScrollOffset + upH;
    }

    /**
     * Cuts the content below the current scroll view to optimize memory usage and performance.
     * This method removes the bitmaps and pages that are no longer visible below the scroll view.
     */
    public void cutDown(){
        ArrayList<Bitmap> oldBitmapDown = bitmapDown;
        ArrayList<Page> oldPageDown = pageDown;
        bitmapDown = new ArrayList<>();
        pageDown = new ArrayList<>();
        int i = 0;
        downH = 0;
        while ((downH < viewH) && (i < oldBitmapDown.size())) {
            bitmapDown.add(oldBitmapDown.get(i));
            pageDown.add(oldPageDown.get(i));
            downH = downH + oldBitmapDown.get(i).getHeight();
            i++;
        }
    }

    /**
     * Navigates to the previous page in the current story or image file.
     * If the previous page exists, it updates the displayed content and page information.
     * If the next page belongs to the same file, it ensures that any resources are properly closed.
     */
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

    /**
     * Navigates to the next page in the current story or image file.
     * If the next page exists, it updates the displayed content and page information.
     * If the previous page belongs to the same file, it ensures that any resources are properly closed.
     */
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

    /**
     * Toggles the visibility of UI elements (e.g., status bar, action bar) for immersive mode.
     * If currently hidden, it shows the UI elements; if visible, it hides them.
     * After toggling, it delays a short time before updating the displayed bitmap.
     */
    private void toggle() {
        if (hide) {
            hide = false;
            show();
        } else {
            hide = true;
            hide();
        }
        // Delay a short time before updating the displayed bitmap to allow time for the UI elements to change.
        Handler handler = new Handler();
        handler.postDelayed(this::displayBitmap, 300);
    }

    /**
     * Hides the UI elements for immersive mode. This method hides the action bar first
     * and schedules a runnable to remove the status and navigation bar after a short delay.
     */
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

    /**
     * Shows the system bars and schedules a runnable to display UI elements after a short delay.
     */
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

    /**
     * A runnable that hides the system bars and provides an immersive user experience.
     */
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

    /**
     * A runnable that shows UI elements, such as the action bar.
     */
    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };

    /**
     * Saves the last viewed page of the story, including its path and parameters, in shared preferences.
     */
    private void saveStoryLastPage(){
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString(path.split("/")[2].split(":")[0] + "lastImage", thisFile.getPath() + ":" + parameterToString());
        editor.apply();
    }

    /**
     * Saves the information about the last viewed stories in the application's shared preferences.
     * It keeps track of the last three viewed stories.
     */
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

    /**
     * Generates the title for the current content being displayed.
     * @return A formatted title string containing information about the content, such as page number for PDFs
     *         or image position within a folder.
     */
    public String title(){
        if (thisFile instanceof Image){
            int imagePos = thisFile.getParentFile().getPos(thisFile)+1;
            return "(" + imagePos + "/" + thisFile.getParentFile().getListFile().size() + ") " + thisFile.getName();
        } else if (thisFile instanceof PDF) {
            int pageNum = thisPage.getPageNumber();
            int lastPage = ((PDF) thisFile).getPageCount();
            return "(" + pageNum + "/" + lastPage + ") " + thisFile.getName();
        }else{
            return thisFile.getName();
        }
    }

    /**
     * Displays a dialog for selecting a specific page within a PDF document.
     */
    private void pageSelectorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.askNumber) + " " + ((PDF) thisFile).getPageCount());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.validationButton), (dialog, which) -> {
            String inputText = input.getText().toString();
            try {
                int number = Integer.parseInt(inputText);
                if (number >= 1 && number <= ((PDF) thisFile).getPageCount()) {
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
                    Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + ((PDF) thisFile).getPageCount(), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + ((PDF) thisFile).getPageCount(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.cancelButton), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Converts the page and split step parameters to a string representation.
     * @return A string representing the page and split step parameters.
     */
    public String parameterToString(){
        return "nPage=" + thisPage.getPageNumber() + ":splitStep=" + thisPage.getSplitStep();
    }

    /**
     * Parses the parameter string to extract page and split step information and updates the corresponding properties.
     * @param parameter The parameter string to parse.
     */
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
