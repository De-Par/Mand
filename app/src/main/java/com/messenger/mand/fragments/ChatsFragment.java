package com.messenger.mand.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.messenger.mand.adapters.ChatAdapter;
import com.messenger.mand.interactions.UserInteraction;
import com.messenger.mand.interfaces.DataPasser;
import static com.messenger.mand.values.Navigation.*;

import com.messenger.mand.entities.Chat;
import com.messenger.mand.entities.User;
import com.messenger.mand.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class ChatsFragment extends Fragment {
    private final String TAG = ChatsFragment.class.toString();

    private ArrayList<User> usersList;
    private ArrayList<String> identifiersList;
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

    @NotNull
    @Override
    public final View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
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
        identifiersList = new ArrayList<>();
        usersList = new ArrayList<>();

        gotoProfile.setOnClickListener(v -> passData(LINK_PROFILE));

        assert firebaseUser != null;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").
                child(firebaseUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                userName.setText(user.getName());

                if (user.getAvatar() != null && !user.getAvatar().equals("default") && isAdded()) {
                    try {
                        Glide.with(view.getRootView()).load(user.getAvatar()).into(userPhoto);
                    } catch (Exception e) {
                        userPhoto.setImageResource(R.drawable.profile_image_default);
                        Log.e(TAG, "Setting user's avatar -> " + e.toString());
                    }
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

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                identifiersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.child("info").getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getRecipient().equals(firebaseUser.getUid())) {
                            identifiersList.add(chat.getInitiator());
                        }
                        if (chat.getInitiator().equals(firebaseUser.getUid())) {
                            identifiersList.add(chat.getRecipient());
                        }
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                etSearch.setText("");
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
    public final void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (DataPasser) context;
    }

    public final void passData(String data) {
        dataPasser.onDataPass(data);
    }

    private void readChats() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    usersList.clear();
                    User user;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        if (!identifiersList.isEmpty()) {
                            for (String id : identifiersList) {
                                if (user != null && user.getId().equals(id)) {
                                    usersList.add(user);
                                }
                            }
                        }
                    }

                    fab.setEnabled(!usersList.isEmpty());
                    animationNobody.setSpeed(1.2f);

                    if (usersList.isEmpty()) {
                        noChats.setVisibility(View.VISIBLE);
                        animationNobody.setVisibility(View.VISIBLE);
                        animationNobody.playAnimation();
                    } else {
                        noChats.setVisibility(View.GONE);
                        animationNobody.setVisibility(View.GONE);
                        animationNobody.pauseAnimation();
                    }
                    chatAdapter = new ChatAdapter(requireContext(), usersList);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    private void searchUsersByCharacter(String character) {
        Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("searchName")
                .startAt(character).endAt(character + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    usersList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            usersList.add(user);
                        }
                    }

                    chatAdapter = new ChatAdapter(requireContext(), usersList);
                    recyclerView.setAdapter(chatAdapter);

                    if (etSearch.getText().toString().trim().equals("")) {
                        usersList.clear();
                        recyclerView.setVisibility(View.VISIBLE);
                        noSearch.setVisibility(View.GONE);
                        readChats();
                    } else if (usersList.isEmpty()) {
                        noSearch.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noSearch.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
