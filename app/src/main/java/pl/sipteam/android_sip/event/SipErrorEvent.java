package pl.sipteam.android_sip.event;

public class SipErrorEvent {
    private String errorMessage;

    public SipErrorEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
