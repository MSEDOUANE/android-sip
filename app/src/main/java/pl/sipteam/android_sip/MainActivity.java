package pl.sipteam.android_sip;

import android.content.Context;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.TransportNotSupportedException;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import pl.sipteam.android_sip.adapter.MessageListAdapter;
import pl.sipteam.android_sip.event.SipErrorEvent;
import pl.sipteam.android_sip.event.SipMessageEvent;
import pl.sipteam.android_sip.event.SipStatusEvent;
import pl.sipteam.android_sip.model.SipMessageItem;
import pl.sipteam.android_sip.model.SipMessageType;
import pl.sipteam.android_sip.runnable.SendMessageRunnable;
import pl.sipteam.android_sip.sip.SipManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.action_button)
    FloatingActionButton actionButton;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.input_layout_address)
    TextInputLayout inputLayoutAddress;

    @Bind(R.id.input_layout_message)
    TextInputLayout inputLayoutMessage;

    @Bind(R.id.input_message)
    EditText inputMessage;

    @Bind(R.id.input_address)
    EditText inputAddress;

    @Bind(R.id.messages_list)
    RecyclerView messagesList;

    @Bind(R.id.tts_check_box)
    CheckBox ttsCheckBox;

    private TextToSpeech tts;

    private MessageListAdapter adapter;

    private EventBus bus;

    private SipManager sipManager;

    private ExecutorService executor;

    private List<SipMessageItem> messages;

    private MediaPlayer mediaPlayer;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        bus = EventBus.getDefault();
        mediaPlayer = MediaPlayer.create(this, R.raw.message_sound);
        bus.register(this);
        executor = Executors.newFixedThreadPool(1);
        context = this;

        try {
            sipManager = SipManager.getInstance("ja", 5060);
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        } catch (TransportNotSupportedException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ObjectInUseException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        messages = new ArrayList<>();
        initializeMessagesList();
        initializeTextToSpeach();

        ttsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.message_sound);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        });
    }

    private void initializeMessagesList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(false);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setItemAnimator(new DefaultItemAnimator());
        adapter = new MessageListAdapter(messages);
        messagesList.setAdapter(adapter);
    }

    private void initializeTextToSpeach() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
    }

    @OnClick(R.id.action_button)
    public void onActionButtonClick() {
        clearErrors();
        if (isOnline()) {
            if (validateFields()) {
                sendMessage();
                inputMessage.setText("");
            }
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        Runnable runnable = new SendMessageRunnable(
                sipManager,
                inputAddress.getText().toString(),
                inputMessage.getText().toString()
        );
        executor.execute(runnable);
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(inputMessage.getText().toString())){
            inputLayoutMessage.setErrorEnabled(true);
            inputMessage.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputAddress.getText().toString())) {
            inputLayoutAddress.setErrorEnabled(true);
            inputAddress.requestFocus();
            return false;
        }

        return true;
    }

    private void clearErrors() {
        hideKeyboard();
        inputLayoutAddress.setErrorEnabled(false);
        inputLayoutMessage.setErrorEnabled(false);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void speak(SipMessageItem messageItem) {
        StringBuilder sb = new StringBuilder();
        sb.append("New message from ");
        sb.append(messageItem.getUser());
        sb.append("...");
        sb.append(messageItem.getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(sb.toString(), TextToSpeech.QUEUE_ADD, null, Long.toString(System.currentTimeMillis()));
        } else {
            tts.speak(sb.toString(), TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(SipMessageEvent event) {
        SipMessageItem messageItem = event.getMessageItem();
        messages.add(messageItem);
        adapter.notifyItemInserted(messages.size() - 1);
        messagesList.scrollToPosition(messages.size() - 1);

        if (messageItem.getMessageType() == SipMessageType.INCOMING_MESSAGE) {
            Toast.makeText(this, getString(R.string.new_message), Toast.LENGTH_SHORT).show();
            if (ttsCheckBox.isChecked()) {
                speak(messageItem);
            } else {
                mediaPlayer.start();
            }
        } else if (messageItem.getMessageType() == SipMessageType.OUTCOMING_MESSAGE) {
            Toast.makeText(this, getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(SipStatusEvent event) {
        Log.d(TAG, event.getStatusMessage());
        Toast.makeText(this, event.getStatusMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(SipErrorEvent event) {
        Log.d(TAG, event.getErrorMessage());
        Toast.makeText(this, event.getErrorMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        mediaPlayer.stop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
