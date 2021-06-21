package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class UserRecyclerAdapter extends ListAdapter<User, RecyclerView.ViewHolder> {
    private static final String TAG = "UserRecyclerAdapter";
    private OnActionListener listener;
    private ValueEventListener userListenerCreate;
    private StorageReference storageReference;
    private DatabaseReference currentUserReference;
    private DatabaseReference usersReference;
    private FirebaseDatabase database;
    private Context context;

    public UserRecyclerAdapter(Context context, OnActionListener listener) {
        super(new UserDiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user,
                parent, false);

        //TODO vedere Nota: volendo si può creare una classe ViewHolder a parte.
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
        User user = getItem(position);

        MaterialCardView cardView = itemView.findViewById(R.id.user_item_card);
        cardView.setOnClickListener(view -> listener.onItemClicked(user));

        TextView fullNameTextView = itemView.findViewById(R.id.fullname_TextView);
        TextView emailTextView = itemView.findViewById(R.id.email_TextView);
        ImageView photoProfile = itemView.findViewById(R.id.image_account);
        ImageView imgStar= itemView.findViewById(R.id.img_star);
        if (position == 0){
            imgStar.setVisibility(View.VISIBLE);
        }

        fullNameTextView.setText(user.getFullName());
        emailTextView.setText(user.getEmail());

        storageReference = FirebaseStorage.getInstance().getReference();
        String userid= user.getAccountId();

        database = FirebaseDbHelper.getDBInstance();
        usersReference = database.getReference(FirebaseDbHelper.TABLE_USERS);
        currentUserReference = usersReference.child(userid);

        userListenerCreate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                //Se il nodo "profileImage" è popolato nel database
                if(user.getProfileImage()!= null){
                    //Acquisizione dell'Url dell'immagine dell'utente presente nello Storage
                    storageReference.child("profileimages/"+userid).child(user.getProfileImage()).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set ImageView foto profilo
                                    Glide.with(context)
                                            .load(uri)
                                            .into(photoProfile);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG," Failed setImageView");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        currentUserReference.addListenerForSingleValueEvent(userListenerCreate);
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<User> {

        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnActionListener {
        void onItemClicked (User string);
    }
}
