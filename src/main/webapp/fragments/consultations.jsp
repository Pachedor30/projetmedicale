<div class="page-head">
    <div>
        <h2>Consultations</h2>
        <p class="muted">Suivi des consultations : motifs, observations, diagnostics et prescriptions</p>
    </div>
    <div class="page-actions">
        <button class="btn btn-primary" data-demo data-msg="Nouvelle consultation (démonstration)">
            <svg class="ic"><use href="#i-plus"/></svg>Nouvelle consultation
        </button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher un patient, un médecin ou un motif…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les statuts</option>
            <option value="terminee">Terminée</option>
            <option value="en_cours">En cours</option>
            <option value="planifiee">Planifiée</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-clipboard"/></svg>
        <p>Aucune consultation enregistrée.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
