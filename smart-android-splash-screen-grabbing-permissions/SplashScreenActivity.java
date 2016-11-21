package com.pcess.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

/**
 * This abstract {@link Activity}, which extends the {@link ImageActivity}
 * class, functions as a base class for a splash screen. This class will
 * function differently on pre- and post-Android 6.0 devices, although in both
 * cases, the {@link #getNextActivityClass()} method must be overridden, since
 * {@link #getNextActivityClass()} returns the {@link Activity} to start once
 * the splash screen times out.
 *
 *
 * <p/>
 *
 * On pre-Android 6.0 devices, this {@link Activity} will display a
 * {@link Base64} image for {@link #getTimeoutMillis()} milliseconds, before
 * starting the {@link Activity} specified by {@link #getNextActivityClass()}.
 *
 * <p/>
 *
 * On post-Android 6.0 devices, this app will additionally force the user to
 * grant all of the currently ungranted app permissions before timing out and
 * starting the next {@link Activity} specified by
 * {@link #getNextActivityClass()} (see
 * <a href="http://developer.android.com/training/permissions/requesting.html">
 * Requesting Android Permissions</a>). In pre-Android 6.0 devices, app
 * permissions were granted during installation and could not be revoked.
 * However, since Android 6.0, users can revoke app permissions after
 * installation. This {@link Activity} will gather all of the required app
 * permissions from the manifest, and check that this app has been granted all
 * of those permissions. The user will then be forced to granted all ungranted
 * permissions before continuing. Note, however, that the user may still revoke
 * permissions while the app is running, and this {@link Activity} does nothing
 * to protect your app from such occurrences. Specifically, this
 * {@link Activity} only does a check at start up.
 *
 * <p/>
 *
 * By default, this splash screen will show the Pcess logo for 1 second, and it
 * will not request any permissions. However, you can change the image that is
 * shown on the splash screen, the timeout duration (in milliseconds), and the
 * permissions required by your app by extending this class and overriding the
 * {@link #getBase64Image()}, {@link #getTimeoutMillis()}, and
 * {@link #getRequiredPermissions()} methods. Furthermore, if you would like to
 * display a different image while acquiring permissions, you can override
 * {@link #getBase64ImageDuringPermissionAcquisition()}
 */
@SuppressWarnings("rawtypes")
abstract public class SplashScreenActivity extends ImageActivity {

    /**
     * ---------------------------------------------
     *
     * Private Fields
     *
     * ---------------------------------------------
     */
    /**
     * The time that the splash screen will be on the screen in milliseconds.
     */
    private int              timeoutMillis       = 1000;

    /** The time when this {@link Activity} was created. */
    private long             startTimeMillis     = 0;

    /** The code used when requesting permissions */
    private static final int PERMISSIONS_REQUEST = 4646;

    /**
     * ---------------------------------------------
     *
     * Getters
     *
     * ---------------------------------------------
     */
    /**
     * Get the time (in milliseconds) that the splash screen will be on the
     * screen before starting the {@link Activity} who's class is returned by
     * {@link #getNextActivityClass()}.
     */
    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    /** Get the {@link Activity} to start when the splash screen times out. */
    abstract public Class getNextActivityClass();

    /**
     * Get the list of required permissions by searching the manifest. If you
     * don't think the default behavior is working, then you could try
     * overriding this function to return something like:
     *
     * <pre>
     * <code>
     * return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
     * </code>
     * </pre>
     */
    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return permissions.clone();
    }

    /** Get the {@link Base64} image to display while acquiring permissions. */
    public String getBase64ImageDuringPermissionAcquisition() {
        return Base64Images.waitingForPermissions768();
    };

    /**
     * ---------------------------------------------
     *
     * {@link Activity} Methods
     *
     * ---------------------------------------------
     */
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /** Default creation code. */
        super.onCreate(savedInstanceState);

        /**
         * Save the start time of this Activity, which will be used to determine
         * when the splash screen should timeout.
         */
        startTimeMillis = System.currentTimeMillis();

        /**
         * On a post-Android 6.0 devices, check if the required permissions have
         * been granted.
         */
        if (Build.VERSION.SDK_INT >= 23) {
            getImageView().setImageBitmap(Base64Images.toBitmap(getBase64ImageDuringPermissionAcquisition()));
            checkPermissions();
        } else {
            startNextActivity();
        }
    }

    /**
     * See if we now have all of the required dangerous permissions. Otherwise,
     * tell the user that they cannot continue without granting the permissions,
     * and then request the permissions again.
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            checkPermissions();
        }
    }

    /**
     * ---------------------------------------------
     *
     * Other Methods
     *
     * ---------------------------------------------
     */
    /**
     * After the timeout, start the {@link Activity} as specified by
     * {@link #getNextActivityClass()}, and remove the splash screen from the
     * backstack.
     */
    private void startNextActivity() {
        long delayMillis = getTimeoutMillis() - (System.currentTimeMillis() - startTimeMillis);
        if (delayMillis < 0) {
            delayMillis = 0;
        } else {
            getImageView().setImageBitmap(Base64Images.toBitmap(getBase64Image()));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, getNextActivityClass()));
                finish();
            }
        }, delayMillis);
    }

    /**
     * Check if the required permissions have been granted, and
     * {@link #startNextActivity()} if they have. Otherwise
     * {@link #requestPermissions(String[], int)}.
     */
    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0) {
            startNextActivity();
        } else {
            requestPermissions(ungrantedPermissions, PERMISSIONS_REQUEST);
        }
    }

    /**
     * Convert the array of required permissions to a {@link Set} to remove
     * redundant elements. Then remove already granted permissions, and return
     * an array of ungranted permissions.
     */
    @TargetApi(23)
    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(SplashScreenActivity.class.getSimpleName(), "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(SplashScreenActivity.class.getSimpleName(), "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}