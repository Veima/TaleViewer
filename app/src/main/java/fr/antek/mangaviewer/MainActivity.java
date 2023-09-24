package fr.antek.mangaviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private Button buttonContinueUltime;
    private Button buttonContinuePenultieme;
    private Button buttonContinueAntepenultieme;
    private TextView textContinueUltimeLine1;
    private TextView textContinueUltimeLine2;
    private TextView textContinueUltimeLine3;
    private TextView textContinueUltimeLine4;
    private TextView textContinueUltimeLine5;
    private TextView textContinuePenultiemeLine1;
    private TextView textContinuePenultiemeLine2;
    private TextView textContinuePenultiemeLine3;
    private TextView textContinuePenultiemeLine4;
    private TextView textContinuePenultiemeLine5;
    private TextView textContinueAntepenultiemeLine1;
    private TextView textContinueAntepenultiemeLine2;
    private TextView textContinueAntepenultiemeLine3;
    private TextView textContinueAntepenultiemeLine4;
    private TextView textContinueAntepenultiemeLine5;
    private ListView listViewStory;
    private Uri storyFolderUri = null;
    private SharedPreferences memoire;
    private StoryLib storyLib;
    private ArrayList<File> listStory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonContinueUltime = findViewById(R.id.buttonContinueUltime);
        buttonContinuePenultieme = findViewById(R.id.buttonContinuePenultieme);
        buttonContinueAntepenultieme = findViewById(R.id.buttonContinueAntepenultieme);

        textContinueUltimeLine1 = findViewById(R.id.textContinueUltimeLine1);
        textContinueUltimeLine2 = findViewById(R.id.textContinueUltimeLine2);
        textContinueUltimeLine3 = findViewById(R.id.textContinueUltimeLine3);
        textContinueUltimeLine4 = findViewById(R.id.textContinueUltimeLine4);
        textContinueUltimeLine5 = findViewById(R.id.textContinueUltimeLine5);

        textContinuePenultiemeLine1 = findViewById(R.id.textContinuePenultiemeLine1);
        textContinuePenultiemeLine2 = findViewById(R.id.textContinuePenultiemeLine2);
        textContinuePenultiemeLine3 = findViewById(R.id.textContinuePenultiemeLine3);
        textContinuePenultiemeLine4 = findViewById(R.id.textContinuePenultiemeLine4);
        textContinuePenultiemeLine5 = findViewById(R.id.textContinuePenultiemeLine5);

        textContinueAntepenultiemeLine1 = findViewById(R.id.textContinueAntepenultiemeLine1);
        textContinueAntepenultiemeLine2 = findViewById(R.id.textContinueAntepenultiemeLine2);
        textContinueAntepenultiemeLine3 = findViewById(R.id.textContinueAntepenultiemeLine3);
        textContinueAntepenultiemeLine4 = findViewById(R.id.textContinueAntepenultiemeLine4);
        textContinueAntepenultiemeLine5 = findViewById(R.id.textContinueAntepenultiemeLine5);

        Button buttonCherche = findViewById(R.id.buttonCherche);
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        listViewStory = findViewById(R.id.listViewStory);


        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);
        recupLastStory();
        storyFolderUri = getStoredUri();

        if (storyFolderUri != null){
            updateListView(storyFolderUri);
        }

        buttonCherche.setOnClickListener(v -> pickDirectory());

        buttonUpdate.setOnClickListener(v -> updateListView(storyFolderUri));


    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK){
            Intent intent = result.getData();
            if (intent != null) {
                storyFolderUri = intent.getData();
                getContentResolver().takePersistableUriPermission(storyFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                saveUriToSharedPreferences(storyFolderUri);
                updateListView(storyFolderUri);
            }
        }
    });

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                chargeMiniature();
                handler.post(() -> {

                });
            });

        }
    }

    private void updateListView(Uri storyFolderUri){
        storyLib = new StoryLib(this, storyFolderUri);
        listStory = storyLib.getListFile();

        ListAdapter adapter = new FileAdapter(this, R.layout.item_file, listStory);
        listViewStory.setAdapter(adapter);
        listViewStory.setOnItemClickListener((parent, view, position, id) -> {
            File selectedStory = storyLib.getFileWithPos(position);
            if (selectedStory instanceof Directory) {
                Intent intentToStoryActivity = new Intent(MainActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToStoryActivity.putExtra("path",selectedStory.getPath());
                startActivity(intentToStoryActivity);
            }else if((selectedStory instanceof Image) || (selectedStory instanceof PDF)){
                Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedStory.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }

    private void pickDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        activityResultLauncher.launch(intent);
    }

    private void saveUriToSharedPreferences(Uri uri) {
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString("mangaFolder", uri.toString());
        editor.apply();
    }

    private Uri getStoredUri() {
        String uriString = memoire.getString("mangaFolder", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }else{
            return null;
        }
    }


    private void recupLastStory(){

        String nameUltimeStory = memoire.getString("nameUltimeStory", null);
        if (nameUltimeStory != null){
            String pathLastImage = memoire.getString(nameUltimeStory + "lastImage", null);
            if (pathLastImage != null){
                String[] splitedPath = splitPath(pathLastImage.split(":")[0]);
                if (splitedPath[0] != null){
                    textContinueUltimeLine1.setText(splitedPath[0]);
                    textContinueUltimeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":").length>1){
                    textContinueUltimeLine1.setText(pathLastImage.split(":")[1]);
                    textContinueUltimeLine1.setVisibility(View.VISIBLE);
                }
                if (splitedPath[1] != null){
                    textContinueUltimeLine2.setText(splitedPath[1]);
                    textContinueUltimeLine2.setVisibility(View.VISIBLE);

                }
                if (splitedPath[2] != null){
                    textContinueUltimeLine3.setText(splitedPath[2]);
                    textContinueUltimeLine3.setVisibility(View.VISIBLE);
                }
                if (splitedPath[3] != null){
                    textContinueUltimeLine4.setText(splitedPath[3]);
                    textContinueUltimeLine4.setVisibility(View.VISIBLE);
                }
                if (splitedPath[4] != null){
                    textContinueUltimeLine5.setText(splitedPath[4]);
                    textContinueUltimeLine5.setVisibility(View.VISIBLE);
                }

                String textButtonUltime = getString(R.string.buttonContinueText) + " " + nameUltimeStory.split(":")[0];
                buttonContinueUltime.setText(textButtonUltime);

                buttonContinueUltime.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                buttonContinueUltime.setVisibility(View.GONE);
            }
        }else {
            buttonContinueUltime.setVisibility(View.GONE);
        }

        String namePenultiemeStory = memoire.getString("namePenultiemeStory", null);
        if (namePenultiemeStory != null){
            String pathLastImage = memoire.getString(namePenultiemeStory + "lastImage", null);
            if (pathLastImage != null){
                String[] splitedPath = splitPath(pathLastImage.split(":")[0]);
                if (splitedPath[0] != null){
                    textContinuePenultiemeLine1.setText(splitedPath[0]);
                    textContinuePenultiemeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":").length>1){
                    textContinuePenultiemeLine1.setText(pathLastImage.split(":")[1]);
                    textContinuePenultiemeLine1.setVisibility(View.VISIBLE);
                }
                if (splitedPath[1] != null){
                    textContinuePenultiemeLine2.setText(splitedPath[1]);
                    textContinuePenultiemeLine2.setVisibility(View.VISIBLE);
                }
                if (splitedPath[2] != null){
                    textContinuePenultiemeLine3.setText(splitedPath[2]);
                    textContinuePenultiemeLine3.setVisibility(View.VISIBLE);
                }
                if (splitedPath[3] != null){
                    textContinuePenultiemeLine4.setText(splitedPath[3]);
                    textContinuePenultiemeLine4.setVisibility(View.VISIBLE);
                }
                if (splitedPath[4] != null){
                    textContinuePenultiemeLine5.setText(splitedPath[4]);
                    textContinuePenultiemeLine5.setVisibility(View.VISIBLE);
                }

                String textButtonPenultieme = getString(R.string.buttonContinueText) + " " + namePenultiemeStory.split(":")[0];
                buttonContinuePenultieme.setText(textButtonPenultieme);

                buttonContinuePenultieme.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                buttonContinuePenultieme.setVisibility(View.GONE);
            }
        }else {
            buttonContinuePenultieme.setVisibility(View.GONE);
        }


        String nameAntepenultiemeStory = memoire.getString("nameAntepenultiemeStory", null);
        if (nameAntepenultiemeStory != null){
            String pathLastImage = memoire.getString(nameAntepenultiemeStory + "lastImage", null);
            if (pathLastImage != null){
                String[] splitedPath = splitPath(pathLastImage.split(":")[0]);
                if (splitedPath[0] != null){
                    textContinueAntepenultiemeLine1.setText(splitedPath[0]);
                    textContinueAntepenultiemeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":").length>1){
                    textContinueAntepenultiemeLine1.setText(pathLastImage.split(":")[1]);
                    textContinueAntepenultiemeLine1.setVisibility(View.VISIBLE);
                }
                if (splitedPath[1] != null){
                    textContinueAntepenultiemeLine2.setText(splitedPath[1]);
                    textContinueAntepenultiemeLine2.setVisibility(View.VISIBLE);
                }
                if (splitedPath[2] != null){
                    textContinueAntepenultiemeLine3.setText(splitedPath[2]);
                    textContinueAntepenultiemeLine3.setVisibility(View.VISIBLE);
                }
                if (splitedPath[3] != null){
                    textContinueAntepenultiemeLine4.setText(splitedPath[3]);
                    textContinueAntepenultiemeLine4.setVisibility(View.VISIBLE);
                }
                if (splitedPath[4] != null){
                    textContinueAntepenultiemeLine5.setText(splitedPath[4]);
                    textContinueAntepenultiemeLine5.setVisibility(View.VISIBLE);
                }

                String textButtonAntepenultieme = getString(R.string.buttonContinueText) + " " + nameAntepenultiemeStory.split(":")[0];
                buttonContinueAntepenultieme.setText(textButtonAntepenultieme);

                buttonContinueAntepenultieme.setOnClickListener(v -> {
                    Intent intentToImageActivity = new Intent(MainActivity.this, ImageActivity.class);
                    intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                    intentToImageActivity.putExtra("path", pathLastImage);
                    startActivity(intentToImageActivity);
                });
            }else{
                buttonContinueAntepenultieme.setVisibility(View.GONE);
            }
        }else {
            buttonContinueAntepenultieme.setVisibility(View.GONE);
        }


    }

    public void chargeMiniature(){
        for (int i=0; i<listStory.size(); i++) {
            File file =  listStory.get(i);
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
                    PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
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