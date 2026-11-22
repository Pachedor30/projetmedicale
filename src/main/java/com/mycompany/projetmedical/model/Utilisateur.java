package com.mycompany.projetmedical.model;

import com.mycompany.projetmedical.util.Constantes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité utilisateur : compte de l'application (personnel ET patients).
 * La colonne role_code porte l'un des 6 rôles officiels du cahier des charges.
 * Correspond à la table « utilisateurs » (+ jointure personnel / patients).
 */
public class Utilisateur {

    private Long id;
    private String nom;
    private String prenoms;
    private String email;
    private String motDePasseHash;
    private String role;
    private Long serviceId;
    private String nomService;
    private String statut;
    private boolean doitChangerMotDePasse;
    private int tentativesEchouees;
    private LocalDateTime verrouJusquA;
    private LocalDateTime dateDerniereConnexion;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String sexe;
    private LocalDate dateNaissance;
    private String telephone;
    private String adresse;

    public Utilisateur() {
    }

    public Utilisateur(Long id, String nom, String prenoms, String email, String role) {
        this.id = id;
        this.nom = nom;
        this.prenoms = prenoms;
        this.email = email;
        this.role = role;
        this.statut = Constantes.STATUT_ACTIF;
    }

    /* ---------- Accesseurs ---------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isDoitChangerMotDePasse() {
        return doitChangerMotDePasse;
    }

    public void setDoitChangerMotDePasse(boolean doitChangerMotDePasse) {
        this.doitChangerMotDePasse = doitChangerMotDePasse;
    }

    public int getTentativesEchouees() {
        return tentativesEchouees;
    }

    public void setTentativesEchouees(int tentativesEchouees) {
        this.tentativesEchouees = tentativesEchouees;
    }

    public LocalDateTime getVerrouJusquA() {
        return verrouJusquA;
    }

    public void setVerrouJusquA(LocalDateTime verrouJusquA) {
        this.verrouJusquA = verrouJusquA;
    }

    public LocalDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
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

    /* ---------- Méthodes métier ---------- */

    /** Nom complet affichable. */
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

    /** Initiale pour l'avatar de la topbar. */
    public String getInitiale() {
        String source = (prenoms != null && !prenoms.isEmpty()) ? prenoms : nom;
        return (source == null || source.isEmpty()) ? "?" : source.substring(0, 1).toUpperCase();
    }

    /** Libellé affichable du rôle. */
    public String getLibelleRole() {
        return Constantes.libelleRole(role);
    }

    /** Page d'espace correspondant au rôle (redirection après connexion). */
    public String getLienEspace() {
        return Constantes.pageEspace(role);
    }

    /** Vrai si le compte est du personnel (non patient). */
    public boolean estInterne() {
        return Constantes.estInterne(role);
    }

    /** Vrai si le compte est actif et non verrouillé. */
    public boolean estActif() {
        return Constantes.STATUT_ACTIF.equals(statut);
    }

    /** Vrai si le compte est temporairement verrouillé (trop de tentatives). */
    public boolean estVerrouille() {
        return verrouJusquA != null && verrouJusquA.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * Vérifie le mot de passe en clair contre le hash stocké (BCrypt).
     * Comparaison à temps constant.
     */
    public boolean verifierMotDePasse(String motDePasseClair) {
        return com.mycompany.projetmedical.util.Hashage.verifier(motDePasseClair, motDePasseHash);
    }
}
