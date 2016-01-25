package pl.sipteam.android_sip.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import pl.sipteam.android_sip.model.SipMessageItem;

public class SettingsService {
    private static final String USER_KEY = "user_key";
    private static final String MESSAGES_KEY = "messages_key";

    private Context context;
    private SharedPreferences preferences;

    public SettingsService(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("android_sip", Context.MODE_PRIVATE);
    }

    public void setUserName(String userName) {
        preferences.edit().putString(USER_KEY, userName);
    }

    public String getUserName() {
        return preferences.getString(USER_KEY, null);
    }

    public void setMessages(List<SipMessageItem> messages) {
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        Log.d(SettingsService.class.getSimpleName(), json);
        preferences.edit().putString(MESSAGES_KEY, json).commit();
    }

    public List<SipMessageItem> getMessages() {
        Gson gson = new Gson();
        String json = preferences.getString(MESSAGES_KEY, null);
        if (!TextUtils.isEmpty(json)) {
            Type type = new TypeToken<List<SipMessageItem>>(){}.getType();
            List<SipMessageItem> messages = gson.fromJson(json, type);
            return messages;
        }
        return new ArrayList<>();
    }

}
