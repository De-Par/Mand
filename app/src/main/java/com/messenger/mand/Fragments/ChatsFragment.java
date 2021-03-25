package com.messenger.mand.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.messenger.mand.Adapters.ChatAdapter;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.Interfaces.DataPasser;
import static com.messenger.mand.Values.Navigation.*;

import com.messenger.mand.Objects.Chat;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    private ArrayList<User> mUsers;
    private ArrayList<String> usersList;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private TextView noSearch;
    private TextView noChats;
    private FloatingActionButton fab;
    private TextInputLayout searchLayout;
    private EditText etSearch;

    private Animation changAnimIn;
    private Animation changAnimOut;
    private Animation fbAnim;
    private LottieAnimationView animationNobody;

    private boolean isSearch = false;

    private FirebaseUser firebaseUser;
    DataPasser dataPasser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.userListRecyclerView);
        noSearch = view.findViewById(R.id.noSearch);
        noChats = view.findViewById(R.id.noChats);
        fab = view.findViewById(R.id.fab);
        searchLayout = view.findViewById(R.id.til1);
        etSearch = view.findViewById(R.id.searchUsers);
        View gotoProfile = view.findViewById(R.id.clickableZone);

        ImageView userPhoto = view.findViewById(R.id.profile_image);
        TextView userName = view.findViewById(R.id.username);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");

        changAnimIn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_decrease);
        changAnimOut = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_increase);
        fbAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_button_pressing);
        animationNobody = view.findViewById(R.id.lottieAnimNoChats);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();
        mUsers = new ArrayList<>();

        gotoProfile.setOnClickListener(v -> passData(LINK_PROFILE));

        assert firebaseUser != null;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(firebaseUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                userName.setText(user.getName());

                if (!user.getAvatar().equals("default") && isAdded()) {
                    Glide.with(view.getRootView()).load(user.getAvatar()).into(userPhoto);
                } else {
                    userPhoto.setImageResource(R.drawable.profile_image_default);
                }

                if (!UserInteraction.hasInternetConnection(view.getContext())) {
                    Toast.makeText(view.getContext(), R.string.internet_connection,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("ChatsList");
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getRecipient().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getInitiator());
                    }
                    if (chat.getInitiator().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getRecipient());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        changAnimIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                InputMethodManager imm = (InputMethodManager) requireActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(searchLayout.getWindowToken(), 0);
                etSearch.setText("");
                searchLayout.setVisibility(View.GONE);
                etSearch.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        fab.setOnClickListener(v -> {
            fab.startAnimation(fbAnim);
            if (isSearch) {
                searchLayout.startAnimation(changAnimIn);
                isSearch = false;
            } else {
                searchLayout.startAnimation(changAnimOut);
                searchLayout.setVisibility(View.VISIBLE);
                etSearch.setVisibility(View.VISIBLE);
                isSearch = true;
            }
        });

        final String[] request = {null};
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                request[0] = etSearch.getText().toString().trim();
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (etSearch.getText().toString().startsWith(" ")) {
                    etSearch.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (!etSearch.getText().toString().trim().equals(request[0]) && isSearch) {
                    searchUsersByCharacter(etSearch.getText().toString().trim().toLowerCase());
                }
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        inflater.inflate(R.menu.search_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (DataPasser) context;
    }

    public void passData(String data) {
        dataPasser.onDataPass(data);
    }

    private void readChats() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (String id : usersList) {
                        assert user != null;
                        if (user.getId().equals(id)) {
                            mUsers.add(user);
                        }
                    }
                }

                fab.setEnabled(!mUsers.isEmpty());
                animationNobody.setSpeed(1.2f);

                if (mUsers.isEmpty()) {
                    noChats.setVisibility(View.VISIBLE);
                    animationNobody.setVisibility(View.VISIBLE);
                    animationNobody.playAnimation();
                } else {
                    noChats.setVisibility(View.GONE);
                    animationNobody.setVisibility(View.GONE);
                    animationNobody.pauseAnimation();
                }
                chatAdapter = new ChatAdapter(getContext(), mUsers);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void searchUsersByCharacter(String character) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("searchName")
                .startAt(character).endAt(character + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (isAdded()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                }
                chatAdapter = new ChatAdapter(getContext(), mUsers);
                recyclerView.setAdapter(chatAdapter);

                if (etSearch.getText().toString().trim().equals("")) {
                    readChats();
                    recyclerView.setVisibility(View.VISIBLE);
                    noSearch.setVisibility(View.GONE);
                } else if (mUsers.isEmpty()) {
                    noSearch.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
