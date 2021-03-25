package it.uniba.di.sms2021.managerapp.lists;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import it.uniba.di.sms2021.managerapp.R;

public class DeviceRecyclerAdapter extends ListAdapter<BluetoothDevice, RecyclerView.ViewHolder> {
    private OnActionListener listener;

    public DeviceRecyclerAdapter(OnActionListener listener) {
        super(new StringDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device,
                parent, false);

        //Nota: volendo si può creare una classe ViewHolder a parte.
        return new RecyclerView.ViewHolder(view) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;
        itemView.setOnClickListener(view -> listener.onItemClicked(getItem(position).getAddress()));

        TextView contentTextView = itemView.findViewById(R.id.nameDeviceTextView);
        contentTextView.setText(getItem(position).getName()+" "+getItem(position).getAddress());
    }

    static class StringDiffCallback extends DiffUtil.ItemCallback<BluetoothDevice> {

        @Override
        public boolean areItemsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnActionListener {
        void onItemClicked (String string);
    }
}
