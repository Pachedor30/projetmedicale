package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.UtilisateurService;
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
 * Changement OBLIGATOIRE du mot de passe provisoire (§4 du cahier des charges).
 *
 * Tant que le flag doit_changer_mot_de_passe est actif, l'utilisateur
 * est redirigé vers cette page à chaque connexion. Après changement,
 * le flag est levé et l'accès à l'espace est autorisé.
 */
@WebServlet(name = "ChangementMotDePasseServlet", urlPatterns = {"/changements-mdp"})
public class ChangementMotDePasseServlet extends HttpServlet {

    private final UtilisateurService service = new UtilisateurService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object u = sessionUtilisateur(request);
        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/connexion.jsp?erreur=acces");
            return;
        }
        request.getRequestDispatcher("/changements-mdp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Utilisateur u = sessionUtilisateur(request);
        if (u == null) {
            response.sendRedirect(request.getContextPath() + "/connexion.jsp?erreur=acces");
            return;
        }

        String ancien = request.getParameter("ancien");
        String nouveau = request.getParameter("nouveau");
        String confirmation = request.getParameter("confirmation");
        String ip = request.getRemoteAddr();

        if (ancien == null || ancien.isEmpty()
                || nouveau == null || nouveau.isEmpty()
                || confirmation == null || confirmation.isEmpty()) {
            erreur(response, "champs");
            return;
        }
        if (nouveau.length() < 8) {
            erreur(response, "court");
            return;
        }
        if (nouveau.equals(ancien)) {
            erreur(response, "identique");
            return;
        }
        if (!nouveau.equals(confirmation)) {
            erreur(response, "confirmation");
            return;
        }

        try {
            service.changerMotDePasse(u.getId(), ancien, nouveau, ip);
        } catch (UtilisateurService.ValidationException ve) {
            String m = ve.getMessage();
            if (m != null && m.contains("ancien")) {
                erreur(response, "ancien");
            } else {
                erreur(response, "validation");
            }
            return;
        } catch (SQLException e) {
            log("Échec de changement de mot de passe : " + e.getMessage());
            erreur(response, "base");
            return;
        }

        // Rafraîchit l'utilisateur en session (flag levé)
        try {
            Utilisateur misAJour = service.parId(u.getId());
            if (misAJour != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute(Constantes.SESSION_UTILISATEUR, misAJour);
                u = misAJour;
            }
        } catch (SQLException e) {
            log("Impossible de rafraîchir la session : " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/" + u.getLienEspace()
                + "?mdp=change");
    }

    private Utilisateur sessionUtilisateur(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute(Constantes.SESSION_UTILISATEUR);
        return (u instanceof Utilisateur) ? (Utilisateur) u : null;
    }

    private void erreur(HttpServletResponse response, String code) throws IOException {
        response.sendRedirect("changements-mdp.jsp?erreur=" + code);
    }
}
