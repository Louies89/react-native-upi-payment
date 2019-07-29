package com.upi.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
// import android.util.Log;   //added by Chandrajyoti for debug

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.List;


public class UpiPaymentModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final int REQUEST_CODE = 123;
    private Callback successHandler;
    private Callback failureHandler;
    private String FAILURE = "FAILURE";

    public UpiPaymentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "UpiPayment";
    }

    @ReactMethod
    public void intializePayment(ReadableMap config, Callback successHandler, Callback failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        //intent.setClassName("in.org.npci.upiapp","in.org.npci.upiapp.HomeActivity"); //Only to do transaction via BHIM, Added by Chandrajyoti
        intent.setData(Uri.parse(config.getString("upiString")));
        Context currentContext = getCurrentActivity().getApplicationContext();
        if (intent != null) {
            Intent chooser = Intent.createChooser(intent, "Choose App");
            if (isCallable(chooser, currentContext)) {
                getCurrentActivity().startActivityForResult(chooser, REQUEST_CODE);
            } else {
				WritableMap responseData = new WritableNativeMap();
                responseData.putString("message", "UPI supporting app not installed");
                responseData.putString("status", FAILURE);
                this.failureHandler.invoke(responseData);
            }
        }
    }

    private boolean isCallable(Intent intent, Context context) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

		WritableMap responseData = new WritableNativeMap();
        try {
            if (data == null) {
                responseData.putString("status", FAILURE);
                responseData.putString("message", "No action taken");
                this.failureHandler.invoke(responseData);
                return;
            }

            if (requestCode == REQUEST_CODE) {
                Bundle bundle = data.getExtras();
		//response = "txnId=undefined&responseCode=00&ApprovalRefNo=undefined&Status=SUCCESS&txnRef=SRCN-1918-CEBPR8TBCANB0K"
		//Below codes are commented by Chandrajyoti
                //if (bundle.getString("response") == "SUCCESS") {
                //    responseData.putString("status", data.getStringExtra("Status"));
                //    responseData.putString("message", bundle.getString("response"));
                //    this.successHandler.invoke(responseData);

                //} else {
                //    responseData.putString("status", data.getStringExtra("Status"));
                //    responseData.putString("message", bundle.getString("response"));
                //    this.failureHandler.invoke(responseData);
                //}				
	        responseData.putString("message", bundle.getString("response"));
		this.successHandler.invoke(responseData);
            } else {
                responseData.putString("message", "Request Code Mismatch");
                responseData.putString("status", FAILURE);
                this.failureHandler.invoke(responseData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void hasBhim(Callback callaback) {
        Uri uri = Uri.parse("upi://pay?pa=payee_address&pn=payee_name&tn=transaction_name&am=1&cu=INR&url=url");//url with http or https
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClassName("in.org.npci.upiapp","in.org.npci.upiapp.HomeActivity");
        List<ResolveInfo> list = getCurrentActivity().getApplicationContext().getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        callaback.invoke(list.size() > 0);
    }
		
    @ReactMethod
    public void hasUpiApp(Callback callaback) {
        Uri uri = Uri.parse("upi://pay?pa=payee_address&pn=payee_name&tn=transaction_name&am=1&cu=INR&url=url");//url with http or https
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(intent, "Choose App");
        List<ResolveInfo> list = getCurrentActivity().getApplicationContext().getPackageManager().queryIntentActivities(chooser,PackageManager.MATCH_DEFAULT_ONLY);
        callaback.invoke(list.size() > 0);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
