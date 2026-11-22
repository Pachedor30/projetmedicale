package com.mycompany.projetmedical.dao;

import com.mycompany.projetmedical.util.Bdd;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Lecture des rôles configurés dans PostgreSQL pour l'écran administrateur. */
public class AdminGestionDao {
    public record RoleClinique(String code, String libelle, String description, boolean interne) { }

    public List<RoleClinique> roles() throws SQLException {
        String sql = "SELECT code, libelle, description, est_interne FROM roles ORDER BY code";
        try (Connection c = Bdd.connexion(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            List<RoleClinique> rows = new ArrayList<>();
            while (r.next()) rows.add(new RoleClinique(r.getString(1), r.getString(2),
                    r.getString(3), r.getBoolean(4)));
            return rows;
        }
    }
}
