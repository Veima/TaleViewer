package fr.antek.historyviewer;

import android.app.Activity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main activity for the History Viewer Android application.
 * It serves as the entry point of the app and handles user interactions and navigation.
 *
 * This activity is responsible for managing the user's interaction with the app:
 * - Selecting a directory to view stories.
 * - Displaying a list of stories within the selected directory.
 * - Handling user interactions with the list, including opening stories and continuing from where they left off.
 * - Providing a menu for app settings and configuration.
 *
 * The activity also keeps track of the last three stories the user has interacted with and allows
 * them to continue reading from where they left off.
 */
public class MainActivity extends AppCompatActivity {

    // UI elements
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

    // Data and state
    private Uri storyFolderUri = null;
    private SharedPreferences memoire;
    private StoryLib storyLib;
    private ArrayList<File> listStory;

    /**
     * Called when the activity is created. Initializes the UI, sets up click listeners,
     * and retrieves the previously selected directory from SharedPreferences.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
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

        Button buttonFind = findViewById(R.id.buttonCherche);
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        listViewStory = findViewById(R.id.listViewStory);

        // Initialize SharedPreferences for storing app data
        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);

        // Retrieve the previously selected directory if available
        getLastStory();
        storyFolderUri = getStoredUri();

        // If a directory was previously selected, update the list of stories in that directory
        if (storyFolderUri != null){
            updateListView(storyFolderUri);
        }

        // Set up click listeners for buttons
        buttonFind.setOnClickListener(v -> pickDirectory());
        buttonUpdate.setOnClickListener(v -> updateListView(storyFolderUri));
    }

    // Register an ActivityResultLauncher for directory selection...
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

    /**
     * Called when the activity's window focus changes. It is used to load thumbnails for the displayed stories.
     * @param hasFocus True if the window has focus, false otherwise.
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(this::chargeMiniature);

        }
    }

    /**
     * Inflate the options menu for the activity. It includes an item for app settings.
     * @param menu The menu to be inflated.
     * @return True if the menu is successfully inflated.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dynamic, menu);
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.parameter));
        return true;
    }

    /**
     * Handle menu item selection. In this case, it navigates to the app's settings activity.
     * @param item The selected menu item.
     * @return True if the item is successfully handled.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 0) {
            Intent intentToParameterActivity = new Intent(MainActivity.this, ParameterActivity.class);
            intentToParameterActivity.putExtra("activityAfter", "MainActivity");
            intentToParameterActivity.putExtra("storyFolderUri", "");
            intentToParameterActivity.putExtra("path", "");
            startActivity(intentToParameterActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the list of stories in the selected directory. This function retrieves the list of
     * stories and displays them in the ListView using a custom adapter.
     * @param storyFolderUri The URI of the selected directory.
     */
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

    /**
     * Launch the directory picker activity for the user to select a directory.
     */
    private void pickDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        activityResultLauncher.launch(intent);
    }

    /**
     * Save the selected directory's URI to SharedPreferences for future reference.
     * @param uri The URI of the selected directory.
     */
    private void saveUriToSharedPreferences(Uri uri) {
        SharedPreferences.Editor editor = memoire.edit();
        editor.putString("historyFolder", uri.toString());
        editor.apply();
    }

    /**
     * Retrieve the stored URI of the previously selected directory from SharedPreferences.
     * @return The stored directory URI or null if not available.
     */
    private Uri getStoredUri() {
        String uriString = memoire.getString("historyFolder", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }else{
            return null;
        }
    }

    // Methods for managing display for the last three stories
    private void getLastStory(){
        String nameUltimeStory = memoire.getString("nameUltimeStory", null);
        if (nameUltimeStory != null){
            String pathLastImage = memoire.getString(nameUltimeStory + "lastImage", null);
            if (pathLastImage != null){
                String[] splitPath = splitPath(pathLastImage.split(":")[0]);
                if (splitPath[0] != null){
                    textContinueUltimeLine1.setText(splitPath[0]);
                    textContinueUltimeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":nPage=").length>1){
                    textContinueUltimeLine1.setText(pathLastImage.split(":")[1].replace("nPage=",""));
                    textContinueUltimeLine1.setVisibility(View.VISIBLE);
                }
                if (splitPath[1] != null){
                    textContinueUltimeLine2.setText(splitPath[1]);
                    textContinueUltimeLine2.setVisibility(View.VISIBLE);

                }
                if (splitPath[2] != null){
                    textContinueUltimeLine3.setText(splitPath[2]);
                    textContinueUltimeLine3.setVisibility(View.VISIBLE);
                }
                if (splitPath[3] != null){
                    textContinueUltimeLine4.setText(splitPath[3]);
                    textContinueUltimeLine4.setVisibility(View.VISIBLE);
                }
                if (splitPath[4] != null){
                    textContinueUltimeLine5.setText(splitPath[4]);
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
                String[] splitPath = splitPath(pathLastImage.split(":")[0]);
                if (splitPath[0] != null){
                    textContinuePenultiemeLine1.setText(splitPath[0]);
                    textContinuePenultiemeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":").length>1){
                    textContinuePenultiemeLine1.setText(pathLastImage.split(":")[1].replace("nPage=",""));
                    textContinuePenultiemeLine1.setVisibility(View.VISIBLE);
                }
                if (splitPath[1] != null){
                    textContinuePenultiemeLine2.setText(splitPath[1]);
                    textContinuePenultiemeLine2.setVisibility(View.VISIBLE);
                }
                if (splitPath[2] != null){
                    textContinuePenultiemeLine3.setText(splitPath[2]);
                    textContinuePenultiemeLine3.setVisibility(View.VISIBLE);
                }
                if (splitPath[3] != null){
                    textContinuePenultiemeLine4.setText(splitPath[3]);
                    textContinuePenultiemeLine4.setVisibility(View.VISIBLE);
                }
                if (splitPath[4] != null){
                    textContinuePenultiemeLine5.setText(splitPath[4]);
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
                String[] splitPath = splitPath(pathLastImage.split(":")[0]);
                if (splitPath[0] != null){
                    textContinueAntepenultiemeLine1.setText(splitPath[0]);
                    textContinueAntepenultiemeLine1.setVisibility(View.VISIBLE);
                }else if (pathLastImage.split(":").length>1){
                    textContinueAntepenultiemeLine1.setText(pathLastImage.split(":")[1].replace("nPage=",""));
                    textContinueAntepenultiemeLine1.setVisibility(View.VISIBLE);
                }
                if (splitPath[1] != null){
                    textContinueAntepenultiemeLine2.setText(splitPath[1]);
                    textContinueAntepenultiemeLine2.setVisibility(View.VISIBLE);
                }
                if (splitPath[2] != null){
                    textContinueAntepenultiemeLine3.setText(splitPath[2]);
                    textContinueAntepenultiemeLine3.setVisibility(View.VISIBLE);
                }
                if (splitPath[3] != null){
                    textContinueAntepenultiemeLine4.setText(splitPath[3]);
                    textContinueAntepenultiemeLine4.setVisibility(View.VISIBLE);
                }
                if (splitPath[4] != null){
                    textContinueAntepenultiemeLine5.setText(splitPath[4]);
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

    /**
     * Load thumbnails for the stories displayed in the list.
     */
    public void chargeMiniature(){
        if (listStory !=null) {
            ArrayList<Thread> listThread = new ArrayList<>();
            for (int i = 0; i < listStory.size(); i++) {

                int finalI = i;
                Thread thread = new Thread(() -> {
                    File file = listStory.get(finalI);
                    if (file instanceof Image) {
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
                    } else if (file instanceof PDF) {
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
    }

    /**
     * Split a file path into multiple lines to fit in TextViews.
     * @param path The file path to split.
     * @return An array of strings containing the split path lines.
     */
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