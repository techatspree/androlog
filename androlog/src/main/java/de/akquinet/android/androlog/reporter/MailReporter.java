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
import java.io.File;
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
public class MailReporter implements EnhancedReporter {

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
     * @see #send(Context, Report)
     * @see de.akquinet.android.androlog.reporter.Reporter#send(Context, String, Throwable)
     */
    @Override
    public boolean send(Context context, String mes, Throwable err) {
        return send(context, new Report(context, mes, err));
    }

    /**
     * If the reporter was configured correctly, post the report to the set
     * e-mail address. IF the mail cannot be sent (no mail client), the method
     * returns <code>false</code>
     *
     * @see de.akquinet.android.androlog.reporter.EnhancedReporter#send(Context, Report)
     */
    @Override
    public boolean send(Context context, Report report) {
        if (to != null) {
            String reportStr = report.asJSON()
                    .toString();
            
            // Create androlog folder
            File andrologFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/androlog");
            andrologFolder.mkdir();
            String reportFilePath =
                    andrologFolder.getPath()
                            + "/androlog-report-" + createReportFilenameSuffix(context, report) + ".json";

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, createSubject(context, report));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });

            try {
                File reportFile = new File(reportFilePath);
                writeStringToFile(reportStr, reportFile);
                // Add report as email attachment.
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(reportFile));
                intent.putExtra(Intent.EXTRA_TEXT, "- Please add some info to this error report here, thank you -");
                intent.setType("application/json");
            }
            catch (IOException e) {
                // We could not write to SD card.
                // Fallback: Write the report to the email body.
                e.printStackTrace();
                intent.putExtra(Intent.EXTRA_TEXT, reportStr);
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

    /**
     * Override to customise report filename suffix, which forms part of attachment name
     * @param context the android context
     * @param report the report being sent
     * @return string appended to report filename (before <code>.json</code>).
     */
    protected String createReportFilenameSuffix(Context context, Report report) {
        return "" + report.getCreated();
    }

    /**
     * Override to customise email subject using application or report properties
     * @param context the android context
     * @param report the report being sent
     * @return email subject
     */
    protected String createSubject(Context context, Report report) {
        return "Application Error Report";
    }

    private void writeStringToFile(String string, File file) throws IOException {
        BufferedWriter out =
                    new BufferedWriter(new FileWriter(file));
        out.write(string);
        out.close();
    }

}
