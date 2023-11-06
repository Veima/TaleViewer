package fr.antek.historyviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryActivity extends AppCompatActivity {
    private TextView textContinueStoryLine1;
    private TextView textContinueStoryLine2;
    private TextView textContinueStoryLine3;
    private TextView textContinueStoryLine4;
    private TextView textContinueStoryLine5;
    private Button buttonContinueStory;
    private Uri storyFolderUri;
    private String path;
    private SharedPreferences memoire;
    private Directory thisStory;
    private ArrayList<File> listFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);

        ListView listViewStory = findViewById(R.id.listViewFile);

        buttonContinueStory = findViewById(R.id.buttonContinuStory);
        textContinueStoryLine1 = findViewById(R.id.textContinueStoryLine1);
        textContinueStoryLine2 = findViewById(R.id.textContinueStoryLine2);
        textContinueStoryLine3 = findViewById(R.id.textContinueStoryLine3);
        textContinueStoryLine4 = findViewById(R.id.textContinueStoryLine4);
        textContinueStoryLine5 = findViewById(R.id.textContinueStoryLine5);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        recupLastStory();

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        thisStory = (Directory) storyLib.buildFromPath(path.split("/", 3)[2]);

        Objects.requireNonNull(getSupportActionBar()).setTitle(thisStory.getName());
        listFile = thisStory.getListFile();

        ListAdapter adapter = new FileAdapter(this, R.layout.item_file, listFile);
        listViewStory.setAdapter(adapter);
        listViewStory.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = thisStory.getFileWithPos(position);
            if (selectedFile instanceof Directory) {
                Intent intentToDirectoryActivity = new Intent(StoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path",selectedFile.getPath());
                startActivity(intentToDirectoryActivity);
            }else if((selectedFile instanceof Image) || (selectedFile instanceof PDF)){
                Intent intentToImageActivity = new Intent(StoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(this::chargeMiniature);

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.parameter));
        menu.add(Menu.NONE, 10, Menu.NONE, getString(R.string.home));
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 0) {
            Intent intentToParameterActivity = new Intent(StoryActivity.this, ParameterActivity.class);
            intentToParameterActivity.putExtra("activityAfter", "StoryActivity");
            intentToParameterActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToParameterActivity.putExtra("path", path);
            startActivity(intentToParameterActivity);
        }else if(itemId == 10){
            Intent intentToMain = new Intent(StoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
        }

        return super.onOptionsItemSelected(item);
    }

    private void recupLastStory(){
        String storyName = path.split("/")[2];
        String pathLastImage = memoire.getString(storyName + "lastImage", null);
        if (pathLastImage != null){
            String[] splitedPath = splitPath(pathLastImage.split(":")[0]);
            if (splitedPath[0] != null){
                textContinueStoryLine1.setText(splitedPath[0]);
                textContinueStoryLine1.setVisibility(View.VISIBLE);
            }
            if (splitedPath[1] != null){
                textContinueStoryLine2.setText(splitedPath[1]);
                textContinueStoryLine2.setVisibility(View.VISIBLE);
            }
            if (splitedPath[2] != null){
                textContinueStoryLine3.setText(splitedPath[2]);
                textContinueStoryLine3.setVisibility(View.VISIBLE);
            }
            if (splitedPath[3] != null){
                textContinueStoryLine4.setText(splitedPath[3]);
                textContinueStoryLine4.setVisibility(View.VISIBLE);
            }
            if (splitedPath[4] != null){
                textContinueStoryLine5.setText(splitedPath[4]);
                textContinueStoryLine5.setVisibility(View.VISIBLE);
            }

            String textButton = getString(R.string.buttonContinueText) + " " + storyName;
            buttonContinueStory.setText(textButton);

            buttonContinueStory.setOnClickListener(v -> {
                Intent intentToImageActivity = new Intent(StoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", pathLastImage);
                startActivity(intentToImageActivity);
            });
        }else{
            buttonContinueStory.setVisibility(View.GONE);
        }
    }

    public void chargeMiniature(){
        ArrayList<Thread> listThread = new ArrayList<>();
        for (int i=0; i< listFile.size(); i++) {

            int finalI = i;
            Thread thread = new Thread(() -> {
                File file =  listFile.get(finalI);
                if(file instanceof Image) {
                    Bitmap bitmapRaw;
                    try {
                        bitmapRaw = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ((Image) file).getUri());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (bitmapRaw != null) {
                        Bitmap bitmap = BitmapUtility.correctSize(bitmapRaw, 512, 512);
                        ((Image) file).setMiniature(bitmap);
                    }
                }else if(file instanceof PDF) {
                    Bitmap bitmapRaw;
                    try {
                        ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(((PDF) file).getUri(), "r");
                        PdfRenderer pdfRenderer = new PdfRenderer(Objects.requireNonNull(fileDescriptor));
                        PdfRenderer.Page pdfPage = pdfRenderer.openPage(0);

                        bitmapRaw = Bitmap.createBitmap(pdfPage.getWidth(), pdfPage.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmapRaw);
                        canvas.drawColor(Color.WHITE);
                        pdfPage.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        pdfPage.close();
                        pdfRenderer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (bitmapRaw != null) {
                        Bitmap bitmap = BitmapUtility.correctSize(bitmapRaw, 512, 512);
                        ((PDF) file).setMiniature(bitmap);
                    }
                }
            });
            listThread.add(thread);
            thread.start();

        }
        for (Thread thread : listThread) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] splitPath(String path){
        String[] pathPart = path.split("/");
        int pathSize = pathPart.length;
        String line1 =null;
        if (pathSize>=4){
            line1 = pathPart[3];
        }

        String line2 =null;
        if (pathSize>=5){
            line2 = pathPart[4];
        }

        String line3 =null;
        if (pathSize>8){
            line3 = "...";
        } else if (pathSize>=6){
            line3 = pathPart[5];
        }

        String line4 =null;
        if (pathSize>=7){
            line4 = pathPart[pathSize-2];
        }

        String line5 =null;
        if (pathSize>=8){
            line5 = pathPart[pathSize-1];
        }
        return new String[]{line1,line2,line3,line4,line5};
    }
}