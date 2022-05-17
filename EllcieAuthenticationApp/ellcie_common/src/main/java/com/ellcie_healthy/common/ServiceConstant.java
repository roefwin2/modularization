package com.ellcie_healthy.common;

public class ServiceConstant {
    public final static String KEEP_APP = "keepApp";
    public final static String EXIT_APP = "exit";



    /**
     * Used to Broadcast the fall status to the activity
     **/
    public static final String BROADCAST_FALL_STATUS = "BROADCAST_FALL_STATUS";
    public static final String FALL_VALUE = "FALL_VALUE";
    public static final String FALL_COUNTDOWN = "FALL_COUNTDOWN";
    public static final String FALL_RESCUE_STATE = "FALL_RESCUE_STATE";
    public static final String FALL_RESCUE_HELPER = "FALL_RESCUE_HELPER";
    public static final String FALL_VITARIS_ACK_REASON = "FALL_VITARIS_ACK_REASON";
    public static final String FALL_VITARIS_ACK_HELPER = "FALL_VITARIS_ACK_HELPER";


    /**
     * Used to interact with the activity.
     */
    public enum FallStateEnum {
        NO_ALERT,
        ALERT_ENGAGED,
        ALERT_CONFIRMED
    }

    /**
     * Default mac address used for no glassesthread
     */
    public static final String DEFAULT_MAC_ADDRESS_NO_GLASSES = "XX:XX:XX:XX:XX:XX";


}
