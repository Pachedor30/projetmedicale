<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.mycompany.projetmedical.dao.AdminGestionDao,com.mycompany.projetmedical.util.Echappement" %>
<%List<AdminGestionDao.RoleClinique> roles=(List<AdminGestionDao.RoleClinique>)request.getAttribute("roles");%>
<section class="admin-page">
 <div class="page-head"><div><h2>Rôles de l’application</h2><p class="muted">Les six rôles sont définis par le modèle de sécurité et ne peuvent pas être créés ou supprimés depuis l’interface.</p></div></div>
 <%if(request.getAttribute("erreur")!=null){%><div class="alert-error" role="alert"><%=Echappement.html((String)request.getAttribute("erreur"))%></div><%}%>
 <div class="panel"><div class="table-wrap"><table class="table"><thead><tr><th>Code</th><th>Rôle</th><th>Description</th><th>Catégorie</th></tr></thead><tbody>
 <%if(roles!=null)for(AdminGestionDao.RoleClinique r:roles){%><tr><td><code><%=Echappement.html(r.code())%></code></td><td><strong><%=Echappement.html(r.libelle())%></strong></td><td><%=r.description()==null?"—":Echappement.html(r.description())%></td><td><%=r.interne()?"Personnel":"Patient"%></td></tr><%}%>
 </tbody></table><%if(roles==null||roles.isEmpty()){%><p class="empty-state">Les rôles n’ont pas pu être lus depuis PostgreSQL.</p><%}%></div></div>
 <div class="panel access-note"><strong>Gestion des accès</strong><p>Le serveur applique les autorisations par rôle à chaque rubrique. Les comptes du personnel sont créés avec l’un de ces rôles prédéfinis.</p></div>
</section>
