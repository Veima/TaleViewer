package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryActivity extends AppCompatActivity {

    private Uri storyFolderUri;
    private String path;
    private Directory thisDirectory;
    private boolean miniatureCharged = false;
    private FileAdapter adapter;
    private ArrayList<File> listFile;
    private ListView listViewFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        storyFolderUri = Uri.parse(getIntent().getStringExtra("storyFolderUri"));
        path = getIntent().getStringExtra("path");

        StoryLib storyLib = new StoryLib(this, storyFolderUri);

        thisDirectory = (Directory) storyLib.buildFromPath(path.split("/", 3)[2]);

        listViewFile = findViewById(R.id.listViewFile);

        Objects.requireNonNull(getSupportActionBar()).setTitle(thisDirectory.getName());

        listFile = thisDirectory.getListFile();
        displayList();

    }


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

        if (itemId == 1){
            Intent intentToMain = new Intent(DirectoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }else{
            StringBuilder newPath = new StringBuilder();
            String[] splitPath = path.split("/");
            for (int i = 1; i < itemId+1; i++) {
                newPath.append("/").append(splitPath[i]);
            }
            if (itemId == 2) {
                Intent intentToStoryActivity = new Intent(DirectoryActivity.this, StoryActivity.class);
                intentToStoryActivity.putExtra("storyFolderUri",storyFolderUri.toString());
                intentToStoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToStoryActivity);
            }else {
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", newPath.toString());
                startActivity(intentToDirectoryActivity);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayList(){
        adapter = new FileAdapter(this, R.layout.item_file, listFile);
        listViewFile.setAdapter(adapter);
        listViewFile.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = thisDirectory.getFileWithPos(position);
            if (selectedFile instanceof Directory) {
                Intent intentToDirectoryActivity = new Intent(DirectoryActivity.this, DirectoryActivity.class);
                intentToDirectoryActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToDirectoryActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToDirectoryActivity);
            }else if(selectedFile instanceof Image){
                Intent intentToImageActivity = new Intent(DirectoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", selectedFile.getPath());
                startActivity(intentToImageActivity);
            }
        });
    }
    public void chargeMiniature(){
        View viewImage = null;
        for (int i=0; i<listFile.size(); i++) {
            File file =  listFile.get(i);
            if(file instanceof Image) {
                Bitmap bitmapRaw;
                try {
                    bitmapRaw = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ((Image) file).getUri());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (bitmapRaw != null) {
                    if (viewImage == null) {
                        viewImage = listViewFile.getChildAt(i);
                    }

                    if (viewImage != null) {
                        ImageView imageView = viewImage.findViewById(R.id.image);

                        Bitmap bitmap = BitmapUtility.adaptBitmap2View(bitmapRaw, imageView);
                        ((Image) file).setMiniature(bitmap);
                    }
                }
            }
        }
    }

}