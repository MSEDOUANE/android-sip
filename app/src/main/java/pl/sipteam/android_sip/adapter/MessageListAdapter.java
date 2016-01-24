package pl.sipteam.android_sip.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.sipteam.android_sip.R;
import pl.sipteam.android_sip.model.SipMessageItem;
import pl.sipteam.android_sip.model.SipMessageType;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INCOMING = 1;
    private static final int TYPE_OUTCOMING = 2;

    private List<SipMessageItem> data;

    public MessageListAdapter(List<SipMessageItem> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case TYPE_INCOMING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_in_message_item, parent, false);
                return new IncomingMessageViewHolder(view);
            case TYPE_OUTCOMING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_out_message_item, parent, false);
                return new OutcomingMessageViewHolder(view);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_INCOMING:
                final IncomingMessageViewHolder inHolder = (IncomingMessageViewHolder) holder;
                inHolder.populateView(data.get(position));
                break;
            case TYPE_OUTCOMING:
                final OutcomingMessageViewHolder outHolder = (OutcomingMessageViewHolder) holder;
                outHolder.populateView(data.get(position));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        SipMessageItem messageItem = data.get(position);

        if (messageItem.getMessageType() == SipMessageType.INCOMING_MESSAGE) {
            return TYPE_INCOMING;
        } else {
            return TYPE_OUTCOMING;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class IncomingMessageViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.user)
        TextView user;

        @Bind(R.id.date)
        RelativeTimeTextView date;

        @Bind(R.id.content)
        TextView content;

        public IncomingMessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void populateView(SipMessageItem messageItem) {
            user.setText(messageItem.getUser() + ",");
            date.setReferenceTime(messageItem.getDate().getMillis());
            content.setText(messageItem.getMessage());
        }
    }

    public class OutcomingMessageViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.user)
        TextView user;

        @Bind(R.id.date)
        RelativeTimeTextView date;

        @Bind(R.id.content)
        TextView content;

        public OutcomingMessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void populateView(SipMessageItem messageItem) {
            user.setText(messageItem.getUser() + ",");
            date.setReferenceTime(messageItem.getDate().getMillis());
            content.setText(messageItem.getMessage());
        }
    }
}
