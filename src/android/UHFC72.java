package com.pactera.hifm.uhf;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.TimeZone;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.provider.Settings;
import android.widget.Toast;
import com.rscja.deviceapi.RFIDWithUHF;

public class UHFC72 extends CordovaPlugin {

    /**
     * Constructor.
     */
    public UHFC72() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like get
     * file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // init code
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into
     *                        JavaScript.
     * @return True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        try {
            final RFIDWithUHF mReader = RFIDWithUHF.getInstance();
            if (mReader != null) {
                if (action.equals("init")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            init(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("free")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            free(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("getPower")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            getPower(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("setPower")) {
                    final int iPower = args.getInt(0);
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            setPower(iPower, mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("inventorySingleTag")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            inventorySingleTag(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("startInventoryTag")) {
                    final int flagAnti = args.getInt(0);
                    final int initQ = args.getInt(1);
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            startInventoryTag(flagAnti, initQ, mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("startInventoryTagCnt")) {
                    final int flagAnti = args.getInt(0);
                    final int initQ = args.getInt(1);
                    final int cnt = args.getInt(2);
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            startInventoryTag(flagAnti, initQ, cnt, mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("readTagFromBuffer")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            readTagFromBuffer(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("stopInventory")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            stopInventory(mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("convertUiiToEPC")) {
                    final String uii = args.getString(0);
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            convertUiiToEPC(uii, mReader, callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("triggerListening")) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            // triggerListening(callbackContext);
                        }
                    });
                    return true;
                } else if (action.equals("toastMessage")) {
                    final String msg = "UHFC72.action.equals(\"toastMessage\")";
                    Toast.makeText(cordova.getActivity(), msg, Toast.LENGTH_SHORT).show();
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            // toastMessage(msg);
                            callbackContext.success(msg);
                        }
                    });
                    return true;
                } else {
                    return false;
                }
            }
            callbackContext.error("mReader is null!");
            Toast.makeText(cordova.getActivity(), "Toast: mReader is null!", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception ex) {
            // Toast.makeText(cordova.getActivity(), ex.getMessage(),
            // Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    private void init(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            if (mReader.init()) {
                callbackContext.success("Successfully initialized the UHF module.");
            } else {
                // toastMessage("Failure: UHFC72.init() returns false");
                callbackContext.error("Failed to initialize UHF module.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void free(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            if (mReader.free()) {
                callbackContext.success("The UHF module was successfully closed.");
            } else {
                // toastMessage("Failure: UHFC72.free() returns false");
                callbackContext.error("Failed to close the UHF module.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getPower(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            int iPower = mReader.getPower();
            if (iPower > -1) {
                callbackContext.success(iPower);
            } else {
                // toastMessage("Failure: UHFC72.getPower().iPower <= -1");
                callbackContext.error("Failed to read module power.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void setPower(int iPower, RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            if (mReader.setPower(iPower)) {
                callbackContext.success(iPower);
            } else {
                // toastMessage("Failure: UHFC72.setPower().iPower: ");
                callbackContext.error("Failed to set the power of the module.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void inventorySingleTag(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            String uiiStr = mReader.inventorySingleTag();
            if (uiiStr != null) {
                callbackContext.success(uiiStr);
            } else {
                // toastMessage("Failure: UHFC72.inventorySingleTag.uiiStr is null");
                callbackContext.error("Failed to identify the label in single step.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void startInventoryTag(int flagAnti, int initQ, RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            if (mReader.startInventoryTag(flagAnti, initQ)) {
                // toastMessage("flagAnti: " + flagAnti + " initQ: " + initQ);
                callbackContext.success("Start the recognition tag cycle successfully.");
            } else {
                // toastMessage("Failure: UHFC72.startInventoryTag() returns false");
                callbackContext.error("Failed to start the recognition tag cycle.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void startInventoryTag(int flagAnti, int initQ, int cnt, RFIDWithUHF mReader,
            CallbackContext callbackContext) {
        try {
            if (mReader.startInventoryTag(flagAnti, initQ, cnt)) {
                callbackContext.success("Start the recognition tag cycle successfully.");
            } else {
                // toastMessage("Failure: UHFC72.startInventoryTag() returns false");
                callbackContext.error("Failed to start the recognition tag cycle.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void readTagFromBuffer(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            String strTID, strUII;
            String strResult;
            String[] res = null;
            res = mReader.readTagFromBuffer();
            if (res != null) {
                // strTID = res[0];
                // strUII = res[1];
                // if
                // (!strTID.equals("0000000000000000")&&!strTID.equals("000000000000000000000000"))
                // {
                // strResult = "TID:" + strTID + "\n";
                // } else {
                // strResult = "";
                // }
                List<String> tagList = Arrays.asList(res);
                JSONArray tagJarray = new JSONArray(tagList);
                // callbackContext.success(strResult);
                callbackContext.success(tagJarray);
            } else {
                // toastMessage("Failure: UHFC72.readTagFromBuffer().res is null");
                callbackContext.error("Failed to read the TID and UII of the tag returned from the buffer.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void stopInventory(RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            if (mReader.stopInventory()) {
                callbackContext.success("The stop loop recognition is successful.");
            } else {
                // toastMessage("Failure: UHFC72.stopInventory() returns false");
                callbackContext.error("Stop loop recognition failed.");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void convertUiiToEPC(String uii, RFIDWithUHF mReader, CallbackContext callbackContext) {
        try {
            String epcStr = mReader.convertUiiToEPC(uii);
            callbackContext.success(epcStr);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    // private void triggerListening(CallbackContext callbackContext) {
    // try {
    // HomeKeyEventBroadCastReceiver receiver = new
    // HomeKeyEventBroadCastReceiver(cordova, callbackContext);
    // webView.getContext().registerReceiver(receiver, new
    // IntentFilter("com.rscja.android.KEY_DOWN"));
    // } catch (Exception e) {
    // callbackContext.error(e.getMessage());
    // }
    // }

    private void toastMessage(final String msg) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(cordova.getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
