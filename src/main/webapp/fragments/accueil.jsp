<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.mycompany.projetmedical.dao.AdminDashboardDao" %>
<%
    AdminDashboardDao.Statistiques stats =
            (AdminDashboardDao.Statistiques) request.getAttribute("statistiquesAdmin");
    String erreur = (String) request.getAttribute("erreurStatistiques");
%>
<section aria-labelledby="admin-welcome-title">
    <div class="page-head">
        <div>
            <h2 id="admin-welcome-title">Vue d’ensemble</h2>
            <p class="muted">Suivi de l’activité de la clinique aujourd’hui.</p>
        </div>
    </div>

    <% if (erreur != null) { %>
        <div class="panel" role="alert">
            <div class="empty-state">
                <svg class="ic" aria-hidden="true"><use href="#i-alert"/></svg>
                <p><%= erreur %></p>
                <button type="button" class="btn btn-primary" onclick="window.location.reload()">
                    Réessayer
                </button>
            </div>
        </div>
    <% } else if (stats != null) { %>
        <div class="cards" aria-label="Indicateurs de la clinique">
            <article class="card">
                <span class="card-ic"><svg class="ic" aria-hidden="true"><use href="#i-users"/></svg></span>
                <div><p class="card-label">Comptes utilisateurs</p>
                    <p class="card-value"><%= stats.utilisateurs() %></p>
                    <p class="card-meta"><%= stats.utilisateursActifs() %> comptes actifs</p></div>
            </article>
            <article class="card">
                <span class="card-ic"><svg class="ic" aria-hidden="true"><use href="#i-folder"/></svg></span>
                <div><p class="card-label">Patients enregistrés</p>
                    <p class="card-value"><%= stats.patients() %></p>
                    <p class="card-meta">Dossiers patients présents en base</p></div>
            </article>
            <article class="card">
                <span class="card-ic"><svg class="ic" aria-hidden="true"><use href="#i-calendar"/></svg></span>
                <div><p class="card-label">Rendez-vous aujourd’hui</p>
                    <p class="card-value"><%= stats.rendezVousJour() %></p>
                    <p class="card-meta">Rendez-vous non annulés</p></div>
            </article>
            <article class="card">
                <span class="card-ic"><svg class="ic" aria-hidden="true"><use href="#i-clipboard"/></svg></span>
                <div><p class="card-label">Consultations aujourd’hui</p>
                    <p class="card-value"><%= stats.consultationsJour() %></p>
                    <p class="card-meta">Consultations non annulées</p></div>
            </article>
        </div>

    <% } else { %>
        <div class="panel"><div class="empty-state"><p>Les statistiques sont en cours de chargement.</p></div></div>
    <% } %>
</section>
