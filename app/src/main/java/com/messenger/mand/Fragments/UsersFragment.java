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
import com.messenger.mand.Adapters.UserAdapter;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;
import java.util.Objects;

public class UsersFragment extends Fragment {

    private ArrayList<User> userArrayList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TextView noUsers;
    private TextView noSearch;
    private FloatingActionButton fab;
    private TextInputLayout searchLayout;
    private EditText etSearch;

    private Animation changAnimIn;
    private Animation changAnimOut;
    private Animation fbAnim;

    private boolean search = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.userListRecyclerView);
        noUsers = view.findViewById(R.id.noUsers);
        noSearch = view.findViewById(R.id.noSearch);
        fab = view.findViewById(R.id.fab);
        searchLayout = view.findViewById(R.id.til1);
        etSearch = view.findViewById(R.id.searchUsers);

        // load animation scale
        changAnimIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_decrease);
        changAnimOut = AnimationUtils.loadAnimation(getContext(), R.anim.scale_increase);
        fbAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_button_pressing);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (userArrayList == null) {
            userArrayList = new ArrayList<>();
        }
        //
        readUsers();
        //
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
                //searchUsers(charSequence.toString().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    private void searchUsersByCharacter(String character) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("searchName")
                .startAt(character).endAt(character + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!userArrayList.isEmpty()) userArrayList.clear();
                if (isAdded()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            userArrayList.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), userArrayList);
                recyclerView.setAdapter(userAdapter);

                if (etSearch.getText().toString().trim().equals("")) {
                    readUsers();
                    recyclerView.setVisibility(View.VISIBLE);
                    noSearch.setVisibility(View.GONE);
                } else if (userArrayList.isEmpty()){
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
    }  // another variant to search in Firebase

    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.
                getInstance().getReference().child("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((etSearch.getText().toString().trim().equals("") || !search) && isAdded()) {
                    if (!userArrayList.isEmpty()) userArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid()) && isAdded()) {
                            userArrayList.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), userArrayList);
                    recyclerView.setAdapter(userAdapter);

                    if (userArrayList.isEmpty()) {
                        noUsers.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        fab.setEnabled(false);
                    } else {
                        noUsers.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        fab.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
