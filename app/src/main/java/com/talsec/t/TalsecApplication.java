package com.talsec.t;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo;
import com.aheaditec.talsec_security.security.api.Talsec;
import com.aheaditec.talsec_security.security.api.TalsecConfig;
import com.aheaditec.talsec_security.security.api.ThreatListener;

import java.util.List;

public class TalsecApplication extends Application implements ThreatListener.ThreatDetected {

    private static final String TAG = TalsecApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        TalsecConfig config = new TalsecConfig.Builder(getApplicationContext().getPackageName(),
                new String[] {"Tmac/QIomCqEGS1jYqy9cMMrqaitVoZLpjXzCMnt55Q="})
                .blacklistedPackageNames(new String[]{"com.spotify.music", "com.leavjenn.hews2"})
                .suspiciousPermissions(new String[][]{{"android.permission.READ_CONTACTS"}, {"android.permission.SEND_SMS"}})
                .prod(true)
                .build();
        ThreatListener threatListener = new ThreatListener(this);
        threatListener.registerListener(getApplicationContext());
        Talsec.start(this, config);

        /**
         * Pre-Android 15
         * An alternative approach to screen block protection
         * No need to provide activity instance
         * simply pass context
         */
        ScreenBlock.Block(getApplicationContext(), true);

    }

    @Override
    public void onRootDetected() {
        Log.d(TAG, "Root Detected");
        Toast.makeText(getApplicationContext(), "Root Detected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDebuggerDetected() {
        Log.d(TAG, "Debugger Detected");
        Toast.makeText(getApplicationContext(), "Debugger Detected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEmulatorDetected() {
        Toast.makeText(getApplicationContext(), "Emulator Detected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTamperDetected() {
        Log.d(TAG, "Tamper Detected");
        Toast.makeText(getApplicationContext(), "Tamper Detected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUntrustedInstallationSourceDetected() {

    }

    @Override
    public void onHookDetected() {
        Log.d(TAG, "Hook Detected");
    }

    @Override
    public void onDeviceBindingDetected() {

    }

    @Override
    public void onObfuscationIssuesDetected() {

    }

    @Override
    public void onMalwareDetected(List<SuspiciousAppInfo> list) {
        if(list != null) {
            for(int i=0; i<list.size(); i++) {
                Log.d(TAG, String.format("%s %s", list.get(i).getReason(), list.get(i).getPackageInfo().packageName));
            }
        }

    }

    @Override
    public void onScreenshotDetected() {

    }

    @Override
    public void onScreenRecordingDetected() {

    }
}
