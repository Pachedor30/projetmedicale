<div class="page-head">
    <div>
        <h2>Rendez-vous</h2>
        <p class="muted">Programmation et suivi des rendez-vous de la clinique</p>
    </div>
    <div class="page-actions">
        <button class="btn btn-primary" data-demo data-msg="Nouveau rendez-vous (démonstration)">
            <svg class="ic"><use href="#i-plus"/></svg>Nouveau rendez-vous
        </button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un patient ou un médecin…">
        </div>
        <input type="date" class="input" aria-label="Date">
        <select class="input" data-filter>
            <option value="">Tous les statuts</option>
            <option value="a_venir">À venir</option>
            <option value="effectue">Effectué</option>
            <option value="annule">Annulé</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-calendar"/></svg>
        <p>Aucun rendez-vous enregistré.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
