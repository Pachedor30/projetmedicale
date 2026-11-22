package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.CaisseDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CaisseService {
    private final CaisseDao dao=new CaisseDao();
    public CaisseDao.Stats stats()throws SQLException{return dao.stats();}
    public List<CaisseDao.Patient> patients(String q)throws SQLException{return dao.patients(q);}
    public CaisseDao.Patient patient(int id)throws SQLException{return dao.patient(id);}
    public List<CaisseDao.Facture> facturesPatient(int id)throws SQLException{return dao.facturesPatient(id);}
    public List<CaisseDao.Prestation> prestations()throws SQLException{return dao.prestations();}
    public List<CaisseDao.Facture> factures(String q,String statut,boolean impayees)throws SQLException{return dao.factures(q,statut,impayees);}
    public CaisseDao.Facture facture(long id)throws SQLException{return dao.facture(id);}
    public long creerFacture(int patient,List<Integer> prestations,List<BigDecimal> quantites,String notes,LocalDate echeance,long user,String ip)throws SQLException{return dao.creerFacture(patient,prestations,quantites,notes,echeance,user,ip);}
    public long payer(long facture,BigDecimal montant,String mode,String reference,String notes,long user,String ip)throws SQLException{return dao.payer(facture,montant,mode,reference,notes,user,ip);}
    public List<CaisseDao.Paiement> paiements(String q,LocalDate du,LocalDate au)throws SQLException{return dao.paiements(q,du,au);}
    public CaisseDao.Paiement paiement(long id)throws SQLException{return dao.paiement(id);}
    public List<CaisseDao.Paiement> paiementsFacture(long id)throws SQLException{return dao.paiementsFacture(id);}
    public List<CaisseDao.Rapport> rapport(LocalDate du,LocalDate au)throws SQLException{return dao.rapport(du,au);}
}
