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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

/**
 * Implements a small layer on top of the
 * <a href="http://developer.android.com/reference/android/util/Log.html">Android Log</a>
 * to provide programmatic enabling and disabling of logging.
 * <p>
 * To support <code>wtf</code> methods, Androlog detects if we're on Android 2.2+. If not,
 * the ASSERT level is used.
 * </p>
 * <p>
 * Log methods returns an integer. This integer is the number of byte written in the log.
 * If a method returns 0, the message was not logged.
 * </p>
 */
public class Log {

    public static final String ANDROLOG_REPORT_LOG_ITEMS = "androlog.report.log.items";

    public static final String ANDROLOG_REPORT_SENDERS = "androlog.report.senders";

    public static final String ANDROLOG_REPORT_DEFAULT_LEVEL = "androlog.report.default.level";

    public static final String ANDROLOG_REPORT_ACTIVE = "androlog.report.active";

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

    /**
     * Androlog default properties file.
     */
    public static final String ANDROLOG_PROPERTIES = "androlog.properties";

    /**
     * Property to deactivate the delagation on the Android Log wtf method
     * which may cause the process to terminate.
     * On android 2.2, this property allow to disable this delegation. Androlog
     * then just log an ASSERT message.
     */
    public static final String ANDROLOG_DELEGATE_WTF = "androlog.delegate.wtf";

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
     * Default report level.
     */
    private static int defaultReportLevel = INFO;


    /**
     * Android Log wtf method.
     * wtf(String tag, String msg)
     */
    private static Method wtfTagMessageMethod;

    /**
     * Android Log wtf method.
     * wtf(String tag, Throwable tr)
     */
    private static Method wtfTagErrorMethod;

    /**
     * Android Log wtf method.
     * wtf(String tag, String msg, Throwable tr)
     */
    private static Method wtfTagMessageErrorMethod;

    /**
     * Sets to true if the wtf method delegates on the Android
     * Log wtf methods which may cause the process to terminate.
     */
    private static boolean useWTF;

    /**
     * The list of log entries
     */
    private static List<String> entries;

    private static boolean enableLogEntryCollection;

    private static int maxOfEntriesInReports;

    private static boolean reportingActivated;

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
     * Activates the reporting.
     */
    public static void activateReporting() {
        reportingActivated = true;
    }

    /**
     * Deactivating the reporting.
     */
    public static void deactivateReporting() {
        reportingActivated = false;
    }

    /**
     * Enables or disables the delegation to the Android log
     * wtf methods. Those methods when present (android 2.2+)
     * may cause the termination of the application.
     * @param delegation enables or disables the delegation
     * @return if the delegation is enabled. Indeed, on 1.6 and
     * 2.0 it can't be enabled.
     */
    public static boolean setWTFDelegation(boolean delegation) {
        // We can't enable the wtf delegation if we're on 1.6 or 2.0
        if (wtfTagErrorMethod == null) {
            useWTF = false;
        } else {
            useWTF = delegation;
        }
        return useWTF;
    }

    /**
     * Resets the configuration.
     */
    public static void reset() {
        deactivateLogging();
        defaultLogLevel = INFO;
        detectWTFMethods();
        logLevels.clear();
    }

    /**
     * Androlog init method. This method uses the Android API. Inits the logger
     * by reading the file: <code>/SDCARD/fileName</code>. The file must be a
     * valid Java properties file.
     *
     * @param fileName
     *            the file name
     */
    public static void init(String fileName) {
        init(null, fileName);
    }

    /**
     * Androlog init method. This method uses the Android API. This methods
     * computes the package ({@link Context#getPackageName()}) of the
     * application configuring the logger, and initializes the logger with a
     * property file named 'package.properties' on the SDCARD or in the application
     * assets (if the file cannot be found on the SDCard). If the file is readable
     * and exist, the Log is configured. The file must be a valid Java properties file.
     *
     * @see Properties
     */
    public static void init(Context context) {
        init(context, null);
    }

    /**
     * Androlog init method. This method uses the Android API. It reads the
     * {@link Log#ANDROLOG_PROPERTIES} file on the SDCARD. If the file is
     * readable and exist, the Log is configured. The file must be a valid Java
     * properties file.
     *
     * @see Properties
     */
    public static void init() {
        init(null, ANDROLOG_PROPERTIES);
    }

    /**
     * Androlog Init Method. This method loads the Androlog Configuration from:
     * <ol>
     * <li><code>/SDCARD/fileName</code> if the file name if not <code>null</code></li>
     * <li><code>/SDCARD/Application_Package.properties</code> if
     * the file name is <code>null</code> and context is not <code>null</code></li>
     * <li><code>Application_Assets/fileName</code> if the file name if not <code>null</code>
     * and the context is not <code>null</code></li>
     * <li><code>Application_Assets/Application_Package.properties</code> if the file name is
     * <code>null</code> and the context is not <code>null</code></li>
     * </ol>
     * The first found file is used, allowing overriding the configuration by just pushing a file
     * on the SDCard.
     * Passing <code>null</code> to both parameters is equivalent to the case 2. If the lookup
     * failed, the logging is disabled.
     * @param context the application context
     * @param fileName the file name
     */
    public static void init(Context context, String fileName) {
        reset();

        String file = fileName;
        if (file == null  && context != null) {
            file = context.getPackageName() + ".properties";
        }

        // Check from SDCard
        InputStream fileIs = getConfigurationFileFromSDCard(file);
        if (fileIs == null) {
            // Check from Assets
            fileIs = getConfigurationFileFromAssets(context, file);
        }

        if (fileIs != null) {
            Properties configuration = new Properties();

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


    }

    /**
     * Gets an input on a configuration file
     * placed on the the SDCard.
     * @param fileName the file name
     * @return the input stream to read the file or <code>null</code>
     * if the file does not exist.
     */
    private static InputStream getConfigurationFileFromSDCard(String fileName) {
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
     * Gets an input on a configuration file
     * placed in the application assets.
     * @param context the Android context to use
     * @param fileName the file name
     * @return the input stream to read the file or <code>null</code>
     * if the file does not exist.
     */
    private static InputStream getConfigurationFileFromAssets(Context context, String fileName) {
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
     * Sets the default report level.
     *
     * @param logLevel
     *            the report level
     */
    public static void setDefaultReportLevel(int logLevel) {
        defaultReportLevel = logLevel;
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
     * Gets the default report level.

     * @return the log level
     */
    public static int getDefaultReportLevel() {
        return defaultReportLevel;
    }

    /**
     * Configures the logger with the given properties.
     *
     * @param configuration
     *            the configuration
     */
    public static void configure(Properties configuration) {

        boolean activate = "true".equalsIgnoreCase(
                configuration.getProperty(ANDROLOG_ACTIVE));
        if (activate) {
            activateLogging();
        }

        boolean activate4Report = "true".equalsIgnoreCase(
                configuration.getProperty(ANDROLOG_REPORT_ACTIVE));
        if (activate4Report) {
            activateReporting();
        }

        detectWTFMethods();

        if (configuration.containsKey(ANDROLOG_DEFAULT_LEVEL)) {
            String level = configuration.getProperty(ANDROLOG_DEFAULT_LEVEL);
            defaultLogLevel = getLevel(level);
        }

        if (configuration.containsKey(ANDROLOG_REPORT_DEFAULT_LEVEL)) {
            String level = configuration.getProperty(ANDROLOG_REPORT_DEFAULT_LEVEL);
            defaultReportLevel = getLevel(level);
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


        if (useWTF) {
            // Check if androlog configuration does not override this.
            if (configuration.containsKey(ANDROLOG_DELEGATE_WTF)) {
                String v = configuration.getProperty(ANDROLOG_DELEGATE_WTF);
                // If androlog.delegate.wtf is set to true, we really call Log.wtf which
                // may terminate the process.
                useWTF = "true".equals(v.toLowerCase());
                // In other cases, androlog does log a message in the ASSERT level.
            }
        }

        // Do we need to store the log entries for Reports ?
        enableLogEntryCollection = false;
        if (configuration.containsKey(ANDROLOG_REPORT_SENDERS)) {
            // We enable the collection only if we have senders
            String s = configuration.getProperty(ANDROLOG_REPORT_SENDERS);
            String[] senders = s.split(",");
            //TODO Create the senders.
            enableLogEntryCollection = true;
        }

        if (enableLogEntryCollection) {
            if (configuration.containsKey(ANDROLOG_REPORT_LOG_ITEMS)) {
                String p = configuration.getProperty(ANDROLOG_REPORT_LOG_ITEMS);
                maxOfEntriesInReports = Integer.parseInt(p);
            } else {
                maxOfEntriesInReports = 25; // Default
            }
            entries = new ArrayList<String>(maxOfEntriesInReports);
        }

    }

    /**
     * Check if the android Log class contains the <code>wtf</code>
     * method (Android 2.2+). In that case, the delegation to those
     * method is enabled. If not, calling {@link Log#wtf(Object, String)}
     * log a message with the level {@link Log#ASSERT}
     */
    private static void detectWTFMethods() {
        // Check if wtf exists (android 2.2+)
        // static int	 wtf(String tag, String msg)
        // static int	 wtf(String tag, Throwable tr)
        // static int	 wtf(String tag, String msg, Throwable tr)
        try {
            wtfTagMessageMethod = android.util.Log.class.getMethod("wtf", new Class[] {String.class, String.class});
            wtfTagErrorMethod = android.util.Log.class.getMethod("wtf", new Class[] {String.class, Throwable.class});
            wtfTagMessageErrorMethod = android.util.Log.class.getMethod("wtf", new Class[] {String.class,
                    String.class, Throwable.class});
            useWTF = true;
        } catch (Exception e) {
            // wtf is not defined, will use ASSERT level.
            useWTF = false;
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
        if (isReportable(VERBOSE)) {
            addEntry(print(VERBOSE, tag, msg, null));
        }
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
        if (isReportable(VERBOSE)) {
            addEntry(print(VERBOSE, tag, msg, tr));
        }
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
        if (isReportable(DEBUG)) {
            addEntry(print(DEBUG, tag, msg, null));
        }
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
        if (isReportable(DEBUG)) {
            addEntry(print(DEBUG, tag, msg, tr));
        }
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
        if (isReportable(INFO)) {
            addEntry(print(INFO, tag, msg, null));
        }
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
        if (isReportable(INFO)) {
            addEntry(print(INFO, tag, msg, tr));
        }
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
        if (isReportable(WARN)) {
            addEntry(print(WARN, tag, msg, null));
        }
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
        if (isReportable(WARN)) {
             addEntry(print(WARN, tag, msg, tr));
        }
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
        if (isReportable(WARN)) {
            addEntry(print(WARN, tag, "", null));
        }
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
        if (isReportable(ERROR)) {
            addEntry(print(ERROR, tag, msg, null));
        }
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
        if (isReportable(ERROR)) {
            addEntry(print(ERROR, tag, msg, tr));
        }
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
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT despite the logging is disabled.
     * Depending on system configuration, and on Android 2.2+, a report may be added to
     * the DropBoxManager and/or the process may be terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the Assert level. Those
     * log messages will always be logged.
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int wtf(String tag, String msg) {
        if (isReportable(ASSERT)) {
            addEntry(print(ASSERT, tag, msg, null));
        }
        if (isLoggable(tag, ASSERT)) {
            if (useWTF) {
                try {
                    return  (Integer)
                        wtfTagMessageMethod.invoke(null, new Object[] {tag, msg});
                } catch (Exception e) {
                    return println(ASSERT, tag, msg);
                }
            } else {
                return println(ASSERT, tag, msg);
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT despite the logging is disabled.
     * Depending on system configuration, and on Android 2.2+, a report may be added to
     * the DropBoxManager and/or the process may be terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the Assert level. Those
     * log messages will always be logged.
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param tr
     *            The exception to log
     */
    public static int wtf(String tag, Throwable tr) {
        if (isReportable(ASSERT)) {
            addEntry(print(ASSERT, tag, "", null));
        }
        if (isLoggable(tag, ASSERT)) {
            if (useWTF) {
                try {
                    return (Integer) wtfTagErrorMethod.invoke(null, new Object[] {tag, tr});
                } catch (Exception e) {
                    return println(ASSERT, tag, getStackTraceString(tr));
                }
            } else {
                return println(ASSERT, tag, getStackTraceString(tr));
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT despite the logging is disabled.
     * Depending on system configuration, and on Android 2.2+, a report may be added to
     * the DropBoxManager and/or the process may be terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the Assert level. Those
     * log messages will always be logged.
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            The exception to log
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        if (isReportable(ASSERT)) {
            addEntry(print(ASSERT, tag, msg, tr));
        }
        if (isLoggable(tag, ASSERT)) {
            if (useWTF) {
                try {
                    return (Integer) wtfTagMessageErrorMethod.invoke(null, new Object[] {tag, msg, tr});
                } catch (Exception e) {
                    return println(ASSERT, tag, msg + '\n' + getStackTraceString(tr));
                }
            } else {
                return println(ASSERT, tag, msg + '\n' + getStackTraceString(tr));
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT despite the logging is disabled.
     * Depending on system configuration, and on Android 2.2+, a report may be added to
     * the DropBoxManager and/or the process may be terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the Assert level. Those
     * log messages will always be logged.
     * @param object
     *            Used to compute the tag.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            The exception to log
     */
    public static int wtf(Object object, String msg, Throwable tr) {
        if (object != null) {
            return wtf(object.getClass().getName(), msg, tr);
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT despite the logging is disabled.
     * Depending on system configuration, and on Android 2.2+, a report may be added to
     * the DropBoxManager and/or the process may be terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the Assert level. Those
     * log messages will always be logged.
     * @param object
     *            Used to compute the tag.
     * @param msg
     *            The message you would like logged.
     */
    public static int wtf(Object object, String msg) {
        if (object != null) {
            return wtf(object.getClass().getName(), msg);
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
        if (!activated  && level != ASSERT) {
            return false;
        }
        Integer logLevel = logLevels.get(tag);
        if (logLevel == null) {
            logLevel = defaultLogLevel;
        }
        return level >= logLevel;
    }

    /**
     * Checks to see whether or not a log is reportable at
     * the specified level.
     *
     * The default level of any tag is set as specified in
     * {@link #setDefaultReportLevel(int)}. This means that any level above and
     * including that level will be logged.
     *
     * @param level
     *            The level to check.
     * @return Whether or not that this is allowed to be reported.
     */
    public static boolean isReportable(int level) {
        return reportingActivated
            && enableLogEntryCollection
            && level >= defaultReportLevel;
    }

    /**
     * Checks to see whether or not a log for the specified object is loggable at
     * the specified level. The tag is computed from the given object (qualified
     * class name).
     * @param object
     * 				the object.
     * @param level
     * 				the level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    public static boolean isLoggable(Object object, int level) {
        return isLoggable(object.getClass().getName(), level);
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
     * 			  The error, can be <code>null</code>
     * @return The String form.
     */
    public static String print(int priority, String tag, String msg, Throwable tr) {
        // Compute the letter for the given priority
        String p = "X"; // X => Unknown
        switch(priority) {
            case DEBUG: p = "D"; break;
            case INFO: p = "I"; break;
            case WARN: p = "W"; break;
            case ERROR: p = "E"; break;
            case ASSERT: p = "F"; break;
        }
        if (tr == null) {
            return p + "/" + tag + ": " + msg;
        } else {
            return p + "/" + tag + ": " + msg + "\n" + getStackTraceString(tr);
        }
    }

    private static void addEntry(String entry) {
        if (maxOfEntriesInReports > 0
                && entries.size() == maxOfEntriesInReports) {
            entries.remove(0); // Remove the first element.
        }
        entries.add(entry);
    }

    public static List<String> getReportedEntries() {
        return new ArrayList<String>(entries);
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
