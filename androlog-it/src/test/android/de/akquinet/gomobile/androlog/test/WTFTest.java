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
import java.io.IOException;
import java.util.Properties;

import android.os.Environment;

import de.akquinet.android.androlog.Log;
import junit.framework.Assert;
import junit.framework.TestCase;

public class WTFTest extends TestCase {

    public void setUp() {
        Log.reset();
        Log.activateLogging();
        Log.setDefaultLogLevel(Log.VERBOSE);
    }

    public void testWTFWhenLogActive() {
        Assert.assertTrue(Log.setWTFDelegation(true));
        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        String message = "This is a REALLY BAD error";
        int x = Log.wtf(tag, message);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWhenLogInactive() {
        Log.deactivateLogging();
        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        String message = "This is a REALLY BAD error";
        int x = Log.wtf(tag, message);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWhenLogLevelSetToError() {
        Log.setDefaultLogLevel(Log.ERROR);
        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        String message = "This is a REALLY BAD error";
        int x = Log.wtf(tag, message);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWhenLogLevelSetToAssert() {
        Log.setDefaultLogLevel(Log.ASSERT);
        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        String message = "This is a REALLY BAD error";
        int x = Log.wtf(tag, message);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWithThis() {
        String message = "This is a REALLY BAD error";
        int x = Log.wtf(this, message);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWithThisWithException() {
        String message = "This is a REALLY BAD error";
        Exception ex = new NullPointerException();
        int x = Log.wtf(this, message, ex);
        Assert.assertTrue(x > 0);
    }

    public void testWTFWithExceptionOnly() {
        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        Exception ex = new NullPointerException();
        int x = Log.wtf(tag, ex);
        Assert.assertTrue(x > 0);
    }

    public void testDisablingDelegationFromConfiguration() {
        Log.reset();
        Log.setWTFDelegation(true);
        Properties props = new Properties();
        props.setProperty(Log.ANDROLOG_ACTIVE, "true");
        props.setProperty(Log.ANDROLOG_DELEGATE_WTF, "false");
        Log.configure(props);

        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        Exception ex = new NullPointerException();
        int x = Log.wtf(tag, ex);
        Assert.assertTrue(x > 0);
        // A real wtf produce more than 1000 characters.
        Assert.assertTrue("" + x, x < 1000);
    }

    public void testDisablingDelegationFromFile() throws IOException {
        Log.reset();
        Log.setWTFDelegation(true);
        Properties props = new Properties();
        props.setProperty(Log.ANDROLOG_ACTIVE, "true");
        props.setProperty(Log.ANDROLOG_DELEGATE_WTF, "false");

        File file = new File(Environment.getExternalStorageDirectory(),
            "wtf.properties");
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        props.store(out, "Disable WTF delegation from file");
        out.close();

        Log.init("wtf.properties");

        String tag = "de.akquinet.gomobile.androlog.test.WTFTest";
        Exception ex = new NullPointerException();
        int x = Log.wtf(tag, ex);
        Assert.assertTrue(x > 0);
        // A real wtf produce more than 1000 characters.
        Assert.assertTrue("" + x, x < 1000);
    }


}
