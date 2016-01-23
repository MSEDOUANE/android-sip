package pl.sipteam.android_sip;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class SipApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
