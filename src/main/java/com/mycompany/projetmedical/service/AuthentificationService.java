package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.UtilisateurDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import com.mycompany.projetmedical.util.Journal;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Règles métier d'authentification (§4 du cahier des charges) :
 *  - contrôle du mot de passe (BCrypt, via le DAO)
 *  - gestion des tentatives échouées et du verrouillage temporaire
 *  - traçabilité des connexions dans le journal
 *
 * La vérification cryptographique réelle est déléguée à request.login()
 * côté servlet (realm Elytron JAAS/JDBC) ; ce service gère l'état du compte
 * et le journal avant/après.
 */
public class AuthentificationService {

    private final UtilisateurDao utilisateurs = new UtilisateurDao();

    /** Résultat d'une tentative de connexion. */
    public static class Resultat {
        private final boolean reussie;
        private final String codeErreur;
        private final Utilisateur utilisateur;

        private Resultat(boolean reussie, String codeErreur, Utilisateur utilisateur) {
            this.reussie = reussie;
            this.codeErreur = codeErreur;
            this.utilisateur = utilisateur;
        }

        static Resultat ok(Utilisateur u) {
            return new Resultat(true, null, u);
        }

        static Resultat echec(String code) {
            return new Resultat(false, code, null);
        }

        public boolean isReussie() {
            return reussie;
        }

        public String getCodeErreur() {
            return codeErreur;
        }

        public Utilisateur getUtilisateur() {
            return utilisateur;
        }
    }

    /**
     * Prépare et valide une tentative de connexion.
     *
     * Codes d'erreur renvoyés :
     *  - champs      : email ou mot de passe vide
     *  - verrouille  : trop de tentatives, réessayer plus tard
     *  - inactif     : compte désactivé par l'administrateur
     *  - identifiants: email inconnu OU mot de passe incorrect (même message)
     *  - provisoire  : mot de passe provisoire à changer (signalé séparément)
     */
    public Resultat verifierTentative(String emailSaisi, String motDePasse, String ip)
            throws SQLException {

        String email = Echappement.email(emailSaisi);
        if (email.isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            return Resultat.echec("champs");
        }

        Utilisateur u = utilisateurs.parEmail(email);

        // Email inconnu : même message que mot de passe faux (pas d'énumération de comptes)
        if (u == null) {
            Journal.ecrire(null, Journal.CONNEXION_ECHEC, "utilisateur", null,
                    "Tentative avec un email inconnu", false, ip);
            return Resultat.echec("identifiants");
        }

        if (u.estVerrouille()) {
            Journal.ecrire(u.getId(), Journal.CONNEXION_ECHEC, "utilisateur", u.getId(),
                    "Compte verrouillé (trop de tentatives)", false, ip);
            return Resultat.echec("verrouille");
        }

        if (!u.estActif()) {
            Journal.ecrire(u.getId(), Journal.CONNEXION_ECHEC, "utilisateur", u.getId(),
                    "Compte inactif", false, ip);
            return Resultat.echec("inactif");
        }

        // Mot de passe vérifié par le realm Elytron via request.login() dans le servlet.
        // Ici on ne fait que préparer le contexte (compteur de tentatives).
        return Resultat.ok(u);
    }

    /** Appelé après un échec de request.login() : incmente et verrouille si besoin. */
    public void enregistrerEchec(Utilisateur u, String ip) throws SQLException {
        int tentatives = u.getTentativesEchouees() + 1;
        LocalDateTime verrou = null;
        if (tentatives >= Constantes.TENTATIVES_MAX) {
            verrou = LocalDateTime.now().plusMinutes(Constantes.DUREE_VERROUILLAGE_MINUTES);
        }
        utilisateurs.enregistrerEchec(u.getId(), tentatives, verrou);
        Journal.ecrire(u.getId(), Journal.CONNEXION_ECHEC, "utilisateur", u.getId(),
                "Mot de passe incorrect (tentative " + tentatives + "/"
                        + Constantes.TENTATIVES_MAX + ")", false, ip);
    }

    /** Appelé après une connexion réussie : reset du compteur + journal. */
    public void enregistrerSucces(Utilisateur u, String ip) throws SQLException {
        utilisateurs.marquerConnexionReussie(u.getId());
        Journal.ecrire(u.getId(), Journal.CONNEXION, "utilisateur", u.getId(),
                "Connexion réussie (" + u.getLibelleRole() + ")", true, ip);
    }

    /** Journalise une déconnexion. */
    public void journaliserDeconnexion(Utilisateur u, String ip) {
        if (u != null) {
            Journal.ecrire(u.getId(), Journal.DECONNEXION, "utilisateur", u.getId(),
                    "Déconnexion", true, ip);
        }
    }
}
