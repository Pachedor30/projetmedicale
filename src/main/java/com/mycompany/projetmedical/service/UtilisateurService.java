package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.UtilisateurDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import com.mycompany.projetmedical.util.Hashage;
import com.mycompany.projetmedical.util.Journal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Règles métier de gestion des comptes (§3 + §5 du cahier des charges).
 *
 * RÈGLE FONDAMENTALE : un compte de personnel ne peut être créé
 * QUE par un administrateur. Ce service ne propose aucune méthode
 * d'auto-inscription pour un rôle interne.
 */
public class UtilisateurService {

    private final UtilisateurDao utilisateurs = new UtilisateurDao();

    /** Erreurs de validation, message affichable. */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    /** Liste des comptes selon les filtres (null = aucun filtre). */
    public List<Utilisateur> lister(String role, String statut, String recherche) throws SQLException {
        return utilisateurs.lister(role, statut, recherche);
    }

    public Utilisateur parId(long id) throws SQLException {
        return utilisateurs.parId(id);
    }

    public Utilisateur parEmail(String email) throws SQLException {
        return utilisateurs.parEmail(email);
    }

    /**
     * Crée un compte du personnel (appelé exclusivement par l'espace ADMIN).
     *
     * Le mot de passe initial est choisi par l'administrateur et doit être changé
     * à la première connexion.
     */
    public void creerComptePro(Utilisateur u, String motDePasseInitial, Long creePar,
                               String ip) throws SQLException, ValidationException {

        valider(u, null, true);

        if (!Constantes.estInterne(u.getRole())) {
            throw new ValidationException(
                    "Un compte professionnel ne peut pas porter le rôle « "
                            + Constantes.libelleRole(u.getRole()) + " ».");
        }
        if (motDePasseInitial == null || motDePasseInitial.length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères.");
        }

        u.setMotDePasseHash(Hashage.hacher(motDePasseInitial));
        u.setStatut(Constantes.STATUT_ACTIF);
        u.setDoitChangerMotDePasse(true);

        // L'auteur (ADMIN) est porté par l'id pour la colonne cree_par
        Utilisateur auteur = new Utilisateur();
        auteur.setId(creePar);
        long id = utilisateurs.creer(u);
        u.setId(id);

        Journal.ecrire(creePar, Journal.CREATION_COMPTE, "utilisateur", id,
                "Création du compte " + u.getEmail() + " (rôle " + u.getRole() + ")", true, ip);
    }

    /**
     * Crée un compte patient (portail public, §2).
     * Le rôle est FORCÉ à PATIENT : aucun paramètre ne peut le changer.
     */
    public Utilisateur creerComptePatient(String nom, String prenoms, String email,
                                          String motDePasse, String dateNaissance,
                                          String sexe, String telephone, String adresse,
                                          String ip)
            throws SQLException, ValidationException {

        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenoms(prenoms);
        u.setEmail(Echappement.email(email));
        u.setSexe(sexe);
        u.setTelephone(telephone);
        u.setAdresse(adresse);
        // RÔLE IMPOSÉ : portail public = patients uniquement
        u.setRole(Constantes.ROLE_INSCRIPTION_PUBLIQUE);
        u.setStatut(Constantes.STATUT_ACTIF);
        u.setDoitChangerMotDePasse(false);

        if (dateNaissance != null && !dateNaissance.isEmpty()) {
            try {
                u.setDateNaissance(java.time.LocalDate.parse(dateNaissance));
            } catch (java.time.format.DateTimeParseException e) {
                throw new ValidationException("La date de naissance est invalide.");
            }
        }

        valider(u, null, false);

        if (motDePasse == null || motDePasse.length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères.");
        }

        u.setMotDePasseHash(Hashage.hacher(motDePasse));
        long id = utilisateurs.creer(u);
        u.setId(id);

        // Le patient est créé mais NON rattaché à un dossier : vérification d'identité requise
        creerLignePatient(id, ip);

        Journal.ecrire(id, Journal.CREATION_COMPTE, "utilisateur", id,
                "Inscription publique d'un patient", true, ip);
        return u;
    }

    /** Ouvre la ligne « patients » en attente de vérification d'identité. */
    private void creerLignePatient(long utilisateurId, String ip) throws SQLException {
        String sql = "INSERT INTO patients (utilisateur_id, numero_dossier, rattachement_statut) "
                + "VALUES (?, ?, ?)";
        try (java.sql.Connection c = com.mycompany.projetmedical.util.Bdd.connexion();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            ps.setString(2, genererNumeroDossier());
            ps.setString(3, com.mycompany.projetmedical.model.Patient.RATTACHEMENT_EN_ATTENTE);
            ps.executeUpdate();
        }
        Journal.ecrire(utilisateurId, Journal.CREATION_PATIENT, "patients", utilisateurId,
                "Dossier ouvert en attente de vérification d'identité", true, ip);
    }

    /** Numéro de dossier unique : PAT-AAAA-XXXXXX */
    private String genererNumeroDossier() throws SQLException {
        String prefixe = "PAT-" + java.time.Year.now().getValue() + "-";
        String sql = "SELECT coalesce(max(id), 0) + 1 FROM patients";
        try (java.sql.Connection c = com.mycompany.projetmedical.util.Bdd.connexion();
             java.sql.PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            rs.next();
            long n = rs.getLong(1);
            return String.format(Locale.ROOT, "%s%06d", prefixe, n);
        }
    }

    /** Modifie les informations d'un compte (rôle et service inclus, sauf soi-même). */
    public void modifier(Utilisateur u, Long auteurId, boolean estSoiMeme, String ip)
            throws SQLException, ValidationException {

        valider(u, u.getId(), true);

        if (estSoiMeme && !u.getRole().equals(
                utilisateurs.parId(auteurId).getRole())) {
            throw new ValidationException("Impossible de modifier votre propre rôle.");
        }

        utilisateurs.modifier(u);
        Journal.ecrire(auteurId, Journal.MODIFICATION_COMPTE, "utilisateur", u.getId(),
                "Modification du compte " + u.getEmail(), true, ip);
    }

    /** Active ou désactive un compte. */
    public void changerStatut(long id, String statut, Long auteurId, String ip)
            throws SQLException, ValidationException {

        if (!Constantes.STATUT_ACTIF.equals(statut)
                && !Constantes.STATUT_INACTIF.equals(statut)) {
            throw new ValidationException("Statut invalide.");
        }
        if (id == auteurId && Constantes.STATUT_INACTIF.equals(statut)) {
            throw new ValidationException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        utilisateurs.changerStatut(id, statut);
        Journal.ecrire(auteurId, Journal.CHANGEMENT_STATUT, "utilisateur", id,
                "Compte " + (Constantes.STATUT_ACTIF.equals(statut) ? "activé" : "désactivé"),
                true, ip);
    }

    /** Réinitialise l'accès : nouveau mot de passe provisoire + changement obligatoire. */
    public String reinitialiserAcces(long id, Long auteurId, String ip)
            throws SQLException, ValidationException {

        Utilisateur u = utilisateurs.parId(id);
        if (u == null) {
            throw new ValidationException("Compte introuvable.");
        }
        String provisoire = genererProvisoire();
        utilisateurs.changerMotDePasse(id, Hashage.hacher(provisoire), true);
        Journal.ecrire(auteurId, Journal.REINITIALISATION_MDP, "utilisateur", id,
                "Réinitialisation de l'accès de " + u.getEmail(), true, ip);
        return provisoire;
    }

    /** Change le mot de passe de l'utilisateur connecté (après saisie de l'ancien). */
    public void changerMotDePasse(long id, String ancien, String nouveau, String ip)
            throws SQLException, ValidationException {

        Utilisateur u = utilisateurs.parId(id);
        if (u == null) {
            throw new ValidationException("Compte introuvable.");
        }
        if (!u.verifierMotDePasse(ancien)) {
            throw new ValidationException("L'ancien mot de passe est incorrect.");
        }
        if (nouveau == null || nouveau.length() < 8) {
            throw new ValidationException("Le nouveau mot de passe doit contenir au moins 8 caractères.");
        }
        utilisateurs.changerMotDePasse(id, Hashage.hacher(nouveau), false);
        Journal.ecrire(id, Journal.CHANGEMENT_MDP, "utilisateur", id,
                "Changement de mot de passe", true, ip);
    }

    /** Mot de passe provisoire alphanumérique de 12 caractères. */
    public static String genererProvisoire() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(12);
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    /** Nombre de comptes (statistiques du tableau de bord). */
    public long combien(String role) throws SQLException {
        return utilisateurs.combien(role);
    }

    /* ------------------------------------------------------------------ */
    /* Validation commune                                                  */
    /* ------------------------------------------------------------------ */

    private void valider(Utilisateur u, Long saufId, boolean exigerRole)
            throws SQLException, ValidationException {

        List<String> erreurs = new ArrayList<>();

        if (u.getNom() == null || u.getNom().trim().isEmpty()) {
            erreurs.add("Le nom est obligatoire.");
        }
        if (u.getPrenoms() == null || u.getPrenoms().trim().isEmpty()) {
            erreurs.add("Le ou les prénoms sont obligatoires.");
        }
        if (u.getEmail() == null || !Echappement.emailValide(u.getEmail())) {
            erreurs.add("L'adresse e-mail est invalide.");
        } else if (utilisateurs.emailDejaUtilise(u.getEmail(), saufId == null ? -1 : saufId)) {
            erreurs.add("Un compte utilise déjà cette adresse e-mail.");
        }
        if (exigerRole && (u.getRole() == null || !Constantes.roleValide(u.getRole()))) {
            erreurs.add("Le rôle sélectionné est invalide.");
        }
        if (u.getSexe() != null && !u.getSexe().isEmpty()
                && !"M".equals(u.getSexe()) && !"F".equals(u.getSexe())) {
            erreurs.add("Le sexe doit être M ou F.");
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException(String.join(" ", erreurs));
        }
    }
}
