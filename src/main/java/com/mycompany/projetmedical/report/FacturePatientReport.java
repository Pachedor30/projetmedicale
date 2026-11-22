package com.mycompany.projetmedical.report;

import com.mycompany.projetmedical.dao.PatientPortailDao;
import java.awt.Color;
import java.util.*;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JasperDesign;

/** Export JasperReports de la facture et de ses lignes déjà bornées au patient. */
public final class FacturePatientReport {
    private FacturePatientReport(){}
    public static byte[] generer(PatientPortailDao.Facture f,PatientPortailDao.Profil patient)throws JRException{
        JasperDesign d=new JasperDesign();d.setName("facture_patient");d.setPageWidth(595);d.setPageHeight(842);d.setColumnWidth(515);d.setLeftMargin(40);d.setRightMargin(40);d.setTopMargin(36);d.setBottomMargin(36);
        for(String n:new String[]{"NUMERO","PATIENT","NUMERO_PATIENT","DATE","ECHEANCE","STATUT","TOTAL","PAYE","RESTE"}){JRDesignParameter p=new JRDesignParameter();p.setName(n);p.setValueClass(String.class);d.addParameter(p);}
        for(String n:new String[]{"LIBELLE","QUANTITE","PRIX","MONTANT"}){JRDesignField fied=new JRDesignField();fied.setName(n);fied.setValueClass(String.class);d.addField(fied);}
        JRDesignBand title=new JRDesignBand();title.setHeight(170);text(title,0,0,515,28,"CLINIQUE MIRACLE",20,true,new Color(19,135,126));text(title,0,38,515,25,"FACTURE",16,true,Color.DARK_GRAY);text(title,0,75,120,18,"N° facture",10,true,Color.GRAY);field(title,130,74,385,20,"NUMERO");text(title,0,98,120,18,"Patient",10,true,Color.GRAY);field(title,130,97,385,20,"PATIENT");text(title,0,121,120,18,"N° patient",10,true,Color.GRAY);field(title,130,120,385,20,"NUMERO_PATIENT");text(title,0,144,120,18,"Date / échéance",10,true,Color.GRAY);field(title,130,143,385,20,"DATE");d.setTitle(title);
        JRDesignBand header=new JRDesignBand();header.setHeight(24);text(header,0,0,235,22,"Prestation",10,true,Color.DARK_GRAY);text(header,240,0,65,22,"Quantité",10,true,Color.DARK_GRAY);text(header,310,0,90,22,"Prix unitaire",10,true,Color.DARK_GRAY);text(header,405,0,110,22,"Montant",10,true,Color.DARK_GRAY);d.setColumnHeader(header);
        JRDesignBand detail=new JRDesignBand();detail.setHeight(25);detailField(detail,0,2,235,20,"LIBELLE");detailField(detail,240,2,65,20,"QUANTITE");detailField(detail,310,2,90,20,"PRIX");detailField(detail,405,2,110,20,"MONTANT");((JRDesignSection)d.getDetailSection()).addBand(detail);
        JRDesignBand summary=new JRDesignBand();summary.setHeight(125);text(summary,295,10,100,18,"Total",10,true,Color.GRAY);field(summary,405,8,110,20,"TOTAL");text(summary,295,36,100,18,"Payé",10,true,Color.GRAY);field(summary,405,34,110,20,"PAYE");text(summary,295,62,100,18,"Reste dû",10,true,Color.GRAY);field(summary,405,60,110,20,"RESTE");text(summary,0,96,515,20,"Statut : "+f.statut(),10,false,Color.DARK_GRAY);d.setSummary(summary);
        List<Map<String,?>> rows=new ArrayList<>();for(PatientPortailDao.Ligne l:f.lignes()){Map<String,Object> m=new HashMap<>();m.put("LIBELLE",l.libelle());m.put("QUANTITE",l.quantite().toPlainString());m.put("PRIX",l.prix().toPlainString());m.put("MONTANT",l.montant().toPlainString());rows.add(m);}Map<String,Object> p=new HashMap<>();p.put("NUMERO",f.numero());p.put("PATIENT",patient.prenoms()+" "+patient.nom());p.put("NUMERO_PATIENT",patient.numero());p.put("DATE",f.date()+" / "+(f.echeance()==null?"—":f.echeance()));p.put("ECHEANCE",f.echeance()==null?"—":f.echeance().toString());p.put("STATUT",f.statut());p.put("TOTAL",f.total().toPlainString());p.put("PAYE",f.paye().toPlainString());p.put("RESTE",f.restant().toPlainString());
        JasperReport report=JasperCompileManager.compileReport(d);JasperPrint print=JasperFillManager.fillReport(report,p,new JRMapCollectionDataSource(rows));return JasperExportManager.exportReportToPdf(print);
    }
    private static void text(JRDesignBand b,int x,int y,int w,int h,String value,float size,boolean bold,Color color){JRDesignStaticText e=new JRDesignStaticText();e.setX(x);e.setY(y);e.setWidth(w);e.setHeight(h);e.setText(value);e.setFontName("SansSerif");e.setFontSize(size);e.setBold(bold);e.setForecolor(color);b.addElement(e);}
    private static void field(JRDesignBand b,int x,int y,int w,int h,String param){JRDesignTextField e=new JRDesignTextField();e.setX(x);e.setY(y);e.setWidth(w);e.setHeight(h);e.setFontName("SansSerif");e.setFontSize(10f);e.setBlankWhenNull(true);e.setExpression(new JRDesignExpression("$P{"+param+"}"));b.addElement(e);}
    private static void detailField(JRDesignBand b,int x,int y,int w,int h,String name){JRDesignTextField e=new JRDesignTextField();e.setX(x);e.setY(y);e.setWidth(w);e.setHeight(h);e.setFontName("SansSerif");e.setFontSize(10f);e.setBlankWhenNull(true);e.setExpression(new JRDesignExpression("$F{"+name+"}"));b.addElement(e);}
}
