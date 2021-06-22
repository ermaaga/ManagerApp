package it.uniba.di.sms2021.managerapp.firebase;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.Application;
import it.uniba.di.sms2021.managerapp.enitities.Course;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.Evaluation;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.ListProjects;
import it.uniba.di.sms2021.managerapp.enitities.ProjectPermissions;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.enitities.notifications.ExamJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewEvaluation;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReplyReportNotice;
import it.uniba.di.sms2021.managerapp.enitities.notifications.NewReportNotice;
import it.uniba.di.sms2021.managerapp.notifications.EvaluationNotification;
import it.uniba.di.sms2021.managerapp.utility.FileUtil;

public class DummyDataLoader implements DataLoader {
    private static final String TAG = "DummyDataLoader";
    private final List<StorageMetadata> metadataList = new ArrayList<>();
    private final List<Uri> uriList = new ArrayList<>();
    private final List<StorageReference> referencesList = new ArrayList<>();
    private int nextInUploadQueue = 0;
    private int currentlyUploading = -1;

    @Override
    public void loadData(Application application) {
        DatabaseReference root = FirebaseDbHelper.getDBInstance().getReference();

        DatabaseReference departments = root.child(FirebaseDbHelper.TABLE_DEPARTMENTS);
        departments.setValue(null);
        String[] departmentNames = new String[] {"Informatica", "Matematica"};
        HashMap<String, String> departmentIds = new HashMap<>();
        for (String name: departmentNames) {
            DatabaseReference push = departments.push();
            push.setValue(new Department(push.getKey(), name));
            departmentIds.put(name, push.getKey());
        }

        DatabaseReference courses = root.child(FirebaseDbHelper.TABLE_COURSES);
        courses.setValue(null);
        String[] informaticaCourses = {"Informatica", "Informatica e Tecnologie di Produzione del Software",
                                        "Informatica e Comunicazione Digitale"};
        String[] matematicaCourses = {"Matematica", "Magistrale in Matematica"};
        HashMap<String, String> coursesIds = new HashMap<>();
        for (String name: informaticaCourses) {
            DatabaseReference push = courses.push();
            push.setValue(new Course(push.getKey(), name, departmentIds.get("Informatica")));
            coursesIds.put(name, push.getKey());
        }
        for (String name: matematicaCourses) {
            DatabaseReference push = courses.push();
            push.setValue(new Course(push.getKey(), name, departmentIds.get("Matematica")));
            coursesIds.put(name, push.getKey());
        }

        //TODO aggiungere account nostri
        DatabaseReference users = root.child(FirebaseDbHelper.TABLE_USERS);
        users.setValue(null);
        User[] userArray = {
                //0 Utente di test
                new User("wiHIHFr6hSN4QCKC0XcCBKNmbTD2", "Mario", "Rossi",
                        "test@test.com", User.ROLE_PROFESSOR,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //1
                new User(null, "Paolo", "Buono",
                        "paolo.buono@test.com", User.ROLE_PROFESSOR,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]),
                                coursesIds.get(informaticaCourses[0]))),
                //2
                new User(null, "Filippo", "Rossi",
                        "filippo.rossi@test.com", User.ROLE_PROFESSOR,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //3
                new User(null, "Mario", "Verdi",
                        "mario.verdi@test.com", User.ROLE_PROFESSOR,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //4
                new User(null, "Giuseppe", "Gialli",
                        "giuseppe.gialli@test.com", User.ROLE_STUDENT,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //5
                new User(null, "Marco", "Marrone",
                        "marco.marrone@test.com", User.ROLE_STUDENT,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //6
                new User("M2DNRGpEwlWSZ0pCPR4E2QzfV1p1", "Mario", "Gargiulo",
                        "m.gargiulo5@studenti.uniba.it", User.ROLE_STUDENT,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //7
                new User("Dfatkfs7cyViPHZT2ur6mkazaEv1", "Cecilia Valery", "Andresano",
                        "c.andresano@studenti.uniba.it", User.ROLE_STUDENT,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
                //8
                new User("Ac4kSWIut8duOu0vGPeJvVt1ojw2", "Sara", "Carecci",
                        "s.carecci@studenti.uniba.it", User.ROLE_STUDENT,
                        Arrays.asList(departmentIds.get(departmentNames[0])),
                        Arrays.asList(coursesIds.get(informaticaCourses[1]))),
        };
        for (User user: userArray) {
            DatabaseReference userRef;
            if (user.getAccountId() == null) {
                userRef = users.push();
                user.setAccountId(userRef.getKey());
            } else {
                userRef = users.child(user.getAccountId());
            }
            userRef.setValue(user);
        }

        DatabaseReference exams = root.child(FirebaseDbHelper.TABLE_EXAMS);
        exams.setValue(null);
        Exam[] examArrays = {
                new Exam(null, "Sviluppo di Mobile Software",
                        Arrays.asList(userArray[1].getAccountId()),
                        Arrays.asList(userArray[0].getAccountId(), userArray[4].getAccountId(),
                                userArray[5].getAccountId(), userArray[6].getAccountId(),
                                userArray[7].getAccountId(), userArray[8].getAccountId()),
                        2020),
                new Exam(null, "Progettazione dell'Interazione con l'Utente",
                        Arrays.asList(userArray[2].getAccountId()),
                        Arrays.asList(userArray[4].getAccountId(), userArray[0].getAccountId()),
                        2020),
                new Exam(null, "Integrazione e Test di Sistemi Software",
                        Arrays.asList(userArray[3].getAccountId()),
                        Arrays.asList(userArray[5].getAccountId(), userArray[7].getAccountId()),
                        2020),
                new Exam(null, "Modelli e Metodi di Qualità del Software",
                        Arrays.asList(userArray[4].getAccountId()),
                        Arrays.asList(userArray[6].getAccountId(), userArray[8].getAccountId()),
                        2020),
                new Exam(null, "Tesi di Laurea",
                        Arrays.asList(userArray[0].getAccountId()),
                        Arrays.asList(userArray[4].getAccountId(), userArray[5].getAccountId(),
                                userArray[7].getAccountId(),
                                userArray[8].getAccountId()),
                        2020),
        };
        for (Exam exam: examArrays) {
            DatabaseReference push = exams.push();
            exam.setId(push.getKey());
            push.setValue(exam);
        }

        DatabaseReference studyCases = root.child(FirebaseDbHelper.TABLE_STUDYCASES);
        studyCases.setValue(null);
        StudyCase[] studyCasesArray = {
                new StudyCase(null, "SensorStudyApp", "L'app è pensata per supportare" +
                        " studi di usabilità con utenti e, più in particolare, gli studi di usabilità in cui si " +
                        "chiede ai partecipanti di eseguire dei task e di cui si vuole misurare l'efficacia " +
                        "(tasso di successo) e l'efficienza (tempo impiegato).",
                        examArrays[0].getId()),
                new StudyCase(null, "ContagiApp", "Le pandemie creano problemi a " +
                        "livello sanitario ed organizzativo in tutto il mondo. L’app da realizzare " +
                        "sfrutta le dinamiche delle reti iter-personali per ‘verificare’ volontariamente" +
                        " lo status sanitario degli utenti e poter organizzare " +
                        "(con le dovute precauzioni) incontri ed eventi o, più semplicemente, " +
                        "uscire di casa per una passeggiata o shopping tenendo conto di chi ci circonda.",
                        examArrays[0].getId()),
                new StudyCase(null, "ManagerApp", "I casi di studio degli esami nella facoltà di " +
                        "Informatica (ma non solo) sono tanti ed il materiale prodotto si accumula " +
                        "e confonde nei vari supporti anno dopo anno. Si vuole perciò realizzare una " +
                        "app che permette la visione ed organizzazione di questi progetti " +
                        "(spesso costituiti da materiali eterogenei).",
                        examArrays[0].getId()),
                new StudyCase(null, "ArkanApp", "Questa app è una variante del famoso videogioco " +
                        "Arkanoid. Si parta dal codice nel repository github " +
                        "(https://github.com/Ludovit-Laca/Arkanoid-android-game ) " +
                        "interamente scritto in Java per Android Studio.",
                        examArrays[0].getId()),
                new StudyCase(null, "Rilevamento di emozioni da tratti facciali",
                        "Per questa tesi di laurea si userà un programma open source per il " +
                                "rilevamento dei tratti facciali e la successiva classificazione" +
                                "di valori etichetta rappresentanti le emozioni",
                        examArrays[4].getId()),
                new StudyCase(null, "Studio IOT",
                        "Studio IOT presso azienda.",
                        examArrays[4].getId()),
                new StudyCase(null, "Progettazione Sito Turismo",
                        "Progettare un'interfaccia grafica per un sito dedito al turismo.",
                        examArrays[1].getId())
        };
        File caseStudyFile = new File("/sdcard/Download/Elenco casi di studio 20-21.pdf");
        if (!caseStudyFile.exists()) {
            Log.e(TAG, "There is no file with path " + caseStudyFile.getAbsolutePath() +
                    ", verrà saltato il settaggio dei file dei casi di studio");
        }
        for (StudyCase studyCase: studyCasesArray) {
            DatabaseReference push = studyCases.push();
            studyCase.setId(push.getKey());
            push.setValue(studyCase);
            if (caseStudyFile.exists()) {
                scheduleUpload(application, FirebaseDbHelper.getStudyCasePathReference(studyCase), caseStudyFile);
            }
        }

        DatabaseReference groups = root.child(FirebaseDbHelper.TABLE_GROUPS);
        groups.setValue(null);
        Group mobileTechs = new Group(null, "Mobile Techs", studyCasesArray[2].getId(), examArrays[0].getId(),
                Arrays.asList(userArray[0].getAccountId(), userArray[6].getAccountId(),
                        userArray[7].getAccountId(), userArray[8].getAccountId()));
        mobileTechs.setEvaluation(new Evaluation(30, "Buon Lavoro!"));
        ProjectPermissions mobileTechsPermissions = new ProjectPermissions();
        mobileTechsPermissions.setAccessible(true);
        mobileTechsPermissions.setCanAddFiles(Arrays.asList(
                userArray[0].getAccountId(), userArray[6].getAccountId(),
                userArray[7].getAccountId(), userArray[8].getAccountId()
        ));
        mobileTechsPermissions.setFileAccessible(true);
        mobileTechsPermissions.setMaxMembers(4);
        mobileTechs.setPermissions(mobileTechsPermissions);
        mobileTechs.setWhoPrefers(Arrays.asList(userArray[0].getAccountId()));
        mobileTechs.setReleaseNames(Arrays.asList("link github.txt"));
        Group skyMind = new Group(null, "Skymind", studyCasesArray[0].getId(), examArrays[0].getId(),
                Arrays.asList(userArray[4].getAccountId(), userArray[5].getAccountId()));
        ProjectPermissions skyMindPermissions = new ProjectPermissions();
        skyMindPermissions.setAccessible(true);
        skyMindPermissions.setFileAccessible(true);
        skyMindPermissions.setMaxMembers(4);
        skyMind.setPermissions(skyMindPermissions);
        skyMind.setReleaseNames(Arrays.asList("link github.txt"));
        Group tesiGiuseppeGialli = new Group(null, "Tesi-Giuseppe Gialli", studyCasesArray[4].getId(),
                examArrays[4].getId(), Arrays.asList(userArray[4].getAccountId()));
        tesiGiuseppeGialli.setPermissions(new ProjectPermissions());
        tesiGiuseppeGialli.setReleaseNames(Arrays.asList("link github.txt"));
        Group tesiMarcoMarrone = new Group(null, "Tesi-Marco Marrone", studyCasesArray[5].getId(),
                examArrays[4].getId(), Arrays.asList(userArray[5].getAccountId()));
        tesiMarcoMarrone.setPermissions(new ProjectPermissions());
        tesiMarcoMarrone.setReleaseNames(Arrays.asList("link github.txt"));
        tesiMarcoMarrone.setEvaluation(new Evaluation(100, "Bella tesi, complimenti."));
        Group turismTech = new Group(null, "TurismTech", studyCasesArray[6].getId(),
                examArrays[1].getId(), Arrays.asList(userArray[0].getAccountId()));
        Group[] groupsArray = {
                mobileTechs,
                skyMind,
                tesiGiuseppeGialli,
                tesiMarcoMarrone,
                turismTech
        };
        File[] groupFiles = {
                new File("/sdcard/Download/link github.txt"),
                new File("/sdcard/Download/Appunti Documentazione.docx"),
                new File("/sdcard/Download/Demonstration App.gif"),
                new File("/sdcard/Download/Presentazione App.mov"),
                new File("/sdcard/Download/Screen2 App.jpg"),
                new File("/sdcard/Download/ScreenApp.jpg"),
                new File("/sdcard/Download/Tema App.mp3")
        };
        for (Group group: groupsArray) {
            DatabaseReference push = groups.push();
            group.setId(push.getKey());
            push.setValue(group);
        }
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(FirebaseDbHelper.GROUPS_FOLDER);
        for (File file: groupFiles) {
            if (!file.exists()) {
                Log.e(TAG, "Non esiste un file nella path " + file.getAbsolutePath() + ", " +
                        "verrà saltata l'aggiunta del file");
            }
            scheduleUpload(application, storageReference.child(groupsArray[0].getId()), file);
        }
        scheduleUpload(application, storageReference.child(groupsArray[2].getId()), groupFiles[0]);
        scheduleUpload(application, storageReference.child(groupsArray[3].getId()), groupFiles[0]);

        root.child(FirebaseDbHelper.TABLE_LISTS_PROJECTS).setValue(null);
        FirebaseDbHelper.getFavouriteProjectsReference(userArray[0].getAccountId())
                .child(groupsArray[0].getId()).setValue(true);
        FirebaseDbHelper.getTriedProjectsReference(userArray[0].getAccountId())
                .child(groupsArray[1].getId()).setValue(true);
        FirebaseDbHelper.getEvaluatedProjectsReference(userArray[0].getAccountId())
                .child(groupsArray[3].getId()).setValue(true);
        ListProjects listProjects = new ListProjects(null, "Tesi da Valutare",
                Arrays.asList(tesiGiuseppeGialli.getId(), tesiMarcoMarrone.getId()));
        DatabaseReference projectListPush = FirebaseDbHelper
                .getReceivedProjectListsReference(userArray[0].getAccountId()).push();
        listProjects.setIdList(projectListPush.getKey());
        projectListPush.setValue(listProjects);

        //2 Report -> Uno per gruppo mobileTechs ed uno per testiGiuseppeGialli
        DatabaseReference reportsReference = root.child(FirebaseDbHelper.TABLE_REPORTS);
        reportsReference.setValue(null);
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Report[] reportArray = {
                new Report(null, userArray[1].getAccountId(), date,
                mobileTechs.getId(), "Assicuratevi di aver aggiunto la mia email" +
                        " a quelle autorizzate a visualizzare la cartella drive"),
                new Report(null, userArray[0].getAccountId(), date,
                        tesiGiuseppeGialli.getId(), "Consegna entro la prossima settimana" +
                        " o salterai la sessione di laurea.")
        };
        for (Report report: reportArray) {
            DatabaseReference reportPush = reportsReference
                    .child(FirebaseDbHelper.CHILD_REPORTS_LIST).push();
            report.setReportId(reportPush.getKey());
            reportPush.setValue(report);
            reportsReference.child(FirebaseDbHelper.CHILD_LATEST_REPORTS)
                    .child(report.getGroupId()).setValue(reportPush.getKey());
        }

        root.child(FirebaseDbHelper.TABLE_REPLIES_REPORT).setValue(null);
        DatabaseReference reportReplyReference = root.child(FirebaseDbHelper.TABLE_REPLIES_REPORT)
                .push();
        Reply reply = new Reply(reportReplyReference.getKey(), userArray[4].getAccountId(),
                date, "D'accordo, consegnerò fra due giorni al massimo.",
                reportArray[1].getReportId());
        reportReplyReference.setValue(reply);


        //Varie notifiche
        root.child(FirebaseDbHelper.TABLE_NOTIFICATIONS).setValue(null);
        Log.d(TAG, examArrays[4].getName());
        DatabaseReference examRequestReference = FirebaseDbHelper.getExamJoinRequestReference(userArray[0].getAccountId()).push();
        ExamJoinRequest examRequest = new ExamJoinRequest(examRequestReference.getKey(), userArray[6].getAccountId(),
                userArray[6].getFullName(), examArrays[4], System.currentTimeMillis());
        examRequestReference.setValue(examRequest);

        DatabaseReference groupJoinNoticeReference = FirebaseDbHelper
                .getGroupUserJoinNoticeReference(userArray[0].getAccountId()).push();
        GroupJoinNotice notice = new GroupJoinNotice(userArray[0].getAccountId(), groupJoinNoticeReference.getKey(),
                userArray[7], groupsArray[0], false, System.currentTimeMillis());
        groupJoinNoticeReference.setValue(notice);

        DatabaseReference groupJoinRequestReference = FirebaseDbHelper
                .getGroupJoinRequestReference(userArray[0].getAccountId()).push();
        GroupJoinRequest groupRequest = new GroupJoinRequest(groupJoinRequestReference.getKey(),
                userArray[4].getAccountId(), turismTech.getId(), userArray[0].getAccountId());
        groupJoinRequestReference.setValue(groupRequest);

        DatabaseReference newEvaluationReference = FirebaseDbHelper
                .getNewEvaluationReference(userArray[0].getAccountId()).push();
        EvaluationNotification evaluationNotification = new EvaluationNotification(
                new NewEvaluation(newEvaluationReference.getKey(), userArray[1].getAccountId(),
                        mobileTechs.getId(), false));
        newEvaluationReference.setValue(evaluationNotification);

        DatabaseReference newReportReference = FirebaseDbHelper
                .getNewReportReference(userArray[0].getAccountId()).push();
        NewReportNotice newReportNotice = new NewReportNotice(reportArray[0].getReportId(),
                reportArray[0].getUserId(), reportArray[0].getGroupId());
        newReportReference.setValue(newReportNotice);

        DatabaseReference newReplyReportReference = FirebaseDbHelper
                .getNewReplyReportReference(userArray[0].getAccountId()).push();
        NewReplyReportNotice newReplyReportNotice = new NewReplyReportNotice(reply.getReplyId(),
                reply.getUserId(), reply.getOriginId(), tesiGiuseppeGialli.getId());
        newReplyReportReference.setValue(newReplyReportNotice);


        //Recensioni
        DatabaseReference reviews = root.child(FirebaseDbHelper.TABLE_REVIEWS);
        reviews.setValue(null);
        Review[] reviewArray = {
                new Review(null, userArray[4].getAccountId(), date, 3, groupsArray[0].getId(),
                        "Da quello che ho visto l'app sembra essere discreta."),
                new Review(null, userArray[5].getAccountId(), date, 5, groupsArray[0].getId(),
                        "Non vedo l'ora di provarla, continuate così!")
        };
        for (Review review: reviewArray) {
            DatabaseReference push = reviews.push();
            review.setReviewId(push.getKey());
            push.setValue(review);
        }

        DatabaseReference reviewReplies = root.child(FirebaseDbHelper.TABLE_REPLIES_REVIEW);
        reviewReplies.setValue(null);
        Reply[] reviewReplyArray = {
                new Reply(null, userArray[7].getAccountId(), date,
                        "Grazie, facciamo del nostro meglio.", reviewArray[1].getReviewId()),
                new Reply(null, userArray[8].getAccountId(), date,
                        "Faremo in modo di mettere l'apk finale così che potrete provarla.",
                        reviewArray[1].getReviewId()),
        };
        for (Reply reviewReply : reviewReplyArray) {
            DatabaseReference push = reviewReplies.push();
            reviewReply.setReplyId(push.getKey());
            push.setValue(reviewReply);
        }
    }

    private void scheduleUpload(Application application, StorageReference reference, File file) {
        if (!application.shouldUploadFiles()) {
            return;
        }

        Uri uri = FileUtil.getUriFromFile(application, file);
        metadataList.add(new StorageMetadata.Builder()
                .setContentType(FileUtil.getMimeTypeFromUri(application, uri))
                .build());
        uriList.add(uri);
        referencesList.add(reference);

        uploadNextItem();
    }

    private void uploadNextItem() {
        if (nextInUploadQueue == 0 && currentlyUploading != 0) {
            Log.i(TAG, "Started uploading files...");
        }
        if (uriList.size() <= nextInUploadQueue) {
            Log.i(TAG, "Finished uploading files.");
            return;
        }
        if (nextInUploadQueue != currentlyUploading) {
            Log.i(TAG, "Still uploading files...");
            currentlyUploading = nextInUploadQueue;
            Uri uri = uriList.get(nextInUploadQueue);
            referencesList.get(nextInUploadQueue).child(uri.getLastPathSegment())
                    .putFile(uri, metadataList.get(nextInUploadQueue)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    new Handler().postAtTime(() -> {
                        nextInUploadQueue++;
                        uploadNextItem();
                    }, 100);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    new Handler().postAtTime(() -> {
                        uploadNextItem();
                    }, 1000);
                }
            });
        }
    }

    // departments courses users exams studyCases groups
}
