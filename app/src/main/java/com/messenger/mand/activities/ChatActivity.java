package com.messenger.mand.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.messenger.mand.Adapters.MessageAdapter;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.Interactions.UserInteraction;
import static com.messenger.mand.Values.Sensor.*;

import com.messenger.mand.Objects.Message;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private final ArrayList<Message> messages = new ArrayList<>();
    private RecyclerView messagesRecyclerView;
    private CircleImageView avatar;
    ImageButton sendImageButton;
    ImageButton sendMessageButton;
    private EditText messageEditText;
    private TextView userName;
    private TextView statusUser;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    FirebaseStorage storage;
    private DatabaseReference messagesReference;
    private ValueEventListener statusListener;
    DatabaseReference useReference;
    private StorageReference chatImagesStorageReference;
    private String recipientUserId;
    String recipientUserName;
    String[] galleryPermissions;

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
            startActivity(new Intent(ChatActivity.this, NavigationActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recipientUserId = extras.getString("user_id");
            recipientUserName = extras.getString("user_name");
        }

        database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        messagesReference = database.getReference().child("Messages");
        useReference = database.getReference().child("Users").child(recipientUserId);
        chatImagesStorageReference = storage.getReference().child("Chat_images");

        messagesRecyclerView = findViewById(R.id.messListRecyclerView);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        userName = findViewById(R.id.username);
        statusUser = findViewById(R.id.statusUser);
        avatar = findViewById(R.id.profile_image);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        sendMessageButton.setOnClickListener(v -> {
            if (!UserInteraction.getTrimLen(messageEditText).equals("")) {
                //                try {
//                    Cipher cipher = Cipher.getInstance("AES");
//                    byte[] encryptedData = encryptString(UserInteraction.getTrimLen(messageEditText), cipher);
//                    String mes = new String(encryptedData);
//                    sendMessageToDB(firebaseUser.getUid(), recipientUserId, mes,
//                            "", DataInteraction.getTimeNow());
//
//                } catch (Exception e) {
//                    Log.e("TAG", e.getLocalizedMessage());
//                }
                sendMessageToDB(firebaseUser.getUid(), recipientUserId, UserInteraction.getTrimLen(messageEditText),
                        "", DataInteraction.getTimeNow());
            } else {
                Toast.makeText(ChatActivity.this, getString(R.string.null_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        avatar.setOnClickListener(v -> profilePreview());
        userName.setOnClickListener(v -> profilePreview());
        sendImageButton.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                pickFromGallery();
            }
        });

        messageEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(700)});

        useReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String status;
                assert user != null;
                userName.setText(user.getName());

                if (!user.getStatus().equals("online")) {
                    String[] date = user.getStatus().split(", ");
                    status = getString(R.string.lastSeen) + " " + date[1] + " " + getString(R.string.at) + " " + date[2];
                } else {
                    status = getString(R.string.online);
                }
                statusUser.setText(status);

                if (!user.getAvatar().equals("default") && !user.getAvatar().equals("")) {
                    try {
                        Glide.with(getApplicationContext()).load(user.getAvatar()).into(avatar);
                    } catch (Exception e) {
                        Log.e("AVATAR", e.getLocalizedMessage());
                        avatar.setImageResource(R.drawable.profile_image_default);
                    }
                } else {
                    avatar.setImageResource(R.drawable.profile_image_default);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        readMessages();
        seenMessage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_STORAGE_CODE) {
                assert data != null;
                Uri imageUri = data.getData();
                uploadImageToChat(imageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        UserInteraction.showPopUpSnackBar(getString(R.string.error_smth),
                                getCurrentFocus(), getApplicationContext());
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        DatabaseInteraction.pushUserStatus("online");
        super.onStart();
    }

    @Override
    protected void onResume() {
        DatabaseInteraction.pushUserStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        messagesReference.removeEventListener(statusListener);
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
        super.onPause();
    }

    private void readMessages() {
        messages.clear();
        messagesReference = database.getReference().child("Messages");
        messagesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!messages.isEmpty()) messages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    assert message != null;
                    if ((message.getSender().equals(firebaseUser.getUid()) && message.getRecipient().
                            equals(recipientUserId)) || (message.getSender().equals(recipientUserId) &&
                            message.getRecipient().equals(firebaseUser.getUid()))) {
                        if (message.getText().equals("")) {
                            message.setText(null);
                        } else {
                            message.setImageUrl(null);
                        }
                        messages.add(message);
                    }
                }
                adapter = new MessageAdapter(ChatActivity.this, messages);
                adapter.notifyDataSetChanged();

                messagesRecyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void seenMessage() {
        messagesReference = database.getReference().child("Messages");
        statusListener = messagesReference.addValueEventListener(new ValueEventListener() {
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

    private void uploadImageToChat(Uri uri) {
        final Dialog progressDialog = new Dialog(ChatActivity.this);
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        if (uri != null) {
            String imageName = new File(uri.getPath()).getName();
            final StorageReference fileReference = chatImagesStorageReference.child(imageName);
            fileReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()) {
                    assert downloadUri != null;
                    sendMessageToDB(firebaseUser.getUid(), recipientUserId, "",
                            downloadUri.toString(), DataInteraction.getTimeNow());
                } else {
                    Toast.makeText(ChatActivity.this, R.string.error_smth, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            progressDialog.dismiss();
        }
    }

    private void sendMessageToDB(final String sender, final String receiver, final String message,
                                 final String URI, final String time) {
        messagesReference = database.getReference().child("Messages");

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", sender);
        messageMap.put("recipient", receiver);
        messageMap.put("text", message);
        messageMap.put("time", time);
        messageMap.put("imageUrl", URI);
        messageMap.put("isSeen", "Delivered");

        messagesReference.push().setValue(messageMap);
        messageEditText.setText("");
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_STORAGE_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        requestPermissions(galleryPermissions, STORAGE_REQUEST_CODE);
    }

    private void profilePreview() {
        //
    }
}