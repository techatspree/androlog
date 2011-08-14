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

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.content.Context;
import de.akquinet.android.androlog.reporter.Reporter;
import de.akquinet.android.androlog.reporter.ReporterFactory;

/**
 * Implements a small layer on top of the <a
 * href="http://developer.android.com/reference/android/util/Log.html">Android
 * Log</a> to provide programmatic enabling and disabling of logging as well as
 * providing report support
 * <p>
 * To support <code>wtf</code> methods, Androlog detects if we're on Android
 * 2.2+. If not, the Constants.ASSERT level is used.
 * </p>
 * <p>
 * Log methods returns an integer. This integer is the number of byte written in
 * the log. If a method returns 0, the message was not logged.
 * </p>
 * <p>
 * Reporting is enabled by defining {@link Reporter} objects and activating the
 * reporting. Reporting is supported only if Androlog is initialized with an
 * Android {@link Context}.
 * </p>
 */
public class Log {

    /**
     * Global activation flag.
     */
    private static boolean activated = true;

    /**
     * Default log level.
     */
    private static int defaultLogLevel = Constants.INFO;

    /**
     * Default report level.
     */
    private static int defaultReportLevel = Constants.INFO;

    /**
     * Android Log wtf method. wtf(String tag, String msg)
     */
    private static Method wtfTagMessageMethod;

    /**
     * Android Log wtf method. wtf(String tag, Throwable tr)
     */
    private static Method wtfTagErrorMethod;

    /**
     * Android Log wtf method. wtf(String tag, String msg, Throwable tr)
     */
    private static Method wtfTagMessageErrorMethod;

    /**
     * Sets to true if the wtf method delegates on the Android Log wtf methods
     * which may cause the process to terminate.
     */
    private static boolean useWTF;

    /**
     * The list of log entries
     */
    private static List<String> entries;

    /**
     * Flag enabling / Disabling the log entry collection.
     */
    private static boolean enableLogEntryCollection;

    /**
     * Maximum number of entry to store.
     */
    private static int maxOfEntriesInReports;

    /**
     * Flag indicating if the reporting is activated.
     */
    private static boolean reportingActivated;

    /**
     * The list of reporters.
     */
    private static List<Reporter> reporters = new ArrayList<Reporter>(0);

    /**
     * The Android context.
     */
    private static Context context;

    /**
     * Map storing the log levels.
     */
    private static final Map<String, Integer> logLevels = new HashMap<String, Integer>();

    /**
     * Log level triggering reports
     */
    private static int reportTriggerLevel = Constants.ASSERT;

    /**
     * Is the {@link UncaughtExceptionHandler} enabled ?
     */
    private static boolean exceptionHandlerActivated = true;

    /**
     * Is the propagation to the default {@link UncaughtExceptionHandler}
     * enabled ?
     */
    private static boolean exceptionHandlerPropagation = true;


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
     * Enables or disables the delegation to the Android log wtf methods. Those
     * methods when present (android 2.2+) may cause the termination of the
     * application.
     *
     * @param delegation
     *            enables or disables the delegation
     * @return if the delegation is enabled. Indeed, on 1.6 and 2.0 it can't be
     *         enabled.
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
        deactivateReporting();
        defaultLogLevel = Constants.INFO;
        defaultReportLevel = Constants.INFO;
        detectWTFMethods();
        logLevels.clear();
        maxOfEntriesInReports = 25;
        enableLogEntryCollection = false;
        entries = null;
        reporters.clear();
        reportTriggerLevel = Constants.ASSERT;
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
     * property file named 'package.properties' on the SDCARD or in the
     * application assets (if the file cannot be found on the SDCard). If the
     * file is readable and exist, the Log is configured. The file must be a
     * valid Java properties file.
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
        init(null, Constants.ANDROLOG_PROPERTIES);
    }

    /**
     * Androlog Init Method. This method loads the Androlog Configuration from:
     * <ol>
     * <li><code>/SDCARD/fileName</code> if the file name if not
     * <code>null</code></li>
     * <li><code>/SDCARD/Application_Package.properties</code> if the file name
     * is <code>null</code> and context is not <code>null</code></li>
     * <li><code>Application_Assets/fileName</code> if the file name if not
     * <code>null</code> and the context is not <code>null</code></li>
     * <li><code>Application_Assets/Application_Package.properties</code> if the
     * file name is <code>null</code> and the context is not <code>null</code></li>
     * </ol>
     * The first found file is used, allowing overriding the configuration by
     * just pushing a file on the SDCard. Passing <code>null</code> to both
     * parameters is equivalent to the case 2. If the lookup failed, the logging
     * is disabled.
     *
     * @param context
     *            the application context
     * @param fileName
     *            the file name
     */
    public static void init(Context context, String fileName) {
        reset();
        Log.context = context;

        String file = fileName;
        if (file == null && context != null) {
            file = context.getPackageName() + ".properties";
        }

        // Check from SDCard
        InputStream fileIs = LogHelper.getConfigurationFileFromSDCard(file);
        if (fileIs == null) {
            // Check from Assets
            fileIs = LogHelper.getConfigurationFileFromAssets(context, file);
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
                LogHelper.closeQuietly(fileIs);
            }
        }

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
     *
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

        boolean activate = "true".equalsIgnoreCase(configuration
                .getProperty(Constants.ANDROLOG_ACTIVE));
        if (activate) {
            activateLogging();
        }

        boolean activate4Report = "true".equalsIgnoreCase(configuration
                .getProperty(Constants.ANDROLOG_REPORT_ACTIVE));
        if (activate4Report) {
            activateReporting();
        }

        detectWTFMethods();

        if (configuration.containsKey(Constants.ANDROLOG_DEFAULT_LEVEL)) {
            String level = configuration.getProperty(Constants.ANDROLOG_DEFAULT_LEVEL);
            defaultLogLevel = LogHelper.getLevel(level, defaultLogLevel);
        }

        if (configuration.containsKey(Constants.ANDROLOG_REPORT_DEFAULT_LEVEL)) {
            String level = configuration
                    .getProperty(Constants.ANDROLOG_REPORT_DEFAULT_LEVEL);
            defaultReportLevel = LogHelper.getLevel(level, defaultReportLevel);
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>) configuration
                .propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.startsWith(Constants.ANDROLOG_PREFIX)) {
                String level = configuration.getProperty(name);
                int log = LogHelper.getLevel(level, defaultLogLevel);
                logLevels.put(name, log);
            }
        }

        if (useWTF) {
            // Check if androlog configuration does not override this.
            if (configuration.containsKey(Constants.ANDROLOG_DELEGATE_WTF)) {
                String v = configuration.getProperty(Constants.ANDROLOG_DELEGATE_WTF);
                // If androlog.delegate.wtf is set to true, we really call
                // Log.wtf which
                // may terminate the process.
                useWTF = "true".equals(v.toLowerCase());
                // In other cases, androlog does log a message in the Constants.ASSERT
                // level.
            }
        }

        // Do we need to store the log entries for Reports ?
        enableLogEntryCollection = false;
        if (context != null
                && configuration.containsKey(Constants.ANDROLOG_REPORT_REPORTERS)) {
            // We enable the collection only if we have reporters AND a valid
            // context
            String s = configuration.getProperty(Constants.ANDROLOG_REPORT_REPORTERS);
            String[] senders = s.split(",");
            for (String sender : senders) {
                String cn = sender.trim();
                Reporter reporter = ReporterFactory.newInstance(cn);
                if (reporter != null) {
                    reporter.configure(configuration);
                    reporters.add(reporter);
                }
            }

            // Configure the UncaughtExceptionHandler
            if (configuration.containsKey(Constants.ANDROLOG_REPORT_EXCEPTION_HANDLER)
                    && "false".equals(configuration.getProperty(Constants.ANDROLOG_REPORT_EXCEPTION_HANDLER))) {
                exceptionHandlerActivated = false;
            }

            if (configuration.containsKey(Constants.ANDROLOG_REPORT_EXCEPTION_HANDLER_PROPAGATION)
                    && "false".equals(configuration.getProperty(Constants.ANDROLOG_REPORT_EXCEPTION_HANDLER_PROPAGATION))) {
                exceptionHandlerPropagation = false;
            }

            // Define an default error handler, reporting the error.
            if (exceptionHandlerActivated) {
                final UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread arg0, Throwable arg1) {
                        report("Uncaught Exception", arg1);
                        // If there is a original handler, propagate the exception.
                        if (exceptionHandlerPropagation  && originalHandler != null) {
                            originalHandler.uncaughtException(arg0, arg1);
                        }
                    }
                });
            }

            if (configuration.containsKey(Constants.ANDROLOG_REPORT_TRIGGER_LEVEL)) {
                String l = configuration.getProperty(Constants.ANDROLOG_REPORT_TRIGGER_LEVEL);
                reportTriggerLevel = LogHelper.getLevel(l, defaultReportLevel);
            } else {
                reportTriggerLevel = Constants.ASSERT;
            }

            enableLogEntryCollection = true;
        }

        if (enableLogEntryCollection) {
            if (configuration.containsKey(Constants.ANDROLOG_REPORT_LOG_ITEMS)) {
                String p = configuration.getProperty(Constants.ANDROLOG_REPORT_LOG_ITEMS);
                maxOfEntriesInReports = Integer.parseInt(p);
            } else {
                maxOfEntriesInReports = 25; // Default
            }
            entries = new ArrayList<String>(maxOfEntriesInReports);
        }

    }

    /**
     * Check if the android Log class contains the <code>wtf</code> method
     * (Android 2.2+). In that case, the delegation to those method is enabled.
     * If not, calling {@link Log#wtf(Object, String)} log a message with the
     * level {@link Log#Constants.ASSERT}
     */
    private static void detectWTFMethods() {
        // Check if wtf exists (android 2.2+)
        // static int wtf(String tag, String msg)
        // static int wtf(String tag, Throwable tr)
        // static int wtf(String tag, String msg, Throwable tr)
        try {
            wtfTagMessageMethod = android.util.Log.class.getMethod("wtf",
                    new Class[] { String.class, String.class });
            wtfTagErrorMethod = android.util.Log.class.getMethod("wtf",
                    new Class[] { String.class, Throwable.class });
            wtfTagMessageErrorMethod = android.util.Log.class
                    .getMethod("wtf", new Class[] { String.class, String.class,
                            Throwable.class });
            useWTF = true;
        } catch (Exception e) {
            // wtf is not defined, will use Constants.ASSERT level.
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
        collectLogEntry(Constants.VERBOSE, tag, msg, null);
        if (isLoggable(tag, Constants.VERBOSE)) {
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
        collectLogEntry(Constants.VERBOSE, tag, msg, tr);
        if (isLoggable(tag, Constants.VERBOSE)) {
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

        String caller = LogHelper.getCaller();
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
     * Send a {@link #Constants.DEBUG} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int d(String tag, String msg) {
        collectLogEntry(Constants.DEBUG, tag, msg, null);
        if (isLoggable(tag, Constants.DEBUG)) {
            return android.util.Log.d(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.DEBUG} log message and log the exception.
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
        collectLogEntry(Constants.DEBUG, tag, msg, tr);
        if (isLoggable(tag, Constants.DEBUG)) {
            return android.util.Log.d(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.DEBUG} log message.
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
     * Send a {@link #Constants.DEBUG} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int d(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }

        String caller = LogHelper.getCaller();
        if (caller != null) {
            return d(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.DEBUG} log message and log the exception.
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
     * Send an {@link #Constants.INFO} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int i(String tag, String msg) {
        collectLogEntry(Constants.INFO, tag, msg, null);
        if (isLoggable(tag, Constants.INFO)) {
            return android.util.Log.i(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.INFO} log message and log the exception.
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
        collectLogEntry(Constants.INFO, tag, msg, tr);
        if (isLoggable(tag, Constants.INFO)) {
            return android.util.Log.i(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.INFO} log message.
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
     * Send a {@link #Constants.INFO} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int i(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }

        String caller = LogHelper.getCaller();
        if (caller != null) {
            return i(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.INFO} log message and log the exception.
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
     * Send a {@link #Constants.WARN} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int w(String tag, String msg) {
        collectLogEntry(Constants.WARN, tag, msg, null);
        if (isLoggable(tag, Constants.WARN)) {
            return android.util.Log.w(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.WARN} log message and log the exception.
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
        collectLogEntry(Constants.WARN, tag, msg, tr);
        if (isLoggable(tag, Constants.WARN)) {
            return android.util.Log.w(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.WARN} log message.
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
     * Send a {@link #Constants.WARN} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int w(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }
        String caller = LogHelper.getCaller();
        if (caller != null) {
            return w(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.WARN} log message and log the exception.
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
     * Send a {@link #Constants.WARN} log message and log the exception.
     *
     * @param tr
     *            An exception to log
     */
    public static int w(String tag, Throwable tr) {
        collectLogEntry(Constants.WARN, tag, "", null);
        if (isLoggable(tag, Constants.WARN)) {
            return android.util.Log.w(tag, tr);
        }
        return 0;
    }

    /**
     * Send an {@link #Constants.ERROR} log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int e(String tag, String msg) {
        collectLogEntry(Constants.ERROR, tag, msg, null);
        if (isLoggable(tag, Constants.ERROR)) {
            return android.util.Log.e(tag, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.ERROR} log message and log the exception.
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
        collectLogEntry(Constants.ERROR, tag, msg, tr);
        if (isLoggable(tag, Constants.ERROR)) {
            return android.util.Log.e(tag, msg, tr);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.ERROR} log message.
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
     * Send a {@link #Constants.ERROR} log message.
     *
     * @param msg
     *            The message you would like logged.
     */
    public static int e(String msg) {
        // This is a quick check to avoid the expensive stack trace reflection.
        if (!activated) {
            return 0;
        }
        String caller = LogHelper.getCaller();
        if (caller != null) {
            return e(caller, msg);
        }
        return 0;
    }

    /**
     * Send a {@link #Constants.ERROR} log message and log the exception.
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
     * What a Terrible Failure: Report a condition that should never happen. The
     * error will always be logged at level Constants.ASSERT despite the logging is
     * disabled. Depending on system configuration, and on Android 2.2+, a
     * report may be added to the DropBoxManager and/or the process may be
     * terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the
     * Assert level. Those log messages will always be logged.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     */
    public static int wtf(String tag, String msg) {
        collectLogEntry(Constants.ASSERT, tag, msg, null);
        if (isLoggable(tag, Constants.ASSERT)) {
            if (useWTF) {
                try {
                    return (Integer) wtfTagMessageMethod.invoke(null,
                            new Object[] { tag, msg });
                } catch (Exception e) {
                    return LogHelper.println(Constants.ASSERT, tag, msg);
                }
            } else {
                return LogHelper.println(Constants.ASSERT, tag, msg);
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The
     * error will always be logged at level Constants.ASSERT despite the logging is
     * disabled. Depending on system configuration, and on Android 2.2+, a
     * report may be added to the DropBoxManager and/or the process may be
     * terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the
     * Assert level. Those log messages will always be logged.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param tr
     *            The exception to log
     */
    public static int wtf(String tag, Throwable tr) {
        collectLogEntry(Constants.VERBOSE, tag, "", tr);
        if (isLoggable(tag, Constants.ASSERT)) {
            if (useWTF) {
                try {
                    return (Integer) wtfTagErrorMethod.invoke(null,
                            new Object[] { tag, tr });
                } catch (Exception e) {
                    return LogHelper.println(Constants.ASSERT, tag, LogHelper.getStackTraceString(tr));
                }
            } else {
                return LogHelper.println(Constants.ASSERT, tag, LogHelper.getStackTraceString(tr));
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The
     * error will always be logged at level Constants.ASSERT despite the logging is
     * disabled. Depending on system configuration, and on Android 2.2+, a
     * report may be added to the DropBoxManager and/or the process may be
     * terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the
     * Assert level. Those log messages will always be logged.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually
     *            identifies the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            The exception to log
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        collectLogEntry(Constants.ASSERT, tag, msg, tr);
        if (isLoggable(tag, Constants.ASSERT)) {
            if (useWTF) {
                try {
                    return (Integer) wtfTagMessageErrorMethod.invoke(null,
                            new Object[] { tag, msg, tr });
                } catch (Exception e) {
                    return LogHelper.println(Constants.ASSERT, tag, msg + '\n'
                            + LogHelper.getStackTraceString(tr));
                }
            } else {
                return LogHelper.println(Constants.ASSERT, tag, msg + '\n'
                        + LogHelper.getStackTraceString(tr));
            }
        }
        return 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The
     * error will always be logged at level Constants.ASSERT despite the logging is
     * disabled. Depending on system configuration, and on Android 2.2+, a
     * report may be added to the DropBoxManager and/or the process may be
     * terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the
     * Assert level. Those log messages will always be logged.
     *
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
     * What a Terrible Failure: Report a condition that should never happen. The
     * error will always be logged at level Constants.ASSERT despite the logging is
     * disabled. Depending on system configuration, and on Android 2.2+, a
     * report may be added to the DropBoxManager and/or the process may be
     * terminated immediately with an error dialog.
     *
     * On older Android version (before 2.2), the message is logged with the
     * Assert level. Those log messages will always be logged.
     *
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
     * Use {@link #configure(Properties)} to define different log levels for
     * each tag.
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
        if (! activated && level != Constants.ASSERT) {
            return false;
        }
        Integer logLevel = logLevels.get(tag);
        if (logLevel == null) {
            logLevel = defaultLogLevel;
        }
        return level >= logLevel;
    }

    /**
     * Checks to see whether or not a log is reportable at the specified level.
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
        return reportingActivated && enableLogEntryCollection
                && level >= defaultReportLevel;
    }

    /**
     * Checks to see whether or not a log for the specified object is loggable
     * at the specified level. The tag is computed from the given object
     * (qualified class name).
     *
     * @param object
     *            the object.
     * @param level
     *            the level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    public static boolean isLoggable(Object object, int level) {
        return isLoggable(object.getClass().getName(), level);
    }

    /**
     * Triggers a report without message and error
     *
     * @return <code>true</code> if the report was successfully sent by
     *         <b>all</b> reporters, <code>false</code> otherwise.
     */
    public static boolean report() {
        return report(null, null);
    }

    /**
     * Triggers a Report. This method generates the report and send it with all
     * configured reporters.
     *
     * @param message
     *            the message
     * @param error
     *            the error
     * @return <code>true</code> if the report was successfully sent by
     *         <b>all</b> reporters, <code>false</code> otherwise.
     */
    public static boolean report(String message, Throwable error) {
        boolean acc = true;
        for (Reporter reporter : reporters) {
            acc = acc && reporter.send(context, message, error);
        }
        return acc;
    }

    /**
     * Adds a log entry to the collected entry list. This method managed the
     * maximum number of entries and triggers report if the entry priority is
     * superior or equals to the report trigger level.
     *
     * @param level
     *            the log level of the entry
     * @param tag
     *            the tag
     * @param message
     *            the message
     * @param err
     *            the error message
     */
    private static void collectLogEntry(int level, String tag, final String message,
            final Throwable err) {
        if (!isReportable(level)) {
            return;
        }

        if (maxOfEntriesInReports > 0
                && entries.size() == maxOfEntriesInReports) {
            entries.remove(0); // Remove the first element.
        }
        entries.add(LogHelper.print(level, tag, message, err));

        if (level >= reportTriggerLevel) {
            // Must be in another thread
            new Thread(new Runnable() {
                public void run() {
                    try {
                        report(message, err);
                    } catch (Throwable e) {
                        // Ignore
                    }
                }
            }).start();
        }
    }

    /**
     * Gets the list of reported entries.
     *
     * @return a copy of the reported entries or <code>null</code> if no entries
     *         were collected.
     */
    public static List<String> getReportedEntries() {
        if (entries != null) {
            return new ArrayList<String>(entries);
        } else {
            return null;
        }
    }

}
