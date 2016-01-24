package pl.sipteam.android_sip.event;

import pl.sipteam.android_sip.model.SipMessageItem;

public class SipMessageEvent {

    private SipMessageItem messageItem;

    public SipMessageEvent(SipMessageItem messageItem) {
        this.messageItem = messageItem;
    }

    public SipMessageItem getMessageItem() {
        return messageItem;
    }

}
