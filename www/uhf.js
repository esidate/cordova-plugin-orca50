var channel = require("cordova/channel");
var exec = require("cordova/exec");
var cordova = require("cordova");
// var argscheck = require('cordova/argscheck');
// var utils = require('cordova/utils');

// channel.createSticky('onCordovaInfoReady');
// Tell cordova channel to wait on the CordovaInfoReady event
// channel.waitForInitialization('onCordovaInfoReady');

channel.onCordovaReady.subscribe(function () {
  function success(msg) {
    cordova.fireWindowEvent("triggerActivated", {
      msg: msg,
    });
    // alert("channel.onCordovaReady.subscribe... " + msg);
  }
  try {
    document.addEventListener("menubutton", () => {
      success(null);
    });
  } catch (e) {
    console.warn(e);
  }
});

/**
 * @constructor
 */
function UHF() {}

UHF.prototype.init = function (success, error) {
  exec(success, error, "UHFINV", "init", []);
};
UHF.prototype.free = function (success, error) {
  exec(success, error, "UHFINV", "free", []);
};
UHF.prototype.getPower = function (success, error) {
  exec(success, error, "UHFINV", "getPower", []);
};
UHF.prototype.setPower = function (iPower, success, error) {
  exec(success, error, "UHFINV", "setPower", [iPower]);
};
UHF.prototype.inventorySingleTag = function (success, error) {
  exec(success, error, "UHFINV", "inventorySingleTag", []);
};
UHF.prototype.startInventoryTag = function (flagAnti, initQ, success, error) {
  exec(success, error, "UHFINV", "startInventoryTag", [flagAnti, initQ]);
};
UHF.prototype.startInventoryTagCnt = function (
  flagAnti,
  initQ,
  cnt,
  success,
  error
) {
  exec(success, error, "UHFINV", "startInventoryTagCnt", [
    flagAnti,
    initQ,
    cnt,
  ]);
};
UHF.prototype.stopInventory = function (success, error) {
  exec(success, error, "UHFINV", "stopInventory", []);
};
UHF.prototype.readTagFromBuffer = function (success, error) {
  exec(success, error, "UHFINV", "readTagFromBuffer", []);
};
UHF.prototype.readEPCsFromBuffer = function (success, error) {
  exec(success, error, "UHFINV", "readEPCsFromBuffer", []);
};
UHF.prototype.convertUiiToEPC = function (uii, success, error) {
  exec(success, error, "UHFINV", "convertUiiToEPC", [uii]);
};
UHF.prototype.triggerListening = function (success) {
  exec(success, null, "UHFINV", "triggerListening", []);
};
UHF.prototype.toastMessage = function (success, error) {
  exec(success, error, "UHFINV", "toastMessage", []);
};
UHF.prototype.deviceInfo = function (success, error) {
  exec(success, error, "UHFINV", "deviceInfo", []);
};
// UHF.prototype.c72Toast = function (success, error) {
//     exec(success, error, 'UHFC72', 'toastMessage', []);
// };
// UHF.prototype.keyUp = function (success, error) {
//     exec(success, error, 'UHFINV', 'keyUp', []);
// };
// UHF.prototype.keyDown = function (success, error) {
//     exec(success, error, 'UHFINV', 'keyDown', []);
// };
// UHF.prototype.stopKeyUp = function (success, error) {
//     exec(success, error, 'UHFINV', 'stopKeyUp', []);
// };
// UHF.prototype.stopKeyDown = function (success, error) {
//     exec(success, error, 'UHFINV', 'stopKeyDown', []);
// };
// UHF.prototype.testKeyUp = function (success, error) {
//     exec(success, error, 'UHFINV', 'testKeyUp', []);
// };
// UHF.prototype.testKeyDown = function (success, error) {
//     exec(success, error, 'UHFINV', 'testKeyDown', []);
// };
// UHF.prototype.getStatus = function (success, error) {
//     exec(success, error, 'UHFINV', 'getStatus', []);
// };
UHF.prototype.Barcode2DWithSoftIsPowerOn = function (success, error) {
  exec(success, error, "UHFINV", "Barcode2DWithSoftIsPowerOn", []);
};
UHF.prototype.Barcode2DWithSoftOpen = function (way, success, error) {
  exec(success, error, "UHFINV", "Barcode2DWithSoftOpen", [way]);
};
UHF.prototype.Barcode2DWithSoftClose = function (success, error) {
  exec(success, error, "UHFINV", "Barcode2DWithSoftClose", []);
};
UHF.prototype.Barcode2DWithSoftScan = function (success, error) {
  exec(success, error, "UHFINV", "Barcode2DWithSoftScan", []);
};
UHF.prototype.Barcode2DWithSoftStopScan = function (success, error) {
  exec(success, error, "UHFINV", "Barcode2DWithSoftStopScan", []);
};
UHF.prototype.ListenForButtonTrigger = function (success, error) {
  exec(success, error, "UHFINV", "ListenForButtonTrigger", []);
};

module.exports = new UHF();
