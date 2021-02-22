package com.messenger.mand.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.R;

public class EditProfileActivity extends AppCompatActivity {

    public EditProfileActivity() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        View cancelButton = findViewById(R.id.closePage);
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, NavigationActivity.class);
            intent.putExtra("position", 3);
            startActivity(intent);
        });
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
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
        super.onPause();
    }

    private void showEditDialog() {
        String[] options = {"Edit name", "Edit phone", "About you"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Title");
        builder.setItems(options, (dialogInterface, i) -> {
            switch (i) {
                case 0:

                    break;
                case 1:

                    break;
                case 2:

                    break;

            }
        });
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null){
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        }

        dialog.show();
    }
}
