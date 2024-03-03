package com.maforn.timedshutdown;

/**
 * An enum containing the power off gestures possible types
 */
public enum PowerOffType {
    ONECLICK, LONGPRESS, TWOCLICKS, SWIPE;

    public static final PowerOffType[] values = values();
}
