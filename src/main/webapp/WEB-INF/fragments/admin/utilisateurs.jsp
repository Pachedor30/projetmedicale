<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.mycompany.projetmedical.model.Utilisateur,com.mycompany.projetmedical.util.Echappement,com.mycompany.projetmedical.util.Constantes" %>
<%
List<Utilisateur> utilisateurs=(List<Utilisateur>)request.getAttribute("utilisateurs");
String csrf=(String)session.getAttribute(Constantes.SESSION_CSRF);
String motDePasse=(String)request.getAttribute("motDePasseProvisoire");
String recherche=Echappement.html(request.getParameter("q"));
String roleFiltre=request.getParameter("role"), statutFiltre=request.getParameter("statut");
%>
<section class="admin-page">
 <div class="page-head"><div><h2>Comptes du personnel</h2><p class="muted">Créez et gérez les comptes professionnels. Les comptes patient ne se créent pas depuis cet écran.</p></div></div>
 <%if(request.getAttribute("messageSucces")!=null){%><div class="alert-success"><%=Echappement.html((String)request.getAttribute("messageSucces"))%></div><%}%>
 <%if(request.getAttribute("erreur")!=null){%><div class="alert-error" role="alert"><%=Echappement.html((String)request.getAttribute("erreur"))%></div><%}%>
 <%if(motDePasse!=null){%><div class="alert-success credential-box"><strong>Mot de passe provisoire — à communiquer de manière sécurisée</strong><br><code><%=Echappement.html(motDePasse)%></code><br><small>Il sera demandé à l’utilisateur de le remplacer à sa première connexion.</small></div><%}%>
 <details class="panel admin-form" open><summary class="panel-head"><div><h3>Créer un compte du personnel</h3><p>Définissez le mot de passe initial. L’utilisateur devra le changer à sa première connexion.</p></div><span class="btn btn-primary">Nouveau compte</span></summary>
  <form method="post" action="<%=request.getContextPath()%>/admin/utilisateurs/creer" data-ajax-form class="form-grid"><input type="hidden" name="csrf" value="<%=Echappement.html(csrf)%>">
   <label>Prénom(s)<input name="prenoms" required maxlength="80" autocomplete="given-name"></label>
   <label>Nom<input name="nom" required maxlength="60" autocomplete="family-name"></label>
   <label>Adresse e-mail<input type="email" name="email" required maxlength="120" autocomplete="email"></label>
   <label>Rôle<select name="role" required><option value="">Choisir un rôle professionnel</option><option value="ADMIN">Administrateur</option><option value="ACCUEIL">Agent d’accueil</option><option value="MEDECIN">Médecin</option><option value="LABORANTIN">Laborantin</option><option value="CAISSIER_COMPTABLE">Caissier / Comptable</option></select></label>
   <label>Mot de passe initial<input type="password" name="motDePasse" required minlength="8" maxlength="128" autocomplete="new-password"></label>
   <label>Confirmer le mot de passe<input type="password" name="confirmationMotDePasse" required minlength="8" maxlength="128" autocomplete="new-password"></label>
   <label>Téléphone<input name="telephone" maxlength="20" autocomplete="tel"></label>
   <label>Sexe<select name="sexe"><option value="">Non renseigné</option><option value="M">Masculin</option><option value="F">Féminin</option></select></label>
   <label>Adresse<input name="adresse" maxlength="300"></label>
   <div class="form-actions"><button type="submit" class="btn btn-primary">Créer le compte</button></div>
  </form>
 </details>
 <div class="panel"><form class="toolbar" action="<%=request.getContextPath()%>/admin/utilisateurs" method="get" data-ajax-form>
  <input type="search" name="q" value="<%=recherche%>" placeholder="Rechercher par nom, e-mail ou téléphone">
  <select name="role"><option value="">Tous les rôles</option><%for(String r:Constantes.TOUS_ROLES){%><option value="<%=r%>" <%=r.equals(roleFiltre)?"selected":""%>><%=Echappement.html(Constantes.libelleRole(r))%></option><%}%></select>
  <select name="statut"><option value="">Tous les statuts</option><%for(String s:new String[]{"ACTIF","INACTIF","VERROUILLE"}){%><option value="<%=s%>" <%=s.equals(statutFiltre)?"selected":""%>><%=s%></option><%}%></select><button type="submit" class="btn">Filtrer</button>
 </form><div class="table-wrap"><table class="table"><thead><tr><th>Nom</th><th>E-mail</th><th>Rôle</th><th>Service</th><th>Statut</th><th>Actions</th></tr></thead><tbody>
 <%if(utilisateurs!=null)for(Utilisateur u:utilisateurs){%><tr><td><strong><%=Echappement.html(u.getPrenoms()+" "+u.getNom())%></strong><small><%=u.getTelephone()==null?"":Echappement.html(u.getTelephone())%></small></td><td><%=Echappement.html(u.getEmail())%></td><td><%=Echappement.html(Constantes.libelleRole(u.getRole()))%></td><td><%=u.getNomService()==null?"—":Echappement.html(u.getNomService())%></td><td><span class="chip"><%=Echappement.html(u.getStatut())%></span></td><td class="actions">
  <%if(!u.getId().equals(((Utilisateur)session.getAttribute(Constantes.SESSION_UTILISATEUR)).getId())){%><form method="post" action="<%=request.getContextPath()%>/admin/utilisateurs/statut" data-ajax-form><input type="hidden" name="csrf" value="<%=Echappement.html(csrf)%>"><input type="hidden" name="id" value="<%=u.getId()%>"><input type="hidden" name="statut" value="<%="ACTIF".equals(u.getStatut())?"INACTIF":"ACTIF"%>"><button class="btn btn-small" data-confirm-submit="Confirmer le changement de statut ?"><%="ACTIF".equals(u.getStatut())?"Désactiver":"Activer"%></button></form><form method="post" action="<%=request.getContextPath()%>/admin/utilisateurs/reinitialiser" data-ajax-form><input type="hidden" name="csrf" value="<%=Echappement.html(csrf)%>"><input type="hidden" name="id" value="<%=u.getId()%>"><button class="btn btn-small" data-confirm-submit="Réinitialiser l’accès de cet utilisateur ?">Réinitialiser l’accès</button></form><%}else{%><small>Compte connecté</small><%}%>
 </td></tr><%}%>
 </tbody></table><%if(utilisateurs==null||utilisateurs.isEmpty()){%><p class="empty-state">Aucun compte ne correspond aux filtres.</p><%}%></div></div>
</section>
