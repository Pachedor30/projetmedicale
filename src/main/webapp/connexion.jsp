<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String erreur = request.getParameter("erreur");
    String message = "";
    if (erreur != null) {
        switch (erreur) {
            case "champs":       message = "Veuillez renseigner tous les champs."; break;
            case "identifiants": message = "Identifiant ou mot de passe incorrect."; break;
            case "compte":       message = "Aucun compte ne correspond à cet email."; break;
            case "verrouille":   message = "Compte verrouillé : trop de tentatives échouées. Réessayez dans 15 minutes."; break;
            case "inactif":      message = "Votre compte est désactivé. Contactez l'administrateur de la clinique."; break;
            case "base":         message = "Erreur serveur, veuillez réessayer plus tard."; break;
            case "session":      message = "Votre session a expiré, veuillez vous reconnecter."; break;
            case "acces":        message = "Vous n'avez pas les droits d'accès à cette page."; break;
            case "droits":       message = "Vous n'avez pas les droits d'accès à cette rubrique."; break;
            default:             message = "Une erreur est survenue, veuillez réessayer.";
        }
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion - Clinique Miracle</title>
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
                L'alliance de l'humain et de la rigueur pour un dossier médical
                numérique centralisé. Consultations, analyses et paiements réunis
                dans un espace unique, au service de chaque patient.
            </p>
        </div>
    </aside>

    <main class="form-panel">
        <div class="form-box">

            <nav class="auth-tabs">
                <a href="<%= ctx %>/connexion.jsp" class="active">Se connecter</a>
                <a href="<%= ctx %>/inscription.jsp">S'inscrire</a>
            </nav>

            <h1 class="form-title">Bienvenue</h1>
            <p class="form-subtitle">Connectez-vous à votre espace personnel</p>

            <% if (!message.isEmpty()) { %>
                <div class="alert alert-error"><%= message %></div>
            <% } %>
            <% if (request.getParameter("succes") != null || request.getParameter("inscription") != null) { %>
                <div class="alert alert-success">Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.</div>
            <% } %>
            <% if (request.getParameter("deconnexion") != null) { %>
                <div class="alert alert-success">Vous êtes déconnecté en toute sécurité.</div>
            <% } %>

            <form id="formConnexion"
                  action="<%= ctx %>/connexion"
                  method="post" novalidate>

                <div class="form-group">
                    <label for="email">Adresse e-mail</label>
                    <input type="email" id="email" name="email"
                           placeholder="votre@email.com"
                           autocomplete="email" required>
                    <span class="field-error" data-for="email"></span>
                </div>

                <div class="form-group">
                    <label for="motDePasse">Mot de passe</label>
                    <input type="password" id="motDePasse" name="motDePasse"
                           placeholder="••••••••"
                           autocomplete="current-password" required>
                    <span class="field-error" data-for="motDePasse"></span>
                </div>

                <p class="forgot-password">
                    <a href="#">Mot de passe oublié ?</a>
                </p>

                <button type="submit" class="btn-primary">Se connecter</button>
            </form>

        </div>
    </main>

</div>

<script src="<%= ctx %>/js/auth.js"></script>
</body>
</html>
