package com.example.musiconnection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// This is a custom ArrayAdapter used to populate a ListView with bands information
public class BandAdapter extends ArrayAdapter<BandClass> {
    // The list of bands to be displayed
    private ArrayList<BandClass> bands;

    // The layout inflater to inflate the views
    private LayoutInflater inflater;

    // The user who is viewing the bands
    private User owner;

    // Constructor method for the BandAdapter class
    public BandAdapter(Context context, int resource, int textViewResourceId, ArrayList<BandClass> bands, User owner) {
        super(context, resource, textViewResourceId, bands);
        this.bands = bands;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
    }

    // Returns the size of the list of bands
    @Override
    public int getCount() {
        return bands.size();
    }

    // Returns the band object at the specified position in the list
    @Override
    public BandClass getItem(int position) {
        return bands.get(position);
    }

    // Returns the position of the band in the list
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Method to create a custom view for each band in the ListView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        // If view is null, inflate the view with the layout file
        if (view == null) {
            view = inflater.inflate(R.layout.list_bands_layout, parent, false);
        }

        // Get a reference to the ImageView to display the band icon
        ImageView imageViewOwner = view.findViewById(R.id.bandIcon);

        // Get the BandClass object at the specified position
        BandClass band = getItem(position);

        // Set the band icon based on the user's role in the band
        if (band.getOwner().getMail().equals(owner.getMail())) {
            imageViewOwner.setImageResource(R.drawable.owner_of_band);
        } else if (band.isMember(owner)) {
            imageViewOwner.setImageResource(R.drawable.member_of_band_not_owner);
        } else {
            imageViewOwner.setImageResource(R.drawable.not_member_nor_owner);
        }

        // Get a reference to the TextView to display the band name
        TextView textViewName = view.findViewById(R.id.bandName);
        textViewName.setText(band.getName().replace("-", " "));

        // Get a reference to the TextView to display the band owner's name
        TextView textViewOwner = view.findViewById(R.id.bandOwnerName);
        textViewOwner.setText(band.getOwner().getName());

        // Get a reference to the TextView to display the band location address
        TextView textViewLocation = view.findViewById(R.id.bandLocationAddress);
        textViewLocation.setText(band.getLocation().getAddress());

        // Return the custom view for the band
        return view;
    }
}