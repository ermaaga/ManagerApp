package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.FileDownloader;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.firebase.TemporaryFileDownloader;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.lists.FilesRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

import static android.app.Activity.RESULT_OK;

public class ProjectFilesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProjectFilesFragment";

    private StorageReference storageRef;
    private Set<StorageReference> elaboratingReferences;
    private List<ManagerFile> files;

    private FilesRecyclerAdapter adapter;

    private static final String GROUPS_FOLDER = "Groups";

    private boolean previewWarning = true;
    private boolean downloadWarning = true;

    private Project project;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) getActivity();

        //Quando l'utente digita qualcosa nella barra di ricerca, la lista dei file verrà aggiornata
        //con i file che abbiano o nome o tipo corrispondenti alla query.
        activity.setUpSearchAction(true, new ProjectDetailActivity.OnSearchListener() {
            @Override
            public void onSearchAction(String query) {
                String[] keyWords = query.split(" ");

                List<ManagerFile> searchFile = new ArrayList<>();

                for (ManagerFile file: files) {
                    boolean toAdd = true;
                    for (String string: keyWords) {
                        if (toAdd) {
                            //Se il file non include una delle parole chiavi, non verrà mostrato.
                            //Verrà sempre mostrato sempre invece se la query è vuota
                            toAdd = query.equals("") ||
                                    file.getName().toLowerCase().contains(string.toLowerCase()) ||
                                    file.getType().toLowerCase().contains(string.toLowerCase());
                        }
                    }

                    if (toAdd) {
                        searchFile.add(file);
                    }
                }
                adapter.submitList(searchFile);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_files, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addFileFloatingActionButton =
                view.findViewById(R.id.files_add_file_floating_action_button);
        addFileFloatingActionButton.setOnClickListener(this);

        project = ((ProjectDetailActivity)getActivity()).getSelectedProject();
        storageRef = FirebaseStorage.getInstance().getReference().child(GROUPS_FOLDER).child(project.getId());

        RecyclerView filesRecyclerView = view.findViewById(R.id.files_recyclerView);

        //Nel listener dichiaro tutte le azioni eseguibili su un file della lista
        adapter = new FilesRecyclerAdapter(getContext(), project, new FilesRecyclerAdapter.OnActionListener() {
            @Override
            public void onClick(ManagerFile file) {
                //Scarica il file temporaneo e lo visualizza usando un app esterna
                new TemporaryFileDownloader(getContext()) {
                    @Override
                    protected void showDownloadSuggestion(int messageRes) {
                        //Mostra un messaggio qualora il file non sia scaricabile come file temporaneo
                        ProjectFilesFragment.this.showDownloadSuggestion(messageRes, file);
                    }

                    @Override
                    protected void onSuccessAction(File localFile) {
                        Uri uri = FileUtil.getUriFromFile(getContext(), localFile);

                        previewFile(uri, file);
                    }
                }.downloadTempFile(file, project.getId());
            }

            /**
             * Codice preso e modificato da:
             * https://firebase.google.com/docs/storage/android/delete-files
             * @param file file da cancellare
             */
            @Override
            public void onDelete(ManagerFile file) {
                // Delete the file
                file.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        files.remove(file);
                        adapter.submitList(files);
                        adapter.notifyDataSetChanged();
                        Snackbar.make(requireView(), R.string.text_message_file_deleted_successfully,
                                Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Snackbar.make(requireView(), R.string.text_message_file_deletion_failed,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onShare(ManagerFile file) {
                //Scarica il file temporaneo e lo condivido permettendo all'utente di scegliere il
                //mezzo di condivisione.
                new TemporaryFileDownloader(getContext()) {
                    @Override
                    protected void showDownloadSuggestion(int messageRes) {
                        ProjectFilesFragment.this.showDownloadSuggestion(messageRes, file);
                    }

                    @Override
                    protected void onSuccessAction(File localFile) {
                        Uri uri = FileUtil.getUriFromFile(getContext(), localFile);

                        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

                        if(uri != null) {
                            intentShareFile.setType(file.getType());
                            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);

                            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                                    R.string.text_send_subject);
                            intentShareFile.putExtra(Intent.EXTRA_TEXT,
                                    getString(R.string.text_send_content,
                                            file.getName(), project.getName()));

                            startActivity(Intent.createChooser(intentShareFile, "Share File"));
                        }
                    }
                }.downloadTempFile(file, project.getId());


            }

            @Override
            public void onDownload(ManagerFile file) {
                downloadWithWarning(file);
            }
        });
        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (project.isMember() || project.isProfessor() ||
                project.getPermissions().isFileAccessible()) {
            getFiles();
        } else {
            Snackbar.make(requireView(), R.string.text_message_files_are_private, Snackbar.LENGTH_LONG)
                    .setAction(R.string.text_button_dismiss, v -> {}).show();
        }

        if (project.isProfessor() || !project.isMember()) {
            addFileFloatingActionButton.setVisibility(View.GONE);
        }
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
                        for (StorageReference item : listResult.getItems()) {
                            elaboratingReferences.add(item);
                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    files.add(new ManagerFile(item, item.getName(),
                                            storageMetadata.getContentType(),
                                            storageMetadata.getSizeBytes(),
                                            storageMetadata.getUpdatedTimeMillis()));
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

    /**
     * Mostra la lista dei file trovati
     */
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
            selectDocument();
        }
    }

    static final int REQUEST_DOCUMENT_GET = 1;

    private void selectDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_DOCUMENT_GET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DOCUMENT_GET && resultCode == RESULT_OK) {
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
                .setContentType(FileUtil.getMimeTypeFromUri(getContext(), file))
                .build();

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = storageRef.child(FileUtil.getFileNameFromURI(getContext(), file))
                .putFile(file, metadata);

        // Listen for state changes, errors, and completion of the upload.
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle(R.string.text_label_upload_dialog_title);
        dialog.show();

        uploadTask.addOnProgressListener(requireActivity(), new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress =  Math.round((100.0 *
                        taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()
                        * 100.0) / 100.0;

                dialog.setMessage(getString(R.string.text_message_upload_progress, progress));
                Log.d(TAG, getString(R.string.text_message_upload_progress, progress));
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO Handle unsuccessful uploads
                dialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getFiles(); // Aggiorna la lista di files
                dialog.dismiss();
                // TODO Handle successful uploads on complete
                // ...
            }
        });
    }

    /**
     * Mostra un messaggio informativo all'utente ed apre il file usando una della applicazioni
     * installate.
     */
    private void previewFile(Uri uri, ManagerFile file) {
        if (previewWarning) {
            //Avvisa l'utente che l'app userà applicazioni esterne per fare la preview
            new AlertDialog.Builder(getContext()).setMessage(R.string.text_message_file_preview_message)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                previewWarning = false;
                                //Apre il file o se non è possibile aprirlo, mostra un messaggio all'utente.
                                if (!openFileWithViewIntent(uri, file.getType())) {
                                    showDownloadSuggestion(
                                            R.string.text_message_temp_file_not_supported, file);
                                }
                            }
                        }
                    ).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        } else {
            //Apre il file o se non è possibile aprirlo, mostra un messaggio all'utente.
            if (!openFileWithViewIntent(uri, file.getType())) {
                showDownloadSuggestion(R.string.text_message_temp_file_not_supported, file);
            }
        }
    }

    /**
     * Apre un file usando una della applicazioni installate.
     * @param uri l'uri del file in memoria da aprire
     * @param mimeType il tipo mime del file
     * @return true se il file è apribile, false altrimenti
     */
    private boolean openFileWithViewIntent(Uri uri, String mimeType) {
        // I file apk devono prima essere scaricati
        if (mimeType.equals(("application/vnd.android.package-archive"))) {
            return false;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //Crea un intent chooser per permettere all'utente di scegliere l'app per visualizzare il file
        String title = getString(R.string.chooser_title_preview);
        Intent chooser = Intent.createChooser(intent, title);

        //TODO controllare warning dato dal lint
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(chooser);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Avvisa l'utente che il file sarà accessibile anche ad altre applicazioni.
     * Se l'utente accetta l'applicazione procederà al download.
     * @param file il file da scaricare
     */
    private void downloadWithWarning(ManagerFile file) {
        if (downloadWarning) {
            new AlertDialog.Builder(getContext()).setMessage(R.string.text_message_file_download_message)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadWarning = false;
                                    download(file);
                                }
                            }
                    ).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        } else {
            download(file);
        }
    }

    /**
     * Scarica il file e notifica l'utente a download finito.
     * Se l'utente vuole può anche aprire direttamente il file scaricato.
     * In caso di errori, essi verranno mostrati e sarà data la possibilità all'utente di riprovare.
     */
    private void download (ManagerFile file) {
        new FileDownloader(getContext()) {
            @Override
            protected void onSuccessAction(File localFile) {
                Snackbar.make(getView(), R.string.text_message_file_downloaded_successfully,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.text_button_show_file, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                previewFile(FileUtil.getUriFromFile(getContext(), localFile), file);
                            }
                        }).show();
            }

            @Override
            protected void showErrorMessage(int message) {
                Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.text_button_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downloadWithWarning(file);
                            }
                        }).show();
            }
        }.downloadFile(file);
    }

    /**
     * Mostra un messaggio qualora il file non sia scaricabile come file temporaneo
     * @param textMessageId il messaggio da visualizzare
     */
    private void showDownloadSuggestion (int textMessageId, ManagerFile file) {
        Snackbar.make(getView(), textMessageId, Snackbar.LENGTH_LONG)
                .setAction(R.string.text_button_download, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadWithWarning(file);
                    }
                }).show();
    }
}