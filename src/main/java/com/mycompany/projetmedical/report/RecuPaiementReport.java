package com.mycompany.projetmedical.report;

import com.mycompany.projetmedical.dao.CaisseDao;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

/** Reçu PDF ponctuel généré avec JasperReports depuis les données persistées. */
public final class RecuPaiementReport {
    private RecuPaiementReport(){}
    public static byte[] generer(CaisseDao.Paiement p)throws JRException{
        JasperDesign d=new JasperDesign();d.setName("recu_paiement");d.setPageWidth(595);d.setPageHeight(842);d.setColumnWidth(515);d.setLeftMargin(40);d.setRightMargin(40);d.setTopMargin(36);d.setBottomMargin(36);
        String[] names={"NUMERO_RECU","FACTURE","PATIENT","NUMERO_PATIENT","DATE","MONTANT","MODE","REFERENCE","TOTAL_FACTURE","RESTE_DU","ENCAISSE_PAR"};
        for(String n:names){JRDesignParameter pa=new JRDesignParameter();pa.setName(n);pa.setValueClass(String.class);d.addParameter(pa);}
        JRDesignBand b=new JRDesignBand();b.setHeight(550);
        staticText(b,0,0,515,28,"CLINIQUE MIRACLE",20,true,new Color(19,135,126));
        staticText(b,0,38,515,26,"REÇU DE PAIEMENT",16,true,Color.DARK_GRAY);
        staticText(b,0,82,170,20,"Numéro de reçu",10,true,Color.GRAY);field(b,190,80,325,22,"NUMERO_RECU");
        staticText(b,0,116,170,20,"Facture",10,true,Color.GRAY);field(b,190,114,325,22,"FACTURE");
        staticText(b,0,150,170,20,"Patient",10,true,Color.GRAY);field(b,190,148,325,22,"PATIENT");
        staticText(b,0,184,170,20,"Numéro patient",10,true,Color.GRAY);field(b,190,182,325,22,"NUMERO_PATIENT");
        staticText(b,0,218,170,20,"Date du paiement",10,true,Color.GRAY);field(b,190,216,325,22,"DATE");
        staticText(b,0,260,515,1,"",1,false,Color.LIGHT_GRAY);
        staticText(b,0,278,170,20,"Montant reçu",11,true,Color.GRAY);field(b,190,276,325,26,"MONTANT");
        staticText(b,0,314,170,20,"Mode de paiement",10,true,Color.GRAY);field(b,190,312,325,22,"MODE");
        staticText(b,0,348,170,20,"Référence",10,true,Color.GRAY);field(b,190,346,325,22,"REFERENCE");
        staticText(b,0,382,170,20,"Total de la facture",10,true,Color.GRAY);field(b,190,380,325,22,"TOTAL_FACTURE");
        staticText(b,0,416,170,20,"Solde restant",10,true,Color.GRAY);field(b,190,414,325,22,"RESTE_DU");
        staticText(b,0,458,170,20,"Encaissé par",10,true,Color.GRAY);field(b,190,456,325,22,"ENCAISSE_PAR");
        staticText(b,0,510,515,24,"Merci de conserver ce reçu.",11,false,Color.DARK_GRAY);
        d.setTitle(b);
        Map<String,Object> values=new HashMap<>();values.put("NUMERO_RECU","REC-"+p.id());values.put("FACTURE",p.numeroFacture());values.put("PATIENT",p.patient());values.put("NUMERO_PATIENT",p.numeroPatient());values.put("DATE",p.date().toString());values.put("MONTANT",p.montant().toPlainString());values.put("MODE",p.mode());values.put("REFERENCE",nvl(p.reference()));values.put("TOTAL_FACTURE",p.totalFacture().toPlainString());values.put("RESTE_DU",p.solde().toPlainString());values.put("ENCAISSE_PAR",p.encaisseur());
        JasperReport report=JasperCompileManager.compileReport(d);JasperPrint print=JasperFillManager.fillReport(report,values,new JREmptyDataSource(1));return JasperExportManager.exportReportToPdf(print);
    }
    private static String nvl(String s){return s==null||s.isBlank()?"—":s;}
    private static void staticText(JRDesignBand b,int x,int y,int w,int h,String s,float size,boolean bold,Color color){JRDesignStaticText e=new JRDesignStaticText();e.setX(x);e.setY(y);e.setWidth(w);e.setHeight(Math.max(1,h));e.setText(s);e.setFontName("SansSerif");e.setFontSize(size);e.setBold(bold);e.setForecolor(color);b.addElement(e);}
    private static void field(JRDesignBand b,int x,int y,int w,int h,String param){JRDesignTextField e=new JRDesignTextField();e.setX(x);e.setY(y);e.setWidth(w);e.setHeight(h);e.setFontName("SansSerif");e.setFontSize(11f);e.setBlankWhenNull(true);e.setExpression(new JRDesignExpression("$P{"+param+"}"));b.addElement(e);}
}
