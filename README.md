# Cordova Plugin for Orca50 Devices

This plugin provides and API to communicate with the [Orca 50](http://www.rodinbell.com/en/ProductDetail.html?ID=12423) UHF RFID device

## Installation

```
cordova plugin add https://github.com/esidate/cordova-plugin-orca50.git
```

An instance of the module will be created and stored in a javascript global variable with the name `uhf`.

## Usage examples

```js
init() {
    // Initialize UHF module. Call init() to switch on the device before operating the device.
    uhf.init(
    (callback) => {
        this.initState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.initState = "Error";
        alert("Error: " + callback);
    }
    );
}

free() {
    uhf.free(
    // Switch off UHF module. Call free() to switch off device after using.
    (callback) => {
        this.freeState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.freeState = "Error";
        alert("Error: " + callback);
    }
    );
}

getPower() {
    // Read power of module, returns an int.
    uhf.getPower(
    (callback) => {
        this.getPowerState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.getPowerState = "Error";
        alert("Error: " + callback);
    }
    );
}

inventorySingleTag() {
    // This formula identify tag in single step, return a String UII for only one time.
    uhf.inventorySingleTag(
    (callback) => {
        this.inventorySingleTagState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.inventorySingleTagState = "Error";
        alert("Error: " + callback);
    }
    );
}

startInventoryTag() {
    // Activate identification Tag circulation,
    //  upload the identified tag number to buffer zone.
    //  After starting the circular identification,
    //  the module will respond stopInventory() formula.
    uhf.startInventoryTag(
    0, // flagAnti is an int which uses anti-collision identification function or not，default 0.
    0, // initQ is an int Initial Q value of anti-collision identification process, it will be valid if flagAnti is 1.
    (callback) => {
        this.startInventoryTagState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.startInventoryTagState = "Error";
        alert("Error: " + callback);
    }
    );
}

startInventoryTagCnt() {
    // Activate identification Tag circulation,
    // upload the identified tag number to buffer zone.
    // After starting the circular identification,
    // the module will respond stopInventory() formula.
    uhf.startInventoryTagCnt(
    0, // flagAnti is an int which uses anti-collision identification function or not，default 0.
    0, // initQ is an int Initial Q value of anti-collision identification process, it will be valid if flagAnti is 1.
    6, // cnt is an int TID length, unit is 'byte'.
    (callback) => {
        this.startInventoryTagCntState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.startInventoryTagCntState = "Error";
        alert("Error: " + callback);
    }
    );
}

readEPCsFromBuffer() {
    // Returns a JSONArray of all returned EPC in buffer zone.
    // null means reading failed.
    uhf.readEPCsFromBuffer(
    (callback) => {
        this.readEPCsFromBufferState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.readEPCsFromBufferState = "Error";
        alert("Error: " + callback);
    }
    );
}

readTagFromBuffer() {
    // Read returned TID and UII infor of tag in buffer zone.
    // Returns a JSONArray of which index 0 means TID infor,
    // index 1 means UII, index 2 means RSSI (if it is not supported,
    // it will return N/A), null means reading failed.
    uhf.readTagFromBuffer(
    (callback) => {
        this.readTagFromBufferState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.readTagFromBufferState = "Error";
        alert("Error: " + callback);
    }
    );
}

stopInventory() {
    // Stop circular identification.
    uhf.readTagFromBuffer(
    (callback) => {
        this.stopInventoryState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.stopInventoryState = "Error";
        alert("Error: " + callback);
    }
    );
}

convertUiiToEPC() {
    // UII transform to EPC.
    uhf.convertUiiToEPC(
    "A1234", // uii is a String UII data.
    (callback) => {
        this.convertUiiToEPCState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.convertUiiToEPCState = "Error";
        alert("Error: " + callback);
    }
    );
}

Barcode2DWithSoftOpen() {
    // Switch on 2D scanning device.
    // Call Barcode2DWithSoftOpen() to switch on the device before using.
    // return power-on status.
    uhf.Barcode2DWithSoftOpen(
    (callback) => {
        this.Barcode2DWithSoftOpenState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.Barcode2DWithSoftOpenState = "Error";
        alert("Error: " + callback);
    }
    );
}

ListenForButtonTrigger() {
    uhf.ListenForButtonTrigger(
    (callback) => {
        this.ListenForButtonTriggerState = "Success";
        alert("Success: " + callback);
    },
    (callback) => {
        this.ListenForButtonTriggerState = "Error";
        alert("Error: " + callback);
    }
    );
},
```
