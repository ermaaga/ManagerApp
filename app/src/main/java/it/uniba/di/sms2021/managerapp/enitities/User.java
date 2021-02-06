package it.uniba.di.sms2021.managerapp.enitities;

import com.google.firebase.database.Exclude;

import java.util.List;
import java.util.Objects;

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
    private List<String> dipartimenti;
    private int corso;

    public User() {
    }

    public User(String accountId, String nome, String cognome, String email, int ruolo, List<String> dipartimenti, int corso) {
        this.accountId = accountId;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.ruolo = ruolo;
        this.corso = corso;
        this.dipartimenti = dipartimenti;
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

    public List<String> getDipartimenti() {
        return dipartimenti;
    }

    public void setDipartimenti(List<String> dipartimenti) {
        this.dipartimenti = dipartimenti;
    }

    @Override
    public String toString() {
        return "User{" +
                "accountId='" + accountId + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", ruolo=" + ruolo +
                ", dipartimenti=" + dipartimenti +
                ", corso=" + corso +
                '}';
    }

    //@Exclude esclude il campo dalla serializzazione di firebase.
    @Exclude
    public String getFullName () {
        String nomeLowerCase = nome;
        if (nome.length() > 1) {
            nomeLowerCase = nome.substring(0,1) + nome.substring(1).toLowerCase();
        }

        String cognomeLowerCase = cognome;
        if (cognome.length() > 1) {
            cognomeLowerCase = cognome.substring(0,1) + cognome.substring(1).toLowerCase();
        }

        return nomeLowerCase + " " + cognomeLowerCase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return ruolo == user.ruolo &&
                corso == user.corso &&
                Objects.equals(accountId, user.accountId) &&
                Objects.equals(nome, user.nome) &&
                Objects.equals(cognome, user.cognome) &&
                Objects.equals(email, user.email) &&
                Objects.equals(dipartimenti, user.dipartimenti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, nome, cognome, email, ruolo, dipartimenti, corso);
    }
}
