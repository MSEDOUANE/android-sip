package pl.sipteam.android_sip.event;

import pl.sipteam.android_sip.model.SipMessageItem;
import pl.sipteam.android_sip.model.SipMessageType;

public class SipMessageEvent {

    private SipMessageItem messageItem;
    private SipMessageType messageType;

    public SipMessageEvent(SipMessageItem messageItem, SipMessageType messageType) {
        this.messageItem = messageItem;
        this.messageType = messageType;
    }

    public SipMessageItem getMessageItem() {
        return messageItem;
    }
	
    public SipMessageType getMessageType() {
        return messageType;
    }
}
