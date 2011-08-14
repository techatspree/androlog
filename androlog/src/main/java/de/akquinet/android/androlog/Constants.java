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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.reporter.Reporter;

public class Constants {

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
     * Property to deactivate the delagation on the Android Log wtf method which
     * may cause the process to terminate. On android 2.2, this property allow
     * to disable this delegation. Androlog then just log an ASSERT message.
     */
    public static final String ANDROLOG_DELEGATE_WTF = "androlog.delegate.wtf";
    /**
     * Property to set the number of log entry included in reports.
     */
    public static final String ANDROLOG_REPORT_LOG_ITEMS = "androlog.report.log.items";
    /**
     * Property defining the set of {@link Reporter}s. The property's value is a
     * comma separated list of {@link Reporter} class name.
     */
    public static final String ANDROLOG_REPORT_REPORTERS = "androlog.report.reporters";
    /**
     * Property setting the log level for Reports.
     */
    public static final String ANDROLOG_REPORT_DEFAULT_LEVEL = "androlog.report.default.level";
    /**
     * Property setting the log level sending reports automatically.
     */
    public static final String ANDROLOG_REPORT_TRIGGER_LEVEL = "androlog.report.trigger.level";
    /**
     * Property activating the reporting.
     */
    public static final String ANDROLOG_REPORT_ACTIVE = "androlog.report.active";
    /**
     * Property disabling the {@link UncaughtExceptionHandler}, enabled by default.
     */
    public static final String ANDROLOG_REPORT_EXCEPTION_HANDLER = "androlog.report.exception.handler";
    /**
     * Property disabling the propagation to the default {@link UncaughtExceptionHandler},
     * enabled by default.
     */
    public static final String ANDROLOG_REPORT_EXCEPTION_HANDLER_PROPAGATION = "androlog.report.exception.handler.propagation";
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
     * The classes contained in this list are escaped from the stack trace when
     * computing the tag level from the current stack trace.
     */
    protected final static List<String> CLASSNAME_TO_ESCAPE = new ArrayList<String>();
    static {
        CLASSNAME_TO_ESCAPE.add("java.lang.Thread");
        CLASSNAME_TO_ESCAPE.add("dalvik.system.VMStack");
        CLASSNAME_TO_ESCAPE.add(Log.class.getName());
        CLASSNAME_TO_ESCAPE.add(LogHelper.class.getName());
        CLASSNAME_TO_ESCAPE.add(Constants.class.getName());
    };

}
