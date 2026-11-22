<div class="page-head">
    <div>
        <h2>Mes analyses</h2>
        <p class="muted">Vos demandes d'examens et les comptes rendus disponibles</p>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un type d'analyse…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les statuts</option>
            <option value="attente">En attente</option>
            <option value="disponible">Résultat disponible</option>
            <option value="valide">Validé</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-flask"/></svg>
        <p>Aucune analyse à votre nom.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
