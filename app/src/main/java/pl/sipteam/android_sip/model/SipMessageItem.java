package pl.sipteam.android_sip.model;

import org.joda.time.DateTime;

import java.io.Serializable;

public class SipMessageItem implements Serializable {
    private String user;
    private String address;
    private String message;
    private DateTime date;
    private SipMessageType messageType;

    public SipMessageItem(String user, String address, String message, DateTime date, SipMessageType messageType) {
        this.user = user;
        this.address = address;
        this.message = message;
        this.date = date;
        this.messageType = messageType;
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }

    public String getMessage() {
        return message;
    }

    public DateTime getDate() {
        return date;
    }

    public SipMessageType getMessageType() {
        return messageType;
    }
}
