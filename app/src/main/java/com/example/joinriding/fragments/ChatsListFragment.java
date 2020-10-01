package com.example.joinriding.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.joinriding.CreateGroupActivity;
import com.example.joinriding.MainActivity;
import com.example.joinriding.MyListGroupActivity;
import com.example.joinriding.MyListUsersActivity;
import com.example.joinriding.NotificationActivity;
import com.example.joinriding.R;
import com.example.joinriding.SettingsActivity;
import com.example.joinriding.adapters.AdapterChatsList;
import com.example.joinriding.models.ModelChatList;
import com.example.joinriding.models.ModelChatUsers;
import com.example.joinriding.models.ModelMyListUsers;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsListFragment extends Fragment {
    // Declare Views
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerViewChatList;
    private List<ModelChatList> modelChatLists;
    private List<ModelMyListUsers> modelMyListUsers;
    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private AdapterChatsList adapterChatlist;

    // badge
    BadgeStyle style;
    private int badgeCount = 10;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;

    // Declare Flaoting Menu
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton floatChat, floatGroup;

    public ChatsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        // init swipe layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        // init badge
        style = ActionItemBadge.BadgeStyles.RED.getStyle();

        // Init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerViewChatList = view.findViewById(R.id.recyclerViewChatList);
        modelChatLists = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("TBL_CHATLIST").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelChatLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChatList chatlist = snapshot.getValue(ModelChatList.class);
                    modelChatLists.add(chatlist);
                }
                // loadChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Init Floating Menu
        floatingActionMenu = view.findViewById(R.id.floatingActionMenu);
        floatChat = view.findViewById(R.id.floatingChat);
        floatGroup = view.findViewById(R.id.floatingGroup);

        floatChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyListUsersActivity.class);
                startActivity(intent);
            }
        });

        floatGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyListGroupActivity.class);
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
                        loadChat();
                    }
                }, 5000);
            }
        });
        loadChat();
        return  view;
    }

    private void loadChat() {
        modelMyListUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelMyListUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelMyListUsers user = snapshot.getValue(ModelMyListUsers.class);
                    for (ModelChatList chatlist : modelChatLists){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            modelMyListUsers.add(user);
                            break;
                        }
                    }
                    // adapter
                    adapterChatlist = new AdapterChatsList(getContext(), modelMyListUsers);
                    // set adapter
                    recyclerViewChatList.setAdapter(adapterChatlist);
                    // set last message
                    for (int i = 0; i<modelMyListUsers.size(); i++){
                        lastMassage(modelMyListUsers.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMassage(final String userUid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_CHATS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChatUsers chatQ = snapshot.getValue(ModelChatUsers.class);
                    if (chatQ == null){
                        continue;
                    }
                    String sender = chatQ.getSender();
                    String receiver = chatQ.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chatQ.getReceiver().equals(currentUser.getUid())
                            && chatQ.getSender().equals(userUid) || chatQ.getReceiver().equals(userUid)
                            && chatQ.getSender().equals(currentUser.getUid())){

                        if (chatQ.getType().equals("image")){
                            theLastMessage = "Send Photo";
                        }else{
                            theLastMessage = chatQ.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userUid, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
        }else{
            //user not signed,go to main
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu action in fragment
        super.onCreate(savedInstanceState);
    }

    // inflate optionmenu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflated menu
        inflater.inflate(R.menu.top_nav_menu, menu);

        // hide addpost icon
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

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
            firebaseAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }else if(id == R.id.action_notification){
            ActionItemBadge.update(item, badgeCount);
            startActivity(new Intent(getActivity(), NotificationActivity.class));
        }else if (id == R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
