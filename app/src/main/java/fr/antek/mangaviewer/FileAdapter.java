package fr.antek.mangaviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends ArrayAdapter {
    private Context context;
    private int ressource;
    private ArrayList<String> value;

    public FileAdapter(Context context, int resource, ArrayList<String> value) {
        super(context, resource, value);
        this.value = value;
        this.context = context;
        this.ressource = resource;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
        String file = value.get(pos);
        View itemView = LayoutInflater.from(context).inflate(ressource, parent, false);
        TextView textTitre = itemView.findViewById(R.id.textName);
        textTitre.setText(file);

        return itemView;
    }


}
