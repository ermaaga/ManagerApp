package it.uniba.di.sms2021.managerapp.enitities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class StudyCase implements Parcelable {
    //Aggiungo id per serializzazione più semplice
    private String id;
    private String nome;
    private String descrizione;
    private String esame;

    public StudyCase() {
    }

    public StudyCase(String id, String nome, String descrizione, String esame) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.esame = esame;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getEsame() {
        return esame;
    }

    public void setEsame(String esame) {
        this.esame = esame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyCase studyCase = (StudyCase) o;
        return Objects.equals(id, studyCase.id) &&
                Objects.equals(nome, studyCase.nome) &&
                Objects.equals(descrizione, studyCase.descrizione) &&
                Objects.equals(esame, studyCase.esame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, descrizione, esame);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(descrizione);
        dest.writeString(esame);
    }

    public static final Parcelable.Creator<StudyCase> CREATOR
            = new Parcelable.Creator<StudyCase>() {
        public StudyCase createFromParcel(Parcel in) {
            StudyCase studyCase = new StudyCase();
            studyCase.setId(in.readString());
            studyCase.setNome(in.readString());
            studyCase.setDescrizione(in.readString());
            studyCase.setEsame(in.readString());

            return studyCase;
        }

        public StudyCase[] newArray(int size) {
            return new StudyCase[size];
        }
    };

    public interface Keys{
        String ID = "id";
        String NOME = "nome";
        String DESCRIZIONE = "descrizione";
        String ESAME = "esame";

    }
}
