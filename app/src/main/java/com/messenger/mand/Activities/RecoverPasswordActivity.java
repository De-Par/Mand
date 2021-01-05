package com.messenger.mand.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.R;

import java.util.Objects;

public class RecoverPasswordActivity extends AppCompatActivity {

    private Animation animation_button;
    private Button my_button;
    private EditText send_email;
    private String email;
    private FirebaseAuth firebaseAuth;
    ImageView my_image;
    Animation animation_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        my_image = findViewById(R.id.password_image);
        my_button = findViewById(R.id.sendEmailButton);
        send_email = findViewById(R.id.sendEmailToRecover);

        animation_image = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up_to_down_image);
        animation_button = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_button_pressing);
        animation_image.setRepeatCount(Animation.INFINITE);
        my_image.startAnimation(animation_image);

        firebaseAuth = FirebaseAuth.getInstance();

        my_button.setOnClickListener(v -> {
            UserInteraction.hideKeyboard(RecoverPasswordActivity.this);
            my_button.startAnimation(animation_button);
            email = send_email.getText().toString().trim();

            if (email.equals("") || !email.contains("@mail.ru")) {
                UserInteraction.showPopUpSnackBar(getString(R.string.input_email), v, getApplicationContext());
            } else if (!UserInteraction.hasInternetConnection(getApplicationContext())){
                UserInteraction.showPopUpSnackBar(getString(R.string.internet_connection), v, getApplicationContext());
            } else {
                dialogWindow();
            }
        });
    }

    private void recoverEmailSuccessful() {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RecoverPasswordActivity.this, getString(R.string.check_email),
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(RecoverPasswordActivity.this, LoginActivity.class));
            } else if (!UserInteraction.hasInternetConnection(getApplicationContext())) {
                Toast.makeText(RecoverPasswordActivity.this, R.string.internet_connection,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RecoverPasswordActivity.this, getString(R.string.error_email),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RecoverPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void dialogWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecoverPasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(RecoverPasswordActivity.this).inflate(R.layout.error_dialog,
                (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.password_change));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.password_change_question));
        ((Button) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.no));
        ((Button) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.yes));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_new_releases);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> {
            alertDialog.dismiss();
            send_email.setText("");
        });

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> recoverEmailSuccessful());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
}