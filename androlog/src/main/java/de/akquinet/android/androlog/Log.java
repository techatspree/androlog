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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

/**
 * Implements a small layer on top of the
 * <a href="http://developer.android.com/reference/android/util/Log.html">Android Log</a>
 * to provide programmatic enabling and disabling of logging.
 */
public class Log {
    /**
     * Androlog Prefix. Properties starting with this prefix are not considered
     * as valid tags
     */
    public static final String ANDROLOG_PREFIX = "androlog.";

    /**
     * Property to set the default log level.
     */
    public static final String ANDROLOG_DEFAULT_LEVEL = "androlog.default.level";

    /**
     * Property to activate androlog.
     */
    public static final String ANDROLOG_ACTIVE = "androlog.active";

    public static final String ANDROLOG_PROPERTIES = "androlog.properties";

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = android.util.Log.VERBOSE;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = android.util.Log.DEBUG;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = android.util.Log.INFO;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = android.util.Log.WARN;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = android.util.Log.ERROR;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = android.util.Log.ASSERT;

    /**
     * Global activation flag.
     */
    private static boolean activated = true;
    /**
     * Default log level.
     */
    private static int defaultLogLevel = INFO;

    /**
     * Map storing the log levels.
     */
    private static final Map<String, Integer> logLevels = new HashMap<String, Integer>();

    /**
     * Private constructor to avoid creating instances of {@link Log}
     */
    private Log() {
        // Nothing to do.
    }

    /**
     * Activates the logging.
     */
    public static void activateLogging() {
        activated = true;
    }

    /**
     * Deactivating the logging.
     */
    public static void deactivateLogging() {
        activated = false;
    }

    /**
     * Resets the configuration.
     */
    public static void reset() {
        deactivateLogging();
        defaultLogLevel = INFO;
        logLevels.clear();
    }

    /**
     * Android init method. This method uses the Android API. Inits the logger
     * by reading the file: <code>/SDCARD/fileName</code>. The file must be a
     * valid Java properties file.
     *
     * @param fileName
     *            the file name
     */
    public static void init(String fileName) {
        reset();

        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard == null || !sdcard.exists() || !sdcard.canRead()) {
            return;
        }

        String sdCardPath = sdcard.getAbsolutePath();

        File propFile = new File(sdCardPath + "/" + fileName);
        if (!propFile.exists()) {
            return;
        }

        Properties configuration = new Properties();
        FileInputStream fileIs;
        try {
            fileIs = new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            // should not happen, we check that above
            return;
        }
        try {
            // There is no load(Reader) method on Android,
            // so we have to use InputStream
            configuration.load(fileIs);
            // Then call configure.
            configure(configuration);
        } catch (IOException e) {
            return;
        } finally {
            closeQuietly(fileIs);
        }
    }

    /**
     * Android init method. This method uses the Android API. This methods
     * computes the package ({@link Context#getPackageName()}) of the
     * application configuring the logger, and initializes the logger with a
     * property file named 'package.properties' on the SDCARD. If the file is
     * readable and exist, the Log is configured. The file must be a valid Java
     * properties file.
     *
     * @see Properties
     */
    public static void init(Context context) {
        init(context.getPackageName() + ".properties");
    }

    /**
     * Android init method. This method uses the Android API. It reads the
     * {@link Log#ANDROLOG_PROPERTIES} file on the SDCARD. If the file is
     * readable and exist, the Log is configured. The file must be a valid Java
     * properties file.
     *
     * @see Properties
     */
    public static void init() {
        init(ANDROLOG_PROPERTIES);
    }

    /**
     * Parses the given level to get the log level. This method supports both
     * integer level and String level.
     *
     * @param level
     *            the level
     * @return the parsed level or the default level if the level cannot be
     *         parsed.
     */
    private static int getLevel(String level) {
        try {
            return Integer.parseInt(level);
        } catch (NumberFormatException e) {
            // Try to read the string.
            if ("VERBOSE".equalsIgnoreCase(level)) {
                return VERBOSE;
            } else if ("DEBUG".equalsIgnoreCase(level)) {
                return DEBUG;
            } else if ("INFO".equalsIgnoreCase(level)) {
                return INFO;
            } else if ("WARN".equalsIgnoreCase(level)) {
                return WARN;
            } else if ("ERROR".equalsIgnoreCase(level)) {
                return ERROR;
            } else if ("ASSERT".equalsIgnoreCase(level)) {
                return ASSERT;
            }
        }

        return defaultLogLevel;
    }

    /**
     * Sets the default log level.
     *
     * @param logLevel
     *            the log level
     */
    public static void setDefaultLogLevel(int logLevel) {
        defaultLogLevel = logLevel;
    }

    /**
     * Gets the default log level.
     *
     * @return the log level
     */
    public static int getDefaultLogLevel() {
        return defaultLogLevel;
    }

    /**
     * Configures the logger with the given properties.
     *
     * @param configuration
     *            the configuration
     */
    public static void configure(Properties configuration) {

        boolean activate = "true".equalsIgnoreCase(""
                + configuration.get(ANDROLOG_ACTIVE));

        if (activate) {
            activateLogging();
        }

        if (configuration.containsKey(ANDROLOG_DEFAULT_LEVEL)) {
            String level = configuration.getProperty(ANDROLOG_DEFAULT_LEVEL);
            defaultLogLevel = getLevel(level);
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>) configuration
                .propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.startsWith(ANDROLOG_PREFIX)) {
                String level = configuration.getProperty(name);
                int log = getLevel(level);
                logLevels.put(name, log);
            }
        }
    }

    /**
     * Send a {@link #VERBOSE} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int v(String tag, String msg) {
        if (isLoggable(tag, VERBOSE)) {
            return android.util.Log.v(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        if (isLoggable(tag, VERBOSE)) {
            return android.util.Log.v(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #VERBOSE} log message.
     *
     * @param object
     *            The object logging this message.
     * @param msg
     *            The message you would like logged.
     */
    public static int v(Object object, String msg) {
        if (object != null) {
            return v(object.getClass().getName(), msg);
        }
        return 0;
    }

    /**
     * Send a {@link #VERBOSE} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int v(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }

        String caller = getCaller();
        if (caller != null) {
            return v(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     *
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int v(Object object, String msg, Throwable tr) {
        if (object != null) {
            return v(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #DEBUG} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (isLoggable(tag, DEBUG)) {
            return android.util.Log.d(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (isLoggable(tag, DEBUG)) {
            return android.util.Log.d(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #DEBUG} log message.
     *
     * @param object
     *            The object logging this message.
     * @param msg
     *            The message you would like logged.
     */
    public static int d(Object object, String msg) {
        if (object != null) {
            return d(object.getClass().getName(), msg);
        }
        return 0;
    }

    /**
     * Send a {@link #DEBUG} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int d(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }

        String caller = getCaller();
        if (caller != null) {
            return d(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     *
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int d(Object object, String msg, Throwable tr) {
        if (object != null) {
            return d(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * Send an {@link #INFO} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int i(String tag, String msg) {
        if (isLoggable(tag, INFO)) {
            return android.util.Log.i(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (isLoggable(tag, INFO)) {
            return android.util.Log.i(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #INFO} log message.
     *
     * @param object
     *            The object logging this message.
     * @param msg
     *            The message you would like logged.
     */
    public static int i(Object object, String msg) {
        if (object != null) {
            return i(object.getClass().getName(), msg);
        }
        return 0;
    }

    /**
     * Send a {@link #INFO} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int i(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }

        String caller = getCaller();
        if (caller != null) {
            return i(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     *
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int i(Object object, String msg, Throwable tr) {
        if (object != null) {
            return i(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int w(String tag, String msg) {
        if (isLoggable(tag, WARN)) {
            return android.util.Log.w(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (isLoggable(tag, WARN)) {
            return android.util.Log.w(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message.
     *
     * @param object
     *            The object logging this message.
     * @param msg
     *            The message you would like logged.
     */
    public static int w(Object object, String msg) {
        if (object != null) {
            return w(object.getClass().getName(), msg);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int w(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }
        String caller = getCaller();
        if (caller != null) {
            return w(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     *
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int w(Object object, String msg, Throwable tr) {
        if (object != null) {
            return w(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     *
     * @param tr
     *            An exception to log
     */
    public static int w(String tag, Throwable tr) {
        if (isLoggable(tag, WARN)) {
            return android.util.Log.w(tag, tr);
        }
        return 0;
    }

    /**
     * Send an {@link #ERROR} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int e(String tag, String msg) {
        if (isLoggable(tag, ERROR)) {
            return android.util.Log.e(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        if (isLoggable(tag, ERROR)) {
            return android.util.Log.e(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #ERROR} log message.
     *
     * @param object
     *            The object logging this message.
     * @param msg
     *            The message you would like logged.
     */
    public static int e(Object object, String msg) {
        if (object != null) {
            return e(object.getClass().getName(), msg);
        }
        return 0;
    }

    /**
     * Send a {@link #ERROR} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int e(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }
        String caller = getCaller();
        if (caller != null) {
            return e(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     *
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log
     */
    public static int e(Object object, String msg, Throwable tr) {
        if (object != null) {
            return e(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at
     * the specified level.
     *
     * The default level of any tag is set as specified in
     * {@link #setDefaultLogLevel(int)}. This means that any level above and
     * including that level will be logged.
     *
     * Use {@link #configure(Properties)} to define different log levels for each tag.
     *
     * @param tag
     *            The tag to check.
     * @param level
     *            The level to check.
     * @return Whether or not that this is allowed to be logged.
     * @throws IllegalArgumentException
     *             is thrown if the tag.length() > 23.
     */
    public static boolean isLoggable(String tag, int level) {
        if (!activated) {
            return false;
        }
        Integer logLevel = logLevels.get(tag);
        if (logLevel == null) {
            logLevel = defaultLogLevel;
        }
        return level >= logLevel;
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

    private static void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * The classes contained in this list are escaped from the stack trace when
     * computing the tag level from the current stack trace.
     */
    private final static List<String> CLASSNAME_TO_ESCAPE = new ArrayList<String>();
    static {
        CLASSNAME_TO_ESCAPE.add("java.lang.Thread");
        CLASSNAME_TO_ESCAPE.add("dalvik.system.VMStack");
        CLASSNAME_TO_ESCAPE.add(Log.class.getName());
    };

    /**
     * Extracts the tag from the current stack trace.
     *
     * @return the qualified name of the first non escaped class on the stack.
     */
    private static String getCaller() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        if (stacks != null) {
            for (int i = 0; i < stacks.length; i++) {
                String cn = stacks[i].getClassName();
                if (cn != null && !CLASSNAME_TO_ESCAPE.contains(cn)) {
                    return cn;
                }
            }
        }
        return null;
    }
}
