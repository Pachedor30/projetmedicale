package com.mycompany.projetmedical.dao;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Bdd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données de la table « utilisateurs ».
 * 100 % PreparedStatement (anti-injection SQL).
 * Aucune règle métier ici : uniquement des requêtes.
 */
public class UtilisateurDao {

    private static final String COLONNES =
            "u.id, u.nom, u.prenoms, u.email, u.mot_de_passe_hash, u.role_code, "
            + "u.service_id, s.nom AS service_nom, u.statut, u.doit_changer_mot_de_passe, "
            + "u.tentatives_echouees, u.verrou_jusqu_a, u.date_derniere_connexion, "
            + "u.date_creation, u.date_modification, u.sexe, u.date_naissance, "
            + "u.telephone, u.adresse "
            + "FROM utilisateurs u LEFT JOIN services s ON s.id = u.service_id";

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenoms(rs.getString("prenoms"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setRole(rs.getString("role_code"));
        long serviceId = rs.getLong("service_id");
        u.setServiceId(rs.wasNull() ? null : serviceId);
        u.setNomService(rs.getString("service_nom"));
        u.setStatut(rs.getString("statut"));
        u.setDoitChangerMotDePasse(rs.getBoolean("doit_changer_mot_de_passe"));
        u.setTentativesEchouees(rs.getInt("tentatives_echouees"));
        Timestamp verrou = rs.getTimestamp("verrou_jusqu_a");
        u.setVerrouJusquA(verrou == null ? null : verrou.toLocalDateTime());
        Timestamp derniere = rs.getTimestamp("date_derniere_connexion");
        u.setDateDerniereConnexion(derniere == null ? null : derniere.toLocalDateTime());
        Timestamp creation = rs.getTimestamp("date_creation");
        u.setDateCreation(creation == null ? null : creation.toLocalDateTime());
        Timestamp modif = rs.getTimestamp("date_modification");
        u.setDateModification(modif == null ? null : modif.toLocalDateTime());
        u.setSexe(rs.getString("sexe"));
        u.setDateNaissance(rs.getDate("date_naissance") == null
                ? null : rs.getDate("date_naissance").toLocalDate());
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        return u;
    }

    /** Recherche un compte par email (insensible à la casse). */
    public Utilisateur parEmail(String email) throws SQLException {
        String sql = "SELECT " + COLONNES + " WHERE lower(u.email) = lower(?)";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    /** Recherche un compte par identifiant. */
    public Utilisateur parId(long id) throws SQLException {
        String sql = "SELECT " + COLONNES + " WHERE u.id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    /** Liste des comptes, avec filtres optionnels (null = pas de filtre). */
    public List<Utilisateur> lister(String role, String statut, String recherche) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLONNES + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (role != null && !role.isEmpty()) {
            sql.append(" AND u.role_code = ?");
            params.add(role);
        }
        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND u.statut = ?");
            params.add(statut);
        }
        if (recherche != null && !recherche.trim().isEmpty()) {
            sql.append(" AND (lower(u.nom) LIKE ? OR lower(u.prenoms) LIKE ? "
                    + "OR lower(u.email) LIKE ? OR u.telephone LIKE ?)");
            String motif = "%" + recherche.trim().toLowerCase() + "%";
            params.add(motif);
            params.add(motif);
            params.add(motif);
            params.add(motif);
        }
        sql.append(" ORDER BY u.nom, u.prenoms");

        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Utilisateur> liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(mapper(rs));
                }
                return liste;
            }
        }
    }

    /** Crée un compte et renvoie son identifiant. */
    public long creer(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateurs "
                + "(nom, prenoms, email, mot_de_passe_hash, role_code, service_id, statut, "
                + " doit_changer_mot_de_passe, sexe, date_naissance, telephone, adresse, cree_par) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenoms());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasseHash());
            ps.setString(5, u.getRole());
            if (u.getServiceId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setLong(6, u.getServiceId());
            }
            ps.setString(7, u.getStatut());
            ps.setBoolean(8, u.isDoitChangerMotDePasse());
            ps.setString(9, u.getSexe());
            if (u.getDateNaissance() == null) {
                ps.setNull(10, java.sql.Types.DATE);
            } else {
                ps.setDate(10, java.sql.Date.valueOf(u.getDateNaissance()));
            }
            ps.setString(11, u.getTelephone());
            ps.setString(12, u.getAdresse());
            if (u.getId() == null) {
                ps.setNull(13, java.sql.Types.BIGINT);
            } else {
                ps.setLong(13, u.getId());
            }
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                if (cles.next()) {
                    return cles.getLong(1);
                }
            }
            throw new SQLException("Identifiant du compte créé non retourné");
        }
    }

    /** Met à jour les informations civiles et professionnelles. */
    public void modifier(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateurs SET nom = ?, prenoms = ?, role_code = ?, "
                + "service_id = ?, sexe = ?, date_naissance = ?, telephone = ?, adresse = ?, "
                + "date_modification = ? WHERE id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenoms());
            ps.setString(3, u.getRole());
            if (u.getServiceId() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setLong(4, u.getServiceId());
            }
            ps.setString(5, u.getSexe());
            if (u.getDateNaissance() == null) {
                ps.setNull(6, java.sql.Types.DATE);
            } else {
                ps.setDate(6, java.sql.Date.valueOf(u.getDateNaissance()));
            }
            ps.setString(7, u.getTelephone());
            ps.setString(8, u.getAdresse());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(10, u.getId());
            ps.executeUpdate();
        }
    }

    /** Change le statut du compte (ACTIF / INACTIF / VERROUILLE). */
    public void changerStatut(long id, String statut) throws SQLException {
        String sql = "UPDATE utilisateurs SET statut = ?, date_modification = ? WHERE id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    /** Remplace le hash du mot de passe (provisoire ou définitif). */
    public void changerMotDePasse(long id, String hash, boolean doitChanger) throws SQLException {
        String sql = "UPDATE utilisateurs SET mot_de_passe_hash = ?, "
                + "doit_changer_mot_de_passe = ?, tentatives_echouees = 0, "
                + "verrou_jusqu_a = NULL, date_modification = ? WHERE id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setBoolean(2, doitChanger);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    /** Enregistre une tentative de connexion échouée et verrouille si nécessaire. */
    public void enregistrerEchec(long id, int tentatives, LocalDateTime verrou) throws SQLException {
        String sql = "UPDATE utilisateurs SET tentatives_echouees = ?, verrou_jusqu_a = ? "
                + "WHERE id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tentatives);
            if (verrou == null) {
                ps.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(verrou));
            }
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    /** Réinitialise le compteur de tentatives et marque la dernière connexion. */
    public void marquerConnexionReussie(long id) throws SQLException {
        String sql = "UPDATE utilisateurs SET tentatives_echouees = 0, verrou_jusqu_a = NULL, "
                + "date_derniere_connexion = ? WHERE id = ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /** Vrai si un autre compte que celui donné utilise déjà l'email. */
    public boolean emailDejaUtilise(String email, long saufId) throws SQLException {
        String sql = "SELECT count(*) FROM utilisateurs WHERE lower(email) = lower(?) AND id <> ?";
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setLong(2, saufId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Nombre de comptes (optionnellement filtré par rôle). */
    public long combien(String role) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM utilisateurs WHERE 1=1");
        if (role != null && !role.isEmpty()) {
            sql.append(" AND role_code = ?");
        }
        try (Connection c = Bdd.connexion();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            if (role != null && !role.isEmpty()) {
                ps.setString(1, role);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
