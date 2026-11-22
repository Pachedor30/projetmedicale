package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.UtilisateurService;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Echappement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

/** Formulaires réels de gestion réservés à l'administrateur. */
@WebServlet("/admin/*")
public class AdminGestionServlet extends HttpServlet {
    private static final String BASE = "/WEB-INF/fragments/admin/";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UtilisateurService utilisateurs = new UtilisateurService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Utilisateur admin = admin(req, resp);
        if (admin == null) return;
        jeton(req);
        String page = route(req);
        if (!page.equals("utilisateurs") && !page.equals("roles")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            charger(req, page);
            req.getRequestDispatcher(BASE + page + ".jsp").forward(req, resp);
        } catch (Exception e) {
            log("Échec de chargement de la rubrique administrative " + page, e);
            erreur(req, "Impossible de charger les données enregistrées dans PostgreSQL.");
            req.getRequestDispatcher(BASE + page + ".jsp").forward(req, resp);
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Utilisateur admin = admin(req, resp);
        if (admin == null) return;
        req.setCharacterEncoding("UTF-8");
        if (!jetonValide(req)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String page = route(req);
        String[] path = (req.getPathInfo() == null ? "" : req.getPathInfo()).split("/");
        String action = path.length > 2 ? path[2] : "";
        try {
            switch (page + "/" + action) {
                case "utilisateurs/creer" -> creerUtilisateur(req, admin);
                case "utilisateurs/statut" -> changerStatut(req, admin);
                case "utilisateurs/reinitialiser" -> reinitialiser(req, admin);
                default -> { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            }
        } catch (Exception e) {
            log("Action administrative refusée ou échouée : " + action, e);
            erreur(req, message(e));
        }
        try {
            charger(req, page);
            req.getRequestDispatcher(BASE + page + ".jsp").forward(req, resp);
        } catch (Exception e) {
            log("Échec de rechargement après action administrative", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void creerUtilisateur(HttpServletRequest req, Utilisateur admin) throws Exception {
        Utilisateur u = new Utilisateur();
        u.setNom(requis(req, "nom", 60));
        u.setPrenoms(requis(req, "prenoms", 80));
        u.setEmail(Echappement.email(requis(req, "email", 120)));
        u.setRole(requis(req, "role", 30));
        u.setSexe(optionnel(req, "sexe", 1));
        u.setTelephone(optionnel(req, "telephone", 20));
        u.setAdresse(optionnel(req, "adresse", 2000));
        u.setServiceId(null);
        String motDePasse = requis(req, "motDePasse", 128);
        String confirmation = requis(req, "confirmationMotDePasse", 128);
        if (!motDePasse.equals(confirmation))
            throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
        utilisateurs.creerComptePro(u, motDePasse, admin.getId(), req.getRemoteAddr());
        req.setAttribute("messageSucces", "Le compte professionnel a été créé. L’utilisateur devra changer ce mot de passe à sa première connexion.");
    }

    private void changerStatut(HttpServletRequest req, Utilisateur admin) throws Exception {
        long cible = id(req.getParameter("id"));
        String statut = requis(req, "statut", 20);
        utilisateurs.changerStatut(cible, statut, admin.getId(), req.getRemoteAddr());
        req.setAttribute("messageSucces", "Le statut du compte a été mis à jour.");
    }

    private void reinitialiser(HttpServletRequest req, Utilisateur admin) throws Exception {
        String temporaire = utilisateurs.reinitialiserAcces(id(req.getParameter("id")),
                admin.getId(), req.getRemoteAddr());
        req.setAttribute("motDePasseProvisoire", temporaire);
        req.setAttribute("messageSucces", "Accès réinitialisé. Le mot de passe provisoire ne sera affiché qu’une fois.");
    }

    private void charger(HttpServletRequest req, String page) throws SQLException {
        switch (page) {
            case "utilisateurs" -> {
                req.setAttribute("utilisateurs", utilisateurs.lister(req.getParameter("role"),
                        req.getParameter("statut"), req.getParameter("q")));
                req.setAttribute("roles", Constantes.TOUS_ROLES);
            }
            case "roles" -> req.setAttribute("roles", new com.mycompany.projetmedical.dao.AdminGestionDao().roles());
            default -> throw new IllegalArgumentException("Rubrique administrative inconnue.");
        }
    }

    private Utilisateur admin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object value = req.getSession(false) == null ? null
                : req.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);
        if (!(value instanceof Utilisateur u) || !Constantes.ROLE_ADMIN.equals(u.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return u;
    }

    private void jeton(HttpServletRequest req) {
        if (req.getSession().getAttribute(Constantes.SESSION_CSRF) == null) {
            byte[] token = new byte[32]; RANDOM.nextBytes(token);
            req.getSession().setAttribute(Constantes.SESSION_CSRF,
                    Base64.getUrlEncoder().withoutPadding().encodeToString(token));
        }
    }

    private boolean jetonValide(HttpServletRequest req) {
        Object token = req.getSession().getAttribute(Constantes.SESSION_CSRF);
        return token instanceof String s && s.equals(req.getParameter("csrf"));
    }

    private String route(HttpServletRequest req) {
        String p = req.getPathInfo();
        if (p == null || p.equals("/")) return "accueil";
        String[] pieces = p.substring(1).split("/");
        return pieces[0];
    }

    private String requis(HttpServletRequest req, String key, int max) {
        String value = Echappement.texte(req.getParameter(key));
        if (value.isEmpty() || value.length() > max) throw new IllegalArgumentException("Champ invalide : " + key + ".");
        return value;
    }

    private String optionnel(HttpServletRequest req, String key, int max) {
        String value = Echappement.texte(req.getParameter(key));
        if (value.length() > max) throw new IllegalArgumentException("Valeur trop longue : " + key + ".");
        return value.isEmpty() ? null : value;
    }

    private long id(String value) {
        try { long n = Long.parseLong(value); if (n > 0) return n; }
        catch (Exception ignored) { }
        throw new IllegalArgumentException("Identifiant invalide.");
    }

    private void erreur(HttpServletRequest req, String message) { req.setAttribute("erreur", message); }

    private String message(Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof UtilisateurService.ValidationException)
            return e.getMessage();
        if (e instanceof SQLException sql) {
            if ("23505".equals(sql.getSQLState())) return "Cette adresse courriel ou ce nom de service est déjà utilisé.";
            if ("23503".equals(sql.getSQLState())) return "La référence choisie n’existe plus dans la base.";
        }
        return "Enregistrement impossible. Vérifiez les informations et la connexion PostgreSQL.";
    }
}
