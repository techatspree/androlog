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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import de.akquinet.android.androlog.Log;
import de.akquinet.android.androlog.LogHelper;

/**
 * Report structure.
 * This class defines report content.
 */
public class Report {

    /**
     * An optional message.
     */
    private final String message;

    /**
     * An optional error message.
     */
    private final Throwable err;

    private final long created = System.currentTimeMillis();

    private JSONObject report;
    
    private JSONObject logs;
    
    private JSONObject device;
    
    private JSONObject app;
    
    private JSONObject custom;

    /**
     * Creates a new report.
     *
     * @param context the context
     * @param message the message
     * @param err the error
     */
    public Report(Context context, String message, Throwable err) {
        this.message = message;
        this.err = err;
        buildReport(context);
    }

    public void putCustom(String key, Object value) {
        if (custom == null) {
            custom = new JSONObject();
            try {
                report.put("custom", custom);
            } catch (JSONException ex) {
                ex.printStackTrace(); // not expected
            }
        }
        try {
            custom.put(key, value);
        } catch (JSONException ex) {
            try {
                // should only happen if value is infinite or NaN
                custom.put(key, String.valueOf(value));
            } catch (JSONException ex1) {
                ex1.printStackTrace(); // not expected
            }
        }
    }
    
    public long getCreated() {
        return created;
    }
    
    public Object getDeviceKey(String key) {
        return device.opt(key);
    }
    
    public Object getAppKey(String key) {
        return app.opt(key);
    }
    
    public boolean hasException() {
        return err != null;
    }
    
    /**
     * Creates the report as a JSON Object.
     * @param context 
     * @return the json object containing the report.
     */
    private void buildReport(Context context) {
        try {
            buildBaseReport();
            buildApplicationData(context);
            buildDeviceData(context);
            buildLog();
            report.put("application", app);
            report.put("device", device);
            report.put("log", logs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject asJSON() {
        return report;
    }

    /**
     * Adds the log entries to the report.
     * @throws JSONException if the log entries cannot be added
     */
    private void buildLog() throws JSONException {
        logs = new JSONObject();
        List<String> list = Log.getReportedEntries();
        if (list != null) {
            logs.put("numberOfEntry", list.size());
            JSONArray array = new JSONArray();
            for (String s : list) {
                array.put(s);
            }
            logs.put("log", array);
        }
    }

    /**
     * Adds the device data to the report.
     * @param context 
     * @throws JSONException if the device data cannot be added
     */
    private void buildDeviceData(Context context) throws JSONException {
        device = new JSONObject();
        device.put("device", Build.DEVICE);
        device.put("brand", Build.BRAND);

        Object windowService = context.getSystemService(Context.WINDOW_SERVICE);
        if (windowService instanceof WindowManager) {
            Display display = ((WindowManager)windowService).getDefaultDisplay();
            device.put("resolution", display.getWidth() + "x" + display.getHeight());
            device.put("orientation", display.getOrientation());
        }
        device.put("display", Build.DISPLAY);
        device.put("manufacturer", Build.MANUFACTURER);
        device.put("model", Build.MODEL);
        device.put("product", Build.PRODUCT);
        device.put("build.type", Build.TYPE);
        device.put("android.version", Build.VERSION.SDK_INT);
    }

    /**
     * Adds the application data to the report.
     * @param context 
     * @throws JSONException if the application data cannot be added
     */
    private void buildApplicationData(Context context) throws JSONException {
        app = new JSONObject();
        app.put("package", context.getPackageName());
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            app.put("versionCode", info.versionCode);
            app.put("versionName", info.versionName);
            // TODO firstInstallTime and lastUpdate
        } catch (NameNotFoundException e) {
            // Cannot happen as we're checking a know package.
        }
    }

    /**
     * Adds the report header to the report (dates, locale, message, errors
     * ...).
     * @param report the report
     * @throws JSONException if the data cannot be added
     */
    private void buildBaseReport() throws JSONException {
        report = new JSONObject();
        JSONObject dates = new JSONObject();
        dates.put("date.system", created);
        dates.put("date", new Date(created).toString());
        dates.put("locale", Locale.getDefault());
        report.put("dates", dates);
        if (message != null) {
            report.put("message", message);
        }
        if (err != null) {
            report.put("error", err.getMessage());
            report.put("stackTrace",LogHelper.getStackTraceString(err));
            if (err.getCause() != null) {
                report.put("cause", err.getCause().getMessage());
                report.put("cause.stackTrace",LogHelper.getStackTraceString(err.getCause()));
            }

        }
    }
}
