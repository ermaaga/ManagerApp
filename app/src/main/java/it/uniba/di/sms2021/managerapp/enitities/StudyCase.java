package it.uniba.di.sms2021.managerapp.enitities;

import androidx.annotation.NonNull;

import java.util.Objects;

public class StudyCase {
    //Aggiungo id per serializzazione più semplice
    private String id;
    private String nome;
    private String descrizione;

    public StudyCase() {
    }

    public StudyCase(String id, String nome, String descrizione) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyCase studyCase = (StudyCase) o;
        return nome.equals(studyCase.nome) &&
                Objects.equals(descrizione, studyCase.descrizione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, descrizione);
    }
}
