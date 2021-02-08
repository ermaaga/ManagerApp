package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.User;

public class DepartmentRecyclerAdapter extends ListAdapter<Department, RecyclerView.ViewHolder> {
    private static final String TAG = "DepRecyclerAdapter";

    private Context context;
    private OnActionListener listener;
    private int userRole;
    private final Set<Integer> itemsSelected;

    public DepartmentRecyclerAdapter(Context context, int userRole, OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
        this.context = context;
        this.userRole = userRole;
        itemsSelected = new HashSet<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_simple_string, parent, false);

        return new RecyclerView.ViewHolder(itemView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;

        Department department = getItem(position);

        TextView departmentTextView = itemView.findViewById(R.id.simpleListItemTextView);
        MaterialCardView card = itemView.findViewById(R.id.user_item_card);

        departmentTextView.setText(department.getName());
        //controllare se eliminare questo codice
        //se l'elemento è stato già selezionato lo deseleziona
        if (itemsSelected.contains(position)) {
            card.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
        } else {//se l'elemento non è stato selezionato lo seleziona
            card.setCardBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userRole == User.ROLE_PROFESSOR){
                    if (itemsSelected.contains(position)) {
                        card.setCardBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
                        itemsSelected.remove(position);
                        if (selectedDepartments().size() == 0) {
                            listener.onSelectionActionProfessor(false);
                        }
                    } else {
                        card.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
                        itemsSelected.add(position);
                        listener.onSelectionActionProfessor(true);
                    }
                }else{ //altrimenti l'utente è uno studente e viene passato l'id del singolo dipartimento
                    card.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
                    itemsSelected.add(position);
                    listener.onSelectionActionStudent(department.getId());
                    }
                Log.d(TAG, Arrays.toString(selectedDepartments().toArray()));
            }
        });

    }

    static class DiffCallback extends DiffUtil.ItemCallback<Department> {

        @Override
        public boolean areItemsTheSame(@NonNull Department oldItem, @NonNull Department newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Department oldItem, @NonNull Department newItem) {
            return oldItem.equals(newItem);
        }
    }

    public List<String> selectedDepartments() {
        List<String> selectedDepartments = new ArrayList<>();

        for (int position: itemsSelected) {
            selectedDepartments.add(getItem(position).getId());
        }

        return selectedDepartments;
    }


    public interface OnActionListener {
        void onSelectionActionProfessor(Boolean isSelected);
        void onSelectionActionStudent(String idDepartment);
    }

}