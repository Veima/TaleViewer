package fr.antek.historyviewer;

import android.content.Intent;
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
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for displaying the contents of a directory, allowing navigation to subdirectories and viewing images or PDF files.
 */
public class DirectoryActivity extends AppCompatActivity {
    private Uri storyFolderUri; // The URI of the story library folder.
    private String path; // The current directory path.
    private Directory thisDirectory; // Represents the current directory.
    private ArrayList<File> listFile; // List of files in the current directory.
    private ListView listViewFile; // ListView for displaying files.


    /**
     * Called when the activity is created. Initializes the user interface, retrieves the directory's content, and displays it.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        // Retrieve data passed through an Intent
        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        // Create a Directory object based on the provided path
        StoryLib storyLib = new StoryLib(this, storyFolderUri);
        thisDirectory = (Directory) storyLib.buildFromPath(path.split("/", 3)[2]);

        // Initialize the ListView for displaying files
        listViewFile = findViewById(R.id.listViewFile);

        // Set the title of the ActionBar to the directory's name
        Objects.requireNonNull(getSupportActionBar()).setTitle(thisDirectory.getName());

        // Retrieve the list of files in the directory and display it
        listFile = thisDirectory.getListFile();
        displayList();

    }

    /**
     * Called when the activity's window focus changes. Loads miniature images for the displayed files.
     * @param hasFocus Whether the window has focus.
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // Load miniature images for the displayed files in a background thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(this::chargeMiniature);

        }
    }

    /**
     * Inflates the options menu for the activity, providing navigation and settings options.
     * @param menu The menu to inflate.
     * @return True if the menu is successfully inflated.
     */
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

        return true;
    }

    /**
     * Handles selection of items in the options menu, allowing navigation to directories or settings.
     * @param item The selected menu item.
     * @return True if the item selection is handled successfully.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == 0) {
            // Navigate to the settings activity
            Intent intentToParameterActivity = new Intent(DirectoryActivity.this, ParameterActivity.class);
            intentToParameterActivity.putExtra("activityAfter", "DirectoryActivity");
            intentToParameterActivity.putExtra("storyFolderUri", storyFolderUri.toString());
            intentToParameterActivity.putExtra("path", path);
            startActivity(intentToParameterActivity);
        }else if (itemId == 10){
            // Navigate to the main activity
            Intent intentToMain = new Intent(DirectoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }else{
            // Build a new path for navigation
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId-8; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            if (itemId == 11) {
                // Navigate to a story activity
                Intent intentToStoryActivity = new Intent(DirectoryActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToStoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToStoryActivity);
            }else {
                // Navigate to another directory activity
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToDirectoryActivity);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays the list of files in the directory using a custom adapter.
     */
    private void displayList(){
        FileAdapter adapter = new FileAdapter(this, R.layout.item_file, listFile);
        listViewFile.setAdapter(adapter);
        listViewFile.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = thisDirectory.getFileWithPos(position);
            if (selectedFile instanceof Directory) {
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToDirectoryActivity);
            }else if((selectedFile instanceof Image) || (selectedFile instanceof PDF)){
                Intent intentToImageActivity = new Intent(DirectoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }

    /**
     * Loads miniature images for files in the directory using separate threads.
     */
    public void chargeMiniature(){
        ArrayList<Thread> threads = new ArrayList<>();
        for (File file : listFile) {
            Thread thread = new Thread(() -> {
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

            threads.add(thread);
            thread.start();

        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}