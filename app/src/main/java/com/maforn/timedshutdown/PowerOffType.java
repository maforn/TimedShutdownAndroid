package com.maforn.timedshutdown;

/**
 * An enum containing the power off gestures possible types
 * KEEP THIS ORDER FOR BACKWARD COMPATIBILITY WITH PREVIOUS VERSIONS
 */
public enum PowerOffType {
    ONECLICK, LONGPRESS, TWOCLICKS, THREECLICKS, FOURCLICKS, SWIPE, FIVECLICKS, SIXCLICKS;

    public static final PowerOffType[] values = values();
}
