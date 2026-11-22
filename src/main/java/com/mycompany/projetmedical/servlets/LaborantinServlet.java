package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.LaborantinDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.LaborantinService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/labo/*")
public class LaborantinServlet extends HttpServlet {
    private static final String BASE="/WEB-INF/fragments/labo/";
    private static final SecureRandom RANDOM=new SecureRandom();
    private final LaborantinService service=new LaborantinService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;String route=route(req);
        try{
            if(route.equals("accueil")){req.setAttribute("stats",service.stats());req.setAttribute("demandes",service.demandes("", ""));}
            else if(route.equals("analyses")||route.equals("historique")){String statut=req.getParameter("statut");if(statut!=null&&!statut.isBlank()&&!Set.of("DEMANDEE","EN_COURS","SIS","VALIDE","ANNULEE").contains(statut))throw new IllegalArgumentException("Statut invalide.");req.setAttribute("demandes",service.demandes(statut,req.getParameter("q")));req.setAttribute("statut",statut);}
            else if(route.equals("demande")){LaborantinDao.Demande d=service.demande(id(req.getParameter("id")));if(d==null){resp.sendError(404);return;}req.setAttribute("demande",d);}
            else if(route.equals("profil")){ }
            else{resp.sendError(404);return;}
            req.getRequestDispatcher(BASE+route+".jsp").forward(req,resp);
        }catch(Exception e){log("Erreur espace laboratoire",e);req.setAttribute("erreur",message(e));req.getRequestDispatcher(BASE+"erreur.jsp").forward(req,resp);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!autorise(req,resp))return;req.setCharacterEncoding("UTF-8");String csrf=(String)req.getSession().getAttribute(Constantes.SESSION_CSRF);if(csrf==null||!csrf.equals(req.getParameter("csrf"))){resp.sendError(403);return;}
        Utilisateur u=(Utilisateur)req.getSession().getAttribute(Constantes.SESSION_UTILISATEUR);String action=req.getPathInfo();long id=id(req.getParameter("id"));
        try{
            if("/demande/prise-en-charge".equals(action)){service.prendreEnCharge(id,u.getId(),req.getRemoteAddr());req.setAttribute("messageSucces","Prélèvement reçu, demande attribuée à votre compte.");}
            else if("/demande/enregistrer".equals(action)){String valeur=required(req,"valeur",10000);String unite=optional(req,"unite",30);String ref=optional(req,"reference",2000);String obs=optional(req,"interpretation",5000);service.enregistrer(id,u.getId(),valeur,unite,ref,obs,req.getRemoteAddr());req.setAttribute("messageSucces","Résultat transmis pour validation indépendante. Vous ne pouvez pas le valider vous-même.");}
            else{resp.sendError(404);return;}
        }catch(Exception e){log("Action laboratoire refusée/échouée",e);req.setAttribute("erreur",message(e));}
        try{LaborantinDao.Demande d=service.demande(id);req.setAttribute("demande",d);req.getRequestDispatcher(BASE+"demande.jsp").forward(req,resp);}catch(Exception e){resp.sendError(404);}
    }
    private boolean autorise(HttpServletRequest r,HttpServletResponse p)throws IOException{Object o=r.getSession(false)==null?null:r.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);if(!(o instanceof Utilisateur)){p.sendError(401);return false;}if(!Constantes.ROLE_LABORANTIN.equals(((Utilisateur)o).getRole())){p.sendError(403);return false;}if(r.getSession().getAttribute(Constantes.SESSION_CSRF)==null){byte[] b=new byte[32];RANDOM.nextBytes(b);r.getSession().setAttribute(Constantes.SESSION_CSRF,Base64.getUrlEncoder().withoutPadding().encodeToString(b));}return true;}
    private String route(HttpServletRequest r){String p=r.getPathInfo();return p==null||p.equals("/")?"accueil":p.substring(1).split("/")[0];}
    private long id(String s){try{long n=Long.parseLong(s);if(n>0)return n;}catch(Exception ignored){}throw new IllegalArgumentException("Identifiant invalide.");}
    private String required(HttpServletRequest r,String k,int max){String s=Echappement.texte(r.getParameter(k));if(s.isEmpty()||s.length()>max)throw new IllegalArgumentException("Le champ "+k+" est requis et limité à "+max+" caractères.");return s;}
    private String optional(HttpServletRequest r,String k,int max){String s=Echappement.texte(r.getParameter(k));if(s.length()>max)throw new IllegalArgumentException("Le champ "+k+" est trop long.");return s.isEmpty()?null:s;}
    private String message(Exception e){if(e instanceof IllegalArgumentException)return e.getMessage();if(e instanceof java.sql.SQLException){String s=((java.sql.SQLException)e).getSQLState();if("42501".equals(s))return e.getMessage();if("23514".equals(s))return "Données hors des limites autorisées.";}return "Opération impossible. Vérifiez la base PostgreSQL et réessayez.";}
}
