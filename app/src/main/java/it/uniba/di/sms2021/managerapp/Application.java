package it.uniba.di.sms2021.managerapp;

import it.uniba.di.sms2021.managerapp.firebase.DataLoader;
import it.uniba.di.sms2021.managerapp.firebase.DummyDataLoader;

public class Application extends android.app.Application {
    private static final boolean LOAD_DUMMY_DATA = false;
    private boolean dataLoaded = false;
    private static final boolean UPLOAD_DUMMY_FILES = false;

    public boolean shouldLoadData () {
        return LOAD_DUMMY_DATA && !dataLoaded;
    }

    public DataLoader getDataLoader () {
        return new DummyDataLoader();
    }

    public void stopLoadingData () {
        dataLoaded = true;
    }

    public boolean shouldUploadFiles() {
        return UPLOAD_DUMMY_FILES;
    }
}
