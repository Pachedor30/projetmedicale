package com.mycompany.projetmedical.dao;

import com.mycompany.projetmedical.util.Bdd;
import com.mycompany.projetmedical.util.Journal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Accès aux analyses et résultats autorisés pour le laboratoire. */
public class LaborantinDao {
    public record Stats(long nouvelles,long enCours,long aValider,long validees,long aujourdHui){}
    public record Demande(long id,long patientId,String numeroPatient,String patient,String type,String categorie,String unite,String references,String priorite,String statut,Timestamp date,String medecin,String observations,Long responsable,Long resultatId,String resultat,String uniteResultat,String referenceResultat,String interpretation,String statutResultat,Long saisiPar,Long validePar,Timestamp dateSaisie,Timestamp dateValidation){}
    public Stats stats()throws SQLException{
        String q="SELECT count(*) FILTER(WHERE statut='DEMANDEE'),count(*) FILTER(WHERE statut='EN_COURS'),count(*) FILTER(WHERE statut='SIS'),count(*) FILTER(WHERE statut='VALIDE'),count(*) FILTER(WHERE date_demande::date=CURRENT_DATE) FROM demandes_analyses WHERE statut<>'ANNULEE'";
        try(Connection c=Bdd.connexion();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){r.next();return new Stats(r.getLong(1),r.getLong(2),r.getLong(3),r.getLong(4),r.getLong(5));}
    }
    public List<Demande> demandes(String statut,String q)throws SQLException{
        String sql="SELECT d.id,d.patient_id,p.numero_dossier,pu.prenoms||' '||pu.nom patient,t.libelle,t.categorie,t.unite,t.valeurs_reference,d.priorite,d.statut,d.date_demande,mu.prenoms||' '||mu.nom medecin,d.observations_medecin,d.traite_par,r.id resultat_id,r.valeur,r.unite unite_resultat,r.valeurs_reference reference_resultat,r.observations,r.statut statut_resultat,r.saisi_par,r.valide_par,r.date_saisie,r.date_validation FROM demandes_analyses d JOIN types_analyses t ON t.id=d.type_analyse_id JOIN patients p ON p.utilisateur_id=d.patient_id JOIN utilisateurs pu ON pu.id=p.utilisateur_id JOIN utilisateurs mu ON mu.id=d.medecin_id LEFT JOIN resultats_analyses r ON r.demande_id=d.id WHERE (?='' OR d.statut=?) AND (?='' OR p.numero_dossier ILIKE ? OR pu.nom ILIKE ? OR pu.prenoms ILIKE ? OR t.libelle ILIKE ?) ORDER BY CASE d.priorite WHEN 'URGENTE' THEN 0 WHEN 'NORMALE' THEN 1 ELSE 2 END,d.date_demande LIMIT 500";
        String filter=statut==null?"":statut.trim(),needle=q==null?"":q.trim(),like="%"+needle+"%";
        try(Connection c=Bdd.connexion();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,filter);p.setString(2,filter);p.setString(3,needle);for(int i=4;i<=7;i++)p.setString(i,like);try(ResultSet r=p.executeQuery()){List<Demande>a=new ArrayList<>();while(r.next())a.add(map(r));return a;}}
    }
    public Demande demande(long id)throws SQLException{
        String sql="SELECT d.id,d.patient_id,p.numero_dossier,pu.prenoms||' '||pu.nom patient,t.libelle,t.categorie,t.unite,t.valeurs_reference,d.priorite,d.statut,d.date_demande,mu.prenoms||' '||mu.nom medecin,d.observations_medecin,d.traite_par,r.id resultat_id,r.valeur,r.unite unite_resultat,r.valeurs_reference reference_resultat,r.observations,r.statut statut_resultat,r.saisi_par,r.valide_par,r.date_saisie,r.date_validation FROM demandes_analyses d JOIN types_analyses t ON t.id=d.type_analyse_id JOIN patients p ON p.utilisateur_id=d.patient_id JOIN utilisateurs pu ON pu.id=p.utilisateur_id JOIN utilisateurs mu ON mu.id=d.medecin_id LEFT JOIN resultats_analyses r ON r.demande_id=d.id WHERE d.id=?";
        try(Connection c=Bdd.connexion();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}
    }
    private Demande map(ResultSet r)throws SQLException{return new Demande(r.getLong("id"),r.getLong("patient_id"),r.getString("numero_dossier"),r.getString("patient"),r.getString("libelle"),r.getString("categorie"),r.getString("unite"),r.getString("valeurs_reference"),r.getString("priorite"),r.getString("statut"),r.getTimestamp("date_demande"),r.getString("medecin"),r.getString("observations_medecin"),nullableLong(r,"traite_par"),nullableLong(r,"resultat_id"),r.getString("valeur"),r.getString("unite_resultat"),r.getString("reference_resultat"),r.getString("observations"),r.getString("statut_resultat"),nullableLong(r,"saisi_par"),nullableLong(r,"valide_par"),r.getTimestamp("date_saisie"),r.getTimestamp("date_validation"));}
    private Long nullableLong(ResultSet r,String col)throws SQLException{long n=r.getLong(col);return r.wasNull()?null:n;}
    public void prendreEnCharge(long id,long laborantin,String ip)throws SQLException{
        try(Connection c=Bdd.connexion()){
            c.setAutoCommit(false);
            try{try(PreparedStatement p=c.prepareStatement("UPDATE demandes_analyses SET statut='EN_COURS',traite_par=?,date_prise_en_charge=coalesce(date_prise_en_charge,now()) WHERE id=? AND statut='DEMANDEE' AND (traite_par IS NULL OR traite_par=?)")){p.setLong(1,laborantin);p.setLong(2,id);p.setLong(3,laborantin);if(p.executeUpdate()!=1)throw new SQLException("Demande déjà prise en charge, attribuée à un collègue ou fermée.","42501");}Journal.ecrire(c,laborantin,Journal.PRISE_EN_CHARGE_ANALYSE,"demandes_analyses",id,"Prélèvement reçu et demande prise en charge",true,ip);c.commit();}
            catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    public void enregistrer(long id,long laborantin,String valeur,String unite,String reference,String interpretation,String ip)throws SQLException{
        try(Connection c=Bdd.connexion()){c.setAutoCommit(false);try{
            try(PreparedStatement check=c.prepareStatement("SELECT d.statut,d.traite_par,r.statut resultat_statut,r.saisi_par FROM demandes_analyses d LEFT JOIN resultats_analyses r ON r.demande_id=d.id WHERE d.id=? FOR UPDATE OF d")){check.setLong(1,id);try(ResultSet r=check.executeQuery()){if(!r.next())throw new SQLException("Demande introuvable.","02000");String status=r.getString(1),resultStatus=r.getString(3);Long owner=nullableLong(r,"traite_par"),author=nullableLong(r,"saisi_par");if(!Objects.equals(owner,laborantin)||!Set.of("EN_COURS","SIS").contains(status))throw new SQLException("Demande non attribuée ou fermée.","42501");if("VALIDE".equals(resultStatus))throw new SQLException("Un résultat validé est verrouillé.","42501");if(author!=null&&!Objects.equals(author,laborantin))throw new SQLException("Le résultat a été saisi par un autre laborantin.","42501");}}
            String q="INSERT INTO resultats_analyses(demande_id,valeur,unite,valeurs_reference,observations,statut,saisi_par,date_saisie) VALUES(?,?,?,?,?,'SAISI',?,now()) ON CONFLICT(demande_id) DO UPDATE SET valeur=excluded.valeur,unite=excluded.unite,valeurs_reference=excluded.valeurs_reference,observations=excluded.observations,statut='SAISI',saisi_par=excluded.saisi_par,date_saisie=now() WHERE resultats_analyses.statut<>'VALIDE' AND (resultats_analyses.saisi_par IS NULL OR resultats_analyses.saisi_par=excluded.saisi_par)";
            try(PreparedStatement p=c.prepareStatement(q)){p.setLong(1,id);p.setString(2,valeur);p.setString(3,unite);p.setString(4,reference);p.setString(5,interpretation);p.setLong(6,laborantin);if(p.executeUpdate()!=1)throw new SQLException("Résultat verrouillé ou déjà attribué à un autre laborantin.","42501");}
            try(PreparedStatement p=c.prepareStatement("UPDATE demandes_analyses SET statut='SIS' WHERE id=? AND traite_par=? AND statut IN ('EN_COURS','SIS')")){p.setLong(1,id);p.setLong(2,laborantin);if(p.executeUpdate()!=1)throw new SQLException("Transmission impossible.","42501");}
            Journal.ecrire(c,laborantin,Journal.TRANSMISSION_RESULTAT_ANALYSE,"resultats_analyses",id,"Résultat transmis pour validation indépendante",true,ip);c.commit();
        }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}}
    }
}
