/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.pactera.hifm.uhf;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
// import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.TimeZone;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import android.provider.Settings;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.view.*;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.RFIDWithUHFA8;
import com.rscja.deviceapi.entity.UHFTAGInfo;
// import com.rscja.deviceapi.Barcode1D;
import com.zebra.adc.decoder.Barcode2DWithSoft;

import com.module.interaction.ModuleConnector;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.nativec.tools.ModuleManager;

public class UhfInv extends CordovaPlugin {

  public static String platform; // Device OS
  public static String uuid; // Device UUID

  // private CallbackContext keyup_callback = null;
  // private CallbackContext keydown_callback = null;
  // private CallbackContext allkeys_callback = null;
  // private View currentView = null;

  /**
   * Constructor.
   */
  public UhfInv() {
  }

  Barcode2DWithSoft barcode2DWS;
  // Barcode1D mInstance;
  RFIDWithUHFUART mReader;
  private CallbackContext cordovaCallbackContext;
  HomeKeyEventBroadCastReceiver receiver;
  private String cwBarcode;
  private String cwEPC;
  private List<String> cwEPCs;
  private boolean cwInventoryLooping = false;
  private ModuleConnector connector = new ReaderConnector();
  private RFIDReaderHelper mReaderRB;
  private ReaderSetting m_curReaderSetting;
  private String rdOutputPower = null;
  private String rdEPC;
  private List<String> rdEPCs;
  private List<String> rdCompatibleResults;
  private int onInventoryTagEndCalled = 0;
  // private int rdBufferIndex = 0;
  private boolean rdInventoryStop = true;
  private boolean rdInventoryLoop = false;

  RXObserver rxObserver = new RXObserver() {
    @Override
    protected void onInventoryTag(RXInventoryTag tag) {
      rdEPC = tag.strEPC.replaceAll("\\s+", "");
      // toastMessage(rdEPC);
      Log.d("TAG", tag.strEPC);

      if (null != rdEPC && !rdEPCs.contains(rdEPC)) {
        rdEPCs.add(rdEPC);
      }
    }

    @Override
    protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
      if (0x8B == (endTag.cmd & 0xFF) && !rdInventoryStop
      // && onInventoryTagEndCalled < 3
      ) {
        // toastMessage(rdEPC);
        // toastMessage("endTag.cmd: " + String.valueOf(endTag.cmd & 0xFF));
        // toastMessage("mTotalRead: " + endTag.mTotalRead);
        onInventoryTagEndCalled++;
        // toastMessage("onInventoryTagEndCalled: " + onInventoryTagEndCalled);
        // Thread.sleep(10);
        // mReaderRB.setBeeperMode((byte) 0xFF, (byte) 0x02);
        // mReaderRB.cancelAccessEpcMatch((byte) 0xFF);

        // Handler handler = new Handler();
        // handler.postDelayed(new Runnable() {
        // @Override
        // public void run() {}
        // }, 1000);

        // Handler mLoopHandler = new Handler();
        // Runnable mLoopRunnable = new Runnable() {
        // public void run() {
        // // mLoopHandler.postDelayed(this, 2000);
        // }
        // };

        if (rdInventoryLoop) {
          SystemClock.sleep(10); // 9000
          mReaderRB.customizedSessionTargetInventory((byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01);
        } else {
          rdInventoryStop = true;
          if (null != rdEPC && !"".equals(rdEPC)) {
            cordovaCallbackContext.success(rdEPC);
          } else {
            cordovaCallbackContext.error("single rdEPC unspotted");
          }
        }
      }
    }

    @Override
    protected void refreshSetting(ReaderSetting readerSetting) {
      // m_curReaderSetting = readerSetting;
      rdOutputPower = String.valueOf(readerSetting.btAryOutputPower[0] & 0xFF);
      // toastMessage(rdOutputPower);
      // Toast.makeText(cordova.getActivity(), "Toast.makeText(): " + rdOutputPower,
      // Toast.LENGTH_SHORT).show();
      cordovaCallbackContext.success(rdOutputPower);
    }
  };

  public Barcode2DWithSoft.ScanCallback ScanBack = new Barcode2DWithSoft.ScanCallback() {
    @Override
    public void onScanComplete(int i, int length, byte[] bytes) {
      // toastMessage("i: " + i + "\nlength: " + length);
      // toastMessage("new String(bytes): " + new String(bytes));
      // toastMessage("Arrays.toString(bytes): " + Arrays.toString(bytes));

      if (length < 1) {
        if (length == -1) {
          cordovaCallbackContext.error("Scan cancel");
        } else if (length == 0) {
          cordovaCallbackContext.error("Scan Timeout");
        } else {
          cordovaCallbackContext.error("Scan fail");
        }
        cordovaCallbackContext = null;
      } else {
        try {
          cwBarcode = new String(bytes, 0, length, "ASCII");
          cordovaCallbackContext.success(cwBarcode);
        } catch (UnsupportedEncodingException ex) {
          cordovaCallbackContext.error("UnsupportedEncodingException ex: " + ex);
        }

        // barcode2DWS.stopScan();
        // cwBarcode = new String(bytes);
        // cordovaCallbackContext.success(cwBarcode);
      }
    }
  };

  /**
   * Sets the context of the Command. This can then be used to do things like get
   * file paths associated with the Activity.
   *
   * @param cordova The context of the main Activity.
   * @param webView The CordovaWebView Cordova is running in.
   */
  public void initialize(CordovaInterface cordova, CordovaWebView webView, final CallbackContext callbackContext) {
    super.initialize(cordova, webView);
    // UHFOrca50.uuid = getUuid();
    // barcode2DWS = Barcode2DWithSoft.getInstance();
  }

  // public void pluginInitialize() {
  // super.pluginInitialize();
  // try {
  // barcode2DWS = Barcode2DWithSoft.getInstance();
  // if (null != barcode2DWS) {
  // Toast.makeText(cordova.getActivity(), "barcode2DWS.isPowerOn(): " +
  // barcode2DWS.isPowerOn(), Toast.LENGTH_LONG).show();
  // } else {
  // Toast.makeText(cordova.getActivity(), "barcode2DWS is null",
  // Toast.LENGTH_LONG).show();
  // }
  // } catch (Exception e) {
  // Log.e("UHFINV", e.getMessage());
  // }
  // }

  /**
   * Executes the request and returns PluginResult.
   *
   * @param action          The action to execute.
   * @param args            JSONArry of arguments for the plugin.
   * @param callbackContext The callback id used when calling back into
   *                        JavaScript.
   * @return True if the action was valid, false if not.
   */
  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    try {
      // Toast.makeText(cordova.getActivity(), action, Toast.LENGTH_SHORT).show();

      // PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
      // result.setKeepCallback(true);

      if ("deviceInfo".equals(action)) {
        final String msg = "UhfInv.action.equals(\"deviceInfo\")";
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            // toastMessage(msg);
            toastMessage("MODEL: " + android.os.Build.MODEL);
            toastMessage("SERIAL: " + android.os.Build.SERIAL);
            toastMessage("PRODUCT: " + android.os.Build.PRODUCT);
            toastMessage("MANUFACTURER: " + android.os.Build.MANUFACTURER);
            toastMessage("HARDWARE: " + android.os.Build.HARDWARE);
            toastMessage("FINGERPRINT: " + android.os.Build.FINGERPRINT);

            // Chainway C72
            // "C72"
            // "HC720C180800007"
            // "c72c"
            // "wtk"
            // "mt6735"
            // "wtk/c72c/c72:6.0/MRA58K/1526604563:user/release-keys"

            // Chainway Ura8
            // "A8"
            // "a9ffd39a"
            // "N1"
            // "JSR"
            // "qcom"
            // "JSR/N1/N1:5.1.1/LMY47V/liuqifeng09261747:user/release-keys"

            // RodinBell Orca 50
            // "common"
            // "GB8ZA9UMK4"
            // "industrial"
            // "rockchip"
            // "rk30board"
            // "Android/rk3288:6.0.1/MXC89K/root09100641:userdebug/test-keys"

            callbackContext.success(msg);
          }
        });
        return true;
      }

      if ("common".equals(android.os.Build.MODEL)) {
        if ("init".equals(action)) {
          rdInventoryStop = true;
          rdInventoryLoop = false;
          if (connector.connectCom("dev/ttyS4", 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);

            mReaderRB = RFIDReaderHelper.getDefaultHelper();
            mReaderRB.registerObserver(rxObserver);

            // Thread.currentThread().sleep(500);

            // mReaderRB.realTimeInventory((byte) 0xff, (byte) 0x01);
            callbackContext.success("Orca50 init success");
          } else {
            throw new RuntimeException("connector.connectCom(\"dev/ttyS4\", 115200)) return false");
          }
          return true;
        } else if ("triggerListening".equals(action)) {
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              // toastMessage("triggerListening unavailable for Orca50");
              callbackContext.success("success: triggerListening unavailable for Orca50");
            }
          });
          return true;
        } else if ("toastMessage".equals(action)) {
          final String msg = "UHFOrca50.action.equals(\"toastMessage\")";
          // Toast.makeText(cordova.getActivity(), msg, Toast.LENGTH_SHORT).show();
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              // toastMessage(msg);
              toastMessage("SERIAL: " + android.os.Build.SERIAL); // "GB8ZA9UMK4"
              toastMessage("MODEL: " + android.os.Build.MODEL); // "common"
              toastMessage("PRODUCT: " + android.os.Build.PRODUCT); // "industrial"
              toastMessage("MANUFACTURER: " + android.os.Build.MANUFACTURER); // "rockchip"
              toastMessage("HARDWARE: " + android.os.Build.HARDWARE); // "rk30board"
              toastMessage("FINGERPRINT: " + android.os.Build.FINGERPRINT); // "Android/rk3288:6.0.1/MXC89K/root09100641:userdebug/test-keys"
              callbackContext.success(msg);
            }
          });
          return true;
        }

        if (mReaderRB != null && connector != null) {
          if ("startInventoryTag".equals(action)) {
            rdInventoryStop = false;
            rdInventoryLoop = true;
            onInventoryTagEndCalled = 0;
            rdEPC = null;
            rdEPCs = new ArrayList();
            // rdBufferIndex = 0;
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                // mReaderRB.setBeeperMode((byte) 0xFF, (byte) 0x01);
                mReaderRB.customizedSessionTargetInventory((byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01);
                callbackContext.success("Orca50 startInventoryTag success");
              }
            });
            return true;
          } else if ("stopInventory".equals(action)) {
            onInventoryTagEndCalled = 0;
            rdEPCs = new ArrayList();
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                rdInventoryStop = true;
                rdInventoryLoop = false;
                // mReaderRB.realTimeInventory((byte) 0xff, (byte) 0x01);
                // toastMessage(rdEPC);
                callbackContext.success("Orca50 stopInventory success");
              }
            });
            return true;
          } else if ("readEPCsFromBuffer".equals(action)) {
            // String joinedEPCs = String.join("", rdEPCs);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                rdReadEPCsFromBuffer(callbackContext);
              }
            });
            return true;
          } else if ("free".equals(action)) {
            rdInventoryStop = true;
            rdInventoryLoop = false;
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                if (mReaderRB != null) {
                  mReaderRB.unRegisterObserver(rxObserver);
                  mReaderRB = null;
                }
                if (connector != null) {
                  connector.disConnect();
                  connector = null;
                }
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
                callbackContext.success("Orca50 free success");
              }
            });
            return true;
          } else if ("setPower".equals(action)) {
            rdInventoryStop = true;
            rdInventoryLoop = false;
            final int iPower = args.getInt(0);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                mReaderRB.setOutputPower((byte) 0xff, (byte) iPower); // 0x21
                callbackContext.success("Orca50 setPower success");
              }
            });
            return true;
          } else if ("getPower".equals(action)) {
            rdInventoryStop = true;
            rdInventoryLoop = false;
            cordovaCallbackContext = callbackContext;
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                mReaderRB.getOutputPower((byte) 0xff);
                // callbackContext.success(rdOutputPower);
              }
            });
            return true;
          } else if ("inventorySingleTag".equals(action)) {
            cordovaCallbackContext = callbackContext;
            rdInventoryStop = false;
            rdInventoryLoop = false;
            onInventoryTagEndCalled = 0;
            rdEPC = null;
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                // mReaderRB.setBeeperMode((byte) 0xFF, (byte) 0x01);
                mReaderRB.customizedSessionTargetInventory((byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01);
                // callbackContext.success("action.equals(\"customizedSessionTargetInventory\")
                // success");
              }
            });
            return true;
          } else if ("readTagFromBuffer".equals(action)) {
            // String joinedEPCs = String.join("", rdEPCs);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                rdReadTagFromBuffer(callbackContext);
              }
            });
            return true;
          } else if ("convertUiiToEPC".equals(action)) {
            final String rdEPCRaw = args.getString(0);
            // // return one epc from buffer
            // if (rdBufferIndex >= rdEPCs.size()) {
            // rdBufferIndex = 0;
            // }

            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                // toastMessage("convertUiiToEPC unavailable for Orca50");
                // callbackContext.success("success: convertUiiToEPC unavailable for Orca50");
                // callbackContext.success(rdEPCs.get(rdBufferIndex++));

                if (null == rdEPCRaw || "".equals(rdEPCRaw)) {
                  callbackContext.success();
                } else {
                  // remove whitespaces
                  String convertedRdEpc = rdEPCRaw.replaceAll("\\s+", "");
                  callbackContext.success(convertedRdEpc);
                }
              }
            });
            return true;
          } else if ("startInventoryTagCnt".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                // toastMessage("startInventoryTagCnt unavailable for Orca50");
                callbackContext.success("success: startInventoryTagCnt unavailable for Orca50");
              }
            });
            return true;
          } else if ("ListenForButtonTrigger".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                int nResult = mReaderRB.setTrigger(true);
                callbackContext.success(nResult);
              }
            });
            return true;
          }
        }
        callbackContext.error("Orca50 action failure");
        // Toast.makeText(cordova.getActivity(), "Orca50 action failure",
        // Toast.LENGTH_SHORT).show();
        return false;
      }

      else if ("C66".equals(android.os.Build.MODEL) || "P700".equals(android.os.Build.MODEL)
          || "C71".equals(android.os.Build.MODEL) || "P800".equals(android.os.Build.MODEL)
          || "C72".equals(android.os.Build.MODEL) || "P810".equals(android.os.Build.MODEL)
          || "C76".equals(android.os.Build.MODEL) || "P820".equals(android.os.Build.MODEL)) {
        if ("triggerListening".equals(action)) {
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              triggerListening(callbackContext);
              // HomeKeyEventBroadCastReceiver hkReceiver = new
              // HomeKeyEventBroadCastReceiver(cordova, callbackContext);
              // webView.getContext().registerReceiver(hkReceiver, new
              // IntentFilter("com.rscja.android.KEY_DOWN"));
            }
          });
          return true;
        } else if ("toastMessage".equals(action)) {
          final String msg = "UHFC72.action.equals(\"toastMessage\")";
          // Toast.makeText(cordova.getActivity(), msg, Toast.LENGTH_SHORT).show();
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              toastMessage("MODEL: " + android.os.Build.MODEL); // "C72"
              toastMessage("VERSION.SDK_INT: " + android.os.Build.VERSION.SDK_INT); // "23"
              callbackContext.success(msg);
            }
          });
          return true;
        }

        if (action.contains("Barcode2DWithSoft")) {
          if ("Barcode2DWithSoftOpen".equals(action)) {
            if (null != barcode2DWS) {
              callbackContext.error("Failure: barcode2DWS is NOT null!");
              return false;
            } else {
              final int way = args.getInt(0);
              cordovaCallbackContext = null;
              barcode2DWS = Barcode2DWithSoft.getInstance();
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  Barcode2DWithSoftOpen(way, callbackContext);
                }
              });
              return true;
            }
          } else if ("Barcode2DWithSoftClose".equals(action)) {
            if (null == barcode2DWS) {
              callbackContext.error("Failure: barcode2DWS is null already!");
            } else {
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  Barcode2DWithSoftClose(callbackContext);
                }
              });
            }
            return true;
          }

          if (null != barcode2DWS) {
            if ("Barcode2DWithSoftIsPowerOn".equals(action)) {
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  Barcode2DWithSoftIsPowerOn(callbackContext);
                }
              });
              return true;
            }

            if (barcode2DWS.isPowerOn()) {
              if ("Barcode2DWithSoftScan".equals(action)) {
                cordovaCallbackContext = callbackContext;
                cordova.getThreadPool().execute(new Runnable() {
                  public void run() {
                    Barcode2DWithSoftScan(callbackContext);
                  }
                });
                return true;
              } else if ("Barcode2DWithSoftStopScan".equals(action)) {

                cordova.getThreadPool().execute(new Runnable() {
                  public void run() {
                    Barcode2DWithSoftStopScan(callbackContext);
                  }
                });
                return true;
              } else {
                callbackContext.error("CW Barcode2DWithSoft no action matched");
                return false;
              }
            } else {
              callbackContext.error("Failure: barcode2DWS.isPowerOn() is false");
              return false;
            }
          } else {
            callbackContext.error("Failure: barcode2DWS is null");
            return false;
          }
        } else {
          // final RFIDWithUHFUART mReader = RFIDWithUHFUART.getInstance();
          if ("init".equals(action)) {
            if (null != this.mReader) {
              callbackContext.error("RFIDWithUHFUART mReader is NOT null!");
              return false;
            } else {
              if ("A8".equals(android.os.Build.MODEL)) {
                this.mReader = RFIDWithUHFA8.getInstance();
              } else {
                this.mReader = RFIDWithUHFUART.getInstance();
              }
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  init(callbackContext);
                }
              });
              return true;
            }
          } else if ("free".equals(action)) {
            if (null != this.mReader) {
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  free(callbackContext);
                  mReader = null;
                }
              });
            }
            return true;
          }
          if (null == this.mReader) {
            callbackContext.error("RFIDWithUHFUART mReader is null!");
            return false;
          }
          if ("readEPCsFromBuffer".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                cwReadEPCsFromBuffer(callbackContext);
              }
            });
            return true;
          } else if ("startInventoryTag".equals(action)) {
            final int flagAnti = args.getInt(0);
            final int initQ = args.getInt(1);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                startInventoryTag(flagAnti, initQ, callbackContext);
              }
            });
            return true;
          } else if ("stopInventory".equals(action)) {
            if (this.cwInventoryLooping) {
              cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                  stopInventory(callbackContext);
                }
              });
            }
            return true;
          } else if ("setPower".equals(action)) {
            final int iPower = args.getInt(0);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                setPower(iPower, callbackContext);
              }
            });
            return true;
          } else if ("getPower".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                getPower(callbackContext);
              }
            });
            return true;
          } else if ("inventorySingleTag".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                inventorySingleTag(callbackContext);
              }
            });
            return true;
          } else if ("startInventoryTagCnt".equals(action)) {
            final int flagAnti = args.getInt(0);
            final int initQ = args.getInt(1);
            final int cnt = args.getInt(2);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                startInventoryTag(flagAnti, initQ, cnt, callbackContext);
              }
            });
            return true;
          } else if ("readTagFromBuffer".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                readTagFromBuffer(callbackContext);
              }
            });
            return true;
          } else if ("convertUiiToEPC".equals(action)) {
            final String uii = args.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                convertUiiToEPC(uii, callbackContext);
              }
            });
            return true;
          } else {
            // Toast.makeText(cordova.getActivity(), "Toast: C72 no action matched",
            // Toast.LENGTH_SHORT).show();
            callbackContext.error("Chainway no action matched!");
            return false;
          }
        }
      } else {
        // Toast.makeText(cordova.getActivity(), "Toast: No device matched",
        // Toast.LENGTH_SHORT).show();
        callbackContext.error("No device matched!");
        return false;
      }
    } catch (Exception ex) {
      // Toast.makeText(cordova.getActivity(), ex.getMessage(),
      // Toast.LENGTH_SHORT).show();
      ex.printStackTrace();
      callbackContext.error(ex.getMessage());
      return false;
    }
  }

  // --------------------------------------------------------------------------
  // LOCAL METHODS
  // --------------------------------------------------------------------------

  private void toastMessage(final String msg) {
    cordova.getActivity().runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(cordova.getActivity(), "toastMessage(): " + msg, Toast.LENGTH_SHORT).show();
      }
    });
    // Toast.makeText(cordova.getActivity(), msg, Toast.LENGTH_LONG).show();
  }

  // private void threadSleep() {
  // cordova.getActivity().runOnUiThread(new Runnable() {
  // public void run() {
  // Thread.currentThread().sleep(500);
  // }
  // });
  // }

  private void init(CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("init() failed: STILL LOOPING!");
        return;
      }
      this.cwInventoryLooping = false;
      if (this.mReader.init()) {
        callbackContext.success("Successfully initialized the UHF module.");
      } else {
        // toastMessage("Failure: UHFC72.init() return false");
        callbackContext.error("Failed to initialize UHF module.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void free(CallbackContext callbackContext) {
    try {
      this.cwInventoryLooping = false;
      if (this.mReader.free()) {
        callbackContext.success("The UHF module was successfully closed.");
      } else {
        // toastMessage("Failure: UHFC72.free() return false");
        callbackContext.error("Failed to close the UHF module.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      // this.mReader = null;
      callbackContext.error(e.getMessage());
    }
  }

  private void getPower(CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("getPower() failed: STILL LOOPING!");
        return;
      }
      int iPower = this.mReader.getPower();
      if (iPower > -1) {
        callbackContext.success(iPower);
      } else {
        // toastMessage("Failure: UHFC72.getPower().iPower <= -1");
        callbackContext.error("Failed to read module power.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void setPower(int iPower, CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("setPower() failed: STILL LOOPING!");
        return;
      }
      if (this.mReader.setPower(iPower)) {
        callbackContext.success(iPower);
      } else {
        // toastMessage("Failure: UHFC72.setPower().iPower: ");
        callbackContext.error("Failed to set the power of the module.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void inventorySingleTag(CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("inventorySingleTag() failed: STILL LOOPING!");
        return;
      }
      UHFTAGInfo strUII = this.mReader.inventorySingleTag();
      if (null != strUII) {
        callbackContext.success(strUII.getEPC());
      } else {
        // toastMessage("Failure: UHFC72.inventorySingleTag.uiiStr is null");
        callbackContext.error("Failed to identify the label in single step.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void startInventoryTag(int flagAnti, int initQ, CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("startInventoryTag() failed: STILL LOOPING!");
        return;
      }
      if (this.mReader.startInventoryTag()) {
        this.cwInventoryLooping = true;
        this.cwEPCs = new ArrayList();
        callbackContext.success("Start the recognition tag cycle successfully.");
        this.cwReadingInventory();
      } else {
        // toastMessage("Failure: UHFC72.startInventoryTag() return false");
        callbackContext.error("Failed to start the recognition tag cycle.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void startInventoryTag(int flagAnti, int initQ, int cnt, CallbackContext callbackContext) {
    try {
      if (this.cwInventoryLooping) {
        callbackContext.error("startInventoryTag() failed: STILL LOOPING!");
        return;
      }
      if (this.mReader.startInventoryTag(flagAnti, initQ, cnt)) {
        this.cwInventoryLooping = true;
        this.cwEPCs = new ArrayList();
        this.cwReadingInventory();
        callbackContext.success("Start the recognition tag cycle successfully.");
      } else {
        // toastMessage("Failure: UHFC72.startInventoryTag() return false");
        callbackContext.error("Failed to start the recognition tag cycle.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void cwReadingInventory() {
    UHFTAGInfo res = null;
    while (this.cwInventoryLooping) {
      res = this.mReader.readTagFromBuffer();
      if (null != res) {
        this.cwEPC = res.getEPC();
        if (null != this.cwEPC && 0 != this.cwEPC.length() && !this.cwEPCs.contains(this.cwEPC)) {
          this.cwEPCs.add(this.cwEPC);
        }
      }
      SystemClock.sleep(10);
    }
  }

  private void cwReadEPCsFromBuffer(CallbackContext callbackContext) {
    try {
      if (null == this.cwEPCs) {
        // throw new RuntimeException("cwEPCs is null!");
        this.cwEPCs = new ArrayList();
      }
      JSONArray resultJarray = new JSONArray(this.cwEPCs);
      callbackContext.success(resultJarray);
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void readTagFromBuffer(CallbackContext callbackContext) {
    // try {
    // // String strTID, strUII;
    // // String strResult;
    // String[] res = null;
    // res = mReader.readTagFromBuffer();
    // if (res != null) {
    // // strTID = res[0];
    // // strUII = res[1];
    // // if
    // //
    // (!strTID.equals("0000000000000000")&&!strTID.equals("000000000000000000000000"))
    // // {
    // // strResult = "TID:" + strTID + "\n";
    // // } else {
    // // strResult = "";
    // // }

    // // String epcStr = mReader.convertUiiToEPC(res[1]);
    // // callbackContext.success(epcStr);

    // List<String> tagList = Arrays.asList(res);
    // JSONArray tagJarray = new JSONArray(tagList);
    // callbackContext.success(tagJarray);

    // // List<String> epcList = new ArrayList();
    // // epcList.add(mReader.convertUiiToEPC(res[1]));
    // // JSONArray epcJarray = new JSONArray(epcList);
    // // callbackContext.success(epcJarray);
    // } else {
    // // toastMessage("Failure: UHFC72.readTagFromBuffer().res is null");
    // callbackContext.error("Failed to read the TID and UII of the tag returned
    // from the buffer.");
    // }
    // } catch (Exception e) {
    // callbackContext.error(e.getMessage());
    // }
    callbackContext.error("readTagFromBuffer() DEPRECATED!");
  }

  private void stopInventory(CallbackContext callbackContext) {
    try {
      this.cwInventoryLooping = false;
      // this.cwEPCs = new ArrayList();
      if (this.mReader.stopInventory()) {
        callbackContext.success("The stop loop recognition is successful.");
      } else {
        // toastMessage("Failure: UHFC72.stopInventory() return false");
        callbackContext.error("Stop loop recognition failed.");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void convertUiiToEPC(String uii, CallbackContext callbackContext) {
    try {
      // String epcStr = mReader.convertUiiToEPC(uii);
      // callbackContext.success(epcStr);
      // } catch (Exception e) {
      // callbackContext.error(e.getMessage());
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void triggerListening(CallbackContext callbackContext) {
    try {
      if (this.receiver == null) {
        this.receiver = new HomeKeyEventBroadCastReceiver(cordova, callbackContext);
      }
      webView.getContext().registerReceiver(receiver, new IntentFilter("com.rscja.android.KEY_DOWN"));
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void rdReadEPCsFromBuffer(CallbackContext callbackContext) {
    try {
      if (null == rdEPCs) {
        throw new RuntimeException("rdEPCs is null!");
      } else {
        JSONArray resultJarray = new JSONArray(rdEPCs);
        callbackContext.success(resultJarray);
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private void rdReadTagFromBuffer(CallbackContext callbackContext) {
    try {
      if (null != rdEPC || "null".equals(rdEPC) || "".equals(rdEPC)) {
        rdCompatibleResults = new ArrayList();
        rdCompatibleResults.add(rdEPC);
        rdCompatibleResults.add(rdEPC);
        JSONArray resultJarray = new JSONArray(rdCompatibleResults);
        callbackContext.success(resultJarray);
      } else {
        throw new RuntimeException("rdEPC is empty");
      }
    } catch (Exception e) {
      // toastMessage(e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  // way（0：All methods，1：Bar code，2：QR code）
  private void Barcode2DWithSoftOpen(int way, CallbackContext callbackContext) {
    try {
      if (barcode2DWS.open(cordova.getActivity())) {
        barcode2DWS.stopScan();

        // barcode2DWS.setParameter(6, 1);
        // barcode2DWS.setParameter(22, 0);
        // barcode2DWS.setParameter(23, 55);
        // barcode2DWS.setParameter(8, 1); // CODE128
        // barcode2DWS.setParameter(209, 0);
        // barcode2DWS.setParameter(210, 55);
        // barcode2DWS.setParameter(300, 1); // IMG_AIM_SNAPSHOT
        // barcode2DWS.setParameter(306, 1); // IMG_AIM_MODE
        // barcode2DWS.setParameter(324, 0); // IMG_VIDEOVF
        // barcode2DWS.setParameter(361, 1); // IMG_IMAGE_ILLUM
        // barcode2DWS.setParameter(402, 1); // PICKLIST_MODE
        // barcode2DWS.setParameter(293, 0); // QRCODE
        if (way == 1) { // Disable QR code
          barcode2DWS.setParameter(293, 0);
        }
        if (way == 2) { // Open the QR code
          barcode2DWS.disableAllCodeTypes();
          barcode2DWS.setParameter(293, 1);
        }

        barcode2DWS.setScanCallback(ScanBack);

        callbackContext.success("Successfully opened the 2D scanning device.");
      } else {
        // toastMessage("Failure: barcode2DWS.open() return false");
        callbackContext.error("Failed to open the 2D scanning device.");
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  private void Barcode2DWithSoftScan(CallbackContext callbackContext) {
    try {
      cwBarcode = null;

      // barcode2DWS.setScanCallback(ScanBack);
      barcode2DWS.scan();

      // do {
      // barcode2DWS.scan();
      // // SystemClock.sleep(2000);
      // try {
      // Thread.sleep(5000);
      // } catch (InterruptedException ie) {
      // cwBarcode = null;
      // callbackContext.error(ie.getMessage());
      // }
      // } while ("".equals(cwBarcode));

      // barcode2DWS.setScanCallback(new Barcode2DWithSoft.ScanCallback() {
      // @Override
      // public void onScanComplete(int i, int length, byte[] data) {
      // if (length < 1) {
      // return;
      // } else {
      // cwBarcode = new String(data);
      // cwBarcode = cwBarcode.replaceAll("\u0000", "");
      // callbackContext.success(cwBarcode);
      // }

      // // try {
      // // JSONObject result = new JSONObject().put("text", barCode);
      // // callbackContext.sendPluginResult(new PluginResult(Status.OK, result));
      // // } catch (JSONException e) {
      // // e.printStackTrace();
      // // callbackContext.sendPluginResult(new PluginResult(Status.ERROR, ""));
      // // }
      // }
      // });
      // barcode2DWS.scan();

    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  // private void cwScanningBarcode(Barcode2DWithSoft barcode2DWS) {
  // String[] res = null;
  // while (!cwInventoryStop) {
  // // toastMessage("cwReadingInventory...");
  // res = mReader.readTagFromBuffer();
  // if (res != null) {
  // cwEPC = mReader.convertUiiToEPC(res[1]);
  // if (!cwEPCs.contains(cwEPC)) {
  // cwEPCs.add(cwEPC);
  // }
  // }
  // SystemClock.sleep(10);
  // }
  // }

  private void Barcode2DWithSoftStopScan(CallbackContext callbackContext) {
    try {
      barcode2DWS.stopScan();
      cwBarcode = null;
      callbackContext.success("barcode2DWS.stopScan() success");
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  private void Barcode2DWithSoftIsPowerOn(CallbackContext callbackContext) {
    try {
      callbackContext.success(barcode2DWS.isPowerOn() + "");
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  private void Barcode2DWithSoftClose(CallbackContext callbackContext) {
    try {
      cwBarcode = null;
      barcode2DWS.stopScan();
      if (barcode2DWS.close()) {
        barcode2DWS = null;
        cordovaCallbackContext = null;
        callbackContext.success("Successfully shut down the 2D scanning device.");
      } else {
        barcode2DWS = null;
        cordovaCallbackContext = null;
        // toastMessage("Failure: barcode2DWS.close() return false");
        callbackContext.error("Failed to close the 2D scanning device.");
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }
}