package com.messenger.mand.interactions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class LanguageContextWrapper extends ContextWrapper {

    public LanguageContextWrapper(Context context) {
        super(context);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ContextWrapper wrap(@NotNull Context context, @NotNull String language) {
        Configuration config = context.getResources().getConfiguration();
        Locale sysLocale;
        sysLocale = getSystemLocale(config);
        if (!language.equals("") && !sysLocale.getLanguage().equals(language)) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            setSystemLocale(config, locale);
            context = context.createConfigurationContext(config);
        }
        return new LanguageContextWrapper(context);
    }

    @Contract(pure = true)
    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(@NotNull Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(@NotNull Configuration config){
        return config.getLocales().get(0);
    }

    @SuppressWarnings("deprecation")
    public static void setSystemLocaleLegacy(@NotNull Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(@NotNull Configuration config, Locale locale){
        config.setLocale(locale);
    }

}