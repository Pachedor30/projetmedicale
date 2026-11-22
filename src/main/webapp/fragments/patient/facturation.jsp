<div class="page-head">
    <div>
        <h2>Mes factures et paiements</h2>
        <p class="muted">Vos factures, montants réglés et soldes restants</p>
    </div>
</div>

<div class="panel">
    <div class="toolbar">
        <div class="search-wrap">
            <svg class="ic"><use href="#i-search"/></svg>
            <input type="text" class="input" data-search placeholder="Rechercher une facture…">
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
        <p>Aucune facture à votre nom.<br>La base de données n'est pas encore connectée.</p>
    </div>
</div>
