package com.mycompany.projetmedical.servlets;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.service.UtilisateurService;
import com.mycompany.projetmedical.util.Constantes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur d'inscription — PORTAIL PUBLIC PATIENT UNIQUEMENT.
 *
 * RÈGLE FONDAMENTALE : aucun membre du personnel ne peut s'inscrire seul.
 * Le rôle est FORCÉ à PATIENT ; tout paramètre de rôle envoyé par le
 * navigateur est ignoré et ne peut pas être contourné.
 */
@WebServlet(name = "InscriptionServlet", urlPatterns = {"/inscription"})
public class InscriptionServlet extends HttpServlet {

    private final UtilisateurService service = new UtilisateurService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object u = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);
        if (u instanceof Utilisateur) {
            response.sendRedirect(request.getContextPath()
                    + "/" + ((Utilisateur) u).getLienEspace());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/inscription.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // ----- Lecture des saisies -----
        String nom = request.getParameter("nom");
        String prenoms = request.getParameter("prenoms");
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String confirmation = request.getParameter("confirmation");
        String dateNaissance = request.getParameter("dateNaissance");
        String sexe = request.getParameter("sexe");
        String telephone = request.getParameter("telephone");
        String adresse = request.getParameter("adresse");
        String conditions = request.getParameter("conditions");
        String ip = request.getRemoteAddr();

        // NOTE : le paramètre « profil » n'est JAMAIS lu.
        // Le rôle est imposé par la règle métier (Constantes.ROLE_INSCRIPTION_PUBLIQUE).

        // ----- Validations de forme -----
        if (nom == null || nom.trim().isEmpty()
                || prenoms == null || prenoms.trim().isEmpty()) {
            erreur(response, "champs", "nom et prénoms sont obligatoires");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            erreur(response, "champs", "l'adresse e-mail est obligatoire");
            return;
        }
        if (motDePasse == null || motDePasse.length() < 8) {
            erreur(response, "motdepasse", "le mot de passe doit contenir au moins 8 caractères");
            return;
        }
        if (!motDePasse.equals(confirmation)) {
            erreur(response, "confirmation", "les deux mots de passe ne correspondent pas");
            return;
        }
        if (!"on".equals(conditions)) {
            erreur(response, "conditions", "vous devez accepter les conditions d'utilisation");
            return;
        }
        if (sexe != null && !sexe.isEmpty() && !"M".equals(sexe) && !"F".equals(sexe)) {
            erreur(response, "sexe", "sexe invalide");
            return;
        }

        // ----- Création (rôle forcé côté service) -----
        try {
            service.creerComptePatient(nom.trim(), prenoms.trim(), email,
                    motDePasse, dateNaissance, sexe, telephone, adresse, ip);
        } catch (UtilisateurService.ValidationException ve) {
            erreur(response, "validation", ve.getMessage());
            return;
        } catch (SQLException e) {
            log("Échec d'inscription : " + e.getMessage());
            erreur(response, "base", "une erreur technique est survenue");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/connexion.jsp?inscription=ok");
    }

    private void erreur(HttpServletResponse response, String code, String message)
            throws IOException {
        response.sendRedirect("inscription.jsp?erreur=" + code
                + "&message=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
    }
}
