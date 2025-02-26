package com.talsec.t;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScreenBlock extends ContentProvider implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = ScreenBlock.class.getSimpleName();

    private List<WeakReference<Activity>> activities;
    private Handler mainThreadHandler;
    private boolean block = false;

    public static void Block(Context context, boolean block) {
        String[] selectionArgs = {String.valueOf(block)};
        String provider = String.format("content://%s.screen.block.provider/data", context.getPackageName());
        Cursor cursor = context.getContentResolver().query(Uri.parse(provider), null, null, selectionArgs, null);
        if(cursor != null)
            cursor.close();
    }

    @Override
    public boolean onCreate() {
        this.activities = new ArrayList<>();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        Application application = (Application) Objects.requireNonNull(getContext()).getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if(selectionArgs != null && selectionArgs.length > 0) {
            this.block = Boolean.parseBoolean(selectionArgs[0]);
            this.blockScreenInternal();
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.d(TAG,"Adding " + activity);
        this.activities.add(new WeakReference<>(activity));
        this.blockScreenInternal();
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private void blockScreenInternal() {
        for(WeakReference<Activity> weakReference : this.activities) {
            Activity activity = weakReference.get();
            if(activity != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(block) {
                            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                    WindowManager.LayoutParams.FLAG_SECURE);
                        } else {
                            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        }
                    }
                });
            }
        }
    }
}
