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

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private List<SipMessageItem> data;

    public MessageListAdapter(List<SipMessageItem> data) {
        this.data = data;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.populateView(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.user)
        TextView user;

        @Bind(R.id.date)
        RelativeTimeTextView date;

        @Bind(R.id.content)
        TextView content;

        public MessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void populateView(SipMessageItem messageItem) {
            user.setText(messageItem.getFrom());
            date.setReferenceTime(messageItem.getDate().getMillis());
            content.setText(messageItem.getMessage());
        }
    }
}
