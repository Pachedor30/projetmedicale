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

/**
 * Déconnexion sécurisée : invalidation de la session applicative,
 * logout du conteneur (Elytron) et journalisation.
 */
@WebServlet(name = "DeconnexionServlet", urlPatterns = {"/deconnexion"})
public class DeconnexionServlet extends HttpServlet {

    private final AuthentificationService auth = new AuthentificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object u = session.getAttribute(Constantes.SESSION_UTILISATEUR);
            if (u instanceof Utilisateur) {
                auth.journaliserDeconnexion((Utilisateur) u, request.getRemoteAddr());
            }
            session.invalidate();
        }
        try {
            request.logout();
        } catch (ServletException e) {
            log("Logout conteneur échoué : " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/connexion.jsp?deconnexion=ok");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
