package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.CaisseDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.report.RecuPaiementReport;
import com.mycompany.projetmedical.service.CaisseService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/caisse/*")
public class CaisseServlet extends HttpServlet {
    private static final String BASE="/WEB-INF/fragments/caisse/";
    private static final SecureRandom RANDOM=new SecureRandom();
    private final CaisseService service=new CaisseService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;String route=route(req);LocalDate today=LocalDate.now();
        try{
            switch(route){
                case "accueil" -> {req.setAttribute("stats",service.stats());req.setAttribute("paiements",service.paiements("",today.minusDays(7),today));}
                case "facturation","impayees" -> {req.setAttribute("factures",service.factures(req.getParameter("q"),req.getParameter("statut"),route.equals("impayees")));req.setAttribute("impayees",route.equals("impayees"));}
                case "patients" -> req.setAttribute("patients",service.patients(req.getParameter("q")));
                case "patient" -> {int id=(int)id(req.getParameter("id"));req.setAttribute("patient",service.patient(id));req.setAttribute("factures",service.facturesPatient(id));}
                case "nouvelle-facture" -> chargerFormFacture(req);
                case "facture" -> {long id=id(req.getParameter("id"));req.setAttribute("facture",service.facture(id));req.setAttribute("paiements",service.paiementsFacture(id));}
                case "historique" -> {LocalDate du=date(req.getParameter("du"),today.minusDays(30)),au=date(req.getParameter("au"),today);req.setAttribute("dateDu",du);req.setAttribute("dateAu",au);req.setAttribute("paiements",service.paiements(req.getParameter("q"),du,au));}
                case "rapports" -> chargerRapport(req,today);
                case "profil" -> { }
                case "recu" -> {CaisseDao.Paiement p=service.paiement(id(req.getParameter("id")));if(p==null){resp.sendError(404);return;}byte[] pdf=RecuPaiementReport.generer(p);resp.setContentType("application/pdf");resp.setHeader("Content-Disposition","inline; filename=\"recu-"+p.id()+".pdf\"");resp.setHeader("Cache-Control","no-store");resp.setContentLength(pdf.length);resp.getOutputStream().write(pdf);return;}
                default -> {resp.sendError(404);return;}
            }
            if(route.equals("patient")&&req.getAttribute("patient")==null){resp.sendError(404);return;}if(route.equals("facture")&&req.getAttribute("facture")==null){resp.sendError(404);return;}
            req.getRequestDispatcher(BASE+route+".jsp").forward(req,resp);
        }catch(Exception e){log("Erreur espace caisse",e);req.setAttribute("erreur",message(e));req.getRequestDispatcher(BASE+"erreur.jsp").forward(req,resp);}
    }
    private void chargerFormFacture(HttpServletRequest req)throws SQLException{req.setAttribute("patients",service.patients(req.getParameter("qPatient")));req.setAttribute("prestations",service.prestations());}
    private void chargerRapport(HttpServletRequest req,LocalDate today)throws SQLException{LocalDate du=date(req.getParameter("du"),today.withDayOfMonth(1)),au=date(req.getParameter("au"),today);List<CaisseDao.Rapport> rows=service.rapport(du,au);BigDecimal total=rows.stream().map(CaisseDao.Rapport::encaisse).reduce(BigDecimal.ZERO,BigDecimal::add);long count=rows.stream().mapToLong(CaisseDao.Rapport::nombre).sum();req.setAttribute("dateDu",du);req.setAttribute("dateAu",au);req.setAttribute("rapport",rows);req.setAttribute("totalRapport",total);req.setAttribute("nombreRapport",count);}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;req.setCharacterEncoding("UTF-8");String csrf=(String)req.getSession().getAttribute(Constantes.SESSION_CSRF);if(csrf==null||!csrf.equals(req.getParameter("csrf"))){resp.sendError(403);return;}Utilisateur u=(Utilisateur)req.getSession().getAttribute(Constantes.SESSION_UTILISATEUR);String action=req.getPathInfo();long invoiceId=0;
        try{
            if("/nouvelle-facture".equals(action)){
                int patient=(int)id(req.getParameter("patientId"));String[] ids=req.getParameterValues("prestationId"),qtys=req.getParameterValues("quantite");if(ids==null||qtys==null||ids.length!=qtys.length)throw new IllegalArgumentException("Ajoutez au moins une ligne de prestation.");List<Integer> prestationIds=new ArrayList<>();List<BigDecimal> quantites=new ArrayList<>();for(int i=0;i<ids.length;i++){if(ids[i]==null||ids[i].isBlank())continue;prestationIds.add(Integer.parseInt(ids[i]));quantites.add(new BigDecimal(qtys[i]));}if(prestationIds.isEmpty())throw new IllegalArgumentException("Choisissez au moins une prestation.");LocalDate echeance=date(req.getParameter("echeance"),null);invoiceId=service.creerFacture(patient,prestationIds,quantites,optional(req,"notes",2000),echeance,u.getId(),req.getRemoteAddr());req.setAttribute("messageSucces","Facture créée. Les tarifs et montants ont été recalculés depuis PostgreSQL.");req.setAttribute("facture",service.facture(invoiceId));req.setAttribute("paiements",service.paiementsFacture(invoiceId));req.getRequestDispatcher(BASE+"facture.jsp").forward(req,resp);return;
            }
            if("/paiement".equals(action)){
                invoiceId=id(req.getParameter("factureId"));BigDecimal montant=new BigDecimal(required(req,"montant",20));String mode=required(req,"mode",20);if(!Set.of("ESPECES","CARTE","VIREMENT","CHEQUE","MOBILE_MONEY").contains(mode))throw new IllegalArgumentException("Mode de paiement invalide.");long payment=service.payer(invoiceId,montant,mode,optional(req,"reference",60),optional(req,"notes",1000),u.getId(),req.getRemoteAddr());req.setAttribute("messageSucces","Paiement confirmé et solde recalculé.");req.setAttribute("paiementId",payment);req.setAttribute("facture",service.facture(invoiceId));req.setAttribute("paiements",service.paiementsFacture(invoiceId));req.getRequestDispatcher(BASE+"facture.jsp").forward(req,resp);return;
            }
            resp.sendError(404);
        }catch(Exception e){log("Opération financière refusée/échouée",e);req.setAttribute("erreur",message(e));if("/nouvelle-facture".equals(action)){try{chargerFormFacture(req);}catch(Exception ignored){}req.getRequestDispatcher(BASE+"nouvelle-facture.jsp").forward(req,resp);}else if("/paiement".equals(action)){try{long fid=id(req.getParameter("factureId"));req.setAttribute("facture",service.facture(fid));req.setAttribute("paiements",service.paiementsFacture(fid));}catch(Exception ignored){}req.getRequestDispatcher(BASE+"facture.jsp").forward(req,resp);}}
    }
    private boolean autorise(HttpServletRequest r,HttpServletResponse p)throws IOException{Object o=r.getSession(false)==null?null:r.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);if(!(o instanceof Utilisateur)){p.sendError(401);return false;}if(!Constantes.ROLE_CAISSE.equals(((Utilisateur)o).getRole())){p.sendError(403);return false;}if(r.getSession().getAttribute(Constantes.SESSION_CSRF)==null){byte[] b=new byte[32];RANDOM.nextBytes(b);r.getSession().setAttribute(Constantes.SESSION_CSRF,Base64.getUrlEncoder().withoutPadding().encodeToString(b));}return true;}
    private String route(HttpServletRequest r){String p=r.getPathInfo();return p==null||p.equals("/")?"accueil":p.substring(1).split("/")[0];}
    private long id(String s){try{long n=Long.parseLong(s);if(n>0)return n;}catch(Exception ignored){}throw new IllegalArgumentException("Identifiant invalide.");}
    private String required(HttpServletRequest r,String k,int n){String s=Echappement.texte(r.getParameter(k));if(s.isEmpty()||s.length()>n)throw new IllegalArgumentException("Champ requis ou trop long : "+k+".");return s;}
    private String optional(HttpServletRequest r,String k,int n){String s=Echappement.texte(r.getParameter(k));if(s.length()>n)throw new IllegalArgumentException("Valeur trop longue : "+k+".");return s.isBlank()?null:s;}
    private LocalDate date(String s,LocalDate d){return s==null||s.isBlank()?d:LocalDate.parse(s);}
    private String message(Exception e){if(e instanceof IllegalArgumentException)return e.getMessage();if(e instanceof SQLException){String s=((SQLException)e).getSQLState();if("23514".equals(s))return e.getMessage();if("02000".equals(s))return e.getMessage();}return "Opération impossible. Vérifiez les données puis réessayez.";}
}
