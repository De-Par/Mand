package com.messenger.mand.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.R;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    EditText password, email;
    TextView forgotPassword;
    private Button LoginButton;
    private Animation butt;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        LoginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.recoveryPassword);
        butt = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_button_pressing);

        Intent intent = getIntent();
        if (intent != null) {
            email.setText(intent.getStringExtra("email"));
            password.setText(intent.getStringExtra("password"));
        }

        forgotPassword.setOnClickListener(v -> {
            Intent intent1 = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
            startActivity(intent1);
            finish();
        });

        password.setFilters(new InputFilter[]{(source, start, end, dest, dStart, dEnd) -> {
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
            Matcher matcher = pattern.matcher(source);
            if (!matcher.matches()) return "";
            return null;
        }});

        LoginButton.setOnClickListener(v -> {
            LoginButton.startAnimation(butt);
            UserInteraction.hideKeyboard(LoginActivity.this);

            String txt_email = UserInteraction.getTrLn(email);
            String txt_password = UserInteraction.getTrLn(password);

            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                UserInteraction.showPopUpSnackBar(getString(R.string.input_fields), v, getApplicationContext());

            } else if (!txt_email.contains("@mail.ru")) {
                UserInteraction.showPopUpSnackBar(getString(R.string.input_email), v, getApplicationContext());

            } else {
                loginUser(txt_email, txt_password, v);
            }
        });
    }

    private void loginUser(String txt_email, String txt_password, final View v) {
        final Dialog progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);

        progressDialog.show();

        auth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    } else if (!UserInteraction.hasInternetConnection(LoginActivity.this)) {
                        UserInteraction.showPopUpSnackBar(getString(R.string.internet_connection), v,
                                getApplicationContext());
                    } else {
                        UserInteraction.showPopUpSnackBar(getString(R.string.error_base), v,
                                getApplicationContext());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}

