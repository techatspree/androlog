package de.akquinet.gomobile.androlog.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * Simple Context doing nothing. This class is used easy the overriding and so the
 * definition of fake {@link Context}.
 * @see Context
 */
public class MockContext extends Context {

    @Override
    public boolean bindService(Intent arg0, ServiceConnection arg1, int arg2) {
        return false;
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingPermission(String permission) {
        return 0;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return 0;
    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission,
            String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public Context createPackageContext(String packageName, int flags)
            throws NameNotFoundException {
        return null;
    }

    @Override
    public String[] databaseList() {
        return null;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public void enforceCallingOrSelfPermission(String permission, String message) {
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags,
            String message) {
    }

    @Override
    public void enforceCallingPermission(String permission, String message) {
    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags,
            String message) {
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid,
            String message) {

    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags,
            String message) {

    }

    @Override
    public void enforceUriPermission(Uri uri, String readPermission,
            String writePermission, int pid, int uid, int modeFlags,
            String message) {
    }

    @Override
    public String[] fileList() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public Object getSystemService(String name) {
        return null;
    }

    @Override
    public Theme getTheme() {
        return null;
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public FileInputStream openFileInput(String name)
            throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode)
            throws FileNotFoundException {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
            CursorFactory factory) {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver,
            IntentFilter filter) {
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver,
            IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return null;
    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission,
            BroadcastReceiver resultReceiver, Handler scheduler,
            int initialCode, String initialData, Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public boolean startInstrumentation(ComponentName className,
            String profileFile, Bundle arguments) {
        return false;
    }

    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public void unbindService(ServiceConnection conn) {

    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

}
