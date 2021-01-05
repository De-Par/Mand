package com.messenger.mand.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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
import com.messenger.mand.Objects.Message;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;

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

    private boolean search = false;
    private boolean isActiveApplication = true;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

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

        changAnimIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_decrease);
        changAnimOut = AnimationUtils.loadAnimation(getContext(), R.anim.scale_increase);
        fbAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_button_pressing);
        animationNobody = view.findViewById(R.id.lottieAnimNoChats);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();
        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference().child("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    assert message != null;
                    if (message.getSender().equals(firebaseUser.getUid())) {
                        usersList.add(message.getRecipient());
                    }
                    if (message.getRecipient().equals(firebaseUser.getUid())) {
                        usersList.add(message.getSender());
                    }
                }
                readChats();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        changAnimIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
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
            public void onAnimationRepeat(Animation animation) {}
        });

        fab.setOnClickListener(v -> {
            fab.startAnimation(fbAnim);
            if (search) {
                searchLayout.startAnimation(changAnimIn);
                search = false;
            } else {
                searchLayout.startAnimation(changAnimOut);
                searchLayout.setVisibility(View.VISIBLE);
                etSearch.setVisibility(View.VISIBLE);
                search = true;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (etSearch.getText().toString().startsWith(" ")) {
                    etSearch.setText(etSearch.getText().toString().substring(1));
                }
                searchUsersByCharacter(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        return view;
    }

    private void readChats() {
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (String id : usersList) {
                        assert user != null;
                        if (user.getId().equals(id)) {
                            if (mUsers.size() != 0) {
                                for (User u : mUsers) {
                                    if (!user.getId().equals(u.getId())) {
                                        mUsers.add(user);
                                    }
                                }
                            } else {
                                mUsers.add(user);
                            }
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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
                if (isActiveApplication) {
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
                } else if (mUsers.isEmpty()){
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

    @Override
    public void onPause() {
        super.onPause();
        isActiveApplication = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActiveApplication = true;
    }
}
