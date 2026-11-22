<div class="page-head">
    <div>
        <h2>Facturation et paiements</h2>
        <p class="muted">Factures, règlements, soldes restants et reçus</p>
    </div>
    <div class="page-actions">
        <button class="btn btn-primary" data-demo data-msg="Nouvelle facture (démonstration)">
            <svg class="ic"><use href="#i-plus"/></svg>Nouvelle facture
        </button>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher une facture ou un patient…">
        </div>
        <select class="input" data-filter>
            <option value="">Tous les statuts</option>
            <option value="payee">Payée</option>
            <option value="partiel">Partiellement payée</option>
            <option value="impayee">Impayée</option>
        </select>
    </div>

    <div class="empty-state">
        <svg class="ic"><use href="#i-card"/></svg>
        <p>Aucune facture enregistrée.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
