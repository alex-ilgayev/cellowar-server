package com.alar.cellowar.backend.controller;

public class ErrorStrings {
    public final static String WRONG_PACKET_FORMAT_ERROR = "couldn't parse the body to valid packet object";
    public final static String NO_PACKET_PAYLOAD_INCLUDED_ERROR = "no 'Payload' parameter included";
    public final static String NO_PACKET_DATE_INCLUDED_ERROR = "no 'Date' parameter included";
    public final static String DATE_INCLUDED_NOT_VALID_ERROR = "insert Date as seconds since 1/1/1970";
    public final static String PAYLOAD_NOT_VALID_BASE_64 = "payload not valid base64 format";
    public final static String PAYLOAD_NOT_VALID_MESSAGE = "decoded payload not valid message format";
    public final static String SERVER_ERROR = "internal server error";

    // SERVER ERROR FOR LOG
    public final static String SERVER_ERROR_UNKNOWN_MESSAGE_TYPE = "unknown message type - error";
    public final static String SERVER_ERROR_CLIENT_SESSION_NOT_SYNCED_TO_DB = "client session not synced to db - error";
    public final static String SERVER_ERROR_PACKET_FROM_UNRECOGNIZED_USER = "received packet from unrecognized user - error";
}
