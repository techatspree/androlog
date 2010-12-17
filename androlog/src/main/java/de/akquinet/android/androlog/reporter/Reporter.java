package de.akquinet.android.androlog.reporter;

import java.util.Properties;

import android.content.Context;

public interface Reporter {

    public abstract void configure(Properties configuration);

    public abstract void send(Context context, String message, Throwable error);



}