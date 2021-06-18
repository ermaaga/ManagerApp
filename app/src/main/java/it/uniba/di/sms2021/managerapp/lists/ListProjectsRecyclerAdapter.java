package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;

public class ListProjectsRecyclerAdapter extends ListAdapter<ListProjects, RecyclerView.ViewHolder> {
    private OnActionListener listener;

    public ListProjectsRecyclerAdapter(OnActionListener listener) {
        super(new ListProjectsDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_simple_string,
                parent, false);

        //Nota: volendo si può creare una classe ViewHolder a parte.
        //TODO vedere se eliminare il commento
        return new RecyclerView.ViewHolder(view) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListProjects list = getItem(position);
        View itemView = holder.itemView;
        itemView.setOnClickListener(view -> listener.onItemClicked(list));

        TextView contentTextView = itemView.findViewById(R.id.simpleListItemTextView);
        contentTextView.setText(getItem(position).getNameList());
    }

    static class ListProjectsDiffCallback extends DiffUtil.ItemCallback<ListProjects> {

        @Override
        public boolean areItemsTheSame(@NonNull ListProjects oldItem, @NonNull ListProjects newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListProjects oldItem, @NonNull ListProjects newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnActionListener {
        void onItemClicked (ListProjects list);
    }
}
