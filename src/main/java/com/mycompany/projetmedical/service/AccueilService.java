package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.AccueilDao;
import com.mycompany.projetmedical.util.Echappement;
import com.mycompany.projetmedical.util.Hashage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/** Règles métier de l'espace accueil. */
public class AccueilService {
    private final AccueilDao dao = new AccueilDao();
    public AccueilDao.Indicateurs indicateurs() throws SQLException { return dao.indicateurs(); }
    public java.util.List<AccueilDao.PatientResume> rechercherPatients(String q) throws SQLException { return dao.rechercherPatients(Echappement.texte(q)); }
    public AccueilDao.PatientEdition patient(long id) throws SQLException { return dao.patientParId(id); }
    public java.util.List<AccueilDao.MedecinOption> medecins() throws SQLException { return dao.medecins(); }
    public java.util.List<AccueilDao.PatientOption> patientsVerifies() throws SQLException { return dao.patientsVerifies(); }
    public AccueilDao.RendezVousEdition rendezVousParId(long id)throws SQLException{return dao.rendezVousParId(id);}
    public java.util.List<AccueilDao.RendezVousLigne> rendezVous(LocalDate du, LocalDate au, Long medecin, String statut) throws SQLException { return dao.rendezVous(du, au, medecin, statut); }
    public AccueilDao.PatientCree creerPatient(String nom,String prenoms,String email,String motDePasseInitial,String sexe,LocalDate naissance,String tel,String adresse,String profession,String contact,String telUrgence,long agent,String ip) throws SQLException {
        if (motDePasseInitial == null || motDePasseInitial.length() < 8 || motDePasseInitial.length() > 128) {
            throw new IllegalArgumentException("Le mot de passe doit contenir entre 8 et 128 caractères.");
        }
        return dao.creerPatient(nom,prenoms,email,Hashage.hacher(motDePasseInitial),sexe,naissance,tel,adresse,profession,contact,telUrgence,agent,ip);
    }
    public void modifierPatient(long id,String nom,String prenoms,String email,String sexe,LocalDate naissance,String tel,String adresse,String profession,String contact,String telUrgence,long agent,String ip) throws SQLException { dao.modifierPatient(id,nom,prenoms,email,sexe,naissance,tel,adresse,profession,contact,telUrgence,agent,ip); }
    public boolean ouvrirDossier(long patient,long agent,String ip) throws SQLException { return dao.ouvrirDossier(patient,agent,ip); }
    public long creerRendezVous(long patient,long medecin,LocalDate date,LocalTime debut,LocalTime fin,String motif,long agent,String ip) throws SQLException { return dao.creerRendezVous(patient,medecin,date,debut,fin,motif,agent,ip); }
    public void modifierRendezVous(long id,long patient,long medecin,LocalDate date,LocalTime debut,LocalTime fin,String motif,long agent,String ip) throws SQLException { dao.modifierRendezVous(id,patient,medecin,date,debut,fin,motif,agent,ip); }
    public void confirmer(long id,long agent,String ip) throws SQLException { dao.confirmerRendezVous(id,agent,ip); }
    public void annuler(long id,String motif,long agent,String ip) throws SQLException { dao.annulerRendezVous(id,motif,agent,ip); }
}
