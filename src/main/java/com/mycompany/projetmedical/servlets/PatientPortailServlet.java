package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.CaisseDao;
import com.mycompany.projetmedical.dao.PatientPortailDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.report.FacturePatientReport;
import com.mycompany.projetmedical.report.RecuPaiementReport;
import com.mycompany.projetmedical.service.PatientPortailService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/patient/*")
public class PatientPortailServlet extends HttpServlet {
    private static final String BASE="/WEB-INF/fragments/patient/";
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final Pattern TELEPHONE=Pattern.compile("^[+0-9() .-]{0,20}$");
    private final PatientPortailService service=new PatientPortailService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;String route=route(req);int patient=patientId(req);
        try{
            req.setAttribute("profil",service.profil(patient));
            switch(route){
                case "accueil" -> {req.setAttribute("stats",service.stats(patient));req.setAttribute("rendezVous",service.rendezVous(patient));req.setAttribute("notifications",service.notifications(patient));}
                case "carnets" -> {req.setAttribute("dossier",service.dossier(patient));req.setAttribute("antecedents",service.antecedents(patient));req.setAttribute("consultations",service.consultations(patient));}
                case "consultations" -> {req.setAttribute("consultations",service.consultations(patient));req.setAttribute("prescriptions",service.prescriptions(patient));}
                case "analyses" -> req.setAttribute("analyses",service.analysesValidees(patient));
                case "rendezvous" -> {req.setAttribute("rendezVous",service.rendezVous(patient));req.setAttribute("medecins",service.medecins());}
                case "facturation" -> {req.setAttribute("factures",service.factures(patient));req.setAttribute("paiements",service.paiements(patient));}
                case "notifications" -> req.setAttribute("notifications",service.notifications(patient));
                case "profil" -> { }
                case "facture-pdf" -> {long id=id(req.getParameter("id"));PatientPortailDao.Facture f=service.facture(patient,id);if(f==null){resp.sendError(404);return;}byte[] pdf=FacturePatientReport.generer(f,(PatientPortailDao.Profil)req.getAttribute("profil"));resp.setContentType("application/pdf");resp.setHeader("Content-Disposition","attachment; filename=\"facture-"+f.numero()+".pdf\"");resp.setHeader("Cache-Control","no-store");resp.setContentLength(pdf.length);resp.getOutputStream().write(pdf);return;}
                case "recu-pdf" -> {long id=id(req.getParameter("id"));PatientPortailDao.Paiement pay=service.paiement(patient,id);if(pay==null){resp.sendError(404);return;}PatientPortailDao.Facture f=service.facture(patient,pay.factureId());PatientPortailDao.Profil pr=(PatientPortailDao.Profil)req.getAttribute("profil");CaisseDao.Paiement jasper=new CaisseDao.Paiement(pay.id(),pay.factureId(),pay.numeroFacture(),pr.numero(),pr.prenoms()+" "+pr.nom(),pay.montant(),pay.mode(),pay.date(),pay.reference(),pay.notes(),"—",f.total(),pay.solde());byte[] pdf=RecuPaiementReport.generer(jasper);resp.setContentType("application/pdf");resp.setHeader("Content-Disposition","attachment; filename=\"recu-"+pay.id()+".pdf\"");resp.setHeader("Cache-Control","no-store");resp.setContentLength(pdf.length);resp.getOutputStream().write(pdf);return;}
                default -> {resp.sendError(404);return;}
            }
            if(req.getAttribute("profil")==null){resp.sendError(404);return;}
            req.getRequestDispatcher(BASE+route+".jsp").forward(req,resp);
        }catch(Exception e){log("Erreur portail patient",e);req.setAttribute("erreur",message(e));req.getRequestDispatcher(BASE+"erreur.jsp").forward(req,resp);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;req.setCharacterEncoding("UTF-8");String csrf=(String)req.getSession().getAttribute(Constantes.SESSION_CSRF);if(csrf==null||!csrf.equals(req.getParameter("csrf"))){resp.sendError(403);return;}int patient=patientId(req);String path=req.getPathInfo();String route=route(req);
        try{
            if("/profil/modifier".equals(path)){String email=Echappement.email(required(req,"email",120));if(!Echappement.emailValide(email))throw new IllegalArgumentException("Adresse email invalide.");String tel=optional(req,"telephone",20),telUrg=optional(req,"telephoneUrgence",20);if(tel!=null&&!TELEPHONE.matcher(tel).matches()||telUrg!=null&&!TELEPHONE.matcher(telUrg).matches())throw new IllegalArgumentException("Format de téléphone invalide.");service.modifierProfil(patient,required(req,"nom",60),required(req,"prenoms",80),email,tel,optional(req,"adresse",300),optional(req,"contactUrgence",120),telUrg,req.getRemoteAddr());req.setAttribute("messageSucces","Vos coordonnées personnelles ont été mises à jour.");}
            else if("/rendezvous/demander".equals(path)){LocalDate date=LocalDate.parse(required(req,"date",10));LocalTime debut=LocalTime.parse(required(req,"debut",5)),fin=LocalTime.parse(required(req,"fin",5));service.demanderRendezVous(patient,id(req.getParameter("medecinId")),date,debut,fin,required(req,"motif",500),req.getRemoteAddr());req.setAttribute("messageSucces","Votre demande de rendez-vous a été transmise. Elle reste en attente de confirmation.");}
            else if("/rendezvous/annuler".equals(path)){service.annulerRendezVous(patient,id(req.getParameter("id")),req.getRemoteAddr());req.setAttribute("messageSucces","Le rendez-vous a été annulé.");}
            else if("/notifications/lire".equals(path)){service.lireNotification(patient,id(req.getParameter("id")));req.setAttribute("messageSucces","Notification marquée comme lue.");}
            else{resp.sendError(404);return;}
        }catch(Exception e){log("Action portail patient refusée/échouée",e);req.setAttribute("erreur",message(e));}
        try{req.setAttribute("profil",service.profil(patient));charger(req,route,patient);req.getRequestDispatcher(BASE+route+".jsp").forward(req,resp);}catch(Exception e){resp.sendError(500);}
    }
    private void charger(HttpServletRequest r,String route,int p)throws SQLException{
        switch(route){case "accueil"->{r.setAttribute("stats",service.stats(p));r.setAttribute("rendezVous",service.rendezVous(p));r.setAttribute("notifications",service.notifications(p));}case "profil"->{ }case "rendezvous"->{r.setAttribute("rendezVous",service.rendezVous(p));r.setAttribute("medecins",service.medecins());}case "notifications"->r.setAttribute("notifications",service.notifications(p));default->throw new SQLException("Rubrique invalide.");}
    }
    private boolean autorise(HttpServletRequest r,HttpServletResponse p)throws IOException{Object o=r.getSession(false)==null?null:r.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);if(!(o instanceof Utilisateur)){p.sendError(401);return false;}if(!Constantes.ROLE_PATIENT.equals(((Utilisateur)o).getRole())){p.sendError(403);return false;}if(r.getSession().getAttribute(Constantes.SESSION_CSRF)==null){byte[] b=new byte[32];RANDOM.nextBytes(b);r.getSession().setAttribute(Constantes.SESSION_CSRF,Base64.getUrlEncoder().withoutPadding().encodeToString(b));}return true;}
    private int patientId(HttpServletRequest r){long n=((Utilisateur)r.getSession().getAttribute(Constantes.SESSION_UTILISATEUR)).getId();if(n<1||n>Integer.MAX_VALUE)throw new IllegalStateException("Identifiant patient invalide.");return(int)n;}
    private String route(HttpServletRequest r){String p=r.getPathInfo();return p==null||p.equals("/")?"accueil":p.substring(1).split("/")[0];}
    private long id(String s){try{long n=Long.parseLong(s);if(n>0)return n;}catch(Exception ignored){}throw new IllegalArgumentException("Identifiant invalide.");}
    private String required(HttpServletRequest r,String k,int max){String s=Echappement.texte(r.getParameter(k));if(s.isEmpty()||s.length()>max)throw new IllegalArgumentException("Champ requis ou trop long : "+k+".");return s;}
    private String optional(HttpServletRequest r,String k,int max){String s=Echappement.texte(r.getParameter(k));if(s.length()>max)throw new IllegalArgumentException("Champ trop long : "+k+".");return s.isBlank()?null:s;}
    private String message(Exception e){if(e instanceof IllegalArgumentException)return e.getMessage();if(e instanceof SQLException){String s=((SQLException)e).getSQLState();if("23P01".equals(s))return e.getMessage();if("23505".equals(s))return "Cette adresse email est déjà utilisée.";if("42501".equals(s))return e.getMessage();}return "Opération impossible. Vérifiez les informations et réessayez.";}
}
