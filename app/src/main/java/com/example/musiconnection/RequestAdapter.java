package com.example.musiconnection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
// Custom Adapter for showing the requests using an ArrayAdapter 
public class RequestAdapter extends ArrayAdapter<Request> {

    private ArrayList<Request> requests;
    private LayoutInflater inflater;
    // Constructor for creating a Request Adapter using the context, the requests and inflating using the context.
    public RequestAdapter(Context context, int resource, int viewListResourceID, ArrayList<Request> requests) {
        super(context, resource, viewListResourceID, requests);
        this.requests = requests;
        this.inflater = LayoutInflater.from(context);
    }
    // Returns the size of the requests list
    @Override
    public int getCount() {
        return requests.size();
    }
    // Returns the request in the specified position
    @Override
    public Request getItem(int position) {
        return requests.get(position);
    }
    // Returns the id of the item in the position (no id for items)
    @Override
    public long getItemId(int position) {
        return 0;
    }
    // Setting the view of the request and the returning it
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.request_list_view_adapter, parent, false);
        }

        Request request = getItem(position);
        TextView memberName = view.findViewById(R.id.memberName);
        TextView textRequested = view.findViewById(R.id.textRequested);
        TextView bandName = view.findViewById(R.id.bandName);

        memberName.setText(request.getRequester().getName());
        bandName.setText(request.getBandToJoin().getName());

        return view;
    }
}
