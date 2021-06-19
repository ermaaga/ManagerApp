package it.uniba.di.sms2021.managerapp.projects;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerCloudFile;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerFile;
import it.uniba.di.sms2021.managerapp.enitities.file.ManagerLocalFile;
import it.uniba.di.sms2021.managerapp.firebase.FileDownloader;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.firebase.TemporaryFileDownloader;
import it.uniba.di.sms2021.managerapp.lists.FilesRecyclerAdapter;
import it.uniba.di.sms2021.managerapp.utility.ConnectionCheckBroadcastReceiver;
import it.uniba.di.sms2021.managerapp.utility.FileException;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;
import it.uniba.di.sms2021.managerapp.utility.NotificationUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

import static android.app.Activity.RESULT_OK;

public class ProjectFilesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProjectFilesFragment";
    private static final int REQUEST_PERMISSION_DOWNLOAD = 1;
    private static final int REQUEST_PERMISSION_PREVIEW = 2;
    private static final int REQUEST_PERMISSION_LOCAL_FILES = 3;

    private static final String GROUPS_FOLDER = FirebaseDbHelper.GROUPS_FOLDER;

    private StorageReference projectStorageRef;
    private Set<StorageReference> elaboratingReferences;
    private List<ManagerFile> files;
    private FilesRecyclerAdapter adapter;

    private static boolean previewWarning = true;
    private static boolean downloadWarning = true;

    private Project project;
    private UploadTask uploadTask;

    private FloatingActionButton addFileFloatingActionButton;
    private ConstraintLayout filesLayout;
    private ConstraintLayout warningOfflineMessageLayout;

    private ConstraintLayout emptyLayout;
    private TextView emptyLayoutMessageTextView;
    private Button emptyLayoutButton;

    private String lastQuery = "";
    private final Set<String> searchFilters = new HashSet<>();

    private Uri uriToPreview;
    private ManagerCloudFile fileToPreview;

    private ProjectDetailActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ProjectDetailActivity) requireActivity();

        activity.setUpSearchAction(true, new ProjectFilesOnSearchListener());

        //Mostra la lista o un messaggio informativo qualora la connessione mancasse
        activity.setUpConnectionChangeListener(new ProjectFilesConnectionChangeListener());
    }

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
        warningOfflineMessageLayout = view.findViewById(R.id.files_offline_warning_constraintView);
        emptyLayout = view.findViewById(R.id.empty_state_layout);
        emptyLayoutMessageTextView = view.findViewById(R.id.files_empty_state_message_text_view);
        emptyLayoutButton = view.findViewById(R.id.files_empty_state_button);

        addFileFloatingActionButton = view.findViewById(R.id.files_add_file_floating_action_button);
        addFileFloatingActionButton.setOnClickListener(this);

        project = ((ProjectDetailActivity)getActivity()).getSelectedProject();
        projectStorageRef = FirebaseStorage.getInstance().getReference().child(GROUPS_FOLDER).child(project.getId());

        RecyclerView filesRecyclerView = view.findViewById(R.id.files_recyclerView);

        adapter = new FilesRecyclerAdapter(getContext(), project, new ProjectFilesFileActionsListener());
        filesRecyclerView.setAdapter(adapter);
        filesRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        getFiles();
    }

    public boolean userCantAddFiles() {
        return project.isProfessor() || !project.isMember() || !project.canAddFiles();
    }

    public boolean userCanViewFiles() {
        return project.isMember() || project.isProfessor() ||
                project.getPermissions().isFileAccessible();
    }

    /**
     * Ottiene la lista dei files e la visualizza a schermo se l'utente è abilitato a visualizzarli<br><br>
     *
     * Se la lista è vuota mostra un messaggio informativo all'utente
     */
    private void getFiles() {
        if (userCanViewFiles()) {
            if (filesAlreadyElaborating()) {
                return;
            }

            files = new ArrayList<>();
            elaboratingReferences = new HashSet<>();
            elaboratingReferences.add(projectStorageRef); // Usato per avvisare il programma che i file
            // sono ancora in elaborazione

            listAll();
        } else {
            showMessageLayout(R.string.text_message_files_are_private, null, null);
        }
    }

    /**
     * Ritorna true quando l'elaborazione dei file è iniziata e non è ancora finita.
     */
    private boolean filesAlreadyElaborating() {
        return elaboratingReferences != null && !elaboratingReferences.isEmpty();
    }

    /**
     * Effettua una query su tutti i file del progetto e non appena finisce aggiorna la lista dei files.
     * Codice modificato a partire da: https://firebase.google.com/docs/storage/android/list-files#list_all_files
     *
     * Non supportiamo la paginazione a causa del metodo di ricerca utilizzato, che lavora in locale
     * non avendo query native sul db di firebase
     */
    private void listAll() {
        projectStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            elaboratingReferences.add(item);
                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    files.add(new ManagerCloudFile(item, item.getName(),
                                            storageMetadata.getContentType(),
                                            storageMetadata.getSizeBytes(),
                                            storageMetadata.getUpdatedTimeMillis()));
                                    dropElaboratingReference(item);
                                }
                            });
                        }
                        dropElaboratingReference(projectStorageRef);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    /**
     * Se tutti i file sono stati elaborati, finisce di inizializzare la ui e li visualizza
     */
    private void dropElaboratingReference (StorageReference ref) {
        elaboratingReferences.remove(ref);
        if (elaboratingReferences.isEmpty()) {
            showFiles(false);
        }
    }

    /**
     * Mostra la lista dei file trovati
     * @param offline indica se mostrare un messaggio informativo quando l'applicazione è offline
     */
    private void showFiles (boolean offline) {
        if (offline) {
            warningOfflineMessageLayout.setVisibility(View.VISIBLE);
        } else {
            warningOfflineMessageLayout.setVisibility(View.GONE);
        }

        if (offline || userCantAddFiles()) {
            addFileFloatingActionButton.setVisibility(View.GONE);
        } else {
            addFileFloatingActionButton.setVisibility(View.VISIBLE);
        }

        if (files.isEmpty()) {
            showMessageLayout(R.string.text_message_files_empty, null, null);
        } else {
            showFileLayout();

            adapter.submitList(files);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Avvisa l'utente che servono i permessi  di lettura e scrittura sul disco ed ottiene i file
     * locali.
     */
    private void getLocalFilesWithWarning() {
        if (userCanViewFiles()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.text_message_local_files_require_storage_permissions)
                    .setPositiveButton(R.string.text_button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION_LOCAL_FILES);
                        }
                    })
                    .setNegativeButton(R.string.text_button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showMessageLayout(R.string.text_message_files_connection_down,
                                    null, null);
                        }
                    }).create().show();
        } else {
            showMessageLayout(R.string.text_message_files_are_private,
                    null, null);
        }
    }

    /**
     * Ottiene i file locali scaricati dal progetto presenti sul disco.<br>
     * Richiede i permessi di lettura e scrittura dallo storage.
     */
    private void getLocalFiles () {
        if (userCanViewFiles()) {
            File downloadFolder = FileDownloader.getDownloadPath(project.getName());
            File[] localFiles = downloadFolder.listFiles();
            files = new ArrayList<>();

            for (File file: localFiles) {
                Uri fileUri = FileUtil.getUriFromFile(getContext(), file);
                ManagerLocalFile managerFile = new ManagerLocalFile(file, file.getName(),
                        FileUtil.getMimeTypeFromUri(requireContext(), fileUri),
                        FileUtil.getFileSizeFromURI(requireContext(), fileUri),
                        file.lastModified());

                files.add(managerFile);
            }

            showFiles(true);
        } else {
            showMessageLayout(R.string.text_message_files_are_private,
                    null, null);
        }
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
        uploadTask = projectStorageRef.child(FileUtil.getFileNameFromURI(getContext(), file))
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
    private void previewFile(Uri uri, ManagerCloudFile file) {
        if (shouldShowPreviewWarning()) {
            //Avvisa l'utente che l'app userà applicazioni esterne per fare la preview
            new AlertDialog.Builder(getContext()).setMessage(R.string.text_message_file_preview_message)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                previewWarning = false;
                                uriToPreview = uri;
                                fileToPreview = file;
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_PREVIEW);
                            }
                        }
                    ).setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        } else {
            //Apre il file o se non è possibile aprirlo, mostra un messaggio all'utente.
            try {
                FileUtil.openFileWithViewIntent(getContext(), uri, file.getType());
            } catch (FileException e) {
                if (e.getErrorCode() == FileException.NO_INTENT_FOUND) {
                    showDownloadSuggestion(R.string.text_message_temp_file_not_supported, fileToPreview);
                }
            }
        }
    }

    /**
     * Ritorna true se si deve mostrare all'utente un messaggio informativo sulla preview dei file
     */
    private boolean shouldShowPreviewWarning() {
        return previewWarning || requiresStorageAccess();
    }


    /**
     * Avvisa l'utente che il file sarà accessibile anche ad altre applicazioni.
     * Se l'utente accetta l'applicazione procederà a richiedere i permessi di scrittura e successivamente
     * al download.
     * @param file il file da scaricare
     */
    private void downloadWithWarning(ManagerCloudFile file) {
        if (shouldShowDownloadWarning()) {
            new AlertDialog.Builder(getContext()).setMessage(R.string.text_message_file_download_message)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadWarning = false;
                                    fileToBeDownloaded = file;
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION_DOWNLOAD);
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

    private boolean shouldShowDownloadWarning() {
        return downloadWarning || requiresStorageAccess();
    }

    private boolean requiresStorageAccess() {
        return FileUtil.requiresStorageAccess(getContext());
    }

    private ManagerCloudFile fileToBeDownloaded;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_DOWNLOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, fileToBeDownloaded.toString());
                    download(fileToBeDownloaded);
                } else {
                    Toast.makeText(getContext(), R.string.text_message_file_permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISSION_PREVIEW:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Apre il file o se non è possibile aprirlo, mostra un messaggio all'utente.
                    try {
                        FileUtil.openFileWithViewIntent(getContext(), uriToPreview, fileToPreview.getType());
                    } catch (FileException e) {
                        if (e.getErrorCode() == FileException.NO_INTENT_FOUND) {
                            showDownloadSuggestion(R.string.text_message_temp_file_not_supported, fileToPreview);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), R.string.text_message_file_permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISSION_LOCAL_FILES:
                getLocalFiles();
                break;
        }
    }

    /**
     * Scarica il file e notifica l'utente a download finito.
     * Se l'utente vuole può anche aprire direttamente il file scaricato.
     * In caso di errori, essi verranno mostrati e sarà data la possibilità all'utente di riprovare.
     */
    private void download (ManagerCloudFile file) {
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
        }.downloadFile(file, project.getName());
    }

    /**
     * Mostra un messaggio qualora il file non sia scaricabile come file temporaneo
     * @param textMessageId il messaggio da visualizzare
     */
    private void showDownloadSuggestion (int textMessageId, ManagerCloudFile file) {
        Snackbar.make(getView(), textMessageId, Snackbar.LENGTH_LONG)
                .setAction(R.string.text_button_download, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadWithWarning(file);
                    }
                }).show();
    }

    /**
     * Mostra la lista dei file nascondendo altri layout
     */
    private void showFileLayout () {
        filesLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    /**
     * Nasconde la lista e mostra un messaggio all'utente
     * @param messageRes risorsa del messaggio da visualizzare
     * @param buttonMessageRes risorsa del messaggio del bottone o null se il bottone non deve apparire
     * @param buttonClickListener l'azione da effettuare al click del bottone se presente
     */
    private void showMessageLayout (@StringRes int messageRes,
                                    @Nullable @StringRes Integer buttonMessageRes,
                                    @Nullable View.OnClickListener buttonClickListener) {
        emptyLayoutMessageTextView.setText(messageRes);
        if (buttonMessageRes != null) {
            emptyLayoutButton.setVisibility(View.VISIBLE);
            emptyLayoutButton.setText(buttonMessageRes);
            emptyLayoutButton.setOnClickListener(buttonClickListener);
        } else {
            emptyLayoutButton.setVisibility(View.GONE);
        }

        filesLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Gestisce la barra di ricerca e la selezione di filtri
     */
    private class ProjectFilesOnSearchListener implements SearchUtil.OnSearchListener {
        @Override
        public void onSearchAction(String query) {
            lastQuery = query;

            List<String> keyWords = new ArrayList<>(Arrays.asList(query.toLowerCase().split(" ")));

            List<ManagerFile> searchFile = new ArrayList<>();
            String releaseFilter = getString(R.string.text_filter_file_release).toLowerCase();
            String imagesFilter = getString(R.string.text_filter_file_image).toLowerCase();
            String pdfFilter = getString(R.string.text_filter_file_pdf);

            for (ManagerFile file: files) {
                boolean toAdd = true;

                if (!query.isEmpty()) {
                    for (String string: keyWords) {
                        //Se il file non include una delle parole chiavi, non verrà mostrato.
                        //Verrà sempre mostrato sempre invece se la query è vuota
                        if (toAdd) {
                            // Va aggiunto se il nome corrisponde alla query
                            toAdd = file.getName().toLowerCase().contains(string.toLowerCase()) ;
                        }
                    }
                }

                if (toAdd && !searchFilters.isEmpty()) {
                    // Il file va aggiunto se include almeno uno dei filtri
                    boolean containsFilter = false;
                    for (String string: searchFilters) {
                        containsFilter = // Va aggiunto se il tipo corrisponde alla query
                                (file.getType().toLowerCase().contains(string.toLowerCase()) ||
                                // Va aggiunto se il filtro contiene i rilasci ed il file ne è uno
                                (string.contains(releaseFilter) && project.getReleaseNumber(file.getName()) != 0) ||
                                // Va aggiunto se il filtro contiene le immagini ed il file ne è una.
                                (string.contains(imagesFilter) && file.getType().contains("image/")) ||
                                // Va aggiunto se il filtro contiene i pdf ed il file ne è uno.
                                (string.contains(pdfFilter) && file.getType().equals("application/pdf")));

                        if (containsFilter) {
                            break;
                        }
                    }

                    toAdd = containsFilter;
                }

                if (toAdd) {
                    searchFile.add(file);
                }
            }
            adapter.submitList(searchFile);
        }

        @Override
        public void onFilterAdded(String filter) {
            searchFilters.add(filter.toLowerCase());
            onSearchAction(lastQuery);
        }

        @Override
        public void onFilterRemoved(String filter) {
            searchFilters.remove(filter.toLowerCase());
            onSearchAction(lastQuery);
        }
    }

    /**
     * Nasconde o mostra la lista in base a se è presente la connessione
     */
    private class ProjectFilesConnectionChangeListener implements ConnectionCheckBroadcastReceiver.OnConnectionChangeListener {
        @Override
        public void onConnectionUp() {
            getFiles();
        }

        @Override
        public void onConnectionDown() {
            if (requiresStorageAccess()) {
                getLocalFilesWithWarning();
            } else {
                getLocalFiles();
            }
            ConnectionCheckBroadcastReceiver.dismissConnectivitySnackbar();
        }
    }

    /**
     * Implementazione delle azioni effettuabili su ogni file della lista
     */
    private class ProjectFilesFileActionsListener implements FilesRecyclerAdapter.OnActionListener {
        /**
         * Azione da eseguire quando l'utente clicca su un file presente sul cloud
         */
        @Override
        public void onClick(ManagerCloudFile file) {
            if (project.getReleaseNumber(file.getName()) != 0) {
                FirebaseDbHelper.addProjectToTriedProjects(project);
            }

            //Scarica il file temporaneo e lo visualizza usando un app esterna
            new TemporaryFileDownloader(getContext()) {
                @Override
                protected void showDownloadFailedMessage(int messageRes) {
                    //Mostra un messaggio qualora il file non sia scaricabile come file temporaneo
                    ProjectFilesFragment.this.showDownloadSuggestion(messageRes, file);
                }

                @Override
                protected void onSuccessAction(File localFile) {
                    Uri uri = FileUtil.getUriFromFile(getContext(), localFile);

                    previewFile(uri, file);
                }
            }.downloadTempProjectFile(file, project);
        }

        /**
         * Azione da eseguire quando l'utente clicca su un file presente nello storage
         */
        @Override
        public void onClick(ManagerLocalFile file) {
            try {
                FileUtil.openFileWithViewIntent(getContext(),
                        FileUtil.getUriFromFile(getContext(), file.getLocalFile()),
                        file.getType());
            } catch (FileException e) {
                if (e.getErrorCode() == FileException.NO_INTENT_FOUND) {
                    Toast.makeText(getContext(), R.string.text_message_downloaded_file_not_supported,
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onSetRelease(ManagerCloudFile file, boolean addRelease) {
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
         * Il file viene nascosto dalla lista e l'utente può annullare l'operazione grazie allo
         * snackbar che appare. Se l'utente non esegue alcuna azione, il file viene eliminato
         * definitivamente dal db.
         *
         * Per l'eliminazione definitiva dal db viene usato il codice preso e modificato da:
         * https://firebase.google.com/docs/storage/android/delete-file
         *
         * @param file file da cancellare
         */
        @Override
        public void onDelete(ManagerCloudFile file) {
            // Indica se il file deve essere eliminato definitavemente o no
            final boolean[] toDelete = {true};

            files.remove(file);
            adapter.submitList(files);
            adapter.notifyDataSetChanged();

            Snackbar.make(requireView(), R.string.text_message_file_deletion,
                    Snackbar.LENGTH_LONG).setAction(R.string.text_action_undo, new View.OnClickListener() {
                // Inserisce un'azione che permette di ripristinare un file cancellato per sbaglio.
                // L'azione è permessa solo fino a che lo snackbar rimane a schermo.
                @Override
                public void onClick(View v) {
                    files.add(file);
                    adapter.submitList(files);
                    adapter.notifyDataSetChanged();
                    toDelete[0] = false;
                    Snackbar.make(requireView(), R.string.text_message_file_deletion_stopped,
                            Snackbar.LENGTH_SHORT).show();
                }
            }).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    // Quando lo snackbar sparisce, il file viene eliminato definitivamente dal db
                    super.onDismissed(transientBottomBar, event);

                    if (toDelete[0]) {
                        // Cancella definitivamente il file dal database
                        file.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (project.getReleaseNames().contains(file.getName())) {
                                    project.removeReleaseName(file.getName());
                                }
                                Snackbar.make(requireView(), R.string.text_message_file_deleted_successfully,
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                files.add(file);
                                adapter.submitList(files);
                                adapter.notifyDataSetChanged();
                                Snackbar.make(requireView(), R.string.text_message_file_deletion_failed,
                                        Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).show();
        }

        @Override
        public void onDelete(ManagerLocalFile file) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.text_message_file_local_delete)
                    .setPositiveButton(R.string.text_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean deleted = file.getLocalFile().delete();

                            if (deleted) {
                                files.remove(file);
                                adapter.submitList(files);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), R.string.text_message_file_deleted_successfully,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), R.string.text_message_file_deletion_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
        }

        @Override
        public void onShare(ManagerCloudFile file) {
            //Scarica il file temporaneo e lo condivido permettendo all'utente di scegliere il
            //mezzo di condivisione.
            new TemporaryFileDownloader(getContext()) {
                @Override
                protected void showDownloadFailedMessage(int messageRes) {
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
            }.downloadTempProjectFile(file, project);


        }

        @Override
        public void onDownload(ManagerCloudFile file) {
            if (project.getReleaseNumber(file.getName()) != 0) {
                FirebaseDbHelper.addProjectToTriedProjects(project);
            }

            downloadWithWarning(file);
        }
    }
}