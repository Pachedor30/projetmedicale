<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.mycompany.projetmedical.dao.PatientPortailDao,com.mycompany.projetmedical.util.Echappement" %>
<% PatientPortailDao.Profil p=(PatientPortailDao.Profil)request.getAttribute("profil"); PatientPortailDao.Stats s=(PatientPortailDao.Stats)request.getAttribute("stats"); List<PatientPortailDao.Rdv> rs=(List<PatientPortailDao.Rdv>)request.getAttribute("rendezVous"); List<PatientPortailDao.Notification> ns=(List<PatientPortailDao.Notification>)request.getAttribute("notifications"); %>
<section class="patient-page">
 <div class="page-heading"><div><p class="eyebrow">MON ESPACE PERSONNEL</p><h2>Bonjour <%=Echappement.html(p.prenoms())%></h2><p>Retrouvez ici vos informations médicales et administratives.</p></div><span class="patient-number">N° patient <strong><%=Echappement.html(p.numero())%></strong></span></div>
 <%if(request.getAttribute("messageSucces")!=null){%><div class="alert-success"><%=Echappement.html((String)request.getAttribute("messageSucces"))%></div><%}%>
 <div class="cards patient-stats" aria-label="Statistiques personnelles">
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-calendar"/></svg></span><div><p class="card-label">Rendez-vous à venir</p><p class="card-value"><%=s==null?0:s.prochainsRdv()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-clipboard"/></svg></span><div><p class="card-label">Consultations réalisées</p><p class="card-value"><%=s==null?0:s.consultations()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-card"/></svg></span><div><p class="card-label">Factures à régler</p><p class="card-value"><%=s==null?0:s.facturesOuvertes()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-card"/></svg></span><div><p class="card-label">Solde restant</p><p class="card-value"><%=s==null?"0.00":s.solde().toPlainString()%></p></div></article>
 </div>
 <div class="patient-two-col">
  <div class="panel"><h3>Mes prochains rendez-vous</h3><%boolean found=false;if(rs!=null)for(PatientPortailDao.Rdv r:rs)if(("EN_ATTENTE".equals(r.statut())||"CONFIRME".equals(r.statut()))&&!r.date().isBefore(java.time.LocalDate.now())){found=true;%><article class="patient-list-item"><b><%=r.date()%> · <%=r.debut()%></b><span><%=Echappement.html(r.medecin())%></span><small><%=Echappement.html(r.statut())%></small></article><%}%><%if(!found){%><p class="empty-state">Aucun rendez-vous à venir.</p><%}%></div>
  <div class="panel"><h3>Notifications récentes</h3><%boolean note=false;if(ns!=null)for(PatientPortailDao.Notification n:ns)if(!n.lue()){note=true;%><article class="patient-list-item"><b><%=Echappement.html(n.titre())%></b><span><%=Echappement.html(n.message())%></span><small><%=n.date()%></small></article><%}%><%if(!note){%><p class="empty-state">Aucune notification non lue.</p><%}%></div>
 </div>
 <p class="patient-privacy">Votre espace n’affiche que les données rattachées à votre compte.</p>
</section>
