<div class="page-head">
    <div>
        <h2>Journal d'activité</h2>
        <p class="muted">Historique des actions effectuées sur la plateforme</p>
    </div>
    <div class="page-actions">
        <button class="btn" data-demo data-msg="Export du journal (démonstration)">Exporter</button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher une action ou un utilisateur…">
        </div>
        <input type="date" class="input" aria-label="Date">
        <select class="input" data-filter>
            <option value="">Toutes les rubriques</option>
            <option value="patients">Patients</option>
            <option value="consultations">Consultations</option>
            <option value="analyses">Analyses</option>
            <option value="facturation">Facturation</option>
            <option value="systeme">Système</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-clock"/></svg>
        <p>Aucune activité enregistrée.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
