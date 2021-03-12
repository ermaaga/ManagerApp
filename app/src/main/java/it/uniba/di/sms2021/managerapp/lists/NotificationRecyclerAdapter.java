package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.notifications.Notifiable;
import it.uniba.di.sms2021.managerapp.notifications.NotifiableComparator;

public class NotificationRecyclerAdapter extends ListAdapter<Notifiable, RecyclerView.ViewHolder> {
    Context context;
    OnUpdateDataListener listener;

    public NotificationRecyclerAdapter(Context context, OnUpdateDataListener listener) {
        super(new DiffCallback());
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_notification, parent, false);
        return new RecyclerView.ViewHolder(itemView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    /**
     * Prima di mostrare la lista la ordina per data di invio
     */
    @Override
    public void submitList(@Nullable List<Notifiable> list) {
        Collections.sort(list, new NotifiableComparator());

        super.submitList(list);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;

        Notifiable notification = getItem(position);

        MaterialCardView card = itemView.findViewById(R.id.notification_item_card);
        card.setOnClickListener(view -> notification.onNotificationClick(context, null));

        TextView titleTextView = itemView.findViewById(R.id.title_TextView);
        TextView messageTextView = itemView.findViewById(R.id.message_TextView);
        TextView dateTextView = itemView.findViewById(R.id.date_text_view);

        titleTextView.setText(notification.getNotificationTitle(context));
        messageTextView.setText(notification.getNotificationMessage(context));

        String dateString;
        Calendar sentTime = Calendar.getInstance();
        sentTime.setTime(notification.getNotificationSentTime());
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime (new Date(System.currentTimeMillis()));

        if (sentTime.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH) &&
            sentTime.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) &&
            sentTime.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            dateString = format.format(sentTime.getTime());
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateString = format.format(sentTime.getTime());
        }

        dateTextView.setText(dateString);

        Button action1Button = itemView.findViewById(R.id.action1_button);
        String label = notification.getAction1Label(context);
        if (label != null) {
            action1Button.setText(label);
            action1Button.setOnClickListener(v ->
                    notification.onNotificationAction1Click(context, () -> {
                        listener.onUpdateData();
                    }));
        } else {
            action1Button.setVisibility(View.GONE);
        }

        Button action2Button = itemView.findViewById(R.id.action2_button);
        label = notification.getAction2Label(context);
        if (label != null) {
            action2Button.setText(label);
            action2Button.setOnClickListener(v ->
                    notification.onNotificationAction2Click(context, () -> {
                        listener.onUpdateData();
                    }));
        } else {
            action2Button.setVisibility(View.GONE);
        }

    }

    static class DiffCallback extends DiffUtil.ItemCallback<Notifiable> {

        @Override
        public boolean areItemsTheSame(@NonNull Notifiable oldItem, @NonNull Notifiable newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notifiable oldItem, @NonNull Notifiable newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnUpdateDataListener {
        void onUpdateData ();
    }
}
