package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.callbacks

class EllcieLog {
    var reference: String
        private set
    private var mClassName: String
    var message: String
        private set
    var runtimeTrace: String = ""
        private set
    var value: Number? = null
        private set
    var timestamp // ms
            : Long
        private set

    /**
     *
     * @param reference
     * @param message
     */
    constructor(reference: String, className: String, message: String, runtimeTrace: String) {
        this.reference = reference
        mClassName = className
        this.message = message
        this.runtimeTrace = runtimeTrace
        timestamp = System.currentTimeMillis()
    }

    /**
     *
     * @param reference
     * @param message
     */
    constructor(reference: String, className: String, message: String) {
        this.reference = reference
        mClassName = className
        this.message = message
        timestamp = System.currentTimeMillis()
    }

    /**
     *
     * @param reference
     * @param className
     * @param message
     * @param runtimeValue
     */
    constructor(reference: String, className: String, message: String, runtimeValue: Number?) {
        this.reference = reference
        mClassName = className
        this.message = message
        value = runtimeValue
        timestamp = System.currentTimeMillis()
    }
}