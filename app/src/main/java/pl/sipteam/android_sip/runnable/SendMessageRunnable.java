package pl.sipteam.android_sip.runnable;

import android.javax.sip.InvalidArgumentException;
import android.javax.sip.SipException;

import java.text.ParseException;

import pl.sipteam.android_sip.sip.SipManager;

public class SendMessageRunnable implements Runnable {
    private String to;
    private String message;
    private SipManager sipManager;

    public SendMessageRunnable(SipManager sipManager, String to, String message) {
        this.sipManager = sipManager;
        this.to = to;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            sipManager.sendMessage(to, message);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }
}
