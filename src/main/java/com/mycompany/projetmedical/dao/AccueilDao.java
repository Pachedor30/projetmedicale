package com.mycompany.projetmedical.dao;

import com.mycompany.projetmedical.util.Bdd;
import com.mycompany.projetmedical.util.Journal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Accès JDBC limité aux données administratives utiles à l'accueil. */
public class AccueilDao {

    public record Indicateurs(long patientsTotal, long patientsAujourdhui, long rendezVousAujourdhui) { }
    public record PatientResume(long id, String numeroDossier, String prenoms, String nom,
                                String email, String telephone, String statutRattachement,
                                boolean dossierOuvert) { }
    public record PatientEdition(long id, String numeroDossier, String nom, String prenoms,
                                 String email, String telephone, String adresse, String sexe,
                                 LocalDate dateNaissance, String profession,
                                 String contactUrgence, String telephoneUrgence,
                                 String statutRattachement, boolean dossierOuvert) { }
    public record PatientCree(String numeroDossier) { }
    public record MedecinOption(long id, String nomComplet, String service) { }
    public record PatientOption(long id, String numero, String nomComplet) { }
    public record RendezVousLigne(long id, String numeroPatient, String nomPatient,
                                  long medecinId, String nomMedecin, String service,
                                  LocalDate date, LocalTime debut, LocalTime fin, String statut) { }
    public record RendezVousEdition(long id,long patientId,long medecinId,LocalDate date,LocalTime debut,LocalTime fin) { }

    private static final String PATIENTS_SELECT = "SELECT p.utilisateur_id, p.numero_dossier, "
            + "u.prenoms, u.nom, u.email, u.telephone, p.rattachement_statut, "
            + "EXISTS (SELECT 1 FROM dossiers_medicaux d WHERE d.patient_id = p.utilisateur_id) AS dossier_ouvert "
            + "FROM patients p JOIN utilisateurs u ON u.id = p.utilisateur_id ";

    public Indicateurs indicateurs() throws SQLException {
        String sql = "SELECT (SELECT count(*) FROM patients) AS patients_total, "
                + "(SELECT count(*) FROM patients WHERE date_creation::date = CURRENT_DATE) AS patients_jour, "
                + "(SELECT count(*) FROM rendez_vous WHERE date_rdv = CURRENT_DATE AND statut <> 'ANNULE') AS rdv_jour";
        try (Connection c = Bdd.connexion(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new Indicateurs(rs.getLong("patients_total"), rs.getLong("patients_jour"), rs.getLong("rdv_jour"));
        }
    }

    public List<PatientResume> rechercherPatients(String recherche) throws SQLException {
        StringBuilder sql = new StringBuilder(PATIENTS_SELECT);
        List<String> parametres = new ArrayList<>();
        if (recherche != null && !recherche.isBlank()) {
            sql.append("WHERE p.numero_dossier ILIKE ? OR u.nom ILIKE ? OR u.prenoms ILIKE ? OR coalesce(u.telephone,'') ILIKE ? ");
            String q = "%" + recherche.trim() + "%";
            parametres.add(q); parametres.add(q); parametres.add(q); parametres.add(q);
        }
        sql.append("ORDER BY u.nom, u.prenoms LIMIT 100");
        try (Connection c = Bdd.connexion(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametres.size(); i++) ps.setString(i + 1, parametres.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<PatientResume> resultat = new ArrayList<>();
                while (rs.next()) resultat.add(new PatientResume(rs.getLong("utilisateur_id"),
                        rs.getString("numero_dossier"), rs.getString("prenoms"), rs.getString("nom"),
                        rs.getString("email"), rs.getString("telephone"), rs.getString("rattachement_statut"),
                        rs.getBoolean("dossier_ouvert")));
                return resultat;
            }
        }
    }

    public PatientEdition patientParId(long id) throws SQLException {
        String sql = "SELECT p.utilisateur_id, p.numero_dossier, u.nom, u.prenoms, u.email, u.telephone, "
                + "u.adresse, u.sexe, u.date_naissance, p.profession, p.contact_urgence, p.telephone_urgence, "
                + "p.rattachement_statut, EXISTS (SELECT 1 FROM dossiers_medicaux d WHERE d.patient_id=p.utilisateur_id) AS dossier_ouvert "
                + "FROM patients p JOIN utilisateurs u ON u.id=p.utilisateur_id WHERE p.utilisateur_id=?";
        try (Connection c = Bdd.connexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Date naissance = rs.getDate("date_naissance");
                return new PatientEdition(rs.getLong("utilisateur_id"), rs.getString("numero_dossier"),
                        rs.getString("nom"), rs.getString("prenoms"), rs.getString("email"),
                        rs.getString("telephone"), rs.getString("adresse"), rs.getString("sexe"),
                        naissance == null ? null : naissance.toLocalDate(), rs.getString("profession"),
                        rs.getString("contact_urgence"), rs.getString("telephone_urgence"),
                        rs.getString("rattachement_statut"), rs.getBoolean("dossier_ouvert"));
            }
        }
    }

    public PatientCree creerPatient(String nom, String prenoms, String email, String hash,
            String sexe, LocalDate naissance, String telephone, String adresse,
            String profession, String contactUrgence, String telephoneUrgence,
            long agentId, String ip) throws SQLException {
        try (Connection c = Bdd.connexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement check = c.prepareStatement("SELECT 1 FROM roles WHERE code='PATIENT'")) {
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Le rôle PATIENT doit être initialisé dans la base.", "23503");
                    }
                }
                long id;
                String insertUser = "INSERT INTO utilisateurs (nom, prenoms, email, mot_de_passe_hash, role_code, statut, "
                        + "doit_changer_mot_de_passe, sexe, date_naissance, telephone, adresse, cree_par) "
                        + "VALUES (?, ?, ?, ?, 'PATIENT', 'ACTIF', TRUE, ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = c.prepareStatement(insertUser)) {
                    ps.setString(1, nom); ps.setString(2, prenoms); ps.setString(3, email); ps.setString(4, hash);
                    ps.setString(5, sexe);
                    if (naissance == null) ps.setNull(6, java.sql.Types.DATE); else ps.setDate(6, Date.valueOf(naissance));
                    ps.setString(7, telephone); ps.setString(8, adresse); ps.setLong(9, agentId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); id = rs.getLong(1); }
                }
                String numero = numeroDossier();
                String insertPatient = "INSERT INTO patients (utilisateur_id, numero_dossier, rattachement_statut, verifie_par, verifie_le, profession, contact_urgence, telephone_urgence) "
                        + "VALUES (?, ?, 'VERIFIE', ?, now(), ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(insertPatient)) {
                    ps.setLong(1, id); ps.setString(2, numero); ps.setLong(3, agentId);
                    ps.setString(4, profession); ps.setString(5, contactUrgence); ps.setString(6, telephoneUrgence);
                    ps.executeUpdate();
                }
                Journal.ecrire(c, agentId, Journal.CREATION_PATIENT, "patients", id,
                        "Enregistrement administratif du patient " + numero, true, ip);
                c.commit();
                return new PatientCree(numero);
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private String numeroDossier() {
        return "PAT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void modifierPatient(long id, String nom, String prenoms, String email, String sexe,
            LocalDate naissance, String telephone, String adresse, String profession,
            String contactUrgence, String telephoneUrgence, long agentId, String ip) throws SQLException {
        try (Connection c = Bdd.connexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE utilisateurs SET nom=?, prenoms=?, email=?, sexe=?, date_naissance=?, telephone=?, adresse=?, date_modification=now() WHERE id=? AND role_code='PATIENT'")) {
                    ps.setString(1, nom); ps.setString(2, prenoms); ps.setString(3, email); ps.setString(4, sexe);
                    if (naissance == null) ps.setNull(5, java.sql.Types.DATE); else ps.setDate(5, Date.valueOf(naissance));
                    ps.setString(6, telephone); ps.setString(7, adresse); ps.setLong(8, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Patient introuvable.", "02000");
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE patients SET profession=?, contact_urgence=?, telephone_urgence=? WHERE utilisateur_id=?")) {
                    ps.setString(1, profession); ps.setString(2, contactUrgence); ps.setString(3, telephoneUrgence); ps.setLong(4, id); ps.executeUpdate();
                }
                Journal.ecrire(c, agentId, Journal.MODIFICATION_COMPTE, "patients", id, "Modification des informations administratives du patient", true, ip);
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Ouvre uniquement l'enveloppe du dossier ; aucun champ médical n'est lu ni modifié. */
    public boolean ouvrirDossier(long patientId, long agentId, String ip) throws SQLException {
        try (Connection c = Bdd.connexion()) {
            c.setAutoCommit(false);
            try {
                String rattachement;
                try (PreparedStatement ps = c.prepareStatement("SELECT rattachement_statut FROM patients WHERE utilisateur_id=? FOR UPDATE")) {
                    ps.setLong(1, patientId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Patient introuvable.", "02000");
                        rattachement = rs.getString(1);
                    }
                }
                if (!"VERIFIE".equals(rattachement)) throw new SQLException("L'identité du patient doit être vérifiée avant l'ouverture du dossier.", "23514");
                int cree;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO dossiers_medicaux (patient_id, modifie_par) VALUES (?, ?) ON CONFLICT (patient_id) DO NOTHING")) {
                    ps.setLong(1, patientId); ps.setLong(2, agentId); cree = ps.executeUpdate();
                }
                if (cree > 0) Journal.ecrire(c, agentId, Journal.RATTACHEMENT_DOSSIER, "dossiers_medicaux", patientId,
                        "Ouverture du dossier administratif, sans accès aux données médicales", true, ip);
                c.commit();
                return cree > 0;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<MedecinOption> medecins() throws SQLException {
        String sql = "SELECT u.id, u.prenoms, u.nom, s.nom AS service FROM utilisateurs u "
                + "LEFT JOIN services s ON s.id=u.service_id WHERE u.role_code='MEDECIN' AND u.statut='ACTIF' ORDER BY u.nom,u.prenoms";
        try (Connection c = Bdd.connexion(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<MedecinOption> resultat = new ArrayList<>();
            while (rs.next()) resultat.add(new MedecinOption(rs.getLong("id"), rs.getString("prenoms") + " " + rs.getString("nom"), rs.getString("service")));
            return resultat;
        }
    }

    public List<PatientOption> patientsVerifies() throws SQLException {
        String sql="SELECT p.utilisateur_id,p.numero_dossier,u.prenoms,u.nom FROM patients p JOIN utilisateurs u ON u.id=p.utilisateur_id WHERE p.rattachement_statut='VERIFIE' ORDER BY u.nom,u.prenoms LIMIT 500";
        try(Connection c=Bdd.connexion();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            List<PatientOption> out=new ArrayList<>();while(rs.next())out.add(new PatientOption(rs.getLong(1),rs.getString(2),rs.getString(3)+" "+rs.getString(4)));return out;
        }
    }

    public List<RendezVousLigne> rendezVous(LocalDate du, LocalDate au, Long medecinId, String statut) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT r.id,r.medecin_id,r.date_rdv,r.heure_debut,r.heure_fin,r.statut,p.numero_dossier, "
                + "pu.prenoms AS patient_prenoms,pu.nom AS patient_nom,mu.prenoms AS med_prenoms,mu.nom AS med_nom,s.nom AS service "
                + "FROM rendez_vous r JOIN patients p ON p.utilisateur_id=r.patient_id JOIN utilisateurs pu ON pu.id=p.utilisateur_id "
                + "JOIN utilisateurs mu ON mu.id=r.medecin_id LEFT JOIN services s ON s.id=r.service_id WHERE 1=1 ");
        List<Object> valeurs = new ArrayList<>();
        if (du != null) { sql.append("AND r.date_rdv>=? "); valeurs.add(Date.valueOf(du)); }
        if (au != null) { sql.append("AND r.date_rdv<=? "); valeurs.add(Date.valueOf(au)); }
        if (medecinId != null) { sql.append("AND r.medecin_id=? "); valeurs.add(medecinId); }
        if (statut != null && !statut.isBlank()) { sql.append("AND r.statut=? "); valeurs.add(statut); }
        sql.append("ORDER BY r.date_rdv,r.heure_debut LIMIT 500");
        try (Connection c = Bdd.connexion(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<valeurs.size();i++) ps.setObject(i+1,valeurs.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<RendezVousLigne> resultat = new ArrayList<>();
                while (rs.next()) resultat.add(new RendezVousLigne(rs.getLong("id"), rs.getString("numero_dossier"),
                        rs.getString("patient_prenoms") + " " + rs.getString("patient_nom"), rs.getLong("medecin_id"),
                        rs.getString("med_prenoms") + " " + rs.getString("med_nom"), rs.getString("service"),
                        rs.getDate("date_rdv").toLocalDate(), rs.getTime("heure_debut").toLocalTime(),
                        rs.getTime("heure_fin").toLocalTime(), rs.getString("statut")));
                return resultat;
            }
        }
    }

    public RendezVousEdition rendezVousParId(long id)throws SQLException{
        String sql="SELECT id,patient_id,medecin_id,date_rdv,heure_debut,heure_fin FROM rendez_vous WHERE id=? AND statut IN ('EN_ATTENTE','CONFIRME')";
        try(Connection c=Bdd.connexion();PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())return null;return new RendezVousEdition(rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getDate(4).toLocalDate(),rs.getTime(5).toLocalTime(),rs.getTime(6).toLocalTime());}}
    }

    public long creerRendezVous(long patientId,long medecinId,LocalDate date,LocalTime debut,LocalTime fin,
            String motif,long agentId,String ip) throws SQLException {
        try (Connection c=Bdd.connexion()) {
            c.setAutoCommit(false);
            try {
                verifierDisponibilite(c,patientId,medecinId,date,debut,fin,null);
                String sql="INSERT INTO rendez_vous (patient_id,medecin_id,service_id,date_rdv,heure_debut,heure_fin,motif,statut,cree_par) "
                        + "SELECT ?,u.id,u.service_id,?,?,?,?,'EN_ATTENTE',? FROM utilisateurs u WHERE u.id=? AND u.role_code='MEDECIN' AND u.statut='ACTIF' RETURNING id";
                long id;
                try (PreparedStatement ps=c.prepareStatement(sql)) {
                    ps.setLong(1,patientId); ps.setDate(2,Date.valueOf(date)); ps.setTime(3,Time.valueOf(debut)); ps.setTime(4,Time.valueOf(fin));
                    ps.setString(5,motif); ps.setLong(6,agentId); ps.setLong(7,medecinId);
                    try(ResultSet rs=ps.executeQuery()){ if(!rs.next()) throw new SQLException("Médecin introuvable ou inactif.","02000"); id=rs.getLong(1); }
                }
                Journal.ecrire(c,agentId,Journal.CREATION_RDV,"rendez_vous",id,"Création d'un rendez-vous",true,ip);
                c.commit(); return id;
            } catch(SQLException | RuntimeException e){c.rollback();throw e;} finally {c.setAutoCommit(true);}
        }
    }

    public void modifierRendezVous(long id,long patientId,long medecinId,LocalDate date,LocalTime debut,LocalTime fin,
            String motif,long agentId,String ip) throws SQLException {
        try(Connection c=Bdd.connexion()){
            c.setAutoCommit(false);
            try{
                String statut;
                try(PreparedStatement ps=c.prepareStatement("SELECT statut FROM rendez_vous WHERE id=? FOR UPDATE")){
                    ps.setLong(1,id); try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Rendez-vous introuvable.","02000");statut=rs.getString(1);}
                }
                if(!"EN_ATTENTE".equals(statut)&&!"CONFIRME".equals(statut))throw new SQLException("Ce rendez-vous ne peut plus être modifié.","23514");
                verifierDisponibilite(c,patientId,medecinId,date,debut,fin,id);
                String sql="UPDATE rendez_vous r SET patient_id=?,medecin_id=?,service_id=u.service_id,date_rdv=?,heure_debut=?,heure_fin=?,motif=?,date_modification=now() "
                        + "FROM utilisateurs u,patients p WHERE r.id=? AND u.id=? AND u.role_code='MEDECIN' AND u.statut='ACTIF' AND p.utilisateur_id=? AND p.rattachement_statut='VERIFIE'";
                try(PreparedStatement ps=c.prepareStatement(sql)){
                    ps.setLong(1,patientId);ps.setLong(2,medecinId);ps.setDate(3,Date.valueOf(date));ps.setTime(4,Time.valueOf(debut));ps.setTime(5,Time.valueOf(fin));
                    ps.setString(6,motif);ps.setLong(7,id);ps.setLong(8,medecinId);ps.setLong(9,patientId);
                    if(ps.executeUpdate()!=1)throw new SQLException("Patient vérifié ou médecin actif introuvable.","02000");
                }
                Journal.ecrire(c,agentId,Journal.MODIFICATION_RDV,"rendez_vous",id,"Modification d'un rendez-vous",true,ip);
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    private void verifierDisponibilite(Connection c,long patientId,long medecinId,LocalDate date,LocalTime debut,LocalTime fin,Long saufId)throws SQLException{
        try(PreparedStatement patient=c.prepareStatement("SELECT 1 FROM patients WHERE utilisateur_id=? AND rattachement_statut='VERIFIE'")){
            patient.setLong(1,patientId);try(ResultSet rs=patient.executeQuery()){if(!rs.next())throw new SQLException("Patient introuvable ou identité non vérifiée.","02000");}
        }
        String sql="SELECT count(*) FROM rendez_vous WHERE statut<>'ANNULE' AND date_rdv=? AND (medecin_id=? OR patient_id=?) AND heure_debut<? AND heure_fin>?"+(saufId==null?"":" AND id<>?");
        try(PreparedStatement ps=c.prepareStatement(sql)){
            ps.setDate(1,Date.valueOf(date));ps.setLong(2,medecinId);ps.setLong(3,patientId);ps.setTime(4,Time.valueOf(fin));ps.setTime(5,Time.valueOf(debut));if(saufId!=null)ps.setLong(6,saufId);
            try(ResultSet rs=ps.executeQuery()){rs.next();if(rs.getLong(1)>0)throw new SQLException("Le patient ou le médecin a déjà un rendez-vous sur ce créneau.","23P01");}
        }
    }

    public void confirmerRendezVous(long id,long agentId,String ip)throws SQLException{
        changerStatutRendezVous(id,"CONFIRME",agentId,ip);
    }
    public void annulerRendezVous(long id,String motif,long agentId,String ip)throws SQLException{
        try(Connection c=Bdd.connexion()){
            String sql="UPDATE rendez_vous SET statut='ANNULE',annule_par=?,motif_annulation=?,date_modification=now() WHERE id=? AND statut IN ('EN_ATTENTE','CONFIRME') AND date_rdv>=CURRENT_DATE";
            try(PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,agentId);ps.setString(2,motif);ps.setLong(3,id);if(ps.executeUpdate()!=1)throw new SQLException("Ce rendez-vous n'est plus annulable.","23514");}
            Journal.ecrire(agentId,Journal.ANNULATION_RDV,"rendez_vous",id,"Annulation d'un rendez-vous",true,ip);
        }
    }
    private void changerStatutRendezVous(long id,String statut,long agentId,String ip)throws SQLException{
        try(Connection c=Bdd.connexion()){
            try(PreparedStatement ps=c.prepareStatement("UPDATE rendez_vous SET statut=?,date_modification=now() WHERE id=? AND statut='EN_ATTENTE' AND date_rdv>=CURRENT_DATE")){
                ps.setString(1,statut);ps.setLong(2,id);if(ps.executeUpdate()!=1)throw new SQLException("Seul un rendez-vous en attente et à venir peut être confirmé.","23514");
            }
            Journal.ecrire(agentId,Journal.MODIFICATION_RDV,"rendez_vous",id,"Confirmation d'un rendez-vous",true,ip);
        }
    }
}
