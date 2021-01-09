package com.messenger.mand.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.messenger.mand.Activities.ZoomViewActivity;
import com.messenger.mand.Objects.Constants;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Objects.Message;
import com.messenger.mand.R;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Message> listOfMessages;
    private final Context context;
    private final int MSG_TYPE_MINE = 0;

    public MessageAdapter(Context context, List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_MINE) {
            view = LayoutInflater.from(context).inflate(R.layout.my_message_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.your_message_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = listOfMessages.get(position);

        if (message.getImageUrl() == null) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);

            if (message.getText().trim().length() > 30) {
                holder.layoutMess.setOrientation(LinearLayout.VERTICAL);
                holder.infMess.setPadding(0,20,3,0);
            } else {
                holder.layoutMess.setOrientation(LinearLayout.HORIZONTAL);
                holder.infMess.setPadding(25,15,3,0);
            }
            holder.messageTextView.setText(message.getText());

        } else {
            holder.messageTextView.setVisibility(View.GONE);
            holder.layoutMess.setOrientation(LinearLayout.VERTICAL);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoImageView.getContext()).
                    load(message.getImageUrl()).
                    into(holder.photoImageView);
            holder.infMess.setPaddingRelative(0,15,3,-5);
            holder.infMess.setGravity(Gravity.BOTTOM);
        }

        holder.messageTimeView.setText(getTime(message));

        if (message.getIsSeen().equals("Delivered")) {
            holder.txt_img.setImageResource(R.drawable.ic_done_icon);
        } else {
            holder.txt_img.setImageResource(R.drawable.ic_seen_icon);
        }

        holder.photoImageView.setOnClickListener(v -> {
            zoomImage(holder);
        });
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if (listOfMessages.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_MINE;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return listOfMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final TextView messageTextView;
        private final TextView messageTimeView;
        private final LinearLayout layoutMess;
        private final LinearLayout infMess;
        private final ImageView txt_img;

        ViewHolder(View view) {
            super(view);
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
            messageTimeView = view.findViewById(R.id.messageTimeText);
            layoutMess = view.findViewById(R.id.LinearLayoutMessages);
            infMess = view.findViewById(R.id.llForInf);
            txt_img = view.findViewById(R.id.txt_img);
        }
    }

    private String getTime(Message message) {  //  HH:mm
        return message.getTime().substring(message.getTime().length() - 5);
    }

    private void zoomImage(ViewHolder holder) {
        byte[] arr = DataInteraction.convertDrawableToByteArr(((BitmapDrawable) holder.photoImageView.
                getDrawable()).getBitmap(), Constants.ORIGINAL);
        context.startActivity(new Intent(context, ZoomViewActivity.class).putExtra("photo", arr));
    }
}
