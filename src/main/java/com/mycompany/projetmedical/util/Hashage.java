package com.mycompany.projetmedical.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hachage sécurisé des mots de passe avec BCrypt (jamais en clair).
 * Le hash produit fait 60 caractères, vérifié par le LoginModule JAAS
 * chargé par Elytron et partagé avec les flux JDBC de l'application.
 */
public final class Hashage {

    /** Coût BCrypt : 12 = ~250 ms par vérification (sécurité + performance). */
    private static final int COUT = 12;

    private Hashage() {
    }

    /** Hache un mot de passe en clair. Sél unique générée automatiquement. */
    public static String hacher(String motDePasseClair) {
        if (motDePasseClair == null || motDePasseClair.isEmpty()) {
            throw new IllegalArgumentException("Mot de passe vide");
        }
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt(COUT));
    }

    /**
     * Vérifie un mot de passe en clair contre un hash BCrypt.
     * Comparaison à temps constant (pas de fuite par timing).
     */
    public static boolean verifier(String motDePasseClair, String hashAttendu) {
        if (motDePasseClair == null || hashAttendu == null || hashAttendu.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(motDePasseClair, hashAttendu);
        } catch (IllegalArgumentException e) {
            // Hash malformé : on ne fait pas échouer le serveur
            return false;
        }
    }

    /** Vrai si la valeur ressemble à un hash BCrypt exploitable. */
    public static boolean estHashBCrypt(String valeur) {
        return valeur != null
                && (valeur.startsWith("$2a$") || valeur.startsWith("$2b$") || valeur.startsWith("$2y$"))
                && valeur.length() == 60;
    }
}
