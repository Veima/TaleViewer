package fr.antek.mangaviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends ArrayAdapter {
    private Context context;
    private int ressource;
    private ArrayList<File> fileList;

    public FileAdapter(Context context, int resource, ArrayList<File> fileList) {
        super(context, resource, fileList);
        this.fileList = fileList;
        this.context = context;
        this.ressource = resource;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
        File file = fileList.get(pos);
        View itemView = LayoutInflater.from(context).inflate(ressource, parent, false);

        TextView textTitre = itemView.findViewById(R.id.textName);
        textTitre.setText(file.getName());

        if (file instanceof Directory){
            ImageView image = itemView.findViewById(R.id.image);
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.directory_icon));
        }


        return itemView;
    }


}
