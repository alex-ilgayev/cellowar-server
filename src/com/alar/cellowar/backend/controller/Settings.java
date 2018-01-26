package com.alar.cellowar.backend.controller;

/**
 * Created by Alex on 4/29/2015.
 *
 * Singleton class which holds various application variables.
 */
public class Settings {
    private static Settings _ins = null;

    // the post parameter name for the main servlet post request.
    public final static String SERVLET_PACKET_PAYLOAD_PARAMETER_NAME = "Payload";
    public final static String SERVLET_PACKET_DATE_PARAMETER_NAME = "Date";

    public final static String CHAT_SYSTEM_PREFIX = "System: ";

    // means we will be running queue cleaning once '_TIME_TO_CLEAN_QUEUE_MILLIS' time.
    public final static int _TIME_TO_CLEAN_QUEUE_MILLIS = 10000;

    // means when we will be cleaning queues, any unupdated client by the following time,
    // will be deleted.
    public final static int _TIME_TO_DELETE_CLIENT_MILLIS = 8000;

    private Settings(){

    }

    public static Settings getInstance() {
        if (_ins == null)
            _ins = new Settings();
        return _ins;
    }
}
