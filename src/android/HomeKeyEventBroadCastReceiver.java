package com.pactera.hifm.uhf;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import android.content.BroadcastReceiver;
import android.view.KeyEvent;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

  static final String SYSTEM_REASON = "reason";
  static final String SYSTEM_HOME_KEY = "homekey"; // home key
  static final String SYSTEM_RECENT_APPS = "recentapps"; // long home key

  CordovaInterface cordova;
  CallbackContext callbackContext;

  public HomeKeyEventBroadCastReceiver(CordovaInterface cordova, CallbackContext callbackContext) {
    this.cordova = cordova;
    this.callbackContext = callbackContext;
    // Toast.makeText(cordova.getActivity(), "new HomeKeyEventBroadCastReceiver",
    // Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    // Toast.makeText(cordova.getActivity(), "onReceive",
    // Toast.LENGTH_SHORT).show();
    String action = intent.getAction();
    // Toast.makeText(cordova.getActivity(), action, Toast.LENGTH_SHORT).show();

    if ("com.rscja.android.KEY_DOWN".equals(action)) {
      int reason = intent.getIntExtra("Keycode", 280);
      // getStringExtra
      boolean long1 = intent.getBooleanExtra("Pressed", false);
      // home key
      // Toast.makeText(getApplicationContext(), "home key=" + reason + ", long1=" +
      // long1, Toast.LENGTH_SHORT).show();
      // callbackContext.success(reason + "," + long1);
      String msg = reason + ", " + long1;
      PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
      result.setKeepCallback(true);
      callbackContext.sendPluginResult(result);
    }
  }
}