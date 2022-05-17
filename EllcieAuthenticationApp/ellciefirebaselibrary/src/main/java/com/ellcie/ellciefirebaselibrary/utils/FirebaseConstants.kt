package com.ellcie.ellciefirebaselibrary.utils

object FirebaseConstants {
    const val FILE_OTA = "app.ebl"
    const val NODE_TRIP_ARRIVAL_TS = "arrivalTs"
    const val NODE_TRIP_DEPARTURE_TS = "departureTs"
    const val NODE_TRIP_MAXIMUM_RISKS = "maxRisk"
    const val NODE_TRIP_STREAMING_MODE = "streamingMode"
    const val NODE_TRIP_SERIAL_NUMBER = "glasses"
    const val TAG = "FirebaseDataHelper"
    const val NODE_LOGS = "Logs"
    const val NODE_GLASSES_LOGS = "GlassesLogs"
    const val NODE_SUPPORT_ISSUE = "SupportTickets"

    const val NODE_TRIPS = "Trips"
    const val NODE_TRIP_DETAILS = "TripDetails"
    const val NODE_TRIP_POINTS = "points"
    const val NODE_GPS_POINTS = "gps"
    const val NODE_COORD_POINTS = "coord"
    const val NODE_LAT_POINTS = "lat"
    const val NODE_LONG_POINTS = "long"
    const val NODE_TRIP_FIRMWARE_VERSION = "firmwareVersion"
    const val NODE_TRIP_BEST_MEAN_VALUE = "bestMeanValue"
    const val NODE_TRIP_ALGO_SENSITIVITY = "algSensitivity"


    const val NODE_PROFILES = "Users"
    const val mDbPrefix = "2" // This prefix determines which base is used.


    const val NODE_INVENTORY = "Inventory"
    const val NODE_INVENTORY_FIRMWARE = "firmware"
    const val NODE_INVENTORY_FIRMWARE_UPDATE_TIME = "updateTS"
    const val NODE_INVENTORY_FIRMWARE_VERSION = "version"
    const val NODE_INVENTORY_OWNER = "owner"
    const val NODE_INVENTORY_PRODUCT = "product"


    const val NODE_FIRMWARES = "Firmwares"
    const val NODE_FIRMWARE_CHANNELS = "channels"
    const val NODE_FIRMWARE_VERSIONS = "versions"
    const val NODE_FIRMWARE_MIN_DRIVER = "minMobileVersionDriver"
    const val NOTES_FR = "notes/FR"
    const val NOTES_EN = "notes/EN"
    const val NODE_VERSION = "version"
    const val NODE_RELEASE_DATE = "releaseDate"
    const val FIRMWARE_STORAGE_REF = "firmware"

    const val NODE_DEFAULTS = "Defaults"
    const val NODE_DEFAULT_FEATURES_PERMISSIONS = "FeaturesPermissions"
    const val NODE_DEFAULTS_OPTICIAN_TEST = "opticianTest"
    const val NODE_DEFAULT_URLS = "urls"
    const val NODE_DEFAULT_USER_CONFIG = "userConfig"
    const val NODE_DEFAULT_USER_GLASSES = "userGlasses"
    const val NODE_DEFAULT_MINIMAL_FW_VERSION = "minimalFwVersion"

    const val NODE_STREAMING_TRIP = "streaming"
    const val NODE_STREAMING_OPTICIAN = "optician"
    const val NODE_SENSORS_DATA_FIRMWARE_VERSION = "firmware_version"
    const val NODE_ALGO_EVENT = "algo_event"


    const val NODE_OPTICIAN = "Optician"
    const val NODE_OPTICIAN_TEST = "tests"
    const val NODE_OPTICIAN_TEST_START_TS = "startTs"
    const val NODE_OPTICIAN_TEST_FIRMWARE_VERSION = "firmwareVersion"
    const val NODE_OPTICIAN_TEST_STOP_TS = "stopTs"
    const val NODE_OPTICIAN_TEST_STOP_REASON = "stopReason"
    const val NODE_OPTICIAN_TEST_ERROR_CODE = "errorCode"
    const val NODE_OPTICIAN_TEST_OUTCOME = "outcome"
    const val NODE_OPTICIAN_TEST_OUTCOME_BLINKS_EXPECTED = "blinksExpected"
    const val NODE_OPTICIAN_TEST_OUTCOME_BLINKS_DETECTED_BY_GLASSES =
        "blinksDetectedByGlasses"
    const val NODE_OPTICIAN_TEST_OUTCOME_SUCCESS = "success"
    const val NODE_OPTICIAN_TEST_EVENTS = "events"

    const val NODE_DATA_LABELING_PROTOCOLS = "Lab/dataLabeling/config/protocols"
    const val NODE_DATA_LABELING_DATA = "Lab/dataLabeling/data"
    const val NODE_DATA_LABELING_DATA_PROTOCOLS = "config/protocols"
    const val NODE_DATA_LABELING_DATA_PROTOCOL_NAME = "protocolName"
    const val NODE_DATA_LABELING_DATA_LABELING = "labeling"

    const val NODE_USER_FIRST_PAIRING = "firstPairingDate"
    const val NODE_USER_LAST_CONNECTION = "lastConnectionDate"

    const val NODE_USER_GLASSES_BATTERY = "battery"
    const val NODE_USER_GLASSES_BATTERY_LAST_UPDATE = "lastUpdate"
    const val NODE_USER_GLASSES_BATTERY_LEVEL = "level"
    const val NODE_USER_GLASSES_BATTERY_STATE = "state"

    const val NODE_USER_GLASSES_BLE = "ble"
    const val NODE_USER_GLASSES_BLE_UPDATE_TS = "updateTs"
    const val NODE_USER_GLASSES_BLE_STATUS = "status"

    const val NODE_USER_LAST_GLASSES_USED = "lastGlassesUsed"
    const val NODE_USER_LAST_GLASSES_USED_GLASSES_ID = "glassesId"
    const val NODE_USER_LAST_GLASSES_USED_LAST_UPDATE = "lastUpdate"
    const val NODE_USER_CONFIG = "config"
    const val NODE_USER_CONFIG_SENSITIVITY_LEVEL = "sensitivityLevel"
}