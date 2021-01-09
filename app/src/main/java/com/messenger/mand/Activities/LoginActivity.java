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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.R;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    EditText password, email;
    TextView forgotPassword;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private Button loginButton;
    protected GoogleSignInButton googleButton;
    private Animation btnAnim;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //before auth!
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleLoginBtn);
        forgotPassword = findViewById(R.id.recoveryPassword);
        btnAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_button_pressing);

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

        loginButton.setOnClickListener(v -> {
            loginButton.startAnimation(btnAnim);
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

        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                UserInteraction.showPopUpSnackBar(""+e.getMessage(), getWindow().getCurrentFocus(),
                        getApplicationContext());
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
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
                                NavigationActivity.class);
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

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        final Dialog progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);

        progressDialog.show();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        String id = user.getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
                        reference.setValue(createUserMap(id, user.getDisplayName(), user)).addOnCompleteListener(task1 -> {
                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });
                        //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        UserInteraction.showPopUpSnackBar(getString(R.string.auth_fail_short),
                                getWindow().getCurrentFocus(), getApplicationContext());
                        //updateUI(null);
                    }
                }).addOnFailureListener(e -> UserInteraction.showPopUpSnackBar(""+e.getMessage(),
                        getWindow().getCurrentFocus(), getApplicationContext()));
    }

    private HashMap<String, String> createUserMap(String id, String username, FirebaseUser firebaseUser) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("id", id);
        hashMap.put("name", username);
        hashMap.put("email", firebaseUser.getEmail());
        hashMap.put("avatar", "default");
        hashMap.put("dateCreation", DataInteraction.getTimeNow());
        hashMap.put("status", "offline");
        hashMap.put("searchName", username.toLowerCase());

        return hashMap;
    }
}

