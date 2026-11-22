package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.AuthentificationService;
import com.mycompany.projetmedical.util.Constantes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur d'authentification (portail unique, §4).
 *
 * Utilise request.login() : l'authentification est réellement déléguée au
 * realm Elytron JAAS configuré dans standalone.xml (vérification BCrypt
 * contre la table medical.utilisateurs). Ce servlet pilote les règles
 * métier (tentatives, verrouillage, journal) et la redirection.
 */
@WebServlet(name = "ConnexionServlet", urlPatterns = {"/connexion"})
public class ConnexionServlet extends HttpServlet {

    private final AuthentificationService auth = new AuthentificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Déjà connecté -> on renvoie vers son espace
        Object u = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);
        if (u instanceof Utilisateur) {
            response.sendRedirect(request.getContextPath()
                    + "/" + ((Utilisateur) u).getLienEspace());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/connexion.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String ip = request.getRemoteAddr();

        AuthentificationService.Resultat preparation;
        try {
            preparation = auth.verifierTentative(email, motDePasse, ip);
        } catch (SQLException e) {
            log("Échec de pré-connexion : " + e.getMessage());
            response.sendRedirect(request.getContextPath()
                    + "/connexion.jsp?erreur=base");
            return;
        }

        if (!preparation.isReussie()) {
            redirigerErreur(response, preparation.getCodeErreur());
            return;
        }

        Utilisateur u = preparation.getUtilisateur();

        // ----- Authentification réelle via Elytron JAAS (JDBC / BCrypt) -----
        try {
            request.login(u.getEmail(), motDePasse);
        } catch (ServletException e) {
            // Mot de passe incorrect : on incmente le compteur de tentatives
            try {
                auth.enregistrerEchec(u, ip);
            } catch (SQLException sql) {
                log("Échec d'enregistrement de tentative : " + sql.getMessage());
            }
            redirigerErreur(response, "identifiants");
            return;
        }

        if (!request.isUserInRole(u.getRole())) {
            // Le realm n'a pas retourné le rôle attendu : on refuse par sécurité
            request.logout();
            redirigerErreur(response, "identifiants");
            return;
        }

        // ----- Session applicative -----
        HttpSession session = request.getSession(true);
        session.setAttribute(Constantes.SESSION_UTILISATEUR, u);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        try {
            auth.enregistrerSucces(u, ip);
        } catch (SQLException e) {
            log("Échec de journalisation de connexion : " + e.getMessage());
        }

        // Changement de mot de passe provisoire obligatoire (§4)
        if (u.isDoitChangerMotDePasse()) {
            response.sendRedirect(request.getContextPath() + "/changements-mdp.jsp");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/" + u.getLienEspace());
    }

    private void redirigerErreur(HttpServletResponse response, String code) throws IOException {
        response.sendRedirect("connexion.jsp?erreur=" + code);
    }

}
