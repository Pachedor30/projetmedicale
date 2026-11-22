<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    Object utilisateur = session.getAttribute("utilisateur");
    if (!(utilisateur instanceof com.mycompany.projetmedical.model.Utilisateur)) {
        response.sendRedirect(ctx + "/connexion.jsp?erreur=acces");
        return;
    }
    com.mycompany.projetmedical.model.Utilisateur connecte =
            (com.mycompany.projetmedical.model.Utilisateur) utilisateur;
    if (!"PATIENT".equals(connecte.getRole())) {
        response.sendRedirect(ctx + "/" + connecte.getLienEspace());
        return;
    }
    String nomUtilisateur = connecte.getPrenoms() + " " + connecte.getNom();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mon espace patient - Clinique Miracle</title>
    <link rel="stylesheet" href="<%= ctx %>/css/dashboard.css">
</head>
<body data-ctx="<%= ctx %>" data-frag="patient/" data-section="accueil" data-title="Dashboard">

<%@ include file="/WEB-INF/sprite.jspf" %>

<div class="dash-layout">

    <aside class="sidebar" id="sidebar">
        <div class="sidebar-brand">
            <img src="<%= ctx %>/img/logo-clinique.png" alt="Clinique Miracle — Votre santé, notre priorité">
        </div>

        <nav class="sidebar-nav">
            <div class="cat" data-cat="mon-dossier">
                <button type="button" class="cat-toggle accent-a" aria-expanded="true">
                    <span>Mon dossier</span>
                    <svg class="ic chev"><use href="#i-chevron"/></svg>
                </button>
                <ul class="cat-menu">
                    <li><a href="#" data-section="accueil" data-title="Dashboard" class="active">
                        <svg class="ic"><use href="#i-home"/></svg><span>Dashboard</span></a></li>
                    <li><a href="#" data-section="carnets" data-title="Mon carnet médical">
                        <svg class="ic"><use href="#i-folder"/></svg><span>Mon carnet médical</span></a></li>
                    <li><a href="#" data-section="consultations" data-title="Mes consultations">
                        <svg class="ic"><use href="#i-clipboard"/></svg><span>Mes consultations</span></a></li>
                    <li><a href="#" data-section="analyses" data-title="Mes analyses">
                        <svg class="ic"><use href="#i-flask"/></svg><span>Mes analyses</span></a></li>
                    <li><a href="#" data-section="rendezvous" data-title="Mes rendez-vous">
                        <svg class="ic"><use href="#i-calendar"/></svg><span>Mes rendez-vous</span></a></li>
                </ul>
            </div>

            <div class="cat" data-cat="paiements">
                <button type="button" class="cat-toggle accent-b" aria-expanded="true">
                    <span>Mes paiements</span>
                    <svg class="ic chev"><use href="#i-chevron"/></svg>
                </button>
                <ul class="cat-menu">
                    <li><a href="#" data-section="facturation" data-title="Mes factures et paiements">
                        <svg class="ic"><use href="#i-card"/></svg><span>Mes factures et paiements</span></a></li>
                    <li><a href="#" data-section="notifications" data-title="Mes notifications">
                        <svg class="ic"><use href="#i-bell"/></svg><span>Notifications</span></a></li>
                </ul>
            </div>

            <div class="cat" data-cat="compte">
                <button type="button" class="cat-toggle accent-c" aria-expanded="true">
                    <span>Compte</span>
                    <svg class="ic chev"><use href="#i-chevron"/></svg>
                </button>
                <ul class="cat-menu">
                    <li><a href="#" data-section="profil" data-title="Mon profil">
                        <svg class="ic"><use href="#i-user"/></svg><span>Mon profil</span></a></li>
                    <li class="sep"></li>
                    <li><a href="<%= ctx %>/deconnexion" class="logout-link">
                        <svg class="ic"><use href="#i-logout"/></svg><span>Déconnexion</span></a></li>
                </ul>
            </div>
        </nav>
    </aside>

    <div class="main">

        <header class="topbar">
            <button type="button" class="burger" aria-label="Menu">
                <svg class="ic"><use href="#i-menu"/></svg>
            </button>
            <h1 id="page-title">Dashboard</h1>
            <div class="topbar-user">
                <span class="avatar"><%= nomUtilisateur.substring(0, 1).toUpperCase() %></span>
                <span class="user-info">
                    <strong><%= nomUtilisateur %></strong>
                    <small><%= connecte.getLibelleRole() %></small>
                </span>
            </div>
        </header>

        <main class="content" id="contenu">
            <div class="loader"><span class="spinner"></span>Chargement…</div>
        </main>

        <footer class="footer">© 2026 - Clinique Miracle</footer>

    </div>
</div>

<div class="sidebar-overlay" id="overlay"></div>
<div class="toast-container" id="toasts"></div>

<script src="<%= ctx %>/js/dashboard.js"></script>
</body>
</html>
