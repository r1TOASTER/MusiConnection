package com.example.musiconnection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BandAdapter extends ArrayAdapter<BandClass> {
    private ArrayList<BandClass> bands;
    private LayoutInflater inflater;
    private User owner;

    public BandAdapter(Context context, int resource, int textViewResourceId, ArrayList<BandClass> bands, User owner){
        super(context, resource, textViewResourceId, bands);
        this.bands = bands;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
    }

    @Override
    public int getCount() {
        return bands.size();
    }

    @Override
    public BandClass getItem(int position) {
        return bands.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_bands_layout, parent, false);
        }

        ImageView imageViewOwner = view.findViewById(R.id.bandIcon);

        BandClass band = getItem(position);
        if (band.getOwner().getMail().equals(owner.getMail())){
            imageViewOwner.setImageResource(R.drawable.owner_of_band);
        }
        else if (band.isMember(owner)){
            imageViewOwner.setImageResource(R.drawable.member_of_band_not_owner);
        }
        else {
            imageViewOwner.setImageResource(R.drawable.not_member_nor_owner);
        }

        TextView textViewName = view.findViewById(R.id.bandName);
        textViewName.setText(band.getName().replace("-", " "));

        TextView textViewOwner = view.findViewById(R.id.bandOwnerName);
        textViewOwner.setText(band.getOwner().getName());

        TextView textViewLocation = view.findViewById(R.id.bandLocationAddress);
        textViewLocation.setText(band.getLocation().getAddress());

        return view;
    }
}


