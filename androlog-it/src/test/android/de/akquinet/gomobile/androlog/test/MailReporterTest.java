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
package de.akquinet.gomobile.androlog.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.auth.MalformedChallengeException;

import android.os.Environment;
import android.test.AndroidTestCase;
import de.akquinet.android.androlog.Constants;
import de.akquinet.android.androlog.Log;
import de.akquinet.android.androlog.reporter.MailReporter;

public class MailReporterTest extends AndroidTestCase {


    private File testContext;

    public void setUp() {
        try {
            Assert.assertTrue("No SDCard or not the permission to write",
                    Environment.getExternalStorageDirectory().canWrite());
            // Create files
            Properties propsDefault = new Properties();
            propsDefault.setProperty(Constants.ANDROLOG_ACTIVE, "true");
            propsDefault.setProperty(Constants.ANDROLOG_REPORT_ACTIVE, "true");
            propsDefault.setProperty(Constants.ANDROLOG_REPORT_REPORTERS, "de.akquinet.android.androlog.reporter.NoopReporter");



            Properties propsActive = new Properties();
            propsActive.setProperty(Constants.ANDROLOG_ACTIVE, "true");
            propsActive.setProperty(Constants.ANDROLOG_REPORT_ACTIVE, "true");
            propsActive.setProperty(Constants.ANDROLOG_REPORT_REPORTERS, "de.akquinet.android.androlog.reporter.MailReporter");
            propsActive.setProperty(MailReporter.ANDROLOG_REPORTER_MAIL_ADDRESS, "clement.escoffier@gmail.com");

            testContext = new File(Environment.getExternalStorageDirectory(),
                    getContext().getPackageName() + ".properties");
            testContext.createNewFile();
            (new File(Environment.getExternalStorageDirectory(), "tmp"))
                    .mkdir();

            FileOutputStream out = new FileOutputStream(testContext);
            propsActive.store(out, "Enable Androlog file");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public void tearDown() {
        testContext.delete();
    }

    public void testMail() {
        Log.init(getContext());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(2, list.size()); // i + w

        Log.report();
        Log.report("this is a user message", null);
        Exception error = new MalformedChallengeException("error message", new  NumberFormatException());
        Log.report(null, error);
    }

    public void testMailWithLongLog() {
        Log.init(getContext());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        for (int i = 0; i < 200; i++) {
            Log.w("" + i);
        }
        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(25, list.size());

        Log.report();
        Log.report("this is a user message", null);
        Exception error = new MalformedChallengeException("error message", new  NumberFormatException());
        Log.report(null, error);
    }

}
