package com.zebra.deviceidentifierswrapper;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import java.util.Base64;

public class DIHelper {

    // Placeholder for custom certificate
    // Otherwise, the app will use the first certificate found with the method:
    // final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
    // TODO: Put your custom certificate in the apkCertificate member for MX AccessMgr registering (only if necessary and if you know what you are doing)
    public static Signature apkCertificate = null;

    // This method will return the serial number in the string passed through the onSuccess method
    public static void getSerialNumber(Context context, IDIResultCallbacks callbackInterface)
    {
        if (android.os.Build.VERSION.SDK_INT < 29) {
            returnSerialUsingAndroidAPIs(context, callbackInterface);
        } else {
            returnSerialUsingZebraAPIs(context, callbackInterface);
        }
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt", "HardwareIds"})
    private static void returnSerialUsingAndroidAPIs(Context context, IDIResultCallbacks callbackInterface) {
        if (android.os.Build.VERSION.SDK_INT < 26) {
            callbackInterface.onSuccess(Build.SERIAL);
        } else {
            if (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                callbackInterface.onSuccess(Build.getSerial());
            } else {
                callbackInterface.onError("Please grant READ_PHONE_STATE permission");
            }
        }
    }

    private static void returnSerialUsingZebraAPIs(Context context, IDIResultCallbacks callbackInterface) {
        new RetrieveOEMInfoTask()
            .execute(context, Uri.parse("content://oem_info/oem.zebra.secure/build_serial"),
                callbackInterface);
    }

    // This method will return the imei number in the string passed through the onSuccess method
    public static void getIMEINumber(Context context, IDIResultCallbacks callbackInterface)
    {
        if (android.os.Build.VERSION.SDK_INT < 29) {
            returnImeiUsingAndroidAPIs(context, callbackInterface);
        } else {
            returnImeiUsingZebraAPIs(context, callbackInterface);
        }
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt", "HardwareIds" })
    private static void returnImeiUsingAndroidAPIs(Context context, IDIResultCallbacks callbackInterface) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT < 26) {String imei = telephonyManager.getDeviceId();
            if (imei != null && !imei.isEmpty()) {
                callbackInterface.onSuccess(imei);
            } else {
                callbackInterface.onError("Could not get IMEI number");
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                String imei = telephonyManager.getImei();
                if (imei != null && !imei.isEmpty()) {
                    callbackInterface.onSuccess(imei);
                } else {
                    callbackInterface.onError("Could not get IMEI number");
                }
            } else {
                callbackInterface.onError("Please grant READ_PHONE_STATE permission");
            }
        }
    }

    private static void returnImeiUsingZebraAPIs(Context context, IDIResultCallbacks callbackInterface) {
        new RetrieveOEMInfoTask().execute(context, Uri.parse("content://oem_info/wan/imei"),
            callbackInterface);
    }
}
