package de.akquinet.android.androlog.reporter;

import java.util.Properties;

import android.content.Context;

public class NoopReporter implements Reporter {

    /* (non-Javadoc)
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {

    }

    @Override
    public void send(Context context, String message, Throwable error) {
        // TODO Auto-generated method stub

    }

}
