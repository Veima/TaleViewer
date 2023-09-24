package fr.antek.mangaviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
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

public class FileAdapter extends ArrayAdapter {
    private final Context context;
    private final int ressource;
    private final ArrayList<File> fileList;
    private final ArrayList<View> itemViewList = new ArrayList<>();

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
        if (file instanceof Directory){
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.directory_icon));
        }else if(file instanceof Image) {
            if (((Image) file).getMiniature() == null){
                Bitmap bitmapRaw;
                try {
                    bitmapRaw = MediaStore.Images.Media.getBitmap(context.getContentResolver(), ((Image) file).getUri());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (bitmapRaw != null) {
                    Bitmap bitmap = BitmapUtility.correctSize(bitmapRaw, 512, 512);
                    ((Image) file).setMiniature(bitmap);
                }
            }
            imageView.setImageBitmap(((Image) file).getMiniature());
        }else if(file instanceof PDF) {
            if (((PDF) file).getMiniature() == null) {
                Bitmap bitmapRaw;
                try {
                    ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(((PDF) file).getUri(), "r");
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
            imageView.setImageBitmap(((PDF) file).getMiniature());

        }

        return itemView;
    }


}
