package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.AccueilDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.AccueilService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/accueil/*")
public class AccueilServlet extends HttpServlet {
    private final AccueilService service = new AccueilService();
    private static final String BASE="/WEB-INF/fragments/accueil/";
    private static final SecureRandom RANDOM=new SecureRandom();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;
        String route=route(req);
        try {
            switch(route){
                case "dashboard" -> req.setAttribute("indicateurs",service.indicateurs());
                case "profil" -> { }
                case "patients" -> {
                    req.setAttribute("patients",service.rechercherPatients(req.getParameter("q")));
                    String id=req.getParameter("modifier"); if(id!=null) req.setAttribute("patientEdition",service.patient(parseId(id)));
                }
                case "rendezvous","agenda","historique" -> chargerRendezVous(req,route);
                default -> { resp.sendError(404); return; }
            }
            if(route.equals("rendezvous")&&req.getParameter("modifier")!=null)req.setAttribute("rendezVousEdition",service.rendezVousParId(parseId(req.getParameter("modifier"))));
            // agenda et historique sont deux vues du même écran rendezvous.jsp.
            String vue = (route.equals("agenda") || route.equals("historique")) ? "rendezvous" : route;
            req.getRequestDispatcher(BASE+vue+".jsp").forward(req,resp);
        }catch(Exception ex){ log("Erreur espace accueil",ex); resp.setStatus(500); req.setAttribute("erreur","Impossible de charger les données depuis PostgreSQL."); req.getRequestDispatcher(BASE+"erreur.jsp").forward(req,resp); }
    }
    private void chargerRendezVous(HttpServletRequest req,String route)throws SQLException{
        LocalDate today=LocalDate.now();
        LocalDate du=date(req.getParameter("du"),route.equals("historique")?today.minusMonths(1):today);
        LocalDate au=date(req.getParameter("au"),route.equals("historique")?today:route.equals("agenda")?today.plusDays(6):today.plusDays(30));
        req.setAttribute("medecins",service.medecins()); req.setAttribute("patientsVerifies",service.patientsVerifies()); req.setAttribute("rendezVous",service.rendezVous(du,au,idOption(req.getParameter("medecin")),route.equals("historique")?null:req.getParameter("statut")));
        req.setAttribute("dateDu",du);req.setAttribute("dateAu",au);req.setAttribute("route",route);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return; req.setCharacterEncoding("UTF-8");
        String token=(String)req.getSession().getAttribute(Constantes.SESSION_CSRF);
        if(token==null||!token.equals(req.getParameter("csrf"))){resp.sendError(403);return;}
        Utilisateur u=(Utilisateur)req.getSession().getAttribute(Constantes.SESSION_UTILISATEUR); String path=route(req); String action=action(req);
        try {
            if("patients".equals(path)&&"/creer".equals(action)){
                if(!"on".equals(req.getParameter("identite_verifiee")))throw new IllegalArgumentException("Confirmez la vérification de l’identité du patient.");
                String nom=required(req,"nom",60), prenoms=required(req,"prenoms",80), email=Echappement.email(required(req,"email",120));
                if(!Echappement.emailValide(email))throw new IllegalArgumentException("Adresse courriel invalide.");
                String motDePasse=req.getParameter("motDePasse"), confirmation=req.getParameter("confirmationMotDePasse");
                if(motDePasse==null||motDePasse.length()<8||motDePasse.length()>128)throw new IllegalArgumentException("Le mot de passe doit contenir entre 8 et 128 caractères.");
                if(!motDePasse.equals(confirmation))throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
                String sexe=required(req,"sexe",1); if(!sexe.equals("M")&&!sexe.equals("F"))throw new IllegalArgumentException("Sexe invalide.");
                LocalDate naissance=optionalDate(req,"date_naissance"); if(naissance!=null&&naissance.isAfter(LocalDate.now()))throw new IllegalArgumentException("La naissance ne peut pas être dans le futur.");
                AccueilDao.PatientCree p=service.creerPatient(nom,prenoms,email,motDePasse,sexe,naissance,opt(req,"telephone",20),opt(req,"adresse",300),opt(req,"profession",80),opt(req,"contact_urgence",120),opt(req,"telephone_urgence",20),u.getId(),ip(req));
                req.setAttribute("patientCree",p); req.setAttribute("messageSucces","Patient enregistré. Il devra modifier ce mot de passe à sa première connexion."); path="patients";
            }else if("patients".equals(path)&&"/modifier".equals(action)){
                String email=Echappement.email(required(req,"email",120));if(!Echappement.emailValide(email))throw new IllegalArgumentException("Adresse courriel invalide.");
                service.modifierPatient(parseId(req.getParameter("id")),required(req,"nom",60),required(req,"prenoms",80),email,required(req,"sexe",1),optionalDate(req,"date_naissance"),opt(req,"telephone",20),opt(req,"adresse",300),opt(req,"profession",80),opt(req,"contact_urgence",120),opt(req,"telephone_urgence",20),u.getId(),ip(req)); req.setAttribute("messageSucces","Informations administratives mises à jour."); path="patients";
            }else if("patients".equals(path)&&"/dossier".equals(action)){
                boolean cree=service.ouvrirDossier(parseId(req.getParameter("patientId")),u.getId(),ip(req)); req.setAttribute("messageSucces",cree?"Dossier médical ouvert. Son contenu clinique reste réservé aux professionnels autorisés.":"Le dossier existe déjà. Aucun contenu médical n’a été consulté.");path="patients";
            }else if("rendezvous".equals(path)&&"/creer".equals(action)){
                LocalDate date=LocalDate.parse(required(req,"date",10));LocalTime debut=LocalTime.parse(required(req,"debut",5)),fin=LocalTime.parse(required(req,"fin",5));if(date.isBefore(LocalDate.now())||!fin.isAfter(debut))throw new IllegalArgumentException("Choisissez une date à venir et une heure de fin après le début.");
                service.creerRendezVous(parseId(req.getParameter("patientId")),parseId(req.getParameter("medecinId")),date,debut,fin,opt(req,"motif",500),u.getId(),ip(req));req.setAttribute("messageSucces","Rendez-vous planifié.");
            }else if("rendezvous".equals(path)&&"/modifier".equals(action)){
                LocalDate date=LocalDate.parse(required(req,"date",10));LocalTime debut=LocalTime.parse(required(req,"debut",5)),fin=LocalTime.parse(required(req,"fin",5));if(date.isBefore(LocalDate.now())||!fin.isAfter(debut))throw new IllegalArgumentException("Choisissez une date à venir et une heure de fin après le début.");
                service.modifierRendezVous(parseId(req.getParameter("id")),parseId(req.getParameter("patientId")),parseId(req.getParameter("medecinId")),date,debut,fin,opt(req,"motif",500),u.getId(),ip(req));req.setAttribute("messageSucces","Rendez-vous modifié.");
            }else if("rendezvous".equals(path)&&"/confirmer".equals(action)){service.confirmer(parseId(req.getParameter("id")),u.getId(),ip(req));req.setAttribute("messageSucces","Rendez-vous confirmé.");
            }else if("rendezvous".equals(path)&&"/annuler".equals(action)){service.annuler(parseId(req.getParameter("id")),required(req,"motif",300),u.getId(),ip(req));req.setAttribute("messageSucces","Rendez-vous annulé.");
            }else{resp.sendError(404);return;}
            if(path.equals("patients"))req.setAttribute("patients",service.rechercherPatients(req.getParameter("q")));
            else chargerRendezVous(req,path);
            req.getRequestDispatcher(BASE+path+".jsp").forward(req,resp);
        }catch(Exception ex){log("Action accueil refusée/échouée: "+action,ex);req.setAttribute("erreur",message(ex));
            try{if(path.equals("patients")){req.setAttribute("patients",service.rechercherPatients(req.getParameter("q")));String id=req.getParameter("id");if(id!=null)req.setAttribute("patientEdition",service.patient(parseId(id)));}else chargerRendezVous(req,path);req.getRequestDispatcher(BASE+path+".jsp").forward(req,resp);}catch(Exception e){resp.sendError(500);}}
    }
    private boolean autorise(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        Object o=req.getSession(false)==null?null:req.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);
        if(!(o instanceof Utilisateur)){resp.sendError(401);return false;} String role=((Utilisateur)o).getRole(); if(!Constantes.ROLE_ACCUEIL.equals(role)&&!Constantes.ROLE_ADMIN.equals(role)){resp.sendError(403);return false;}
        if(req.getSession().getAttribute(Constantes.SESSION_CSRF)==null){byte[] b=new byte[32];RANDOM.nextBytes(b);req.getSession().setAttribute(Constantes.SESSION_CSRF,Base64.getUrlEncoder().withoutPadding().encodeToString(b));}
        return true;
    }
    private String route(HttpServletRequest r){String p=r.getPathInfo();if(p==null||p.equals("/"))return "dashboard";String[] a=p.substring(1).split("/");if(a[0].equals("accueil"))return "dashboard";return a.length>1&&a[0].equals("rendezvous")?"rendezvous":a[0];}
    private String action(HttpServletRequest r){String p=r.getPathInfo();if(p==null||p.isBlank()||p.equals("/"))return "";String[] a=p.split("/");return a.length==0?"":"/"+a[a.length-1];}
    private String required(HttpServletRequest r,String key,int max){String s=Echappement.texte(r.getParameter(key));if(s.isEmpty()||s.length()>max)throw new IllegalArgumentException("Champ invalide : "+key+".");return s;}
    private String opt(HttpServletRequest r,String key,int max){String s=Echappement.texte(r.getParameter(key));if(s.length()>max)throw new IllegalArgumentException("Valeur trop longue : "+key+".");return s.isEmpty()?null:s;}
    private LocalDate optionalDate(HttpServletRequest r,String key){String s=r.getParameter(key);return s==null||s.isBlank()?null:LocalDate.parse(s);}
    private LocalDate date(String s,LocalDate def){return s==null||s.isBlank()?def:LocalDate.parse(s);}
    private long parseId(String s){try{long n=Long.parseLong(s);if(n<1)throw new NumberFormatException();return n;}catch(Exception e){throw new IllegalArgumentException("Identifiant invalide.");}}
    private Long idOption(String s){return s==null||s.isBlank()?null:parseId(s);}
    private String ip(HttpServletRequest r){return r.getRemoteAddr();}
    private String message(Exception e){if(e instanceof IllegalArgumentException||e instanceof DateTimeParseException)return e.getMessage()==null?"Données invalides.":e.getMessage();if(e instanceof SQLException){String st=((SQLException)e).getSQLState();if("23505".equals(st))return "Cette adresse courriel ou ce créneau existe déjà.";if("23503".equals(st))return "Donnée de référence manquante : vérifiez que les rôles et le personnel médical sont configurés.";if("23P01".equals(st))return "Le patient ou le médecin a déjà un rendez-vous à cette heure.";if("23514".equals(st))return e.getMessage();}return "Opération impossible. Vérifiez les données PostgreSQL et réessayez.";}
}
