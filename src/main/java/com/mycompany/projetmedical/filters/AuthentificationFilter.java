package com.mycompany.projetmedical.filters;

import com.mycompany.projetmedical.model.Utilisateur;
import com.mycompany.projetmedical.util.Constantes;
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

/**
 * Filtre d'AUTHENTIFICATION (§17 : contrôle côté serveur).
 *
 * Toute requête vers une page ou un service protégé doit venir d'une
 * session valide. Sans session -> redirection vers la page de connexion.
 *
 * Les ressources publiques (connexion, inscription, CSS, JS, images)
 * sont déclarées en exemptées et passent directement.
 */
@WebFilter(filterName = "AuthentificationFilter", urlPatterns = {"/*"})
public class AuthentificationFilter implements Filter {

    /** Préfixes d'URL ouverts à tous (aucune session requise). */
    private static final String[] PUBLIC = {
        "/accueil.jsp",
        "/connexion.jsp",
        "/connexion",
        "/inscription.jsp",
        "/inscription",
        "/mot-de-passe-oublie",
        "/favicon"
    };

    /** Extensions de ressources statiques réellement publiques. */
    private static final String[] STATIQUES = {
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg",
        ".ico", ".woff", ".woff2", ".ttf", ".map"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // rien à initialiser
    }

    @Override
    public void doFilter(ServletRequest requete, ServletResponse reponse, FilterChain chaine)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) requete;
        HttpServletResponse rep = (HttpServletResponse) reponse;

        String chemin = req.getRequestURI().substring(req.getContextPath().length());

        if (estPublic(chemin)) {
            chaine.doFilter(requete, reponse);
            return;
        }

        // Le conteneur (Elytron) a-t-il déjà authentifié la requête ?
        boolean conteneurAuthentifie = req.getUserPrincipal() != null;

        Object attribut = req.getSession(false) == null ? null
                : req.getSession(false).getAttribute(Constantes.SESSION_UTILISATEUR);

        if (!conteneurAuthentifie || !(attribut instanceof Utilisateur)) {
            // Session absente ou expirée -> connexion
            rep.sendRedirect(req.getContextPath() + "/connexion.jsp?erreur=acces");
            return;
        }

        chaine.doFilter(requete, reponse);
    }

    /** Vrai si l'URL appartient au portail public. */
    private boolean estPublic(String chemin) {
        // La racine ne rend publique que la page d'accueil, pas toutes les URL.
        if (chemin.isEmpty() || "/".equals(chemin)) {
            return true;
        }
        for (String p : PUBLIC) {
            if (chemin.equals(p) || chemin.startsWith(p)) {
                return true;
            }
        }
        // Uniquement les ressources statiques (jamais une JSP)
        String bas = chemin.toLowerCase(java.util.Locale.ROOT);
        for (String ext : STATIQUES) {
            if (bas.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // rien à libérer
    }
}
