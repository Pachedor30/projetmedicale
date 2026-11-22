<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.mycompany.projetmedical.dao.CaisseDao,com.mycompany.projetmedical.util.Echappement" %>
<% CaisseDao.Stats s=(CaisseDao.Stats)request.getAttribute("stats"); List<CaisseDao.Paiement> ps=(List<CaisseDao.Paiement>)request.getAttribute("paiements"); %>
<section class="caisse-page">
 <div class="page-heading"><div><p class="eyebrow">GESTION FINANCIÈRE</p><h2>Tableau de bord</h2><p>Factures, encaissements et soldes à recouvrer.</p></div></div>
 <div class="cards finance-stats" aria-label="Statistiques financières">
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-card"/></svg></span><div><p class="card-label">Factures ouvertes</p><p class="card-value"><%=s==null?0:s.facturesOuvertes()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-clock"/></svg></span><div><p class="card-label">Impayées</p><p class="card-value"><%=s==null?0:s.impayees()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-card"/></svg></span><div><p class="card-label">Partiellement payées</p><p class="card-value"><%=s==null?0:s.partielles()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-card"/></svg></span><div><p class="card-label">Solde à recouvrer</p><p class="card-value"><%=s==null?"0.00":s.resteDu().toPlainString()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-check"/></svg></span><div><p class="card-label">Encaissé aujourd’hui</p><p class="card-value"><%=s==null?"0.00":s.encaisseAujourdhui().toPlainString()%></p></div></article>
  <article class="card"><span class="card-ic"><svg class="ic"><use href="#i-chart"/></svg></span><div><p class="card-label">Facturé aujourd’hui</p><p class="card-value"><%=s==null?"0.00":s.facturesAujourdhui().toPlainString()%></p></div></article>
 </div>
 <div class="panel"><div class="panel-head"><div><h3>Paiements récents</h3><p>Dernière semaine · données PostgreSQL.</p></div></div><div class="table-wrap"><table class="table"><thead><tr><th>Date</th><th>Reçu</th><th>Patient</th><th>Facture</th><th>Montant</th><th>Mode</th></tr></thead><tbody><%if(ps!=null)for(CaisseDao.Paiement p:ps){%><tr><td><%=p.date()%></td><td><a href="<%=request.getContextPath()%>/caisse/recu?id=<%=p.id()%>" target="_blank">REC-<%=p.id()%></a></td><td><%=Echappement.html(p.numeroPatient()+" · "+p.patient())%></td><td><a href="<%=request.getContextPath()%>/caisse/facture?id=<%=p.factureId()%>" data-fragment-link><%=Echappement.html(p.numeroFacture())%></a></td><td><%=p.montant().toPlainString()%></td><td><%=Echappement.html(p.mode())%></td></tr><%}%></tbody></table><%if(ps==null||ps.isEmpty()){%><p class="empty-state">Aucun encaissement récent.</p><%}%></div></div>
</section>
