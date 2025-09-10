package com.maforn.timedshutdown;

/**
 * An enum containing the power off gestures possible types
 */
public enum PowerOffType {
    ONECLICK, LONGPRESS, TWOCLICKS, THREECLICKS, FOURCLICKS, FIVECLICKS, SIXCLICKS, SWIPE;

    public static final PowerOffType[] values = values();
}
