package com.mycompany.projetmedical.dao;

import com.mycompany.projetmedical.util.Bdd;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Indicateurs réels utilisés sur l'accueil de l'espace administrateur. */
public class AdminDashboardDao {

    public Statistiques charger() throws SQLException {
        String sql = "SELECT "
                + "(SELECT count(*) FROM utilisateurs) AS utilisateurs, "
                + "(SELECT count(*) FROM utilisateurs WHERE statut = 'ACTIF') AS utilisateurs_actifs, "
                + "(SELECT count(*) FROM patients) AS patients, "
                + "(SELECT count(*) FROM rendez_vous WHERE date_rdv = CURRENT_DATE "
                + "AND statut <> 'ANNULE') AS rendez_vous_jour, "
                + "(SELECT count(*) FROM consultations WHERE date_consultation::date = CURRENT_DATE "
                + "AND statut <> 'ANNULEE') AS consultations_jour";

        try (Connection connexion = Bdd.connexion();
             PreparedStatement requete = connexion.prepareStatement(sql);
             ResultSet resultat = requete.executeQuery()) {
            if (!resultat.next()) {
                throw new SQLException("Les statistiques du tableau de bord sont indisponibles.");
            }
            return new Statistiques(
                    resultat.getLong("utilisateurs"),
                    resultat.getLong("utilisateurs_actifs"),
                    resultat.getLong("patients"),
                    resultat.getLong("rendez_vous_jour"),
                    resultat.getLong("consultations_jour"));
        }
    }

    public record Statistiques(long utilisateurs, long utilisateursActifs, long patients,
                               long rendezVousJour, long consultationsJour) { }
}
