package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.dao.AdminDashboardDao;
import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Constantes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Prépare le fragment d'accueil de l'administrateur avec les données PostgreSQL. */
@WebServlet("/admin/accueil")
public class AdminDashboardServlet extends HttpServlet {

    private final AdminDashboardDao dashboard = new AdminDashboardDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object sessionUser = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);
        if (!(sessionUser instanceof Utilisateur utilisateur)
                || !Constantes.ROLE_ADMIN.equals(utilisateur.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            request.setAttribute("statistiquesAdmin", dashboard.charger());
        } catch (SQLException e) {
            getServletContext().log("Échec du chargement du tableau de bord administrateur", e);
            request.setAttribute("erreurStatistiques",
                    "Les statistiques ne sont pas disponibles. Vérifiez la connexion à la base de données.");
        }
        request.getRequestDispatcher("/fragments/accueil.jsp").forward(request, response);
    }
}
