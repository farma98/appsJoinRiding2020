package com.example.joinriding.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.joinriding.AddPostActivity;
import com.example.joinriding.MainActivity;
import com.example.joinriding.CycleUserActivity;
import com.example.joinriding.NotificationActivity;
import com.example.joinriding.ProfileUserActivity;
import com.example.joinriding.R;
import com.example.joinriding.SettingsActivity;
import com.example.joinriding.adapters.AdapterPosts;
import com.example.joinriding.models.ModelPosts;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    // Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser fuser;
    private FirebaseDatabase databaseUser, databaseCycle;
    private DatabaseReference referenceUser, referenceCycle;


    // Declare View Linear Layout
    private LinearLayout infouser, infomotor, infopost;
    private TextView infouserbtn, infomotorbtn, infopostbtn;

    // Declare View
    private ImageView photoUser,photoCycle;
    private TextView nameUser, idUser, phoneUser, genderUser, addressUser;
    private TextView nameCycle, platCycle, yearCycle;

    // Declare
    private ProgressDialog progressDialog;
    private RecyclerView recyclerViewPostProfile;
    private List<ModelPosts> postsList;
    private AdapterPosts adapterPosts;

    // badge
    BadgeStyle style;
    private int badgeCount = 10;

    // Declare Flaoting Menu
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton floatInfoUser, floatInfoMotor;

    String uid;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        // init swipe layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        // init badge
        style = ActionItemBadge.BadgeStyles.RED.getStyle();

        // init firebase
        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance();
        databaseCycle = FirebaseDatabase.getInstance();
        referenceUser = databaseUser.getReference("TBL_USERS");
        referenceCycle = databaseCycle.getReference("TBL_CYCLE");

        progressDialog = new ProgressDialog(getActivity());
        recyclerViewPostProfile = view.findViewById(R.id.recyclerViewPostProfile);

        // Init View Linear Layout
        infouser = view.findViewById(R.id.infoUser);
        infomotor = view.findViewById(R.id.infoMotor);
        infopost = view.findViewById(R.id.infoPost);
        infouserbtn = view.findViewById(R.id.infoUserBtn);
        infomotorbtn = view.findViewById(R.id.infoMotorBtn);
        infopostbtn = view.findViewById(R.id.infoPostBtn);

        // init view xml
        photoUser = view.findViewById(R.id.photoUserProfile);
        nameUser = view.findViewById(R.id.nameUserProfile);
        idUser = view.findViewById(R.id.idUserProfile);
        phoneUser = view.findViewById(R.id.phoneUserProfile);
        genderUser = view.findViewById(R.id.genderUserProfile);
        addressUser = view.findViewById(R.id.addressUserProfile);

        nameCycle = view.findViewById(R.id.cycleUserProfile);
        platCycle = view.findViewById(R.id.platUserProfile);
        yearCycle = view.findViewById(R.id.yearUserProfile);

        ////////////////////////////////////////////////////////////////////////////////////////////
        /*making layout visible*/
        infouser.setVisibility(View.VISIBLE);
        infomotor.setVisibility(View.GONE);
        infopost.setVisibility(View.GONE);

        infouserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.VISIBLE);
                infomotor.setVisibility(View.GONE);
                infopost.setVisibility(View.GONE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));

            }
        });

        infomotorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.GONE);
                infomotor.setVisibility(View.VISIBLE);
                infopost.setVisibility(View.GONE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));

            }
        });

        infopostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.GONE);
                infomotor.setVisibility(View.GONE);
                infopost.setVisibility(View.VISIBLE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

            }
        });

        // Init Floating Menu
        floatingActionMenu = view.findViewById(R.id.floatingActionMenu);
        floatInfoUser = view.findViewById(R.id.floatingInfoUser);
        floatInfoMotor = view.findViewById(R.id.floatingInfoMotor);

        floatInfoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileUserActivity.class);
                intent.putExtra("key", "editUser");
                intent.putExtra("editUserId", uid);
                startActivity(intent);
            }
        });

        floatInfoMotor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CycleUserActivity.class);
                intent.putExtra("key", "editCycle");
                intent.putExtra("editCycleId", uid);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        // Init Load Post
                        loadDataUser();
                        loadDataCycle();
                        loadMyPosts();
                    }
                }, 5000);
            }
        });

        postsList = new ArrayList<>();

        loadDataUser();
        loadDataCycle();
        loadMyPosts();

        checkUserStatus();

        return  view;
    }

    private void loadDataCycle() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        Query queryCycle = referenceCycle.orderByChild("uid").equalTo(fuser.getUid());
        queryCycle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+ snapshot.child("photoCycle").getValue();
                    String name = ""+ snapshot.child("nameCycle").getValue();
                    String plat = ""+ snapshot.child("platCycle").getValue();
                    String year = ""+ snapshot.child("yearCycle").getValue();

                    nameCycle.setText(name);
                    platCycle.setText(plat);
                    yearCycle.setText(year);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Picasso.get().load(photo).resize(50, 50).centerCrop().into(photoCycle);
                        }
                        catch (Exception e){
                            // if failed set default
                            Picasso.get().load(R.drawable.icon_user_grey).resize(50, 50).centerCrop().into(photoUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataUser() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        Query queryUser = referenceUser.orderByChild("uid").equalTo(fuser.getUid());
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+ snapshot.child("photoUser").getValue();
                    String name = ""+ snapshot.child("nameUser").getValue();
                    uid = ""+ snapshot.child("uid").getValue();
                    String phone = ""+ snapshot.child("phoneUser").getValue();
                    String gender = ""+ snapshot.child("genderUser").getValue();
                    String address= ""+ snapshot.child("addressUser").getValue();


                    nameUser.setText(name);
                    idUser.setText(uid);
                    phoneUser.setText(phone);
                    genderUser.setText(gender);
                    addressUser.setText(address);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Glide.with(getContext())
                                    .load(photo)
                                    .error(R.drawable.icon_user_grey)
                                    .into(photoUser);
                        }
                        catch (Exception e){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyPosts() {
        // linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // show newst post
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // set thisto recyclerview
        recyclerViewPostProfile.setLayoutManager(layoutManager);

        // init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // query to load post
        Query query = reference.orderByChild("uid").equalTo(fuser.getUid());
        // get all data from reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelPosts myPost = snapshot.getValue(ModelPosts.class);

                    // add to list
                    postsList.add(myPost);
                    // adapter
                    adapterPosts = new AdapterPosts(getActivity(), postsList);
                    // set this adapter to recycler view
                    recyclerViewPostProfile.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            uid = user.getUid();
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu action in fragment
        super.onCreate(savedInstanceState);
    }

    // Inflate Option Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflated Menu
        inflater.inflate(R.menu.top_nav_menu, menu);

        // Hide Some Menu
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        if (badgeCount > 0) {
            ActionItemBadge.update(getActivity(), menu.findItem(R.id.action_notification), FontAwesome.Icon.faw_android, ActionItemBadge.BadgeStyles.RED, badgeCount);
        } else {
            ActionItemBadge.hide(menu.findItem(R.id.action_notification));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }else if(id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }else if(id == R.id.action_notification){
            ActionItemBadge.update(item, badgeCount);
            startActivity(new Intent(getActivity(), NotificationActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
