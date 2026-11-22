package com.mycompany.projetmedical.filters;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Constantes;
import com.mycompany.projetmedical.util.Journal;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtre d'AUTORISATION par rôle (§4 : « Un utilisateur ne doit pas accéder
 * à l'espace d'un autre rôle » + « ne doit pas contourner en modifiant une URL »).
 *
 * Chaque URL sensible est associée aux rôles autorisés. Toute requête
 * non conforme est refusée côté serveur, même si l'utilisateur connaît
 * exactement l'adresse.
 */
@WebFilter(filterName = "AutorisationFilter", urlPatterns = {"/*"})
public class AutorisationFilter implements Filter {

    /**
     * Table de correspondance URL -> rôles autorisés.
     * Une entrée « * » signifie : tous les rôles authentifiés.
     */
    private static final Map<String, String[]> REGLES = new HashMap<>();

    static {
        // ---- Espace administrateur ----
        regle("/dashboard.jsp", Constantes.ROLE_ADMIN);
        regle("/admin/", Constantes.ROLE_ADMIN);

        // Fragments réservés à l'ADMIN (uniquement à la racine de /fragments/)
        regle("/fragments/utilisateurs", Constantes.ROLE_ADMIN);
        regle("/fragments/roles", Constantes.ROLE_ADMIN);
        regle("/fragments/services", Constantes.ROLE_ADMIN);
        regle("/fragments/prestations", Constantes.ROLE_ADMIN);
        regle("/fragments/sauvegardes", Constantes.ROLE_ADMIN);
        regle("/fragments/parametres", Constantes.ROLE_ADMIN);
        regle("/fragments/journal", Constantes.ROLE_ADMIN);
        regle("/fragments/rapports", Constantes.ROLE_ADMIN);

        // ---- Espace accueil ----
        regle("/espace-accueil.jsp", Constantes.ROLE_ACCUEIL);
        // L'administrateur dispose aussi des outils administratifs d'accueil
        // (patients et rendez-vous), sans accès aux espaces cliniques.
        regle("/accueil/", Constantes.ROLE_ADMIN, Constantes.ROLE_ACCUEIL);
        regle("/fragments/accueil/", Constantes.ROLE_ADMIN, Constantes.ROLE_ACCUEIL);

        // ---- Espace médecin ----
        regle("/espace-medecin.jsp", Constantes.ROLE_MEDECIN);
        regle("/medecin/", Constantes.ROLE_MEDECIN);
        regle("/fragments/medecin/", Constantes.ROLE_MEDECIN);

        // ---- Espace laborantin ----
        regle("/espace-labo.jsp", Constantes.ROLE_LABORANTIN);
        regle("/labo/", Constantes.ROLE_LABORANTIN);
        regle("/fragments/labo/", Constantes.ROLE_LABORANTIN);

        // ---- Espace caisse ----
        regle("/espace-caisse.jsp", Constantes.ROLE_CAISSE);
        regle("/caisse/", Constantes.ROLE_CAISSE);
        regle("/fragments/caisse/", Constantes.ROLE_CAISSE);

        // ---- Espace patient : strictement son propre dossier ----
        regle("/espace-patient.jsp", Constantes.ROLE_PATIENT);
        regle("/patient/", Constantes.ROLE_PATIENT);
        regle("/fragments/patient/", Constantes.ROLE_PATIENT);
        regle("/mes-", Constantes.ROLE_PATIENT);
    }

    private static void regle(String motif, String... roles) {
        REGLES.put(motif, roles);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // table statique, rien à faire
    }

    @Override
    public void doFilter(ServletRequest requete, ServletResponse reponse, FilterChain chaine)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) requete;
        HttpServletResponse rep = (HttpServletResponse) reponse;

        String chemin = req.getRequestURI().substring(req.getContextPath().length());

        String[] rolesAutorises = rolesAutorises(chemin);
        if (rolesAutorises == null) {
            // URL non répertoriée : on laisse passer (le filtre d'authentification
            // a déjà exigé une session) — le contrôle se fait alors dans le servlet.
            chaine.doFilter(requete, reponse);
            return;
        }

        Object attribut = req.getSession(false) == null ? null
                : req.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);

        if (!(attribut instanceof Utilisateur)) {
            rep.sendRedirect(req.getContextPath() + "/connexion.jsp?erreur=acces");
            return;
        }

        Utilisateur u = (Utilisateur) attribut;
        for (String role : rolesAutorises) {
            if (role.equals(u.getRole())) {
                chaine.doFilter(requete, reponse);
                return;
            }
        }

        // Refus : on journalise la tentative puis on renvoie vers l'espace légitime
        Journal.ecrire(u.getId(), Journal.ACCES_REFUSE, "url", null,
                "Accès refusé à " + chemin + " (rôle " + u.getRole() + ")", false,
                req.getRemoteAddr());
        rep.sendRedirect(req.getContextPath() + "/" + u.getLienEspace() + "?erreur=droits");
    }

    /** Rôles autorisés pour une URL, ou null si aucune règle ne s'applique. */
    private String[] rolesAutorises(String chemin) {
        String meilleurCle = null;
        for (String cle : REGLES.keySet()) {
            if (chemin.startsWith(cle)
                    && (meilleurCle == null || cle.length() > meilleurCle.length())) {
                meilleurCle = cle;
            }
        }
        return meilleurCle == null ? null : REGLES.get(meilleurCle);
    }

    @Override
    public void destroy() {
        // rien à libérer
    }
}
