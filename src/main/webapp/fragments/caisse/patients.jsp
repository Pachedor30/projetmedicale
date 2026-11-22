<div class="page-head">
    <div>
        <h2>Gestion des patients</h2>
        <p class="muted">Recherche des patients pour facturation (accès en lecture)</p>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher par nom, téléphone ou n° dossier…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les sexes</option>
            <option value="m">Masculin</option>
            <option value="f">Féminin</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-users"/></svg>
        <p>Aucun patient enregistré.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
