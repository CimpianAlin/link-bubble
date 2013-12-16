package com.chrislacy.linkbubble;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;


public class MainApplication extends Application {

    private Bus mBus;

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.initModule(this);

        mBus = new Bus();
    }

    public Bus getBus() {
        return mBus;
    }

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        Settings.deinitModule();

        super.onTerminate();
    }

    public static void openLink(Context context, String url, boolean recordHistory) {
        Intent serviceIntent = new Intent(context, MainService.class);
        serviceIntent.putExtra("url", url);
        serviceIntent.putExtra("record_history", recordHistory);
        serviceIntent.putExtra("start_time", System.currentTimeMillis());
        context.startService(serviceIntent);
    }

    public static void loadInBrowser(Context context, Intent intent) {
        boolean activityStarted = false;
        ComponentName defaultBrowserComponentName = Settings.get().getDefaultBrowserComponentName(context);
        if (defaultBrowserComponentName != null) {
            intent.setComponent(defaultBrowserComponentName);
            context.startActivity(intent);
            activityStarted = true;
        }

        if (activityStarted == false) {
            Toast.makeText(context, R.string.no_default_browser, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean loadResolveInfoIntent(Context context, ResolveInfo resolveInfo, String url, long startTime) {
        if (resolveInfo.activityInfo != null) {
            return loadIntent(context, resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, url, startTime);
        }
        return false;
    }

    public static boolean loadIntent(Context context, String packageName, String className, String url, long startTime) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setClassName(packageName, className);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.setData(Uri.parse(url));
        context.startActivity(openIntent);
        //Log.d(TAG, "redirect to app: " + resolveInfo.loadLabel(context.getPackageManager()) + ", url:" + url);
        if (startTime > -1) {
            Log.d("LoadTime", "Saved " + ((System.currentTimeMillis()-startTime)/1000) + " seconds.");
        }
        return true;
    }
}
