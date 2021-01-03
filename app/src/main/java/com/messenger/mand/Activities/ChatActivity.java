package com.messenger.mand.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.messenger.mand.Adapters.MessageAdapter;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.Objects.Message;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int RC_IMAGE = 3316;
    private MessageAdapter adapter;
    private ArrayList<Message> messages;
    private RecyclerView messagesRecyclerView;
    private CircleImageView avatar;
    ImageButton sendImageButton;
    ImageButton sendMessageButton;
    private EditText messageEditText;
    private TextView username;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    FirebaseStorage storage;
    private DatabaseReference mesReference;
    DatabaseReference useReference;
    private StorageReference chatImagesStorageReference;
    private SharedPreferences sharedPreferences;
    private String recipientUserId;
    String recipientUserName;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, MainActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        final Intent intent = getIntent();
        if (intent != null) {
            recipientUserId = intent.getStringExtra("user_id");
            recipientUserName = intent.getStringExtra("user_name");
        }

        database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        mesReference = database.getReference().child("Messages");
        useReference = database.getReference().child("Users").child(recipientUserId);
        chatImagesStorageReference = storage.getReference().child("Chat_images");

        messagesRecyclerView = findViewById(R.id.messListRecyclerView);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        username = findViewById(R.id.username);
        avatar = findViewById(R.id.profile_image);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        //
        unloadUserData();
        //
        sendMessageButton.setOnClickListener(v -> {
            if (!UserInteraction.getTrLn(messageEditText).equals("")) {
                sendMessageToDataBase(firebaseUser.getUid(), recipientUserId,
                        UserInteraction.getTrLn(messageEditText), "", DataInteraction.getTimeNow());
            } else {
                Toast.makeText(ChatActivity.this, getString(R.string.null_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        messageEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(600)});

        sendImageButton.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, RC_IMAGE);
        });

        useReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getName());
                if (user.getAvatar().equals("default")) {
                    avatar.setImageResource(R.drawable.user_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getAvatar()).into(avatar);
                }
                readMessages(firebaseUser.getUid(), recipientUserId, user.getAvatar());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        seenMessage(recipientUserId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE && resultCode == RESULT_OK) {
            assert data != null;
            Uri selectedImageUri = data.getData();
            assert selectedImageUri != null;
            final StorageReference imageReference = chatImagesStorageReference
                    .child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

            final Dialog progressDialog = new Dialog(ChatActivity.this);
            progressDialog.setContentView(R.layout.progress_bar);
            Objects.requireNonNull(progressDialog.getWindow()).
                    setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, getString(R.string.error_smth),
                            Toast.LENGTH_SHORT).show();
                    throw Objects.requireNonNull(task.getException());
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;

                    sendMessageToDataBase(firebaseUser.getUid(), recipientUserId, "",
                            downloadUri.toString(), DataInteraction.getTimeNow());

                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, R.string.successful,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ChatActivity.this, R.string.error_smth,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void seenMessage(final String recipientUserId) {
        mesReference = database.getReference().child("Messages");
        ValueEventListener seenListener = mesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    assert message != null;
                    if (message.getRecipient().equals(firebaseUser.getUid()) && message.getSender().
                            equals(recipientUserId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", "Seen");
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void readMessages(final String userId, final String recipientId, final String imageURL) {
       messages = new ArrayList<>();
       mesReference = database.getReference().child("Messages");

       mesReference.addValueEventListener(new ValueEventListener() {

           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (!messages.isEmpty()) messages.clear();
               for (DataSnapshot ds : dataSnapshot.getChildren()) {
                   Message message = ds.getValue(Message.class);
                   assert message != null;
                   if ((message.getSender().equals(userId) && message.getRecipient().
                           equals(recipientId)) || (message.getSender().equals(recipientId) &&
                           message.getRecipient().equals(userId))) {
                       if (message.getText().equals("")) {
                           message.setText(null);
                       } else {
                           message.setImageUrl(null);
                       }
                       messages.add(message);
                   }
               }
               adapter = new MessageAdapter(ChatActivity.this, messages);
               messagesRecyclerView.setAdapter(adapter);
           }
           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {}
       });
    }

    private void sendMessageToDataBase(final String sender, final String receiver, final String message,
                                       final String URL, final String time) {
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", sender);
        messageMap.put("recipient", receiver);
        messageMap.put("text", message);
        messageMap.put("time", time);
        messageMap.put("imageUrl", URL);
        messageMap.put("isSeen", "Delivered");

        mesReference.push().setValue(messageMap);
        messageEditText.setText("");
        sendListOfChatsToDataBase();
    }

    private void sendListOfChatsToDataBase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                child("ChatsList").child(recipientUserId);

        HashMap<String, Object> chatsMap = new HashMap<>();
        chatsMap.put("initiator", firebaseUser.getUid());
        chatsMap.put("recipient", recipientUserId);
        databaseReference.updateChildren(chatsMap);
    }

    private void saveUserData() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(recipientUserId + "TEXT", Objects.requireNonNull(messageEditText.
                getText()).toString().trim());
        ed.apply();
    }

    private void unloadUserData() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        messageEditText.setText(sharedPreferences.
                getString(recipientUserId + "TEXT", null));
    }
}