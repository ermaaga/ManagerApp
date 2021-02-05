package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.uniba.di.sms2021.managerapp.R;

/**
 * Adapter per una lista di elementi non mutabile. (Per esempio array resource)
 */
public class RecyclerViewArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] itemList;
    private final OnItemSelectedListener listener;

    public RecyclerViewArrayAdapter (String[] itemList, OnItemSelectedListener listener) {
        super();
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_simple_string, parent,
                false);

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
        itemView.setOnClickListener(view -> listener.onItemSelected(itemList[position]));

        TextView textView = itemView.findViewById(R.id.simpleListItemTextView);
        textView.setText(itemList[position]);
    }

    @Override
    public int getItemCount() {
        return itemList.length;
    }

    public interface OnItemSelectedListener {
        void onItemSelected (String item);
    }
}
