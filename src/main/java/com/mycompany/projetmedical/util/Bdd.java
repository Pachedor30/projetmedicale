package com.mycompany.projetmedical.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Accès à la source de données (datasource WildFly CarnetsDS).
 * Toute la JDBC passe par cette classe : une seule porte d'entrée,
 * les DAO n'obtiennent jamais la connexion autrement.
 */
public final class Bdd {

    private static DataSource dataSource;

    private Bdd() {
    }

    private static synchronized DataSource source() throws SQLException {
        if (dataSource == null) {
            try {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(Constantes.JNDI_DATASOURCE);
            } catch (NamingException e) {
                throw new SQLException(
                        "Datasource introuvable : " + Constantes.JNDI_DATASOURCE
                                + " (WildFly non démarré ou datasource non configuré)", e);
            }
        }
        return dataSource;
    }

    /** Ouvre une connexion à la base. À fermer obligatoirement (try-with-resources). */
    public static Connection connexion() throws SQLException {
        return source().getConnection();
    }
}
