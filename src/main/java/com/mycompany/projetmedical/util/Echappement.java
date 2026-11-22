package com.mycompany.projetmedical.util;

/**
 * Échappement anti-XSS.
 * Tout ce qui provient de la base ou de l'utilisateur et qui est réinjecté
 * dans une JSP doit passer par cette méthode.
 */
public final class Echappement {

    private Echappement() {
    }

    /** Échappe HTML/XSS. Null -> chaîne vide. */
    public static String html(String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valeur.length() + 16);
        for (int i = 0; i < valeur.length(); i++) {
            char c = valeur.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Normalise une saisie : trim + espaces multiples réduits. */
    public static String texte(String valeur) {
        if (valeur == null) {
            return "";
        }
        return valeur.trim().replaceAll("\\s+", " ");
    }

    /** Email en minuscules et trim. */
    public static String email(String valeur) {
        if (valeur == null) {
            return "";
        }
        return valeur.trim().toLowerCase();
    }

    /** Valide le format d'un email. */
    public static boolean emailValide(String email) {
        if (email == null || email.length() < 5 || email.length() > 120) {
            return false;
        }
        int arobase = email.indexOf('@');
        return arobase > 0 && arobase == email.lastIndexOf('@')
                && arobase < email.length() - 1
                && email.indexOf('.', arobase) > arobase + 1;
    }
}
