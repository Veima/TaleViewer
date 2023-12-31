package fr.antek.taleviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An ArrayAdapter for displaying a list of files with their names and icons.
 */
public class FileAdapter extends ArrayAdapter {
    private final Context context;
    private final int ressource;
    private final ArrayList<File> fileList;
    private final ArrayList<View> itemViewList = new ArrayList<>();

    /**
     * Constructs a FileAdapter using the provided context, resource, and file list.
     * @param context The application context.
     * @param resource The resource ID for the layout of each file item.
     * @param fileList The list of files to display.
     */
    public FileAdapter(Context context, int resource, ArrayList<File> fileList) {
        super(context, resource, fileList);
        this.fileList = fileList;
        this.context = context;
        this.ressource = resource;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent){
        File file = fileList.get(pos);
        @SuppressLint("ViewHolder") View itemView = LayoutInflater.from(context).inflate(ressource, parent, false);
        itemViewList.add(itemView);

        TextView textTitre = itemView.findViewById(R.id.textName);
        textTitre.setText(file.getName());

        ImageView imageView = itemView.findViewById(R.id.image);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());


        if (file instanceof Directory){
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.directory_icon));
        }else if(file instanceof Image) {
            if (((Image) file).getMiniature() == null){
                executor.execute(() -> {
                    Bitmap bitmapRaw;
                    try {
                        bitmapRaw = MediaStore.Images.Media.getBitmap(context.getContentResolver(), ((Image) file).getUri());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (bitmapRaw == null) {
                        bitmapRaw = BitmapUtility.generateTextBitmap(file.getName() + " " + context.getString(R.string.ErrorOpen), 256, 256);
                    }
                    Bitmap bitmap = BitmapUtility.correctSize(bitmapRaw, 512, 512);
                    ((Image) file).setMiniature(bitmap);

                    handler.post(() -> imageView.setImageBitmap(((Image) file).getMiniature()));
                });

            }
            imageView.setImageBitmap(((Image) file).getMiniature());
        }else if(file instanceof PDF) {
            if (((PDF) file).getMiniature() == null) {
                executor.execute(() -> {
                    Bitmap bitmapRaw;
                    try {
                        ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(((PDF) file).getUri(), "r");
                        PdfRenderer pdfRenderer = new PdfRenderer(Objects.requireNonNull(fileDescriptor));
                        PdfRenderer.Page pdfPage = pdfRenderer.openPage(0);

                        bitmapRaw = Bitmap.createBitmap(pdfPage.getWidth(), pdfPage.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmapRaw);
                        canvas.drawColor(Color.WHITE);
                        pdfPage.render(bitmapRaw, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        pdfPage.close();
                        pdfRenderer.close();
                    } catch (IOException e) {
                        bitmapRaw = BitmapUtility.generateTextBitmap(file.getName() + " " + context.getString(R.string.ErrorOpen), 256, 256);
                    }
                    Bitmap bitmap = BitmapUtility.correctSize(bitmapRaw, 512, 512);
                    ((PDF) file).setMiniature(bitmap);

                    handler.post(() -> imageView.setImageBitmap(((PDF) file).getMiniature()));
                });
            }
            imageView.setImageBitmap(((PDF) file).getMiniature());

        }

        return itemView;
    }


}
