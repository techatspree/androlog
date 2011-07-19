/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.android.androlog.reporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import de.akquinet.android.androlog.Log;

/**
 * Reporter sending the report by mail.
 */
public class MailReporter implements Reporter {

    /**
     * Mandatory Property to set the <tt>to</tt> address..
     */
    public static final String ANDROLOG_REPORTER_MAIL_ADDRESS = "androlog.reporter.mail.address";

    /**
     * The address.
     */
    private String to;

    /**
     * Configures the Mail Reporter. The given configuration <b>must</b> contain
     * the {@link MailReporter#ANDROLOG_REPORTER_MAIL_ADDRESS} property and it
     * must be a valid email address.
     *
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {
        String u = configuration.getProperty(ANDROLOG_REPORTER_MAIL_ADDRESS);
        if (u == null) {
            Log.e(this, "The Property " + ANDROLOG_REPORTER_MAIL_ADDRESS
                    + " is mandatory");
            return;
        }
        to = u;
    }

    /**
     * If the reporter was configured correctly, post the report to the set
     * e-mail address. IF the mail cannot be sent (no mail client), the method
     * returns <code>false</code>
     *
     * @see de.akquinet.android.androlog.reporter.Reporter#send(android.content.Context,
     *      java.lang.String, java.lang.Throwable)
     */
    @Override
    public boolean send(Context context, String mes, Throwable err) {
        if (to != null) {
            String report = new Report(context, mes, err).getReportAsJSON()
                    .toString();
            
            String reportFilePath =
                    Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/androlog-report-" + System.currentTimeMillis() + ".json";

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Application Error Report");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });

            try {
                writeStringToFile(report, reportFilePath);
                // Add report as email attachment.
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse ("file://" + reportFilePath));
                intent.putExtra(Intent.EXTRA_TEXT, "- Please add some info to this error report here, thank you -");
                intent.setType("application/json");
            }
            catch (IOException e) {
                // We could not write to SD card.
                // Fallback: Write the report to the email body.
                intent.putExtra(Intent.EXTRA_TEXT, report);
                intent.setType("message/rfc822");
            }

            // Create a new task because we're not sure to be an Activity
            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            try {
                context.startActivity(intent);
                return true;
            } catch (android.content.ActivityNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private void writeStringToFile(String string, String destFilePath) throws IOException {
        BufferedWriter out =
                    new BufferedWriter(new FileWriter(destFilePath));
        out.write(string);
        out.close();
    }

}
