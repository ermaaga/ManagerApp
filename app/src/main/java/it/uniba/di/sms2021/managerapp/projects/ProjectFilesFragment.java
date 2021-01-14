package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.lists.FilesRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.lists.StudyCasesRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

import static android.app.Activity.RESULT_OK;

public class ProjectFilesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProjectFilesFragment";

    //TODO rendere riferimento dinamico
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Gruppo1");
    private Set<StorageReference> elaboratingReferences;
    private List<ManagerFile> files;

    private FilesRecyclerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_files, container, false);
        FloatingActionButton addFileFloatingActionButton =
                view.findViewById(R.id.files_add_file_floating_action_button);
        addFileFloatingActionButton.setOnClickListener(this);

        RecyclerView filesRecyclerView = view.findViewById(R.id.files_recyclerView);
        adapter = new FilesRecyclerAdapter(getContext(), new FilesRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(ManagerFile file) {
                Toast.makeText(getContext(), R.string.text_message_not_yet_implemented,
                        Toast.LENGTH_SHORT).show();
            }
        });
        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        getFiles();
        return view;
    }

    private void getFiles() {
        files = new ArrayList<>();
        elaboratingReferences = new HashSet<>();    // Usato per avvisare il programma che i file
                                                    // sono ancora in elaborazione
        elaboratingReferences.add(storageRef);

        listAllPaginated(null);
    }

    /**
     * Effettua una query per i file e non appena finisce aggiorna la lista dei files.
     * Codice modificato a partire da: https://firebase.google.com/docs/storage/android/list-files#paginate_list_results
     */
    private void listAllPaginated (@Nullable String pageToken) {
        // Fetch the next page of results, using the pageToken if we have one.
        Task<ListResult> listPageTask = pageToken != null
                ? storageRef.list(100, pageToken)
                : storageRef.list(100);

        listPageTask
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> prefixes = listResult.getPrefixes();
                        List<StorageReference> items = listResult.getItems();

                        for (StorageReference item : listResult.getItems()) {
                            elaboratingReferences.add(item);
                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    files.add(new ManagerFile(item, item.getName()
                                            .replaceAll("[.].{2,3}$", ""),
                                            storageMetadata.getContentType(),
                                            storageMetadata.getSizeBytes()));
                                    dropElaboratingReference(item);
                                }
                            });
                        }
                        dropElaboratingReference(storageRef);

                        // Recurse onto next page
                        if (listResult.getPageToken() != null) {
                            listAllPaginated(listResult.getPageToken());
                        } else {
                            dropElaboratingReference(storageRef);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Uh-oh, an error occurred.
            }
        });
    }

    /**
     * Se tutti i file sono stati elaborati, finisce di inizializzare la ui e li visualizza
     */
    private void dropElaboratingReference (StorageReference ref) {
        elaboratingReferences.remove(ref);
        if (elaboratingReferences.isEmpty()) {
            showFiles();
        }
    }

    private void showFiles () {
        Log.i(TAG, files.toString());
        adapter.submitList(files);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.files_add_file_floating_action_button) {
            selectImage();
        }
    }

    static final int REQUEST_IMAGE_GET = 1;

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data"); /*TODO farci qualcosa come mostrare una finestra di dialogo
                                                                         che mostri il progresso dell'upload e che usi il thumbnail*/
            Uri fullPhotoUri = data.getData();

            upload(fullPhotoUri);
        }
    }

    /**
     * Carica un file a partire da un percorso sulla memoria esterna.
     * Codice modificato a partire da: https://firebase.google.com/docs/storage/android/upload-files#full_example
     */
    private void upload (Uri file) {
        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = storageRef.child(FileUtil.getFileNameFromURI(getContext(), file))
                .putFile(file, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getFiles(); // Aggiorna la lista di files
                // TODO Handle successful uploads on complete
                // ...
            }
        });
    }
}