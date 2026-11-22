<div class="page-head">
    <div>
        <h2>Analyses médicales</h2>
        <p class="muted">Demandes d'examens, suivi des statuts et enregistrement des résultats</p>
    </div>
    <div class="page-actions">
        <button class="btn btn-primary" data-demo data-msg="Nouvelle demande d'analyse (démonstration)">
            <svg class="ic"><use href="#i-plus"/></svg>Nouvelle demande
        </button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un patient, un type d'analyse…">
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
        <p>Aucune demande d'analyse enregistrée.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
