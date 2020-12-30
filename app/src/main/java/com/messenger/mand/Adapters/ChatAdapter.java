package com.messenger.mand.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.mand.Activities.ChatActivity;
import com.messenger.mand.Objects.Message;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UserViewHolder> {

    private final ArrayList<User> listOfUsers;
    private final Context context;
    private String finalText;

    public ChatAdapter(Context context, ArrayList<User> listOfUsers) {
        this.listOfUsers = listOfUsers;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, viewGroup, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int position) {

        final User user = listOfUsers.get(position);
        userViewHolder.user_name.setText(user.getName());

        if (user.getAvatar().equals("default")) {
            userViewHolder.avatar.setImageResource(R.drawable.user_image);
        } else {
            Glide.with(context).load(user.getAvatar()).into(userViewHolder.avatar);
        }

        displayLastMessage(user.getId(), userViewHolder.last_message, user);

        if (user.getStatus().equals("online")) {
            userViewHolder.onlineIndication.setVisibility(View.VISIBLE);
        } else {
            userViewHolder.onlineIndication.setVisibility(View.GONE);
        }

        userViewHolder.itemView.setOnClickListener(v -> {
            final Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("user_name", user.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listOfUsers.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final ImageView onlineIndication;
        private final TextView user_name;
        private final TextView last_message;

        UserViewHolder(@NonNull View view) {
            super(view);
            avatar = view.findViewById(R.id.profile_image);
            onlineIndication = view.findViewById(R.id.onlineIndication);
            user_name = view.findViewById(R.id.userName);
            last_message = view.findViewById(R.id.lastMessage);
        }
    }

    private void displayLastMessage(final String userId, final TextView lastMessage, final User user) {

        finalText = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    if ((message != null && firebaseUser != null) && ((message.getRecipient().
                            equals(firebaseUser.getUid()) && message.getSender().equals(userId)) ||
                            (message.getRecipient().equals(userId) && message.getSender().
                                    equals(firebaseUser.getUid())))) {

                        finalText = messageFormation(message, firebaseUser, user);
                    }
                }
                if (finalText.equals("default")) {
                    lastMessage.setText("");
                } else {
                    lastMessage.setText(abbreviateLongMessage(finalText));
                }
                finalText = "default";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String messageFormation(Message message, FirebaseUser fUser, User u) {
        return identifyingMessageSender(message, fUser.getUid(), u) + defineMessageType(message);
    }

    private String identifyingMessageSender(Message message, String currentId, User user) {
        if (message.getSender().equals(currentId)) {
            return context.getString(R.string.you) + " ";
        }
        return user.getName() + ": ";
    }

    private String defineMessageType(Message message) {
        if (message.getText().trim().length() != 0) {
            return message.getText().trim();
        }
        return context.getString(R.string.photo);
    }

    private String abbreviateLongMessage(String text) {
        int maxLengthStroke = 38;
        if (text.trim().length() > maxLengthStroke) {
            return text.substring(0, maxLengthStroke) + "…";
        }
        return text;
    }
}

