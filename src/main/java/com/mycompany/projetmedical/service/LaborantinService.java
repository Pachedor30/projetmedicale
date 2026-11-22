package com.mycompany.projetmedical.service;

import com.mycompany.projetmedical.dao.LaborantinDao;
import java.sql.SQLException;
import java.util.List;

public class LaborantinService {
    private final LaborantinDao dao=new LaborantinDao();
    public LaborantinDao.Stats stats()throws SQLException{return dao.stats();}
    public List<LaborantinDao.Demande> demandes(String statut,String q)throws SQLException{return dao.demandes(statut,q);}
    public LaborantinDao.Demande demande(long id)throws SQLException{return dao.demande(id);}
    public void prendreEnCharge(long id,long user,String ip)throws SQLException{dao.prendreEnCharge(id,user,ip);}
    public void enregistrer(long id,long user,String valeur,String unite,String reference,String interpretation,String ip)throws SQLException{dao.enregistrer(id,user,valeur,unite,reference,interpretation,ip);}
}
