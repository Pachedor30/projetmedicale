package com.mycompany.projetmedical.util;

/**
 * Constantes de l'application : rôles, libellés, mapping rôle -> espace.
 * Source unique de vérité pour les valeurs de la colonne utilisateurs.role_code.
 */
public final class Constantes {

    private Constantes() {
    }

    /* ---------- Rôles (conforme cahier des charges §4) ---------- */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ACCUEIL = "ACCUEIL";
    public static final String ROLE_MEDECIN = "MEDECIN";
    public static final String ROLE_LABORANTIN = "LABORANTIN";
    public static final String ROLE_CAISSE = "CAISSIER_COMPTABLE";
    public static final String ROLE_PATIENT = "PATIENT";

    public static final String[] TOUS_ROLES = {
        ROLE_ADMIN, ROLE_ACCUEIL, ROLE_MEDECIN, ROLE_LABORANTIN, ROLE_CAISSE, ROLE_PATIENT
    };

    /** Rôle imposé à toute inscription publique (Règle fondamentale). */
    public static final String ROLE_INSCRIPTION_PUBLIQUE = ROLE_PATIENT;

    /* ---------- Statuts ---------- */
    public static final String STATUT_ACTIF = "ACTIF";
    public static final String STATUT_INACTIF = "INACTIF";
    public static final String STATUT_VERROUILLE = "VERROUILLE";

    /* ---------- JNDI ---------- */
    public static final String JNDI_DATASOURCE = "java:jboss/datasources/CarnetsDS";

    /* ---------- Attributs de session ---------- */
    public static final String SESSION_UTILISATEUR = "utilisateur";
    public static final String SESSION_CSRF = "jetonCsrf";

    /* ---------- Tentatives de connexion ---------- */
    public static final int TENTATIVES_MAX = 5;
    public static final int DUREE_VERROUILLAGE_MINUTES = 15;

    /* ---------- Pagination ---------- */
    public static final int PAR_DEFAUT_PAR_PAGE = 10;

    private static final String[] PAGES = {
        "dashboard.jsp", "espace-accueil.jsp", "espace-medecin.jsp",
        "espace-labo.jsp", "espace-caisse.jsp", "espace-patient.jsp"
    };

    private static final String[] LIBELLES = {
        "Administrateur", "Agent d'accueil", "Médecin",
        "Laborantin", "Caissier / Comptable", "Patient"
    };

    /** Libellé affichable d'un rôle. */
    public static String libelleRole(String role) {
        for (int i = 0; i < TOUS_ROLES.length; i++) {
            if (TOUS_ROLES[i].equals(role)) {
                return LIBELLES[i];
            }
        }
        return role == null ? "" : role;
    }

    /** Page d'espace correspondant au rôle (redirection après connexion). */
    public static String pageEspace(String role) {
        for (int i = 0; i < TOUS_ROLES.length; i++) {
            if (TOUS_ROLES[i].equals(role)) {
                return PAGES[i];
            }
        }
        return "connexion.jsp";
    }

    /** Vrai si le rôle correspond à un utilisateur interne (personnel). */
    public static boolean estInterne(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_ACCUEIL.equals(role)
                || ROLE_MEDECIN.equals(role) || ROLE_LABORANTIN.equals(role)
                || ROLE_CAISSE.equals(role);
    }

    /** Vrai si le rôle est valide (appartient à la liste officielle). */
    public static boolean roleValide(String role) {
        for (String r : TOUS_ROLES) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
