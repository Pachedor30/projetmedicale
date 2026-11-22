package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.PatientPortailDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PatientPortailService {
    private final PatientPortailDao dao=new PatientPortailDao();
    public PatientPortailDao.Profil profil(int id)throws SQLException{return dao.profil(id);}
    public PatientPortailDao.Stats stats(int id)throws SQLException{return dao.stats(id);}
    public PatientPortailDao.Dossier dossier(int id)throws SQLException{return dao.dossier(id);}
    public List<PatientPortailDao.Antecedent> antecedents(int id)throws SQLException{return dao.antecedents(id);}
    public List<PatientPortailDao.Consultation> consultations(int id)throws SQLException{return dao.consultations(id);}
    public List<PatientPortailDao.Prescription> prescriptions(int id)throws SQLException{return dao.prescriptions(id);}
    public List<PatientPortailDao.Analyse> analysesValidees(int id)throws SQLException{return dao.analysesValidees(id);}
    public List<PatientPortailDao.Medecin> medecins()throws SQLException{return dao.medecins();}
    public List<PatientPortailDao.Rdv> rendezVous(int id)throws SQLException{return dao.rendezVous(id);}
    public void demanderRendezVous(int patient,long medecin,LocalDate date,LocalTime debut,LocalTime fin,String motif,String ip)throws SQLException{dao.demanderRendezVous(patient,medecin,date,debut,fin,motif,ip);}
    public void annulerRendezVous(int patient,long rdv,String ip)throws SQLException{dao.annulerRendezVous(patient,rdv,ip);}
    public void modifierProfil(int id,String nom,String prenoms,String email,String telephone,String adresse,String contact,String telUrgence,String ip)throws SQLException{dao.modifierProfil(id,nom,prenoms,email,telephone,adresse,contact,telUrgence,ip);}
    public List<PatientPortailDao.Facture> factures(int id)throws SQLException{return dao.factures(id);}
    public PatientPortailDao.Facture facture(int patient,long id)throws SQLException{return dao.facture(patient,id);}
    public List<PatientPortailDao.Paiement> paiements(int id)throws SQLException{return dao.paiements(id);}
    public PatientPortailDao.Paiement paiement(int patient,long id)throws SQLException{return dao.paiement(patient,id);}
    public List<PatientPortailDao.Notification> notifications(int id)throws SQLException{return dao.notifications(id);}
    public void lireNotification(int patient,long id)throws SQLException{dao.lireNotification(patient,id);}
}
