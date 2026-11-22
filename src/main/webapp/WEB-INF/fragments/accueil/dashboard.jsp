<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.mycompany.projetmedical.dao.AccueilDao,com.mycompany.projetmedical.util.Echappement" %>
<% AccueilDao.Indicateurs i=(AccueilDao.Indicateurs)request.getAttribute("indicateurs"); %>
<section class="accueil-page">
 <div class="page-heading">
  <div><p class="eyebrow">ESPACE AGENT D’ACCUEIL</p><h2>Bonjour, bienvenue à l’accueil</h2><p>Suivez l’activité administrative et organisez les rendez-vous.</p></div>
 </div>
 <div class="cards accueil-stats" aria-label="Statistiques de l’accueil">
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-users"/></svg></span><div><p class="card-label">Patients enregistrés</p><p class="card-value"><%=i==null?0:i.patientsTotal()%></p><p class="card-meta">Au total</p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-folder"/></svg></span><div><p class="card-label">Nouveaux patients</p><p class="card-value"><%=i==null?0:i.patientsAujourdhui()%></p><p class="card-meta">Aujourd’hui</p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-calendar"/></svg></span><div><p class="card-label">Rendez-vous du jour</p><p class="card-value"><%=i==null?0:i.rendezVousAujourdhui()%></p><p class="card-meta">Hors rendez-vous annulés</p></div></article>
 </div>
 <p class="access-note">Les diagnostics, prescriptions et autres informations cliniques ne sont pas accessibles depuis cet espace.</p>
</section>
