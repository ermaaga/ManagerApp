package it.uniba.di.sms2021.managerapp.enitities;

import com.google.firebase.database.Exclude;

public class User {
    public static final int ROLE_STUDENT = 1;
    public static final int ROLE_PROFESSOR = 2;

    public static final int COURSE_INFORMATICA = 1;
    public static final int COURSE_ITPS = 2;

    private String accountId;
    private String nome;
    private String cognome;
    private String email;
    private int ruolo;
    private int corso;

    public User() {
    }

    public User(String accountId, String nome, String cognome, String email, int ruolo, int corso) {
        this.accountId = accountId;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.ruolo = ruolo;
        this.corso = corso;
    }

    public User(String accountId, String nome, String cognome, String email) {
        this.accountId = accountId;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRuolo() {
        return ruolo;
    }

    public void setRuolo(int ruolo) {
        this.ruolo = ruolo;
    }

    public int getCorso() {
        return corso;
    }

    public void setCorso(int corso) {
        this.corso = corso;
    }

    //@Exclude esclude il campo dalla serializzazione di firebase.
    @Exclude
    public String getFullName () {
        return nome + " " + cognome;
    }
}
