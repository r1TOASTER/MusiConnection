package com.example.musiconnection;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
// RequestsPage is responsible for showing the requests of the user in a page on the app.
public class RequestsPage extends AppCompatActivity {
    ListView lvRequests;
    String currentUserMail;
    User currentUser;
    ArrayList<Request> requestsList = null;
    RequestAdapter requestAdapter;
    Request lastRequestSelected;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_page);

        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        currentUserMail = sh.getString("UserMail", "");
        String ret = dbInteract("searchmail users " + currentUserMail);

        //if nothing failed -- convert object to user
        if (!ret.equals("Failed"))
            currentUser = toUser(ret);
        else {
            Toast.makeText(this, "Error ocuured during a try to connect. Please try again later", Toast.LENGTH_LONG).show();
            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(RequestsPage.this, MainActivity.class);
            startActivity(intent);
        }

        lvRequests = (ListView) findViewById(R.id.lvRequests);

        requestsList = getMyRequestsFromDB(currentUser);
        requestAdapter = new RequestAdapter(this, 0, 0, requestsList);
        lvRequests.setAdapter(requestAdapter);

        lvRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastRequestSelected = requestAdapter.getItem(position);
                createViewRequestDialog(lastRequestSelected);
            }
        });
    }

    private void createViewRequestDialog(Request lastRequestSelected) {
        Dialog viewRequest = new Dialog(this);
        viewRequest.setContentView(R.layout.list_item_request);
        viewRequest.setCancelable(false);

        Window window = viewRequest.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);


        TextView requesterText = viewRequest.findViewById(R.id.requesterText);
        TextView bandText = viewRequest.findViewById(R.id.bandText);

        // SET the requester info in the dialog
        TextView requesterName = viewRequest.findViewById(R.id.requesterName);
        requesterName.setText(lastRequestSelected.getRequester().getName());

        TextView requesterMail = viewRequest.findViewById(R.id.requesterMail);
        requesterMail.setText(lastRequestSelected.getRequester().getMail());

        TextView textViewGuitar = viewRequest.findViewById(R.id.requesterGuitar);
        textViewGuitar.setText((lastRequestSelected.getRequester().getInstruments()[0]) ? "Guitar " : "");

        TextView textViewPiano = viewRequest.findViewById(R.id.requesterPiano);
        textViewPiano.setText((lastRequestSelected.getRequester().getInstruments()[1]) ? "Piano " : "");

        TextView textViewBass = viewRequest.findViewById(R.id.requesterBass);
        textViewBass.setText((lastRequestSelected.getRequester().getInstruments()[2]) ? "Bass " : "");

        TextView textViewDrums = viewRequest.findViewById(R.id.requesterDrums);
        textViewDrums.setText((lastRequestSelected.getRequester().getInstruments()[3]) ? "Drums " : "");


        // SET the band info in the dialog
        TextView nameOfTheBand = viewRequest.findViewById(R.id.nameOfTheBand);
        nameOfTheBand.setText(lastRequestSelected.getBandToJoin().getName());

        TextView locationOfTheBandInAddress = viewRequest.findViewById(R.id.locationOfTheBandInAddress);
        locationOfTheBandInAddress.setText(lastRequestSelected.getBandToJoin().getLocation().getAddress());

        ListView lvMembersOfBandRequest = viewRequest.findViewById(R.id.lvMembersOfBandRequest);
        ArrayList<User> members = new ArrayList<User>();

        // going through the members of the band requested to join
        for (int i = 0; i < 5; ++i) {
            if (lastRequestSelected.getBandToJoin().getMembers()[i] != null)
                members.add(lastRequestSelected.getBandToJoin().getMembers()[i]);
        }

        MembersAdapter membersAdapter = new MembersAdapter(this, 0, 0, members);
        lvMembersOfBandRequest.setAdapter(membersAdapter);


        // SET the buttons
        Button approve = viewRequest.findViewById(R.id.approveRequester);
        Button reject = viewRequest.findViewById(R.id.rejectRequester);
        Button exitTheDialog = viewRequest.findViewById(R.id.exit);

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete request from db
                // add the member to the band

                BandClass updateTo = new BandClass(lastRequestSelected.getBandToJoin().getName(), lastRequestSelected.getBandToJoin().getLocation(), lastRequestSelected.getBandToJoin().getOwner());
                for (int i = 0; i < 5; ++i) {
                    updateTo.addMember(lastRequestSelected.getBandToJoin().getMemberByIndex(i));
                }

                if (!updateTo.addMember(lastRequestSelected.getRequester())) {
                    Toast.makeText(RequestsPage.this, "Band is full. Try again when a member leave", Toast.LENGTH_LONG).show();
                }

                else {
                    String update = updateTo.toString();
                    if (!dbInteract("updateband " + lastRequestSelected.getBandToJoin().toString() + " " + update).equals("Failed")) {
                        if (!dbInteract("requestremove " + lastRequestSelected.getRequester().toString() + " " + lastRequestSelected.getBandToJoin().toString()).equals("Failed")) {
                            // delete request from listview (adapter)
                            requestAdapter.remove(lastRequestSelected);
                            requestAdapter.notifyDataSetChanged();

                        }
                        else {
                            Toast.makeText(RequestsPage.this, "Failed to accept the requester to the band. Try again later. ", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(RequestsPage.this, "Failed to accept the requester to the band. Try again later. ", Toast.LENGTH_LONG).show();
                    }
                }

                // exit the dialog
                viewRequest.dismiss();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete request from db
                if (!dbInteract("requestremove " + lastRequestSelected.getRequester().toString() + " " + lastRequestSelected.getBandToJoin().toString()).equals("Failed")) {
                    // delete request from listview (adapter)
                    requestAdapter.remove(lastRequestSelected);
                    requestAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(RequestsPage.this, "Failed to reject the request. Try again later. ", Toast.LENGTH_LONG).show();
                }

                // exit the dialog
                viewRequest.dismiss();
            }
        });

        exitTheDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewRequest.dismiss();
            }
        });

        viewRequest.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.request_page_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_my_bands:
                Intent intent = new Intent(RequestsPage.this, MyBandsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                Intent intentS = new Intent(RequestsPage.this, SettingActivirty.class);
                startActivity(intentS);
                return true;
            case R.id.menu_view_all_bands:
                Intent intentR = new Intent(RequestsPage.this, MainScreenApp.class);
                startActivity(intentR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // Converts a user's representation as a string into a User class object and returns it
    private User toUser(String ret) {
        String[] values = ret.split(","); //spliting by ","
        String name = values[0];
        String mail = values[1];
        String password = values[2];
        User returnUser = new User(name, mail, password);
        for (int i = 0; i < 4; ++i){
            returnUser.setInstrument(i, values[i + 3].equals(" true"));
        }
        return returnUser;
    }

    // Returns the string retrieved from the Server side using a socket.
    public String dbInteract(String message) {
        String response;
        try {
            Sockets dbLinker = new Sockets();
            response = dbLinker.execute(message).get();
        } catch (ExecutionException | InterruptedException e) {
            response = "Failed";
            e.printStackTrace();
        }
        return response;
    }

    // Setting all of the requests of a user owner of a bands into an ArrayList of Request class objects, and returning it 
    private ArrayList<Request> getMyRequestsFromDB(User ownerOfBandsThatHaveRequests) {
        ArrayList<Request> ret = new ArrayList<Request>();
        // this is the return value, in edge cases or when there is no data, returning empty ArrayList
        String allofrequestesString;
        try {
            Sockets dbLinker = new Sockets();
            allofrequestesString = dbLinker.execute("getrequests requests " + currentUser.toString()).get();
            // getting the data from the server using sockets
            String[] requestsArray = allofrequestesString.split(":");
            // splitting the data using semicolon (like I chose in the server side when sending)
            // into array of strings
            int length;
            try {
                length = Integer.parseInt(requestsArray[1]);
                // the first element (the 0nth is None) should be the length of the upcoming data in the list
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
                // if the data had been corrupted, return empty ArrayList
            }
            // else, go through the list using the length of it
            for (int i = 1; i <= length; ++i){
                String currentRequest = requestsArray[i + 1];
                // get the string of the current reguest (starting from number 2 - 1 is the length)
                User requester = toUser(String.join(",", Arrays.copyOfRange(currentRequest.split(","), 0, 7)));
                // get the user object requester of the request using a helper function
                BandClass bandToJoin = toBand(String.join(",", Arrays.copyOfRange(currentRequest.split(","), 7, currentRequest.length())));
                // get the band object requester of the request using a helper function
                ret.add(new Request(bandToJoin, requester));
                // adding to the ArrayList the current Request
            }

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
            // if anything happens, return an empty ArrayList
    }
        return ret;
        // return the ArrayList
    }
    // Converts a band's representation as a string into a Band class object and returns it
    private BandClass toBand(String substring) {
        String[] values = substring.split(",");
        String name = values[0];
        LocationClass location = new LocationClass(Double.parseDouble(values[1]), Double.parseDouble(values[2]), values[3]);
        User owner = toUser(String.join(",", Arrays.copyOfRange(values, 4, 11)));
        int lengthOfMembers = Integer.parseInt(values[11]);
        User[] members = new User[lengthOfMembers];

        for (int i = 0; i < lengthOfMembers; ++i){
            members[i] = toUser(String.join(",", Arrays.copyOfRange(values, i * 7 + 12, i * 7 + 19)));
        }

        BandClass ret = new BandClass(name, location, owner);
        for (int i = 0; i < lengthOfMembers; ++i) {
            ret.addMember(members[i]);
        }
        return ret;
    }
}