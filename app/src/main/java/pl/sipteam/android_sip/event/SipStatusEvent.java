package pl.sipteam.android_sip.event;

public class SipStatusEvent {
    private String statusMessage;

    public SipStatusEvent(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
