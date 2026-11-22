<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String erreur = request.getParameter("erreur");
    String message = "";
    if (erreur != null) {
        switch (erreur) {
            case "champs":         message = "Veuillez renseigner tous les champs obligatoires."; break;
            case "validation":     message = request.getParameter("message"); break;
            case "email":          message = "Cette adresse email est déjà utilisée."; break;
            case "email-invalide": message = "L'adresse email saisie n'est pas valide."; break;
            case "motdepasse":     message = "Le mot de passe doit contenir au moins 8 caractères."; break;
            case "confirmation":   message = "Les deux mots de passe ne correspondent pas."; break;
            case "conditions":     message = "Vous devez accepter les conditions d'utilisation."; break;
            case "sexe":           message = "Le sexe sélectionné est invalide."; break;
            case "base":           message = "Erreur serveur, veuillez réessayer plus tard."; break;
            case "serveur":        message = "Erreur serveur, veuillez réessayer plus tard."; break;
            default:               message = "Une erreur est survenue, veuillez réessayer.";
        }
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inscription - Clinique Miracle</title>
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
                <a href="<%= ctx %>/connexion.jsp">Se connecter</a>                <a href="<%= ctx %>/inscription.jsp" class="active">S'inscrire</a>
            </nav>

            <h1 class="form-title">Créer un compte</h1>
            <p class="form-subtitle">Rejoignez l'espace de gestion des carnets patients</p>

            <% if (!message.isEmpty()) { %>
                <div class="alert alert-error"><%= message %></div>
            <% } %>
            <% if (request.getParameter("succes") != null) { %>
                <div class="alert alert-success">Votre compte a été créé avec succès.</div>
            <% } %>

            <form id="formInscription"
                  action="<%= ctx %>/inscription"
                  method="post" novalidate>

                <div class="form-grid">

                    <div class="form-group">
                        <label for="nom">Nom <span class="required">*</span></label>
                        <input type="text" id="nom" name="nom"
                               placeholder="Nom de famille" autocomplete="family-name" required>
                        <span class="field-error" data-for="nom"></span>
                    </div>

                    <div class="form-group">
                        <label for="prenoms">Prénom <span class="required">*</span></label>
                        <input type="text" id="prenoms" name="prenoms"
                               placeholder="Prénom" autocomplete="given-name" required>
                        <span class="field-error" data-for="prenoms"></span>
                    </div>

                    <div class="form-group">
                        <label for="dateNaissance">Date de naissance <span class="required">*</span></label>
                        <input type="date" id="dateNaissance" name="dateNaissance" required>
                        <span class="field-error" data-for="dateNaissance"></span>
                    </div>

                    <div class="form-group">
                        <label for="sexe">Sexe <span class="required">*</span></label>
                        <select id="sexe" name="sexe" required>
                            <option value="">-- Sélectionner --</option>
                            <option value="M">Masculin</option>
                            <option value="F">Féminin</option>
                        </select>
                        <span class="field-error" data-for="sexe"></span>
                    </div>

                    <div class="form-group">
                        <label for="telephone">Téléphone <span class="required">*</span></label>
                        <input type="tel" id="telephone" name="telephone"
                               placeholder="Ex : 01 23 45 67 89" autocomplete="tel" required>
                        <span class="field-error" data-for="telephone"></span>
                    </div>

                    <div class="form-group">
                        <label for="email">Adresse e-mail <span class="required">*</span></label>
                        <input type="email" id="email" name="email"
                               placeholder="votre@email.com" autocomplete="email" required>
                        <span class="field-error" data-for="email"></span>
                    </div>

                    <div class="form-group">
                        <label for="motDePasse">Mot de passe <span class="required">*</span></label>
                        <input type="password" id="motDePasse" name="motDePasse"
                               placeholder="8 caractères minimum" autocomplete="new-password" required>
                        <span class="field-error" data-for="motDePasse"></span>
                    </div>

                    <div class="form-group">
                        <label for="confirmation">Confirmer le mot de passe <span class="required">*</span></label>
                        <input type="password" id="confirmation" name="confirmation"
                               placeholder="Retapez le mot de passe" autocomplete="new-password" required>
                        <span class="field-error" data-for="confirmation"></span>
                    </div>

                    <div class="form-group full">
                        <label for="adresse">Adresse de résidence <span class="required">*</span></label>
                        <input type="text" id="adresse" name="adresse"
                               placeholder="Rue, quartier, ville" autocomplete="street-address" required>
                        <span class="field-error" data-for="adresse"></span>
                    </div>

                    <div class="form-group full">
                        <label class="checkbox-label">
                            <input type="checkbox" id="conditions" name="conditions" required>
                            J'accepte les conditions d'utilisation de l'application
                        </label>
                        <span class="field-error" data-for="conditions"></span>
                    </div>

                </div>

                <button type="submit" class="btn-primary">Créer mon compte</button>
            </form>

        </div>
    </main>

</div>

<script src="<%= ctx %>/js/auth.js?v=20261122"></script>
</body>
</html>
