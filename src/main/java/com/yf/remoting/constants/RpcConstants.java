package com.yf.remoting.constants;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 14:58
 * @version: 1.0.0
 * @url:
 */
public class RpcConstants {
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final byte VERSION = 1;

    public static final byte[] MAGIC_NUMBER = {(byte) 'f', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final int HEAD_LENGTH = 16;

    public static final String PING = "ping";
    public static final String PONG = "pong";

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    public static final int MAX_RECONNECTION_TIMES = 5;

    public static final int BACKLOG = 1024;

    public static final int MAX_SEMAPHORE_NUMS = 50;

    public static final int SERVICE_NODE = 0;
    public static final int PERMIT_NODE = 1;

    public static final int RATELIMIT = 500;

}
