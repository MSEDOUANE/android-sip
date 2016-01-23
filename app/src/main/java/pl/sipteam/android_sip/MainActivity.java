package pl.sipteam.android_sip;

import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.TransportNotSupportedException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import pl.sipteam.android_sip.event.SipErrorEvent;
import pl.sipteam.android_sip.event.SipMessageEvent;
import pl.sipteam.android_sip.event.SipStatusEvent;
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

    @Bind(R.id.input_address)
    EditText inputAddress;

    @Bind(R.id.history_list)
    RecyclerView historyList;

    private EventBus bus;

    private SipManager sipManager;

    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        bus = EventBus.getDefault();
        bus.register(this);
        executor = Executors.newFixedThreadPool(1);

        try {
            sipManager = SipManager.getInstance("norbert", 5060);
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
    }

    @OnClick(R.id.action_button)
    public void onActionButtonClick() {
        //TODO call to selected sip address/hang up conection
        Runnable runnable = new SendMessageRunnable(
                sipManager,
                inputAddress.getText().toString(),
                "test message"
        );
        executor.execute(runnable);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(SipMessageEvent event) {
        Log.d(TAG, event.getFrom() + " | " + event.getMessage());
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
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
        super.onDestroy();
    }
}
