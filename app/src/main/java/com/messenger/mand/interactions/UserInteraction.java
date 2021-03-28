package com.messenger.mand.Interactions;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.messenger.mand.R;

import static androidx.core.content.ContextCompat.getColor;

public class UserInteraction {

    public static void showPopUpSnackBar(String text, View v, Context context) {
        Snackbar snackbar = Snackbar.make(v, text, Snackbar.
                LENGTH_SHORT).setTextColor(getColor(context, R.color.white));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.setBackgroundTint(getColor(context, R.color.red));
        snackbar.show();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocusedView = activity.getCurrentFocus();

        if (currentFocusedView != null) {
            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static String getTrimLen(EditText et) {
        return et.getText().toString().trim();
    }
}
