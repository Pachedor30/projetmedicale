package com.mycompany.projetmedical.security;

import org.mindrot.jbcrypt.BCrypt;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/** JAAS authenticator for the Clinique Elytron realm. */
public final class CliniqueLoginModule implements LoginModule {
    private static final String DATASOURCE = "java:jboss/datasources/CarnetsDS";
    private static final String SQL = "SELECT mot_de_passe_hash, role_code FROM public.utilisateurs "
            + "WHERE lower(email)=lower(?) AND statut='ACTIF'";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String email;
    private char[] password;
    private UserPrincipal userPrincipal;
    private Roles rolePrincipal;
    private boolean authenticated;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (subject == null || callbackHandler == null) {
            throw new LoginException("Configuration JAAS incomplète.");
        }
        NameCallback name = new NameCallback("Email");
        PasswordCallback pass = new PasswordCallback("Mot de passe", false);
        try {
            callbackHandler.handle(new Callback[]{name, pass});
            email = name.getName();
            password = pass.getPassword();
        } catch (Exception e) {
            throw new LoginException("Impossible de lire les identifiants.");
        } finally {
            pass.clearPassword();
        }
        if (email == null || email.isBlank() || password == null || password.length == 0) {
            clearPassword();
            throw new LoginException("Identifiants invalides.");
        }

        try {
            DataSource ds = (DataSource) new InitialContext().lookup(DATASOURCE);
            try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(SQL)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        clearPassword();
                        throw new LoginException("Identifiants invalides.");
                    }
                    String hash = rs.getString("mot_de_passe_hash");
                    String role = rs.getString("role_code");
                    if (!verify(password, hash)) {
                        clearPassword();
                        throw new LoginException("Identifiants invalides.");
                    }
                    userPrincipal = new UserPrincipal(email.trim());
                    rolePrincipal = new Roles(role);
                    authenticated = true;
                    clearPassword();
                    return true;
                }
            }
        } catch (NamingException | SQLException e) {
            clearPassword();
            LoginException failure = new LoginException("Erreur de vérification des identifiants.");
            failure.initCause(e);
            throw failure;
        }
    }

    private static boolean verify(char[] password, String hash) {
        if (hash == null || password == null) return false;
        try {
            return BCrypt.checkpw(new String(password), hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!authenticated) return false;
        if (subject.isReadOnly()) throw new LoginException("Subject JAAS en lecture seule.");
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        return true;
    }

    @Override
    public boolean abort() {
        if (!authenticated) return false;
        logout();
        return true;
    }

    @Override
    public boolean logout() {
        if (subject != null && !subject.isReadOnly()) {
            if (userPrincipal != null) subject.getPrincipals().remove(userPrincipal);
            if (rolePrincipal != null) subject.getPrincipals().remove(rolePrincipal);
        }
        authenticated = false;
        userPrincipal = null;
        rolePrincipal = null;
        clearPassword();
        return true;
    }

    private void clearPassword() {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
            password = null;
        }
    }

    public record UserPrincipal(String name) implements Principal {
        @Override public String getName() { return name; }
    }

    /** Elytron maps this principal's simple class name to the Roles attribute. */
    public record Roles(String name) implements Principal {
        @Override public String getName() { return name; }
    }
}
