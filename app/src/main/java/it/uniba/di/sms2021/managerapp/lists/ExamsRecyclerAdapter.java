package it.uniba.di.sms2021.managerapp.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.User;

public class ExamsRecyclerAdapter extends ListAdapter<Exam, RecyclerView.ViewHolder> {

    private OnActionListener listener;

    public ExamsRecyclerAdapter(OnActionListener listener) {
        super(new ExamDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_exam, parent, false);

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
        itemView.setOnClickListener(view -> listener.onItemClicked(getItem(position)));

        TextView titleTextView = itemView.findViewById(R.id.exam_title_text_view);
        TextView professorTextView = itemView.findViewById(R.id.exam_professor_text_view);

        Exam exam = getItem(position);
        if (exam != null) {
            titleTextView.setText(exam.getName());
            setFirstProfessorName(professorTextView, exam);
        }
    }

    static class ExamDiffCallback extends DiffUtil.ItemCallback<Exam> {

        @Override
        public boolean areItemsTheSame(@NonNull Exam oldItem, @NonNull Exam newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Exam oldItem, @NonNull Exam newItem) {
            return oldItem.equals(newItem);
        }
    }

    //Nota: questo metodo è placeholder, capire se c'è un metodo migliore.
    public void setFirstProfessorName(TextView textView, Exam exam) {
        List<String> professorNames = new ArrayList<>();
        DatabaseReference ref = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    if (exam.getProfessors().contains(child.getKey())) {
                        User professor = child.getValue(User.class);
                        textView.setText(professor.getFullName());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface OnActionListener {
        void onItemClicked (Exam exam);
    }
}
