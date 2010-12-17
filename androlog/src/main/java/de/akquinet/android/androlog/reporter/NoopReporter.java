package de.akquinet.android.androlog.reporter;

import java.util.Properties;

import android.content.Context;

/**
 * Dummy Reporter doing nothing...
 */
public class NoopReporter implements Reporter {

    /**
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {
        // Do nothing.
    }

    /**
     * @see de.akquinet.android.androlog.reporter.Reporter#send(android.content.Context,
     *      java.lang.String, java.lang.Throwable)
     */
    @Override
    public boolean send(Context context, String message, Throwable error) {
        // Always return true.
        return true;
    }

}
