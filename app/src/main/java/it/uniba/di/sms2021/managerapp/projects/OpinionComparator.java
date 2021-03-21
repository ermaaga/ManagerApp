package it.uniba.di.sms2021.managerapp.projects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.enitities.Opinion;

/**
 * Ordina le recensioni e le segnalazioni di un progetto e le risposte di recensioni e segnalazioni,
 * in ordine decrescente di data
 */
public class OpinionComparator implements Comparator<Opinion> {

    public OpinionComparator() {
    }

    @Override
    public int compare(Opinion o1, Opinion o2) {
        String stringO2= o2.getDateOpinion();
        String stringO1= o1.getDateOpinion();
        Date dateO2;
        Date dateO1;
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            dateO2 = format.parse(stringO2);
            dateO1 = format.parse(stringO1);
        return (int) (dateO2.compareTo(dateO1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
