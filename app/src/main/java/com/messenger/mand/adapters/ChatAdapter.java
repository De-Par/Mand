package com.messenger.mand.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.messenger.mand.activities.ChatActivity;
import com.messenger.mand.entities.Message;
import com.messenger.mand.entities.User;
import com.messenger.mand.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UserViewHolder> {
    private final String TAG = ChatAdapter.class.toString();

    private final ArrayList<User> listOfUsers;
    private final Context context;
    private String finalText;

    public ChatAdapter(Context context, ArrayList<User> listOfUsers) {
        this.listOfUsers = listOfUsers;
        this.context = context;
    }

    @NonNull
    @Override
    public final UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, viewGroup, false);
        return new UserViewHolder(view);
    }

    @Override
    public final void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int position) {
        final User user = listOfUsers.get(position);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userViewHolder.user_name.setText(user.getName());

        if (!user.getAvatar().equals("default") && !user.getAvatar().equals("")) {
            try {
                Glide.with(context).load(user.getAvatar()).into(userViewHolder.avatar);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                userViewHolder.avatar.setImageResource(R.drawable.profile_image_default);
            }
        } else {
            userViewHolder.avatar.setImageResource(R.drawable.profile_image_default);
        }

        if (currentUser != null) {
            displayLastMessage(userViewHolder.last_message, currentUser.getUid(), user);
        }

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
    public final int getItemCount() {
        return listOfUsers.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final View onlineIndication;
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

    private void displayLastMessage(final TextView lastMessage, @NotNull final String currentUserId, @NotNull User user) {
        finalText = "default";
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String oneToOneId = currentUserId.hashCode() > user.getId().hashCode() ?
                user.getId() + currentUserId : currentUserId + user.getId();

        reference.child("chats").child(oneToOneId).child("data/messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    if (message != null && (message.getRecipient().equals(currentUserId) &&
                            message.getSender().equals(user.getId()) || message.getRecipient().equals(user.getId()) &&
                            message.getSender().equals(currentUserId))) {

                        finalText = identifyingMessageSender(message, currentUserId) + defineMessageType(message);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @NotNull
    private String identifyingMessageSender(@NotNull Message message, String currentId) {
        if (message.getSender().equals(currentId)) {
            return context.getString(R.string.you) + " ";
        }
        return "";
    }

    private String defineMessageType(@NotNull Message message) {
        if (message.getText().trim().length() != 0) {
            return message.getText().trim();
        } 
        return context.getString(R.string.photo);
    }

    @NotNull
    private String abbreviateLongMessage(@NotNull String text) {
        int maxLengthStroke = 38;
        if (text.trim().length() > maxLengthStroke) {
            return text.substring(0, maxLengthStroke) + "…";
        }
        return text;
    }
}

