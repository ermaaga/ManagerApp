package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
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
import it.uniba.di.sms2021.managerapp.utility.ConnectionCheckBroadcastReceiver;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.NotificationUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

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
    private UploadTask uploadTask;

    private ConstraintLayout filesLayout;
    private ConstraintLayout emptyLayout;
    private TextView emptyMessageTextView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) requireActivity();

        //Quando l'utente digita qualcosa nella barra di ricerca, la lista dei file verrà aggiornata
        //con i file che abbiano o nome o tipo corrispondenti alla query.
        activity.setUpSearchAction(true, searchListener);
        //Mostra la lista o un messaggio informativo qualora la connessione mancasse
        activity.setUpConnectionChangeListener(connectionChangeListener);
    }

    /**
     * Azione da eseguire quando viene effettuata una ricerca
     */
    private final SearchUtil.OnSearchListener searchListener = new SearchUtil.OnSearchListener() {
        @Override
        public void onSearchAction(String query) {
            String[] keyWords = query.toLowerCase().split(" ");

            List<ManagerFile> searchFile = new ArrayList<>();
            String releaseFilter = getString(R.string.text_filter_file_release).toLowerCase();
            String imagesFilter = getString(R.string.text_filter_file_image).toLowerCase();
            String pdfFilter = getString(R.string.text_filter_file_pdf);

            for (ManagerFile file: files) {
                boolean toAdd = true;
                for (String string: keyWords) {
                    //Se il file non include una delle parole chiavi, non verrà mostrato.
                    //Verrà sempre mostrato sempre invece se la query è vuota
                    if (toAdd && !query.equals("")) {
                        toAdd = // Va aggiunto se il nome corrisponde alla query
                                file.getName().toLowerCase().contains(string.toLowerCase()) ||
                                        // Va aggiunto se il tipo corrisponde alla query
                                        file.getType().toLowerCase().contains(string.toLowerCase()) ||
                                        // Va aggiunto se il filtro contiene i rilasci ed il file ne è uno
                                        (string.contains(releaseFilter) && project.getReleaseNumber(file.getName()) != 0) ||
                                        // Va aggiunto se il filtro contiene le immagini ed il file ne è una.
                                        (string.contains(imagesFilter) && file.getType().contains("image/")) ||
                                        // Va aggiunto se il filtro contiene i pdf ed il file ne è uno.
                                        (string.contains(pdfFilter) && file.getType().equals("application/pdf"));
                    }
                }

                if (toAdd) {
                    searchFile.add(file);
                }
            }
            adapter.submitList(searchFile);
        }
    };

    /**
     * Azioni da eseguire quando cambia lo stato della connessione
     */
    private final ConnectionCheckBroadcastReceiver.OnConnectionChangeListener connectionChangeListener =
            new ConnectionCheckBroadcastReceiver.OnConnectionChangeListener() {
                @Override
                public void onConnectionUp() {
                    getFiles();
                }

                @Override
                public void onConnectionDown() {
                    showMessageLayout();
                    emptyMessageTextView.setText(R.string.text_message_files_connection_down);
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filesLayout = view.findViewById(R.id.file_layout);
        emptyLayout = view.findViewById(R.id.empty_state_layout);
        emptyMessageTextView = view.findViewById(R.id.files_empty_state_message_text_view);

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

            @Override
            public void onSetRelease(ManagerFile file, boolean addRelease) {
                if (addRelease) {
                    project.addReleaseName(file.getName());
                    Toast.makeText(getContext(), R.string.text_message_release_add, Toast.LENGTH_LONG).show();
                } else {
                    project.removeReleaseName(file.getName());
                    Toast.makeText(getContext(), R.string.text_message_release_removed, Toast.LENGTH_LONG).show();
                }
                getFiles();
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
                        if (project.getReleaseNames().contains(file.getName())) {
                            project.removeReleaseName(file.getName());
                        }

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

        if (project.isProfessor() || !project.isMember() || !project.canAddFiles()) {
            addFileFloatingActionButton.setVisibility(View.GONE);
        }
    }

    /**
     * Ottiene la lista dei files e la visualizza a schermo
     * Se la lista è vuota mostra un messaggio informativo all'utente
     */
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
        if (files.isEmpty()) {
            showMessageLayout();
            emptyMessageTextView.setText(R.string.text_message_files_empty);
        } else {
            showFileLayout();

            adapter.submitList(files);
            adapter.notifyDataSetChanged();
        }
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
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), R.string.text_message_already_uploading,
                        Toast.LENGTH_LONG).show();
                return;
            }
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

        // Crea la task di upload
        uploadTask = storageRef.child(FileUtil.getFileNameFromURI(getContext(), file))
                .putFile(file, metadata);

        // Crea la notifica dell'upload
        NotificationUtil.createNotificationChannel(getContext());
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                NotificationUtil.DEFAULT_CHANNEL_ID);
        builder.setContentTitle(getString(R.string.text_notification_title_file_upload))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        builder.setProgress(PROGRESS_MAX, 0, false);
        notificationManager.notify(NotificationUtil.UPLOAD_NOTIFICATION_ID, builder.build());

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(requireActivity(), new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) Math.round((100.0 *
                        taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()
                        * 100.0) / 100;

                builder.setProgress(PROGRESS_MAX, progress, false)
                .setContentText(getString(R.string.text_message_upload_progress,
                        FileUtil.getFormattedSize(getContext(), taskSnapshot.getBytesTransferred()),
                        FileUtil.getFormattedSize(getContext(), taskSnapshot.getTotalByteCount())));
                notificationManager.notify(NotificationUtil.UPLOAD_NOTIFICATION_ID, builder.build());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                builder.setProgress(0, 0, false)
                        .setContentText(getString(R.string.text_message_upload_failed));
                notificationManager.notify(NotificationUtil.UPLOAD_NOTIFICATION_ID, builder.build());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
                intent.putExtra(Project.KEY, project);
                intent.putExtra(ProjectDetailActivity.INITIAL_TAB_POSITION_KEY,
                        ProjectDetailActivity.FILES_TAB_POSITION);
                int uniqueRequestCode = file.hashCode(); // Se il request code è uguale ad un'activity già
                                                         // aperta la riusa.
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), uniqueRequestCode, intent, 0);

                // Aggiorna la notifica dopo 500ms per non avere problemi con aggiornamenti troppo frequenti
                // come specificato in https://developer.android.com/training/notify-user/build-notification#Updating
                new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // When done, update the notification one more time to remove the progress bar
                        builder.setContentText(getString(R.string.text_message_upload_complete))
                                .setProgress(0,0,false)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        notificationManager.notify(NotificationUtil.UPLOAD_NOTIFICATION_ID, builder.build());
                    }
                }, 500);

                getFiles(); // Aggiorna la lista di files
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
                                if (!FileUtil.openFileWithViewIntent(getContext(), uri, file.getType())) {
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
            if (!FileUtil.openFileWithViewIntent(getContext(), uri, file.getType())) {
                showDownloadSuggestion(R.string.text_message_temp_file_not_supported, file);
            }
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

    private void showFileLayout () {
        filesLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showMessageLayout () {
        filesLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }
}