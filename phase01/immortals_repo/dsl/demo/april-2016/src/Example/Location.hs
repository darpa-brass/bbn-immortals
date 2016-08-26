module Example.Location where

import DSL.Expr
import DSL.Type
import DSL.Resource


--
-- * Location Provider Challenge Problem
--


-- ** DFUs

-- | A list of all the location DFUs with associated names.
locationDFUs :: [(String, Expr Refined)]
locationDFUs =
    [ ("gps-android", gpsAndroid)
    , ("gps-bluetooth", gpsBluetooth)
    , ("gps-usb", gpsUsb)
    , ("gps-saasm", gpsSaasm)
    , ("dead-reckoning", deadReck)
    ]

-- | Use built-in android GPS API.
gpsAndroid :: Expr Refined
gpsAndroid = Fun "r" gpsAndroidT
    $ provide "Location"
    $ recCheck "GPS-Sat"
    $ recCheck "GPS-Dev"
    $ Use "r"

-- | Bluetooth based GPS.
gpsBluetooth :: Expr Refined
gpsBluetooth = Fun "r" gpsBluetoothT
    $ provide "Location"
    $ recCheck "GPS-Sat"
    $ recCheck "Ext-BT"
    $ Use "r"

-- | Generic USB-based GPS.
gpsUsb :: Expr Refined
gpsUsb = Fun "r" gpsUsbT
    $ provide "Location"
    $ recCheck "GPS-Sat"
    $ recCheck "Ext-USB"
    $ Use "r"

-- | USB-based SAASM GPS.
gpsSaasm :: Expr Refined
gpsSaasm = Fun "r" gpsSaasmT
    $ provide "Location"
    $ provide "SAASM-Location"
    $ recCheck "GPS-Sat"
    $ recCheck "Ext-USB"
    $ Use "r"

-- | Manual / dead reckoning location capability.
deadReck :: Expr Refined
deadReck = Fun "r" deadReckT
    $ provide "Location"
    $ recCheck "Has-UI"
    $ Use "r"


-- ** DFU types (eventually, these can be inferred)

-- | Type of gpsAndroid.
gpsAndroidT :: Schema Refined
gpsAndroidT = Forall ["r"]
    $ polyRec "r" [has "GPS-Sat", has "GPS-Dev"]
  :-> polyRec "r" [has "GPS-Sat", has "GPS-Dev", has "Location"]

-- | Type of gpsBluetooth.
gpsBluetoothT :: Schema Refined
gpsBluetoothT = Forall ["r"]
    $ polyRec "r" [has "GPS-Sat", has "Ext-BT"]
  :-> polyRec "r" [has "GPS-Sat", has "Ext-BT", has "Location"]

-- | Type of gpsUsb.
gpsUsbT :: Schema Refined
gpsUsbT = Forall ["r"]
    $ polyRec "r" [has "GPS-Sat", has "Ext-USB"]
  :-> polyRec "r" [has "GPS-Sat", has "Ext-USB", has "Location"]

-- | Type of gpsSaasm.
gpsSaasmT :: Schema Refined
gpsSaasmT = Forall ["r"]
    $ polyRec "r" [has "GPS-Sat", has "Ext-USB"]
  :-> polyRec "r" [has "GPS-Sat", has "Ext-USB", has "Location", has "SAASM-Location"]

-- | Type of deadReck.
deadReckT :: Schema Refined
deadReckT = Forall ["r"]
    $ polyRec "r" [has "Has-UI"]
  :-> polyRec "r" [has "Has-UI", has "Location"]


-- ** Initial environments

-- | All relevant initial environments for the location scenario.
locationEnvs :: [(String, Expr Refined)]
locationEnvs = namedInitEnvs ["GPS-Sat", "GPS-Dev", "Ext-BT", "Ext-USB", "Has-UI"]


-- ** Mission requirements

-- | Require location.
hasLocation :: Schema Refined
hasLocation = Forall ["r"] $ polyRec "r" [("Location", Bang tUnit)]

-- | Require SAASM location.
hasSaasm :: Schema Refined
hasSaasm = Forall ["r"] $ polyRec "r" [("SAASM-Location", Bang tUnit)]

-- | All relevant mission requirements for the location scenario.
locationReqs :: [(String, Schema Refined)]
locationReqs = [("location", hasLocation), ("saasm", hasSaasm)]
