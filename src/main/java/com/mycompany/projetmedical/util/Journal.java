package com.mycompany.projetmedical.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Journalisation des opérations sensibles (§17 du cahier des charges).
 * Chaque création, modification, désactivation ou tentative de connexion
 * est tracée dans la table journal_activites.
 *
 * Les échecs de journalisation n'interrompent jamais l'opération métier :
 * on logge l'erreur mais on continue.
 */
public final class Journal {

    /* Actions tracées */
    public static final String CONNEXION = "CONNEXION";
    public static final String CONNEXION_ECHEC = "CONNEXION_ECHEC";
    public static final String DECONNEXION = "DECONNEXION";
    public static final String CREATION_COMPTE = "CREATION_COMPTE";
    public static final String MODIFICATION_COMPTE = "MODIFICATION_COMPTE";
    public static final String CHANGEMENT_STATUT = "CHANGEMENT_STATUT";
    public static final String REINITIALISATION_MDP = "REINITIALISATION_MDP";
    public static final String CHANGEMENT_MDP = "CHANGEMENT_MDP";
    public static final String CREATION_PATIENT = "CREATION_PATIENT";
    public static final String RATTACHEMENT_DOSSIER = "RATTACHEMENT_DOSSIER";
    public static final String CREATION_RDV = "CREATION_RDV";
    public static final String MODIFICATION_RDV = "MODIFICATION_RDV";
    public static final String ANNULATION_RDV = "ANNULATION_RDV";
    public static final String CREATION_CONSULTATION = "CREATION_CONSULTATION";
    public static final String MODIFICATION_CONSULTATION = "MODIFICATION_CONSULTATION";
    public static final String ENREGISTREMENT_CONSTANTES = "ENREGISTREMENT_CONSTANTES";
    public static final String CREATION_PRESCRIPTION = "CREATION_PRESCRIPTION";
    public static final String DEMANDE_ANALYSE = "DEMANDE_ANALYSE";
    public static final String PRISE_EN_CHARGE_ANALYSE = "PRISE_EN_CHARGE_ANALYSE";
    public static final String TRANSMISSION_RESULTAT_ANALYSE = "TRANSMISSION_RESULTAT_ANALYSE";
    public static final String VALIDATION_RESULTAT = "VALIDATION_RESULTAT";
    public static final String CREATION_FACTURE = "CREATION_FACTURE";
    public static final String ENCAISSEMENT = "ENCAISSEMENT";
    public static final String ACCES_REFUSE = "ACCES_REFUSE";

    private Journal() {
    }

    /**
     * Écrit une entrée de journal.
     *
     * @param connexion connexion existante (le caller reste responsable du commit)
     * @param utilisateurId auteur (null si inconnu / anonyme)
     * @param action      constante d'action
     * @param entite      table ou module concerné
     * @param entiteId    identifiant de l'objet concerné (nullable)
     * @param details     description lisible
     * @param succes      vrai si l'opération a réussi
     * @param ip          adresse IP du client (nullable)
     */
    public static void ecrire(Connection connexion, Long utilisateurId, String action,
                              String entite, Long entiteId, String details,
                              boolean succes, String ip) {
        String sql = "INSERT INTO journal_activites "
                + "(utilisateur_id, action, entite, entite_id, details, succes, adresse_ip) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            if (utilisateurId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, utilisateurId);
            }
            ps.setString(2, action);
            ps.setString(3, entite);
            if (entiteId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, entiteId);
            }
            ps.setString(5, details);
            ps.setBoolean(6, succes);
            ps.setString(7, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Ne jamais faire échouer l'opération métier à cause du journal
            java.util.logging.Logger.getLogger(Journal.class.getName())
                    .warning("Échec de journalisation (" + action + ") : " + e.getMessage());
        }
    }

    /** Ouvre sa propre connexion pour journaliser (hors transaction métier). */
    public static void ecrire(Long utilisateurId, String action, String entite,
                              Long entiteId, String details, boolean succes, String ip) {
        try (Connection c = Bdd.connexion()) {
            ecrire(c, utilisateurId, action, entite, entiteId, details, succes, ip);
        } catch (SQLException e) {
            java.util.logging.Logger.getLogger(Journal.class.getName())
                    .warning("Échec de journalisation (" + action + ") : " + e.getMessage());
        }
    }
}
