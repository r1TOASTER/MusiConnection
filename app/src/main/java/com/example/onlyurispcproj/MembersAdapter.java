package com.example.onlyurispcproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MembersAdapter extends ArrayAdapter<User> {
    private ArrayList<User> members;
    private LayoutInflater inflater;

    public MembersAdapter(Context context, int resource, int viewListResourceID, ArrayList<User> members){
        super(context, resource, viewListResourceID, members);
        this.members = members;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public User getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_members_band, parent, false);
        }

        User member = getItem(position);

        TextView textViewName = view.findViewById(R.id.memberName);
        textViewName.setText(member.getName());

        TextView textViewMail = view.findViewById(R.id.memberMail);
        textViewMail.setText(member.getMail());

        TextView textViewGuitar = view.findViewById(R.id.memberGuitar);
        textViewGuitar.setText((member.getInstruments()[0]) ? "Guitar " : "");

        TextView textViewPiano = view.findViewById(R.id.memberPiano);
        textViewPiano.setText((member.getInstruments()[1]) ? "Piano " : "");

        TextView textViewBass = view.findViewById(R.id.memberBass);
        textViewBass.setText((member.getInstruments()[2]) ? "Bass " : "");

        TextView textViewDrums = view.findViewById(R.id.memberDrums);
        textViewDrums.setText((member.getInstruments()[3]) ? "Drums " : "");

        return view;
    }
}
