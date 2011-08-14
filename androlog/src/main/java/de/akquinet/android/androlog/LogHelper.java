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
package de.akquinet.android.androlog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class LogHelper {

    /**
     * Parses the given level to get the log level. This method supports both
     * integer level and String level.
     *
     * @param level
     *            the level
     * @param defaultLogLevel
     * 			  the default log level
     * @return the parsed level or the default level if the level cannot be
     *         parsed.
     */
    protected static int getLevel(String level, int defaultLogLevel) {
        try {
            return Integer.parseInt(level);
        } catch (NumberFormatException e) {
            // Try to read the string.
            if ("VERBOSE".equalsIgnoreCase(level)) {
                return Constants.VERBOSE;
            } else if ("DEBUG".equalsIgnoreCase(level)) {
                return Constants.DEBUG;
            } else if ("INFO".equalsIgnoreCase(level)) {
                return Constants.INFO;
            } else if ("WARN".equalsIgnoreCase(level)) {
                return Constants.WARN;
            } else if ("ERROR".equalsIgnoreCase(level)) {
                return Constants.ERROR;
            } else if ("ASSERT".equalsIgnoreCase(level)) {
                return Constants.ASSERT;
            }
        }

        return defaultLogLevel;
    }

    public static void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Extracts the tag from the current stack trace.
     *
     * @return the qualified name of the first non escaped class on the stack.
     */
    public static String getCaller() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        if (stacks != null) {
            for (int i = 0; i < stacks.length; i++) {
                String cn = stacks[i].getClassName();
                if (cn != null && ! Constants.CLASSNAME_TO_ESCAPE.contains(cn)) {
                    return cn;
                }
            }
        }
        return null;
    }

    /**
     * Gets an input on a configuration file placed on the the SDCard.
     *
     * @param fileName
     *            the file name
     * @return the input stream to read the file or <code>null</code> if the
     *         file does not exist.
     */
    protected static InputStream getConfigurationFileFromSDCard(String fileName) {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard == null || !sdcard.exists() || !sdcard.canRead()) {
            return null;
        }

        String sdCardPath = sdcard.getAbsolutePath();

        File propFile = new File(sdCardPath + "/" + fileName);
        if (!propFile.exists()) {
            return null;
        }

        FileInputStream fileIs = null;
        try {
            fileIs = new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            // should not happen, we check that above
            return null;
        }

        return fileIs;
    }

    /**
     * Gets an input on a configuration file placed in the application assets.
     *
     * @param context
     *            the Android context to use
     * @param fileName
     *            the file name
     * @return the input stream to read the file or <code>null</code> if the
     *         file does not exist.
     */
    protected static InputStream getConfigurationFileFromAssets(Context context,
            String fileName) {
        if (context == null) {
            return null;
        }

        AssetManager assets = context.getAssets();
        if (assets == null) {
            return null;
        }

        try {
            return assets.open(fileName);
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr
     *            An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     *
     * @param priority
     *            The priority/type of this log message
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    /**
     * Gets a String form of the log data.
     *
     * @param priority
     *            The priority/type of this log message
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            The error, can be <code>null</code>
     * @return The String form.
     */
    public static String print(int priority, String tag, String msg,
            Throwable tr) {
        // Compute the letter for the given priority
        String p = "X"; // X => Unknown
        switch (priority) {
        case Constants.DEBUG:
            p = "D";
            break;
        case Constants.INFO:
            p = "I";
            break;
        case Constants.WARN:
            p = "W";
            break;
        case Constants.ERROR:
            p = "E";
            break;
        case Constants.ASSERT:
            p = "F";
            break;
        }
        if (tr == null) {
            return p + "/" + tag + ": " + msg;
        } else {
            return p + "/" + tag + ": " + msg + "\n" + getStackTraceString(tr);
        }
    }

}
