package com.example.onlyurispcproj;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyBandsActivity extends AppCompatActivity implements View.OnClickListener {
    Button addBand;
    BandAdapter bandAdapter;
    MembersAdapter membersAdapter;
    ArrayList<BandClass> bands;
    ArrayList<User> membersList;
    Dialog deleteDialog, addBandDialog, removeYourselfAsAMember, addMembers;
    ListView listViewBands;
    String currentUserMail;
    User currentUser, lastUserSelected = null;
    BandClass lastBandSelected, newBandAdd;
    Button deleteBand, cancelDeleteBand, addMembersButtonDialog, saveNewBand, cancelNewBand, resignYourself, cancelResigningYourself, editBand;
    EditText nameOfNewBand;
    AutocompleteSupportFragment searchViewLocationNewBand, searchViewNewLocation;
    TextView nameOfNewOwner;
    LocationClass setNewLocation = null ,newLocationEdit  = null;
    ArrayList<User> newMembersOfBand = new ArrayList<User>();

    // addMemberDialog - general
    Button addNewMemberButton, saveCurrentMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_bands_layout);

        Intent intent = getIntent(); // get intent from previous screen

        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        currentUserMail = sh.getString("UserMail", "");

        String ret = dbInteract("searchmail users " + currentUserMail);

        //if nothing failed -- convert object to user
        if (!ret.equals("Failed")) {
            currentUser = toUser(ret);
        }

        addBand = (Button) findViewById(R.id.addMyBand);

        bands = new ArrayList<BandClass>();

        ArrayList<BandClass> getMyBands = getMyBandsFromDB(currentUser);

        if (getMyBands != null){
            bands = getMyBands;
        }

        listViewBands = (ListView) findViewById(R.id.lvMyBands);
        bandAdapter = new BandAdapter(this, 0, 0, bands, currentUser);
        listViewBands.setAdapter(bandAdapter);
        addBand.setOnClickListener(this);

        listViewBands.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                lastBandSelected = bandAdapter.getItem(i);
                if (lastBandSelected.getOwner().getMail().equals(currentUserMail)) {
                    createDeleteDialog();
                }
                else {
                    createRemoveMemberDialog();
                }
                return false;
            }

        });


    }

    private void createRemoveMemberDialog() {
        removeYourselfAsAMember = new Dialog(MyBandsActivity.this);
        removeYourselfAsAMember.setContentView(R.layout.remove_member_dialog);
        removeYourselfAsAMember.setTitle("Remove yourself as a member of the current band?");
        removeYourselfAsAMember.setCancelable(false);

        resignYourself = (Button) removeYourselfAsAMember.findViewById(R.id.removeMember);
        cancelResigningYourself = (Button) removeYourselfAsAMember.findViewById(R.id.cancelRemoveMember);

        resignYourself.setOnClickListener(this);
        cancelResigningYourself.setOnClickListener(this);
        removeYourselfAsAMember.show();
    }

    private ArrayList<BandClass> getMyBandsFromDB(User currentUser) {
        ArrayList<BandClass> ret = new ArrayList<>();
        String allofbandsString;
        try {
            Sockets dbLinker = new Sockets();
            allofbandsString = dbLinker.execute("getbands bands " + currentUser.toString()).get();
            String[] bandsArray = allofbandsString.split(":");
            int length;

            try {
                length = Integer.parseInt(bandsArray[1]);
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
            for (int i = 1; i <= length; ++i){
                String currentBand = bandsArray[i + 1];
                BandClass band = toBand(String.join(",", Arrays.copyOfRange(currentBand.split(","), 0, currentBand.length())));

                ret.add(band);
            }

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return ret;

    }

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

    private void createDeleteDialog() {
        deleteDialog = new Dialog(MyBandsActivity.this);
        deleteDialog.setContentView(R.layout.delete_band_dialog);
        deleteDialog.setTitle("Delete Current Band?");
        deleteDialog.setCancelable(false);

        deleteBand = deleteDialog.findViewById(R.id.deleteBand);
        cancelDeleteBand = deleteDialog.findViewById(R.id.cancelDeleteBand);
        editBand = deleteDialog.findViewById(R.id.editBand);

        editBand.setOnClickListener(this);
        deleteBand.setOnClickListener(this);
        cancelDeleteBand.setOnClickListener(this);

        deleteDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_bands_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_all_bands:
                Intent intent = new Intent(MyBandsActivity.this, MainScreenApp.class);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                Intent intentS = new Intent(MyBandsActivity.this, SettingActivirty.class);
                startActivity(intentS);
                return true;
            case R.id.menu_requests:
                Intent intentR = new Intent(MyBandsActivity.this, RequestsPage.class);
                startActivity(intentR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == deleteBand) {
            bandAdapter.remove(lastBandSelected);
            bandAdapter.notifyDataSetChanged();
            dbInteract("remove bands " + lastBandSelected);
            deleteDialog.dismiss();
        } else if (view == cancelDeleteBand) {
            deleteDialog.dismiss();
        } else if (view == addBand) {
            membersList = new ArrayList<>();
            createNewBandDialog();
        } else if (view == addMembersButtonDialog) {
            addMembersDialog();
        } else if (view == saveNewBand) {

            if (nameOfNewBand.getText().toString().equals("") || setNewLocation == null) {
                Toast.makeText(this, "Please enter both a name for the new band and choose a valid loctaion", Toast.LENGTH_LONG).show();
            } else {
                if (newBandAdd == null) {
                    newBandAdd = new BandClass("", null, null);
                }
                newBandAdd.setName(nameOfNewBand.getText().toString().replace(" ", "-"));
                newBandAdd.setLocation(setNewLocation);
                newBandAdd.setOwner(currentUser);

                for (int i = 0; i < membersList.size(); ++i) {
                    newBandAdd.addMember(membersList.get(i));
                }

                if (dbInteract("add bands " + newBandAdd).equals("Failed")) {
                    Toast.makeText(MyBandsActivity.this, "Failed to add Band", Toast.LENGTH_LONG).show();
                } else {
                    bandAdapter.add(newBandAdd);
                    bandAdapter.notifyDataSetChanged();
                }
                addBandDialog.dismiss();
            }
        } else if (view == cancelNewBand) {
            addBandDialog.dismiss();
        } else if (view == resignYourself) {

            if (!dbInteract("removemember " + currentUser + " " + lastBandSelected).equals("Failed")) {
                bandAdapter.remove(lastBandSelected);
                bandAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Couldn't resign you as a member of this band. Try again later", Toast.LENGTH_LONG).show();
            }

            removeYourselfAsAMember.dismiss();
        } else if (view == cancelResigningYourself) {
            removeYourselfAsAMember.dismiss();
        } else if (view == editBand) {
            createEditBandDialog(lastBandSelected);
        }

        else if (view == saveCurrentMembers) {
                addMembers.dismiss();
            }
        }

    private void createEditBandDialog(BandClass lastBandSelected) {
        // Create dialog that shows the band's info
        Dialog editBandDialog = new Dialog(MyBandsActivity.this);
        editBandDialog.setContentView(R.layout.edit_my_band_dialog);
        editBandDialog.setCancelable(false);

        Window window = editBandDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        User[] tempMembers = new User[5];
        for (int i = 0; i < 5; ++i) {
            tempMembers[i] = lastBandSelected.getMembers()[i]; // for the edit members
        }

        if (!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), "AIzaSyBEGE6dcNo6OLDeWp-dBNVvR06CO_ffQN0");
        }

        TextView titlePage = editBandDialog.findViewById(R.id.textEditAccount);
        TextView nameOfTheBandOwner = editBandDialog.findViewById(R.id.nameOfTheBandOwnerEdit);
        EditText newBandName = editBandDialog.findViewById(R.id.newNameToTheBandEdit);
        searchViewNewLocation = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autoCompleteSearchViewEdit);
        Button editMembersButton = editBandDialog.findViewById(R.id.editMembersButton);
        Button saveTheNewBandAfterEdit = editBandDialog.findViewById(R.id.saveTheNewBandAfterEdit);
        Button dontSaveBandChanges = editBandDialog.findViewById(R.id.dontSaveBandChanges);

        searchViewNewLocation.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG)); // get LatLng from Place class
        //set up everything
        nameOfTheBandOwner.setText(lastBandSelected.getOwner().getName());
        newBandName.setHint(lastBandSelected.getName());

        searchViewNewLocation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {

            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng locationChosen = place.getLatLng();

                List<Address> addressList;

                Geocoder geocoder = new Geocoder(MyBandsActivity.this);
                try {
                    addressList = geocoder.getFromLocation(locationChosen.latitude, locationChosen.longitude, 1);
                    Address address = addressList.get(0);

                    String addAddress;
                    if (address.getAddressLine(0) != null) {
                        addAddress = address.getAddressLine(0).split(",")[0];
                    }
                    else {
                        addAddress = address.getCountryName();
                    }

                    String newAdd = addAddress.replace(",", "");
                    String newAdd2 = newAdd.replace(" ", "");
                    newLocationEdit = new LocationClass(address.getLatitude(), address.getLongitude(), newAdd2);

                    searchViewNewLocation.setHint(addAddress);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }

        });

        editMembersButton.setOnClickListener(new View.OnClickListener() { // open new dialog of editing current members
            @Override
            public void onClick(View v) {
                // save the members to a temp list , only if save, change the band's members using that list
                createEditMembersDialog(tempMembers);
            }
        });

        saveTheNewBandAfterEdit.setOnClickListener(new View.OnClickListener() { // save everything to a temp band (if save - update both and remove last one from adapter, add the new one)
            @Override
            public void onClick(View v) {
                String newName = newBandName.getText().toString();

                // if name and location hadn't been changed set them to the original band's values
                if (newName.equals("")){
                    newName = lastBandSelected.getName();
                }
                if (newLocationEdit == null){
                    newLocationEdit = lastBandSelected.getLocation();
                }

                BandClass newBand = new BandClass(newName, newLocationEdit, lastBandSelected.getOwner());
                // location new : newLocationEdit (if not null)
                for (User tempMember : tempMembers) {
                    if (tempMember != null) {
                        newBand.addMember(tempMember);
                    }
                }

                if (!dbInteract("updateband " + lastBandSelected.toString() + " " + newBand.toString()).equals("Failed")) {
                    bandAdapter.remove(lastBandSelected);
                    bandAdapter.add(newBand);
                    bandAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(MyBandsActivity.this, "Failed to edit the band. Try again later", Toast.LENGTH_LONG).show();
                }

                editBandDialog.dismiss();
                deleteDialog.dismiss();
            }
        });

        dontSaveBandChanges.setOnClickListener(new View.OnClickListener() { // close the dialog
            @Override
            public void onClick(View v) {
                editBandDialog.dismiss();
                deleteDialog.dismiss();
            }
        });
        // create buttons -> edit members, save band, dont save band

        // -> save in temp and if save change.

        editBandDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(searchViewNewLocation);
                ft.commit();
            }
        });

        editBandDialog.show();
    }

    private void createEditMembersDialog(User[] tempMembers) {
        Dialog editMembersDialog = new Dialog(MyBandsActivity.this);
        editMembersDialog.setContentView(R.layout.edit_members_dialog);
        editMembersDialog.setCancelable(false);

        Window window = editMembersDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        TextView titleOfEditMembers = editMembersDialog.findViewById(R.id.titleAddMembersEdit);
        ListView lvMembersInAddDialogEdit = editMembersDialog.findViewById(R.id.lvMembersInAddDialogEdit);
        Button addNewMemberButtonEdit = editMembersDialog.findViewById(R.id.addNewMemberButtonEdit);
        Button saveNewMembersButtonEdit = editMembersDialog.findViewById(R.id.saveNewMembersButtonEdit);

        ArrayList<User> currentBandMembers = new ArrayList<>();

        for (User s : tempMembers){
            if (s != null) {
                currentBandMembers.add(s);
            }
        }

        MembersAdapter membersAdapterEdit = new MembersAdapter(this, 0, 0, currentBandMembers);
        lvMembersInAddDialogEdit.setAdapter(membersAdapterEdit);

        lvMembersInAddDialogEdit.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteCurrentMemberBand(membersAdapterEdit.getItem(position), currentBandMembers, membersAdapterEdit);
                return false;
            }
        });

        addNewMemberButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddSingleMemberEdit(currentBandMembers, membersAdapterEdit);
            }
        });

        saveNewMembersButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < currentBandMembers.size(); ++i){
                    tempMembers[i] = currentBandMembers.get(i);
                }
                for (int i = currentBandMembers.size(); i < 5; ++i){
                    tempMembers[i] = null;
                }
                editMembersDialog.dismiss();
            }
        });

        editMembersDialog.show();
    }

    private void createAddSingleMemberEdit(ArrayList<User> currentBandMembers, MembersAdapter membersAdapterEdit) {
        Dialog addSingleMemberEdit = new Dialog(MyBandsActivity.this);
        addSingleMemberEdit.setContentView(R.layout.add_single_member_edit_dialog);
        addSingleMemberEdit.setCancelable(false);

        Window window = addSingleMemberEdit.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        EditText newMemberMailSearchEdit = addSingleMemberEdit.findViewById(R.id.newMemberMailSearchEdit);
        Button addSingleMemberButtonEdit = addSingleMemberEdit.findViewById(R.id.addSingleMemberButtonEdit);
        Button dontSaveThisMemberEdit = addSingleMemberEdit.findViewById(R.id.dontSaveThisMemberEdit);

        addSingleMemberButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ret = dbInteract("searchmail users " + newMemberMailSearchEdit.getText().toString());
                if (!ret.equals("Failed")){
                    User newMember = toUser(ret);
                    if (newMember.getMail().equals(currentUserMail)){
                        Toast.makeText(MyBandsActivity.this, "You can't add yourself to the band", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (currentBandMembers.size() < 5) {
                            Boolean check = false;
                            for (int i = 0; i < currentBandMembers.size(); ++i) {
                                if (currentBandMembers.get(i).getMail().equals(newMember.getMail())) {
                                    Toast.makeText(MyBandsActivity.this, "User already in this band", Toast.LENGTH_LONG).show();
                                    check = true;
                                }
                            }
                            if (!check) {
                                currentBandMembers.add(newMember);
                                membersAdapterEdit.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(MyBandsActivity.this, "Band already full.. ", Toast.LENGTH_LONG).show();
                        }
                        addSingleMemberEdit.dismiss();
                    }
                }
                else {
                    Toast.makeText(MyBandsActivity.this, "No such email / user using this mail", Toast.LENGTH_LONG).show();
                }
            }
        });

        dontSaveThisMemberEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSingleMemberEdit.dismiss();
            }
        });

        addSingleMemberEdit.show();
    }

    private void addMembersDialog() {
        addMembers = new Dialog(MyBandsActivity.this);
        addMembers.setContentView(R.layout.add_members_dialog);
        addMembers.setCancelable(false);

        Window window = addMembers.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        TextView title = addMembers.findViewById(R.id.titleAddMembers);
        ListView membersLV = addMembers.findViewById(R.id.lvMembersInAddDialog);;
        saveCurrentMembers = addMembers.findViewById(R.id.saveNewMembersButton);
        addNewMemberButton = addMembers.findViewById(R.id.addNewMemberButton);

        membersAdapter = new MembersAdapter(this, 0, 0, membersList);
        membersLV.setAdapter(membersAdapter);

        membersLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteCurrentMemberBand(membersAdapter.getItem(position), membersList, membersAdapter);
                return false;
            }
        });

        saveCurrentMembers.setOnClickListener(this);
        addNewMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == addNewMemberButton) {
                    openAddSingleMemberDialog();
                }
            }
        });

        addMembers.show();

    }

    // real add a member dialog -> when you enter mail and add him if you can
    private void openAddSingleMemberDialog() {
        Dialog addSingleMember = new Dialog(MyBandsActivity.this);
        addSingleMember.setContentView(R.layout.add_single_member_dialog);
        addSingleMember.setCancelable(false);

        Window window = addSingleMember.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        EditText memberMail = addSingleMember.findViewById(R.id.newMemberMailSearch);
        Button addSingleMemberButtonAndSave = addSingleMember.findViewById(R.id.addSingleMemberButton);
        Button dontSaveThisMember = addSingleMember.findViewById(R.id.dontSaveThisMember);

        addSingleMemberButtonAndSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == addSingleMemberButtonAndSave){
                    String ret = dbInteract("searchmail users " + memberMail.getText().toString());
                    if (!ret.equals("Failed")){
                        User newMember = toUser(ret);
                        if (newMember.getMail().equals(currentUserMail)){
                            Toast.makeText(MyBandsActivity.this, "You can't add yourself to the band", Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (membersList.size() < 5) {
                                Boolean check = false;
                                for (int i = 0; i < membersList.size(); ++i) {
                                    if (membersList.get(i).getMail().equals(newMember.getMail())) {
                                        Toast.makeText(MyBandsActivity.this, "User already in this band", Toast.LENGTH_LONG).show();
                                        check = true;
                                    }
                                }
                                if (!check) {
                                    membersList.add(newMember);
                                    membersAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(MyBandsActivity.this, "Band already full.. ", Toast.LENGTH_LONG).show();
                            }
                            addSingleMember.dismiss();
                        }
                    }
                    else {
                        Toast.makeText(MyBandsActivity.this, "No such email / user using this mail", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        dontSaveThisMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == dontSaveThisMember){
                    addSingleMember.dismiss();
                }
            }
        });

        addSingleMember.show();
    }

    private void deleteCurrentMemberBand(User lastUserSelected, ArrayList<User> membersList, MembersAdapter membersAdapter) {
        Dialog deleteCurrentMember = new Dialog(MyBandsActivity.this);
        deleteCurrentMember.setContentView(R.layout.delete_current_member_dialog);
        deleteCurrentMember.setTitle("Delete Current Memebr?");
        deleteCurrentMember.setCancelable(false);

        Button deleteCurrent = deleteCurrentMember.findViewById(R.id.deleteCurrentMemberButton);
        Button dontDeleteCurrent = deleteCurrentMember.findViewById(R.id.cancelDeleteCurrentMemberButton);

        deleteCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == deleteCurrent){
                        membersList.remove(lastUserSelected);
                        membersAdapter.notifyDataSetChanged();
                        deleteCurrentMember.dismiss();
                }
            }
        });
        dontDeleteCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == dontDeleteCurrent){
                    deleteCurrentMember.dismiss();
                }
            }
        });
        deleteCurrentMember.show();
    }

    private void createNewBandDialog(){
        addBandDialog = new Dialog(MyBandsActivity.this);
        addBandDialog.setContentView(R.layout.add_band_dialog);

        Window window = addBandDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        if (!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), "AIzaSyBEGE6dcNo6OLDeWp-dBNVvR06CO_ffQN0");
        }

        addBandDialog.setTitle("Add A New Band");
        addBandDialog.setCancelable(false);

        nameOfNewBand = addBandDialog.findViewById(R.id.nameOfTheBand);
        nameOfNewOwner = addBandDialog.findViewById(R.id.nameOfTheOwner);

        searchViewLocationNewBand = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autoCompleteSearchViewAdd);
        searchViewLocationNewBand.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG));

        searchViewLocationNewBand.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {

            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng locationChosen = place.getLatLng();

                List<Address> addressList;

                Geocoder geocoder = new Geocoder(MyBandsActivity.this);
                try {
                    addressList = geocoder.getFromLocation(locationChosen.latitude, locationChosen.longitude, 1);
                    Address address = addressList.get(0);

                    String addAddress;
                    if (address.getAddressLine(0) != null) {
                        addAddress = address.getAddressLine(0).split(",")[0];
                    }
                    else {
                        addAddress = address.getCountryName();
                    }

                    String newAdd = addAddress.replace(",", "");
                    String newAdd2 = newAdd.replace(" ", "");
                    setNewLocation = new LocationClass(address.getLatitude(), address.getLongitude(), newAdd2);

                    searchViewLocationNewBand.setHint(addAddress);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        addBandDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(searchViewLocationNewBand);
                ft.commit();
            }
        });

        addMembersButtonDialog = addBandDialog.findViewById(R.id.addMembersButton);
        saveNewBand = addBandDialog.findViewById(R.id.saveTheNewBand);
        cancelNewBand = addBandDialog.findViewById(R.id.cancelTheNewBand);

        addMembersButtonDialog.setOnClickListener(this);
        saveNewBand.setOnClickListener(this);
        cancelNewBand.setOnClickListener(this);

        addBandDialog.show();
    }

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

}
