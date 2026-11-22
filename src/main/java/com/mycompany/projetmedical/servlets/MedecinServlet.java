package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.MedecinDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.MedecinService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/medecin/*")
public class MedecinServlet extends HttpServlet {
    private static final String BASE="/WEB-INF/fragments/medecin/";
    private static final SecureRandom RANDOM=new SecureRandom();
    private final MedecinService service=new MedecinService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;String route=route(req);Utilisateur u=medecin(req);
        try{
            switch(route){
                case "accueil" -> {req.setAttribute("stats",service.stats(u.getId()));req.setAttribute("rendezVous",service.rdv(u.getId(),LocalDate.now(),LocalDate.now().plusDays(6)));req.setAttribute("consultationsEnCours",service.consultationsEnCours(u.getId()));}
                case "calendrier","rendezvous" -> {LocalDate du=date(req.getParameter("du"),LocalDate.now());LocalDate au=date(req.getParameter("au"),du.plusDays(6));req.setAttribute("dateDu",du);req.setAttribute("rendezVous",service.rdv(u.getId(),du,au));}
                case "patients" -> req.setAttribute("patients",service.patients(u.getId(),req.getParameter("q")));
                case "patient" -> chargerPatient(req,u.getId(),id(req.getParameter("id")));
                case "consultations" -> req.setAttribute("consultationsEnCours",service.consultationsEnCours(u.getId()));
                case "analyses" -> req.setAttribute("patients",service.patients(u.getId(),req.getParameter("q")));
                case "profil" -> { }
                default -> {resp.sendError(404);return;}
            }
            req.getRequestDispatcher(BASE+route+".jsp").forward(req,resp);
        }catch(Exception e){log("Erreur poste médecin",e);req.setAttribute("erreur","Accès médical refusé ou données PostgreSQL indisponibles.");req.getRequestDispatcher(BASE+"erreur.jsp").forward(req,resp);}
    }
    private void chargerPatient(HttpServletRequest r,long med,long patient)throws SQLException{
        if(patient<=0)throw new IllegalArgumentException("Sélectionnez un patient.");
        MedecinDao.Patient p=service.patient(med,patient);if(p==null)throw new SQLException("Patient introuvable.","02000");
        r.setAttribute("patient",p);r.setAttribute("antecedents",service.antecedents(med,patient));r.setAttribute("historique",service.historique(med,patient));r.setAttribute("constantes",service.constantes(med,patient));r.setAttribute("prescriptions",service.prescriptions(med,patient));r.setAttribute("analyses",service.analyses(med,patient));
        long c=service.consultationEnCours(med,patient);r.setAttribute("consultationId",c);r.setAttribute("typesAnalyse",service.typesAnalyse());
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;req.setCharacterEncoding("UTF-8");String token=(String)req.getSession().getAttribute(Constantes.SESSION_CSRF);if(token==null||!token.equals(req.getParameter("csrf"))){resp.sendError(403);return;}
        Utilisateur u=medecin(req);String path=route(req),action=action(req);long patient=id(req.getParameter("patientId"));
        try{
            if("patient".equals(path)&&"/demarrer".equals(action)){String rd=req.getParameter("rendezVousId");Long rdv=rd==null||rd.isBlank()?null:id(rd);service.demarrer(u.getId(),patient,rdv,opt(req,"motif",500),req.getRemoteAddr());req.setAttribute("messageSucces","Consultation ouverte et reliée au dossier médical.");}
            else if("patient".equals(path)&&"/enregistrer".equals(action)){service.enregistrer(u.getId(),id(req.getParameter("consultationId")),opt(req,"motif",2000),opt(req,"observations",10000),opt(req,"diagnostic",5000),opt(req,"conduite",5000),req.getRemoteAddr());req.setAttribute("messageSucces","Compte rendu enregistré.");}
            else if("patient".equals(path)&&"/constantes".equals(action)){service.constante(u.getId(),id(req.getParameter("consultationId")),opt(req,"tension",12),integer(req,"pouls"),decimal(req,"temperature"),decimal(req,"poids"),decimal(req,"taille"),integer(req,"spo2"),decimal(req,"glycemie"),req.getRemoteAddr());req.setAttribute("messageSucces","Constantes vitales enregistrées.");}
            else if("patient".equals(path)&&"/prescription".equals(action)){service.prescription(u.getId(),id(req.getParameter("consultationId")),opt(req,"notes",2000),required(req,"medicament",160),opt(req,"dosage",60),required(req,"posologie",160),opt(req,"duree",60),opt(req,"quantite",60),opt(req,"instructions",4000),req.getRemoteAddr());req.setAttribute("messageSucces","Prescription créée.");}
            else if("patient".equals(path)&&"/analyse".equals(action)){String priorite=req.getParameter("priorite");if(!List.of("BASSE","NORMALE","URGENTE").contains(priorite))throw new IllegalArgumentException("Priorité invalide.");service.analyse(u.getId(),id(req.getParameter("consultationId")),(int)id(req.getParameter("typeAnalyseId")),priorite,opt(req,"observations",2000),req.getRemoteAddr());req.setAttribute("messageSucces","Demande d’analyse transmise au laboratoire.");}
            else if("patient".equals(path)&&"/finaliser".equals(action)){service.finaliser(u.getId(),id(req.getParameter("consultationId")),req.getRemoteAddr());req.setAttribute("messageSucces","Consultation finalisée.");}
            else{resp.sendError(404);return;}
        }catch(Exception e){log("Action clinique refusée/échouée",e);req.setAttribute("erreur",message(e));}
        try{chargerPatient(req,u.getId(),patient);req.getRequestDispatcher(BASE+"patient.jsp").forward(req,resp);}catch(Exception e){resp.sendError(403);}
    }
    private boolean autorise(HttpServletRequest r,HttpServletResponse p)throws IOException{Object x=r.getSession(false)==null?null:r.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);if(!(x instanceof Utilisateur)){p.sendError(401);return false;}if(!Constantes.ROLE_MEDECIN.equals(((Utilisateur)x).getRole())){p.sendError(403);return false;}if(r.getSession().getAttribute(Constantes.SESSION_CSRF)==null){byte[] b=new byte[32];RANDOM.nextBytes(b);r.getSession().setAttribute(Constantes.SESSION_CSRF,Base64.getUrlEncoder().withoutPadding().encodeToString(b));}return true;}
    private Utilisateur medecin(HttpServletRequest r){return (Utilisateur)r.getSession().getAttribute(Constantes.SESSION_UTILISATEUR);}
    private String route(HttpServletRequest r){String p=r.getPathInfo();if(p==null||p.equals("/"))return "accueil";String[] a=p.substring(1).split("/");if(a[0].equals("patient")&&a.length>1)return "patient";if(a[0].equals("consultations")||a[0].equals("carnets"))return a[0];return a[0];}
    private String action(HttpServletRequest r){String p=r.getPathInfo();if(p==null||p.isBlank()||p.equals("/"))return "";String[] a=p.split("/");return a.length==0?"":"/"+a[a.length-1];}
    private long id(String s){try{long n=Long.parseLong(s);if(n<=0)throw new NumberFormatException();return n;}catch(Exception e){throw new IllegalArgumentException("Identifiant invalide.");}}
    private String required(HttpServletRequest r,String k,int n){String s=Echappement.texte(r.getParameter(k));if(s.isEmpty()||s.length()>n)throw new IllegalArgumentException("Champ requis ou trop long : "+k);return s;}
    private String opt(HttpServletRequest r,String k,int n){String s=Echappement.texte(r.getParameter(k));if(s.length()>n)throw new IllegalArgumentException("Champ trop long : "+k);return s.isEmpty()?null:s;}
    private Integer integer(HttpServletRequest r,String k){String s=r.getParameter(k);if(s==null||s.isBlank())return null;try{return Integer.valueOf(s);}catch(Exception e){throw new IllegalArgumentException("Valeur entière invalide : "+k);}}
    private BigDecimal decimal(HttpServletRequest r,String k){String s=r.getParameter(k);if(s==null||s.isBlank())return null;try{return new BigDecimal(s);}catch(Exception e){throw new IllegalArgumentException("Valeur numérique invalide : "+k);}}
    private LocalDate date(String s,LocalDate d){return s==null||s.isBlank()?d:LocalDate.parse(s);}
    private String message(Exception e){if(e instanceof IllegalArgumentException)return e.getMessage();if(e instanceof SQLException){String s=((SQLException)e).getSQLState();if("42501".equals(s))return "Cette opération n’est pas autorisée pour votre compte ou cette consultation est terminée.";if("23514".equals(s))return "Une valeur ne respecte pas les limites médicales admises.";if("23503".equals(s))return "La référence demandée n’existe plus.";}return "Impossible d’enregistrer. Vérifiez les données saisies et la connexion PostgreSQL.";}
}
