<div class="page-head">
    <div>
        <h2>Carnets médicaux</h2>
        <p class="muted">Consultation des dossiers et de l'historique médical des patients</p>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un patient ou un n° de dossier…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les carnets</option>
            <option value="actif">Suivi en cours</option>
            <option value="archive">Archivé</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-folder"/></svg>
        <p>Aucun carnet médical disponible.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
