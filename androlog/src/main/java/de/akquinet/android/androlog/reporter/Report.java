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
import de.akquinet.android.androlog.Log;

/**
 * Report structure.
 * This class defines report content.
 */
public class Report {

    /**
     * The Android context.
     */
    private Context context;

    /**
     * An optional message.
     */
    private String message;

    /**
     * An optional error message.
     */
    private Throwable err;

    /**
     * Creates a new report.
     * All reporters share the same report object, so they <b>must</b>
     * not modify the report.
     * @param context the context
     * @param message the message
     * @param err the error
     */
    public Report(Context context, String message, Throwable err) {
        this.context = context;
        this.message = message;
        this.err = err;
    }

    /**
     * Creates the report as a JSON Object.
     * @return the json object containing the report.
     */
    public JSONObject getReportAsJSON() {
        JSONObject report = new JSONObject();

        try {
            addReportHeader(report);
            addApplicationData(report);
            addDeviceData(report);
            addLog(report);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return report;
    }

    /**
     * Adds the log entries to the report.
     * @param report the report
     * @throws JSONException if the log entries cannot be added
     */
    private void addLog(JSONObject report) throws JSONException {
        JSONObject logs = new JSONObject();
        List<String> list = Log.getReportedEntries();
        if (list != null) {
            logs.put("numberOfEntry", list.size());
            JSONArray array = new JSONArray();
            for (String s : list) {
                array.put(s);
            }
            logs.put("log", array);
        }
        report.put("log", logs);
    }

    /**
     * Adds the device data to the report.
     * @param report the report
     * @throws JSONException if the device data cannot be added
     */
    private void addDeviceData(JSONObject report) throws JSONException {
        JSONObject device = new JSONObject();
        device.put("device", Build.DEVICE);
        device.put("brand", Build.BRAND);
        device.put("display", Build.DISPLAY);
        device.put("manufacturer", Build.MANUFACTURER);
        device.put("model", Build.MODEL);
        device.put("product", Build.PRODUCT);
        device.put("build.type", Build.TYPE);
        report.put("device", device);
    }

    /**
     * Adds the application data to the report.
     * @param report the report
     * @throws JSONException if the application data cannot be added
     */
    private void addApplicationData(JSONObject report) throws JSONException {
        JSONObject app = new JSONObject();
        app.put("package", context.getPackageName());
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            app.put("versionCode", info.versionCode);
            app.put("versionName", info.versionName);
            // TODO firstInstallTime and lastUpdate
        } catch (NameNotFoundException e) {
            // Cannot happen as we're checking a know package.
        }
        report.put("application", app);
    }

    /**
     * Adds the report header to the report (dates, locale, message, errors
     * ...).
     * @param report the report
     * @throws JSONException if the data cannot be added
     */
    private void addReportHeader(JSONObject report) throws JSONException {
        JSONObject dates = new JSONObject();
        dates.put("date.system", System.currentTimeMillis());
        dates.put("date", new Date().toString());
        dates.put("locale", Locale.getDefault());
        report.put("dates", dates);
        if (message != null) {
            report.put("message", message);
        }
        if (err != null) {
            report.put("error", err.getMessage());
            report.put("stackTrace",Log.getStackTraceString(err));
            if (err.getCause() != null) {
                report.put("cause", err.getCause().getMessage());
                report.put("cause.stackTrace",Log.getStackTraceString(err.getCause()));
            }

        }
    }

}
