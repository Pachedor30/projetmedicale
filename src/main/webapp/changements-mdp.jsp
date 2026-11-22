<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Réservé aux utilisateurs authentifiés (filtre + security-constraint)
    Object utilisateur = session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/connexion.jsp?erreur=acces");
        return;
    }
    String erreur = request.getParameter("erreur");
    String message = "";
    if (erreur != null) {
        switch (erreur) {
            case "champs":      message = "Veuillez renseigner tous les champs."; break;
            case "ancien":      message = "L'ancien mot de passe est incorrect."; break;
            case "court":       message = "Le nouveau mot de passe doit contenir au moins 8 caractères."; break;
            case "identique":   message = "Le nouveau mot de passe doit être différent de l'ancien."; break;
            case "confirmation":message = "Les deux mots de passe ne correspondent pas."; break;
            case "base":        message = "Erreur serveur, veuillez réessayer plus tard."; break;
            default:            message = "Une erreur est survenue, veuillez réessayer.";
        }
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Changement de mot de passe - Clinique Miracle</title>
    <link rel="stylesheet" href="<%= ctx %>/css/auth.css">
</head>
<body>

<div class="auth-layout">

    <aside class="brand-panel">
        <div class="brand-inner">
            <div class="brand-logo">
                <img class="brand-logo-img" src="<%= ctx %>/img/logo-clinique.png" alt="Logo Clinique Miracle — Votre santé, notre priorité">
            </div>
            <p class="brand-quote">« Prendre soin de chaque patient,<br>à chaque étape du parcours. »</p>
            <p class="brand-text">
                Pour votre sécurité, votre mot de passe provisoire doit être
                remplacé par un mot de passe personnel avant d'accéder à votre espace.
            </p>
        </div>
    </aside>

    <main class="form-panel">
        <div class="form-box">

            <h1 class="form-title">Changer votre mot de passe</h1>
            <p class="form-subtitle">Étape obligatoire — changement requis à la première connexion</p>

            <div class="alert alert-info">
                Vous vous êtes connecté avec un mot de passe provisoire.
                Définissez un nouveau mot de passe d'au moins 8 caractères pour continuer.
            </div>

            <% if (!message.isEmpty()) { %>
                <div class="alert alert-error"><%= message %></div>
            <% } %>

            <form id="formChangementMdp"
                  action="<%= ctx %>/changements-mdp"
                  method="post" novalidate>

                <div class="form-group">
                    <label for="ancien">Mot de passe provisoire <span class="required">*</span></label>
                    <input type="password" id="ancien" name="ancien"
                           placeholder="••••••••" autocomplete="current-password" required>
                    <span class="field-error" data-for="ancien"></span>
                </div>

                <div class="form-group">
                    <label for="nouveau">Nouveau mot de passe <span class="required">*</span></label>
                    <input type="password" id="nouveau" name="nouveau"
                           placeholder="8 caractères minimum" autocomplete="new-password" required>
                    <span class="field-error" data-for="nouveau"></span>
                </div>

                <div class="form-group">
                    <label for="confirmation">Confirmer le nouveau mot de passe <span class="required">*</span></label>
                    <input type="password" id="confirmation" name="confirmation"
                           placeholder="Retapez le nouveau mot de passe" autocomplete="new-password" required>
                    <span class="field-error" data-for="confirmation"></span>
                </div>

                <button type="submit" class="btn-primary">Enregistrer et continuer</button>
            </form>

            <p class="forgot-password" style="margin-top:16px;text-align:center;">
                <a href="<%= ctx %>/deconnexion">Se déconnecter</a>
            </p>

        </div>
    </main>

</div>

<script src="<%= ctx %>/js/auth.js"></script>
</body>
</html>
