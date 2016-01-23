package pl.sipteam.android_sip.event;

public class SipMessageEvent {
    private String message;
    private String from;

    public SipMessageEvent(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }
}
