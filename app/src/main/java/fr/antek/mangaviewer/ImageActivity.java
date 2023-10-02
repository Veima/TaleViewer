package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
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
import java.util.Objects;

import fr.antek.mangaviewer.databinding.ActivityImageBinding;
public class ImageActivity extends AppCompatActivity {
    private final Handler mHideHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private View mContentView;
    private File thisFile;
    private Uri storyFolderUri;
    private String path;
    private ImageView imageView;
    private boolean hide = false;
    private SharedPreferences memoire;
    private File prevFile;
    private File nextFile;
    private Boolean noOtherAction = true;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Bitmap bitmap;
    private Bitmap bitmapRaw;
    private Bitmap bitmapToDisplay;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float currentFocusX = 0f;
    private float currentFocusY = 0f;
    private float currentScale = 1.0f;
    private int currentOrientation;
    private float currentXSlide;
    private float currentYSlide;
    private boolean firstLoad = true;
    private boolean splitPage;
    private boolean firstPage;
    private boolean fullBefore;
    private boolean fullBetween;
    private boolean fullAfter;
    private int overlap;
    private String parameter;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page pdfPage;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fr.antek.mangaviewer.databinding.ActivityImageBinding binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        splitPage = memoire.getBoolean("switchSplit",false);
        firstPage = memoire.getBoolean("switchFirstPage",true);
        fullBefore = memoire.getBoolean("switchFullBefore",false);
        fullBetween = memoire.getBoolean("switchFullBetween",false);
        fullAfter = memoire.getBoolean("switchFullAfter",false);
        overlap = memoire.getInt("overlap",0);

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        String imagePath = path.split("/", 3)[2];
        thisFile = storyLib.buildFromPath(imagePath.split(":")[0]);

        if (imagePath.split(":").length >1){
            parameter = imagePath.split(":")[1];
        }else{
            parameter="";
        }
        if (thisFile == null){
            Toast.makeText(this, getString(R.string.fileNotFound), R.integer.tempsToast).show();
            Intent intentToMain = new Intent(ImageActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }else {

            mContentView = binding.imageView;
            imageView = findViewById(R.id.imageView);

            if (thisFile instanceof Image){
                openImage();
            } else if (thisFile instanceof PDF) {
                openPDF();
                openPDFPage();
            }
            Objects.requireNonNull(getSupportActionBar()).setTitle(title());
            if ((bitmapRaw.getWidth()>bitmapRaw.getHeight()) && splitPage) {
                if (parameter.equals("fullFirst")) {
                    bitmapToDisplay = bitmapRaw;
                } else if (parameter.equals("halfFirst")) {
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw, firstPage, overlap);
                } else if (parameter.equals("fullBetween")) {
                    bitmapToDisplay = bitmapRaw;
                } else if (parameter.equals("halfLast")) {
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw, !firstPage, overlap);
                } else if (parameter.equals("fullLast")) {
                    bitmapToDisplay = bitmapRaw;
                }else{
                    if (fullBefore){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullFirst";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw, firstPage, overlap);
                        parameter = "halfFirst";
                    }

                }
            }else{
                bitmapToDisplay = bitmapRaw;
            }

            displayBitmap();
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
                            goPrevPage();
                        } else {
                            goNextPage();
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
            intentToParameterActivity.putExtra("path", thisFile.getPath() + ":" + parameter);
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
                prevFile = thisFile.getPrev();
                nextFile = thisFile.getNext();
                onNewPage();
                bitmap = BitmapUtility.correctSize(bitmapToDisplay, imageView);
                bitmap = BitmapUtility.correctRatio(bitmap, imageView);
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

            offsetX = (offsetX-newFocusX)/newScale+newFocusX;
            offsetY = (offsetY-newFocusY)/newScale+newFocusY;

            offsetX = offsetX + (currentFocusX - newFocusX);
            offsetY = offsetY + (currentFocusY - newFocusY);
            currentFocusX = newFocusX;
            currentFocusY = newFocusY;

            Bitmap cropedBitmap = BitmapUtility.cropAndCheck(bitmap, offsetX, offsetY, currentScale);

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



    private void openImage(){
        try {
            bitmapRaw = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ((Image) thisFile).getUri());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayBitmap(){

        if (bitmapToDisplay != null) {
            bitmap = BitmapUtility.correctSize(bitmapToDisplay, imageView);
            bitmap = BitmapUtility.correctRatio(bitmap, imageView);

            imageView.setImageBitmap(bitmap);
        }

    }

    private void openPDF(){
        if (parameter == ""){
            parameter = "1";
        }
        try {
            ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(((PDF) thisFile).getUri(), "r");
            pdfRenderer = new PdfRenderer(fileDescriptor);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openPDFPage(){
        pdfPage = pdfRenderer.openPage(Integer.parseInt(parameter)-1);

        bitmapRaw = Bitmap.createBitmap(pdfPage.getWidth()*4, pdfPage.getHeight()*4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapRaw);
        canvas.drawColor(Color.WHITE);

        pdfPage.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        bitmapToDisplay = bitmapRaw;


    }

    private void onNewPage(){
        Objects.requireNonNull(getSupportActionBar()).setTitle(title());
        memoire = this.getSharedPreferences("memoire", MODE_PRIVATE);
        saveStoryLastPage();
        saveAppLastStory();

        invalidateOptionsMenu();
    }

    private void goPrevPage(){
        if (thisFile instanceof Image){
            if ((bitmapRaw.getWidth()>bitmapRaw.getHeight()) && splitPage){
                if (parameter.equals("fullFirst")){
                    goPrevFile();
                }else if(parameter.equals("halfFirst")) {
                    if (fullBefore){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullFirst";
                    }else{
                        goPrevFile();
                    }
                }else if(parameter.equals("fullBetween")) {
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,firstPage,overlap);
                    parameter = "halfFirst";
                }else if(parameter.equals("halfLast")) {
                    if (fullBetween){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullBetween";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,firstPage,overlap);
                        parameter = "halfFirst";
                    }
                }else if(parameter.equals("fullLast")) {
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,!firstPage,overlap);
                    parameter = "halfLast";
                }else{
                    if (fullAfter){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullLast";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,!firstPage,overlap);
                        parameter = "halfLast";
                    }
                }
                displayBitmap();
            }else{
                goPrevFile();
            }
        }else if(thisFile instanceof PDF){
            if (Integer.parseInt(parameter) == 1){
                if (goPrevFile()){
                    pdfPage.close();
                    pdfRenderer.close();
                }
            }else{
                parameter=Integer.toString(Integer.parseInt(parameter)-1);
                pdfPage.close();
                openPDFPage();
                displayBitmap();
            }
        }
        onNewPage();
    }

    private void goNextPage(){
        if (thisFile instanceof Image){
            if ((bitmapRaw.getWidth()>bitmapRaw.getHeight()) && splitPage){
                if (parameter.equals("fullFirst")){
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,firstPage,overlap);
                    parameter = "halfFirst";
                }else if(parameter.equals("halfFirst")) {
                    if (fullBetween){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullBetween";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,!firstPage,overlap);
                        parameter = "halfLast";
                    }
                }else if(parameter.equals("fullBetween")) {
                    bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,!firstPage,overlap);
                    parameter = "halfLast";
                }else if(parameter.equals("halfLast")) {
                    if (fullAfter){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullLast";
                    }else{
                        goNextFile();
                    }
                }else if(parameter.equals("fullLast")) {
                    goNextFile();
                }else{
                    if (fullBefore) {
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullFirst";
                    } else {
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw, firstPage, overlap);
                        parameter = "halfFirst";
                    }
                }
                displayBitmap();
            }else {
                goNextFile();
            }
        }else if(thisFile instanceof PDF){
            if (Integer.parseInt(parameter) >= pdfRenderer.getPageCount()){
                if (goNextFile()){
                    pdfPage.close();
                    pdfRenderer.close();
                }
            }else{
                parameter=Integer.toString(Integer.parseInt(parameter)+1);
                pdfPage.close();
                openPDFPage();
                displayBitmap();
            }
        }
        onNewPage();

    }



    private boolean goPrevFile(){
        if (prevFile != null){
            parameter = "";
            nextFile = thisFile;
            thisFile = prevFile;
            path = thisFile.getPath();

            if (thisFile instanceof Image){
                openImage();
                if ((bitmapRaw.getWidth()>bitmapRaw.getHeight()) && splitPage){
                    if (fullAfter){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullLast";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,!firstPage,overlap);
                        parameter = "halfLast";
                    }
                }else{
                    bitmapToDisplay = bitmapRaw;
                }
            } else if (thisFile instanceof PDF) {
                openPDF();
                parameter = Integer.toString(pdfRenderer.getPageCount());
                openPDFPage();
            }

            displayBitmap();
            prevFile = thisFile.getPrev();
            return true;
        }else{
            Toast.makeText(this,getString(R.string.premiereImage) , R.integer.tempsToast).show();
            return false;
        }

    }

    private boolean goNextFile(){
        if (nextFile != null){
            parameter = "";
            prevFile = thisFile;
            thisFile = nextFile;
            path = thisFile.getPath();

            if (thisFile instanceof Image){
                openImage();
                if ((bitmapRaw.getWidth()>bitmapRaw.getHeight()) && splitPage){
                    if (fullBefore){
                        bitmapToDisplay = bitmapRaw;
                        parameter = "fullFirst";
                    }else{
                        bitmapToDisplay = BitmapUtility.splitPage(bitmapRaw,firstPage,overlap);
                        parameter = "halfFirst";
                    }
                }else{
                    bitmapToDisplay = bitmapRaw;
                }
            } else if (thisFile instanceof PDF) {
                openPDF();
                openPDFPage();
            }

            displayBitmap();
            nextFile = thisFile.getNext();
            return true;
        }else{
            Toast.makeText(this, getString(R.string.derniereImage), R.integer.tempsToast).show();
            return false;
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
        handler.postDelayed(this::openImage, 50);
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
        editor.putString(path.split("/")[2].split(":")[0] + "lastImage", thisFile.getPath() + ":" + parameter);
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
            int pageNum = Integer.parseInt(parameter);
            int lastPage = pdfRenderer.getPageCount();
            return "(" + pageNum + "/" + lastPage + ") " + thisFile.getName();
        }else{
            return thisFile.getName();
        }
    }

    private void pageSelectorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.askNumber) + " " + pdfRenderer.getPageCount());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.validationButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                try {
                    int number = Integer.parseInt(inputText);
                    if (number >= 1 && number <= pdfRenderer.getPageCount()) {
                        parameter = Integer.toString(number);
                        pdfPage.close();
                        openPDFPage();
                        displayBitmap();
                        onNewPage();
                    } else {
                        Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + pdfRenderer.getPageCount(), Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(ImageActivity.this, getString(R.string.errorInvalidNumber) + " " + pdfRenderer.getPageCount(), Toast.LENGTH_SHORT).show();
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

}