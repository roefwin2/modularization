package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase

import com.google.firebase.database.DataSnapshot


class FallEvent// event
    (snapshot: DataSnapshot) {
    var timestamp: Long = 0
        private set
    var eventType = ""
    var payload: String? = ""

    init {
        snapshot.key?.let { key -> timestamp = java.lang.Long.valueOf(key) }
        snapshot.value?.let{ value ->
            val map = value as HashMap<*, *>
            val keys = map.keys
            for(key in keys){
                //event
                eventType = key as String
                payload = map[key] as String

            }
        }
    }
}