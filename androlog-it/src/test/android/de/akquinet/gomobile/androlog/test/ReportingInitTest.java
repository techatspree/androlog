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
import android.os.Environment;
import android.test.AndroidTestCase;
import de.akquinet.android.androlog.Constants;
import de.akquinet.android.androlog.Log;

public class ReportingInitTest extends AndroidTestCase {

    private File activate;
    private File defaultFile;
    private File deactivate;
    private File activateAndDefault;
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

            defaultFile = new File(Environment.getExternalStorageDirectory(),
                    Constants.ANDROLOG_PROPERTIES);
            defaultFile.createNewFile();
            FileOutputStream out = new FileOutputStream(defaultFile);
            propsDefault.store(out, "Default file");
            out.close();

            Properties propsActive = new Properties();
            propsActive.setProperty(Constants.ANDROLOG_ACTIVE, "true");
            propsActive.setProperty(Constants.ANDROLOG_REPORT_ACTIVE, "true");
            propsActive.setProperty(Constants.ANDROLOG_REPORT_REPORTERS, "de.akquinet.android.androlog.reporter.NoopReporter");
            activate = new File(Environment.getExternalStorageDirectory(),
                    "activate.properties");
            testContext = new File(Environment.getExternalStorageDirectory(),
                    getContext().getPackageName() + ".properties");
            activate.createNewFile();
            testContext.createNewFile();
            (new File(Environment.getExternalStorageDirectory(), "tmp"))
                    .mkdir();
            out = new FileOutputStream(activate);
            propsActive.store(out, "Enable Androlog file");
            out.close();
            out = new FileOutputStream(testContext);
            propsActive.store(out, "Enable Androlog file");
            out.close();

            Properties propsDeactive = new Properties();
            propsDeactive.setProperty(Constants.ANDROLOG_ACTIVE, "false");
            propsDeactive.setProperty(Constants.ANDROLOG_REPORT_ACTIVE, "true");
            propsDeactive.setProperty(Constants.ANDROLOG_REPORT_REPORTERS, "de.akquinet.android.androlog.reporter.NoopReporter");
            deactivate = new File(Environment.getExternalStorageDirectory(),
                    "deactivate.properties");
            deactivate.createNewFile();
            out = new FileOutputStream(deactivate);
            propsDeactive.store(out, "Disable Androlog Log, Enable Reporting file");
            out.close();

            Properties propsDefaultLevel = new Properties();
            propsDefaultLevel.setProperty(Constants.ANDROLOG_ACTIVE, "true");
            propsDefaultLevel.setProperty(Constants.ANDROLOG_REPORT_ACTIVE, "true");
            propsDefaultLevel.setProperty(Constants.ANDROLOG_DEFAULT_LEVEL, "ERROR");
            propsDefaultLevel.setProperty(Constants.ANDROLOG_REPORT_DEFAULT_LEVEL, "ERROR");
            propsDefaultLevel.setProperty(Constants.ANDROLOG_REPORT_REPORTERS, "de.akquinet.android.androlog.reporter.NoopReporter");
            activateAndDefault = new File(
                    Environment.getExternalStorageDirectory(),
                    "activateAndDefault.properties");
            activateAndDefault.createNewFile();
            out = new FileOutputStream(activateAndDefault);
            propsDefaultLevel.store(out,
                    "Enable Androlog file and set default log level");
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public void tearDown() {
        // Delete files
        activate.delete();
        defaultFile.delete();
        deactivate.delete();
        activateAndDefault.delete();
        testContext.delete();
    }

    public void testDefaultReport() {
        Log.init(getContext());
        Assert.assertEquals(Constants.INFO, Log.getDefaultLogLevel());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);

        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(2, list.size()); // i + w

    }

    public void testDefaultInitWhenTheFileDoesNotExist() {
        defaultFile.delete();
        Log.init();
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        List<String> list = Log.getReportedEntries();
        Assert.assertNull(list);
    }

    public void testReportWithActivateFile() {
        Log.init(getContext(), activate.getName());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(2, list.size()); // i + w
    }

    public void testReportWithMissingFile() {
        Log.init("missing.properties");
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        // Nothing reported
        List<String> list = Log.getReportedEntries();
        Assert.assertNull(list);
    }

    public void testReportWithDeactivatedLog() {
        Log.init(getContext(), deactivate.getName());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(2, list.size()); // i + w

    }

    public void testReportWithActivateAndDefaultFile() {
        Log.init(getContext(), activateAndDefault.getName());
        Assert.assertEquals(Constants.ERROR, Log.getDefaultLogLevel());
        String message = "This is a INFO test";
        String tag = "my.log.info";
        Log.d(tag, message);
        Log.i(tag, message);
        Log.w(tag, message);
        Log.e(tag, message);
        List<String> list = Log.getReportedEntries();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(1, list.size()); // e
    }

    public void testInitWithContext() {
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
    }

//    public void testInitWithContextAndAssets() {
//        Log.init(getContext());
//        String message = "This is a INFO test";
//        String tag = "my.log.info";
//        int expected = tag.length() + message.length() + 3;
//        int x = Log.d(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.i(tag, message);
//        Assert.assertEquals(expected, x);
//        x = Log.w(tag, message);
//        Assert.assertEquals(expected, x);
//    }
//
//    public void testInitWithContextAndAssetsAndFileName() {
//        Log.init(getContext(), "androlog/my-configuration.properties");
//        String message = "This is a INFO test";
//        String tag = "my.log.info";
//        int expected = tag.length() + message.length() + 3;
//        int x = Log.d(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.i(tag, message);
//        Assert.assertEquals(expected, x);
//        x = Log.w(tag, message);
//        Assert.assertEquals(expected, x);
//    }
//
//    public void testInitWithMissingContext() {
//        testContext.delete();
//        // We create a mock context without asset.
//        // As AssetManager cannot be overridden,
//        // we just return null as AssetManager.
//        Log.init(new MockContext() {
//
//            @Override
//            public String getPackageName() {
//                return getContext().getPackageName();
//            }
//
//            @Override
//            public AssetManager getAssets() {
//                return null;
//            }
//        });
//        String message = "This is a INFO test";
//        String tag = "my.log.info";
//        int x = Log.d(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.i(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.w(tag, message);
//        Assert.assertEquals(0, x);
//    }
//
//    public void testInitWithNullAndNull() {
//        testContext.delete();
//        defaultFile.delete();
//        Log.init(null, null);
//        String message = "This is a INFO test";
//        String tag = "my.log.info";
//        int x = Log.d(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.i(tag, message);
//        Assert.assertEquals(0, x);
//        x = Log.w(tag, message);
//        Assert.assertEquals(0, x);
//    }

}
