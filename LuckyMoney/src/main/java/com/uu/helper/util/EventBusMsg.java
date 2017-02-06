package com.uu.helper.util;

public class EventBusMsg {

    public final static int ACCESSIBILITY_CONNECTED = 0x01;//
    public final static int ACCESSIBILITY_DISCONNECTED = 0x02;//
    public final static int NOTIFICATION_CONNECTED = 0x03;//
    public final static int NOTIFICATION_DISCONNECTED = 0x04;//

    private int type;

    public EventBusMsg() {

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
