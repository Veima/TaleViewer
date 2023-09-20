package fr.antek.mangaviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryActivity extends AppCompatActivity {
    private TextView textContinueStory;
    private Button buttonContinueStory;
    private Uri storyFolderUri;
    private String path;
    private SharedPreferences memoire;
    private Directory thisStory;
    private ArrayList<File> listFile;
    private ListView listViewStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        memoire = this.getSharedPreferences("memoire",MODE_PRIVATE);

        listViewStory = findViewById(R.id.listViewFile);

        buttonContinueStory = findViewById(R.id.buttonContinuStory);
        textContinueStory = findViewById(R.id.textContinuStory);

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
            }else if(selectedFile instanceof Image){
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
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                chargeMiniature();
                handler.post(() -> {

                });
            });

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_story, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main) {
            Intent intentToMain = new Intent(StoryActivity.this, MainActivity.class);
            startActivity(intentToMain);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recupLastStory(){
        String storyName = path.split("/")[2];
        String pathLastImage = memoire.getString(storyName + "lastImage", null);
        if (pathLastImage != null){
            textContinueStory.setText(pathLastImage.split("/",4)[3]);
            String textButton = getString(R.string.buttonContinueText) + " " + storyName;
            buttonContinueStory.setText(textButton);

            buttonContinueStory.setOnClickListener(v -> {
                Intent intentToImageActivity = new Intent(StoryActivity.this, ImageActivity.class);
                intentToImageActivity.putExtra("storyFolderUri", storyFolderUri.toString());
                intentToImageActivity.putExtra("path", pathLastImage);
                startActivity(intentToImageActivity);
            });
        }else{
            textContinueStory.setVisibility(View.GONE);
            buttonContinueStory.setVisibility(View.GONE);
        }
    }

    public void chargeMiniature(){
        View viewImage = null;
        for (int i=0; i< listFile.size(); i++) {
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
                        viewImage = listViewStory.getChildAt(i);
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