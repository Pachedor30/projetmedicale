package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.MedecinDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MedecinService {
    private final MedecinDao dao=new MedecinDao();
    public MedecinDao.Stats stats(long med)throws SQLException{return dao.stats(med);}
    public List<MedecinDao.Consultation> consultationsEnCours(long med)throws SQLException{return dao.consultationsEnCours(med);}
    public List<MedecinDao.Rdv> rdv(long med,LocalDate du,LocalDate au)throws SQLException{return dao.rendezVous(med,du,au);}
    public List<MedecinDao.Patient> patients(long med,String q)throws SQLException{return dao.patients(med,q);}
    public MedecinDao.Patient patient(long med,long id)throws SQLException{return dao.patient(med,id);}
    public List<MedecinDao.Antecedent> antecedents(long med,long id)throws SQLException{return dao.antecedents(med,id);}
    public List<MedecinDao.Consultation> historique(long med,long id)throws SQLException{return dao.historique(med,id);}
    public List<MedecinDao.Constante> constantes(long med,long id)throws SQLException{return dao.constantes(med,id);}
    public List<MedecinDao.Prescription> prescriptions(long med,long id)throws SQLException{return dao.prescriptions(med,id);}
    public List<MedecinDao.Analyse> analyses(long med,long id)throws SQLException{return dao.analyses(med,id);}
    public List<MedecinDao.AnalyseType> typesAnalyse()throws SQLException{return dao.typesAnalyse();}
    public long consultationEnCours(long med,long id)throws SQLException{return dao.consultationEnCours(med,id);}
    public long demarrer(long med,long patient,Long rdv,String motif,String ip)throws SQLException{return dao.demarrer(med,patient,rdv,motif,ip);}
    public void enregistrer(long med,long id,String motif,String obs,String diagnostic,String conduite,String ip)throws SQLException{dao.enregistrer(med,id,motif,obs,diagnostic,conduite,ip);}
    public void constante(long med,long id,String tension,Integer pouls,BigDecimal temperature,BigDecimal poids,BigDecimal taille,Integer spo2,BigDecimal glycemie,String ip)throws SQLException{dao.constante(med,id,tension,pouls,temperature,poids,taille,spo2,glycemie,ip);}
    public void prescription(long med,long id,String notes,String medicament,String dosage,String posologie,String duree,String quantite,String instructions,String ip)throws SQLException{dao.prescription(med,id,notes,medicament,dosage,posologie,duree,quantite,instructions,ip);}
    public void analyse(long med,long id,int type,String priorite,String obs,String ip)throws SQLException{dao.demanderAnalyse(med,id,type,priorite,obs,ip);}
    public void finaliser(long med,long id,String ip)throws SQLException{dao.finaliser(med,id,ip);}
}
