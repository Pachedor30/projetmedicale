package com.mycompany.projetmedical.model;

import java.time.LocalDateTime;

/**
 * Entité patient : rattachement d'un compte au dossier médical.
 * Correspond à la table « patients » (héritage par jointure de utilisateurs).
 */
public class Patient {

    /** Rattachement en attente : le compte existe mais aucun dossier n'est ouvert. */
    public static final String RATTACHEMENT_EN_ATTENTE = "EN_ATTENTE";
    /** Identité vérifiée par l'accueil/admin : le dossier est ouvert. */
    public static final String RATTACHEMENT_VERIFIE = "VERIFIE";
    /** Identité non concordante. */
    public static final String RATTACHEMENT_REJETE = "REJETE";

    private Long utilisateurId;
    private String numeroDossier;
    private String rattachementStatut;
    private Long verifiePar;
    private String verifieParNom;
    private LocalDateTime verifieLe;
    private String profession;
    private String contactUrgence;
    private String telephoneUrgence;
    private String groupeSanguin;
    private LocalDateTime dateCreation;

    /* Informations civiles (portées par utilisateurs) */
    private String nom;
    private String prenoms;
    private String email;
    private String telephone;
    private String adresse;
    private String sexe;
    private java.time.LocalDate dateNaissance;
    private String statut;

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNumeroDossier() {
        return numeroDossier;
    }

    public void setNumeroDossier(String numeroDossier) {
        this.numeroDossier = numeroDossier;
    }

    public String getRattachementStatut() {
        return rattachementStatut;
    }

    public void setRattachementStatut(String rattachementStatut) {
        this.rattachementStatut = rattachementStatut;
    }

    public Long getVerifiePar() {
        return verifiePar;
    }

    public void setVerifiePar(Long verifiePar) {
        this.verifiePar = verifiePar;
    }

    public String getVerifieParNom() {
        return verifieParNom;
    }

    public void setVerifieParNom(String verifieParNom) {
        this.verifieParNom = verifieParNom;
    }

    public LocalDateTime getVerifieLe() {
        return verifieLe;
    }

    public void setVerifieLe(LocalDateTime verifieLe) {
        this.verifieLe = verifieLe;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getContactUrgence() {
        return contactUrgence;
    }

    public void setContactUrgence(String contactUrgence) {
        this.contactUrgence = contactUrgence;
    }

    public String getTelephoneUrgence() {
        return telephoneUrgence;
    }

    public void setTelephoneUrgence(String telephoneUrgence) {
        this.telephoneUrgence = telephoneUrgence;
    }

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(String groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public java.time.LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(java.time.LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    /** Nom complet du patient. */
    public String getNomComplet() {
        StringBuilder sb = new StringBuilder();
        if (prenoms != null && !prenoms.isEmpty()) {
            sb.append(prenoms);
        }
        if (nom != null && !nom.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(nom);
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    /** Vrai si l'identité est vérifiée : le dossier médical est accessible. */
    public boolean estRattache() {
        return RATTACHEMENT_VERIFIE.equals(rattachementStatut);
    }
}
