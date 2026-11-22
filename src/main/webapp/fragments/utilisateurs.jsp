<div class="page-head">
    <div>
        <h2>Utilisateurs</h2>
        <p class="muted">Création, modification et désactivation des comptes</p>
    </div>
    <div class="page-actions">
        <button class="btn btn-primary" data-demo data-msg="Formulaire de création (démonstration)">
            <svg class="ic"><use href="#i-plus"/></svg>Nouvel utilisateur
        </button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un nom ou un email…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les rôles</option>
            <option value="admin">Administrateur</option>
            <option value="medecin">Médecin</option>
            <option value="accueil">Accueil</option>
            <option value="labo">Laboratoire</option>
            <option value="caisse">Caisse</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-user"/></svg>
        <p>Aucun utilisateur enregistré.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
