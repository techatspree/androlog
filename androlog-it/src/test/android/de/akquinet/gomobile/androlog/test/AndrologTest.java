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

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.akquinet.android.androlog.Constants;
import de.akquinet.android.androlog.Log;

public class AndrologTest extends TestCase {

    public void setUp() {
        Log.activateLogging();
        Log.setDefaultLogLevel(Constants.VERBOSE);
    }

    public void testDWithThis() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a DEBUG test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.d(this, message);
        Assert.assertEquals(expected, x);
    }

    public void testDWithThisWithException() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a DEBUG test";
        Exception ex = new NullPointerException();
        int expected = tag.length() + message.length() + 3 + 904; // Size of the
                                                                    // stack
                                                                    // trace
        int x = Log.d(this, message, ex);
        Assert.assertEquals(expected, x);
    }

    public void testWWithThis() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a WARNING test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.w(this, message);
        Assert.assertEquals(expected, x);
    }

    public void testWWithThisWithException() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a WARNING test";
        Exception ex = new NullPointerException();
        int expected = tag.length() + message.length() + 3 + 904; // Size of the
                                                                    // stack
                                                                    // trace
        int x = Log.w(this, message, ex);
        Assert.assertEquals(expected, x);
    }

    public void testVWithThis() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a VERBOSE test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.v(this, message);
        Assert.assertEquals(expected, x);
    }

    public void testVWithThisWithException() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a VERBOSE test";
        Exception ex = new NullPointerException();
        int expected = tag.length() + message.length() + 3 + 904; // Size of the
                                                                    // stack
                                                                    // trace
        int x = Log.v(this, message, ex);
        Assert.assertEquals(expected, x);
    }

    public void testEWithThis() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is an ERROR test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.e(this, message);
        Assert.assertEquals(expected, x);
    }

    public void testEWithThisWithException() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a ERROR test";
        Exception ex = new NullPointerException();
        int expected = tag.length() + message.length() + 3 + 904; // Size of the
                                                                    // stack
                                                                    // trace
        int x = Log.e(this, message, ex);
        Assert.assertEquals(expected, x);
    }

    public void testIWithThis() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a INFO test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.i(this, message);
        Assert.assertEquals(expected, x);
    }

    public void testIWithThisWithException() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a INFO test";
        Exception ex = new NullPointerException();
        int expected = tag.length() + message.length() + 3 + 905; // Size of the
                                                                    // stack
                                                                    // trace
        int x = Log.i(this, message, ex);
        Assert.assertEquals(expected, x);
    }

    public void testIWithCaller() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a INFO test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.i(message);
        Assert.assertEquals(expected, x);
    }

    public void testDWithCaller() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a DEBUG test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.d(message);
        Assert.assertEquals(expected, x);
    }

    public void testWWithCaller() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a WARNING test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.w(message);
        Assert.assertEquals(expected, x);
    }

    public void testVWithCaller() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is a VERBOSE test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.v(message);
        Assert.assertEquals(expected, x);
    }

    public void testEWithCaller() {
        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is an ERROR test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.e(message);
        Assert.assertEquals(expected, x);
    }

    public void testWhenAdronlogIsDisabled() {
        Log.deactivateLogging();
        // V
        Assert.assertEquals(0, Log.v("xxx"));
        Assert.assertEquals(0, Log.v(this, "xxx"));
        Assert.assertEquals(0, Log.v(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.v("TAG", "xxx"));
        Assert.assertEquals(0, Log.v("TAG", "xxx", new NullPointerException()));

        // D
        Assert.assertEquals(0, Log.d("xxx"));
        Assert.assertEquals(0, Log.d(this, "xxx"));
        Assert.assertEquals(0, Log.d(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.d("TAG", "xxx"));
        Assert.assertEquals(0, Log.d("TAG", "xxx", new NullPointerException()));

        // I
        Assert.assertEquals(0, Log.i("xxx"));
        Assert.assertEquals(0, Log.i(this, "xxx"));
        Assert.assertEquals(0, Log.i(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.i("TAG", "xxx"));
        Assert.assertEquals(0, Log.i("TAG", "xxx", new NullPointerException()));

        // W
        Assert.assertEquals(0, Log.w("xxx"));
        Assert.assertEquals(0, Log.w(this, "xxx"));
        Assert.assertEquals(0, Log.w(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.w("TAG", "xxx"));
        Assert.assertEquals(0, Log.w("TAG", "xxx", new NullPointerException()));

        // E
        Assert.assertEquals(0, Log.e("xxx"));
        Assert.assertEquals(0, Log.e(this, "xxx"));
        Assert.assertEquals(0, Log.e(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.e("TAG", "xxx"));
        Assert.assertEquals(0, Log.e("TAG", "xxx", new NullPointerException()));
    }

    public void testDisablingFromConfiguration() {
        Log.reset();
        Properties props = new Properties();
        props.setProperty(Constants.ANDROLOG_ACTIVE, "false");
        Log.configure(props);
        // V
        Assert.assertEquals(0, Log.v("xxx"));
        Assert.assertEquals(0, Log.v(this, "xxx"));
        Assert.assertEquals(0, Log.v(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.v("TAG", "xxx"));
        Assert.assertEquals(0, Log.v("TAG", "xxx", new NullPointerException()));

        // D
        Assert.assertEquals(0, Log.d("xxx"));
        Assert.assertEquals(0, Log.d(this, "xxx"));
        Assert.assertEquals(0, Log.d(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.d("TAG", "xxx"));
        Assert.assertEquals(0, Log.d("TAG", "xxx", new NullPointerException()));

        // I
        Assert.assertEquals(0, Log.i("xxx"));
        Assert.assertEquals(0, Log.i(this, "xxx"));
        Assert.assertEquals(0, Log.i(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.i("TAG", "xxx"));
        Assert.assertEquals(0, Log.i("TAG", "xxx", new NullPointerException()));

        // W
        Assert.assertEquals(0, Log.w("xxx"));
        Assert.assertEquals(0, Log.w(this, "xxx"));
        Assert.assertEquals(0, Log.w(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.w("TAG", "xxx"));
        Assert.assertEquals(0, Log.w("TAG", "xxx", new NullPointerException()));

        // E
        Assert.assertEquals(0, Log.e("xxx"));
        Assert.assertEquals(0, Log.e(this, "xxx"));
        Assert.assertEquals(0, Log.e(this, "xxx", new NullPointerException()));
        Assert.assertEquals(0, Log.e("TAG", "xxx"));
        Assert.assertEquals(0, Log.e("TAG", "xxx", new NullPointerException()));
    }

    public void testLogLevelsFromConfiguration() {
        Log.reset();
        Properties props = new Properties();
        props.setProperty(Constants.ANDROLOG_ACTIVE, "true");
        props.setProperty(Constants.ANDROLOG_DEFAULT_LEVEL, "ERROR");
        props.setProperty("my.log.verbose", "VERBOSE");
        props.setProperty("my.log.debug", "DEBUG");
        props.setProperty("my.log.info", "INFO");
        props.setProperty("my.log.warn", "WARN");
        props.setProperty("my.log.error", "ERROR");
        props.setProperty("my.log.assert", "ASSERT");
        Log.configure(props);

        String message = "This is a VERBOSE test";
        String tag = "my.log.verbose";
        int expected = tag.length() + message.length() + 3;
        int x = Log.v(tag, message);
        Assert.assertEquals(expected, x);
        x = Log.i(tag, message);
        Assert.assertEquals(expected, x);

        message = "This is a DEBUG test";
        tag = "my.log.debug";
        expected = tag.length() + message.length() + 3;
        x = Log.v(tag, message);
        Assert.assertEquals(0, x);
        x = Log.d(tag, message);
        Assert.assertEquals(expected, x);
        x = Log.i(tag, message);
        Assert.assertEquals(expected, x);

        message = "This is a INFO test";
        tag = "my.log.info";
        expected = tag.length() + message.length() + 3;
        x = Log.d(tag, message);
        Assert.assertEquals(0, x);
        x = Log.i(tag, message);
        Assert.assertEquals(expected, x);
        x = Log.w(tag, message);
        Assert.assertEquals(expected, x);

        message = "This is a WARN test";
        tag = "my.log.warn";
        expected = tag.length() + message.length() + 3;
        x = Log.i(tag, message);
        Assert.assertEquals(0, x);
        x = Log.w(tag, message);
        Assert.assertEquals(expected, x);
        x = Log.e(tag, message);
        Assert.assertEquals(expected, x);

        message = "This is a ERROR test";
        tag = "my.log.error";
        expected = tag.length() + message.length() + 3;
        x = Log.w(tag, message);
        Assert.assertEquals(0, x);
        x = Log.e(tag, message);
        Assert.assertEquals(expected, x);
    }

    public void testDefaultLevelFromConfiguration() {
        Log.reset();
        Properties props = new Properties();
        props.setProperty(Constants.ANDROLOG_ACTIVE, "true");
        props.setProperty(Constants.ANDROLOG_DEFAULT_LEVEL, "ERROR");
        Log.configure(props);

        String tag = "de.akquinet.gomobile.androlog.test.AndrologTest";
        String message = "This is an ERROR test";
        int expected = tag.length() + message.length() + 3;
        int x = Log.e(this, message);
        Assert.assertEquals(expected, x);

        expected = tag.length() + message.length() + 3;
        x = Log.i(this, message);
        Assert.assertEquals(0, x);
    }

}
