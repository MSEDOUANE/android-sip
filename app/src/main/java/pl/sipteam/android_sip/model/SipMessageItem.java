package pl.sipteam.android_sip.model;

import org.joda.time.DateTime;

import java.io.Serializable;

public class SipMessageItem implements Serializable {
    private String from;
    private String message;
    private DateTime date;

    public SipMessageItem(String from, String message, DateTime date) {
        this.from = from;
        this.message = message;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public DateTime getDate() {
        return date;
    }
}
