<div class="page-head">
    <div>
        <h2>Paramètres</h2>
        <p class="muted">Configuration générale de l'application</p>
    </div>
</div>

<form class="panel" onsubmit="return false;">
    <div class="panel-head"><h3>Informations de l'établissement</h3></div>
    <div class="form-grid">
        <div class="form-group">
            <label for="p-nom">Nom de l'établissement</label>
            <input type="text" class="input" id="p-nom" placeholder="Nom de la clinique">
        </div>
        <div class="form-group">
            <label for="p-email">Email de contact</label>
            <input type="email" class="input" id="p-email" placeholder="contact@exemple.com">
        </div>
        <div class="form-group">
            <label for="p-tel">Téléphone</label>
            <input type="tel" class="input" id="p-tel" placeholder="00 00 00 00 00">
        </div>
        <div class="form-group">
            <label for="p-adresse">Adresse</label>
            <input type="text" class="input" id="p-adresse" placeholder="Adresse de l'établissement">
        </div>
        <div class="form-group">
            <label for="p-devise">Devise de facturation</label>
            <select class="input" id="p-devise">
                <option value="">-- Sélectionner --</option>
                <option>FCFA</option>
                <option>EUR</option>
                <option>USD</option>
            </select>
        </div>
        <div class="form-group">
            <label for="p-fuseau">Fuseau horaire</label>
            <select class="input" id="p-fuseau">
                <option value="">-- Sélectionner --</option>
                <option>(GMT) Abidjan</option>
                <option>(GMT+1) Douala</option>
            </select>
        </div>
    </div>
    <div class="form-footer">
        <button type="button" class="btn btn-primary" data-demo data-msg="Paramètres indisponibles tant que la base de données n'est pas connectée.">Enregistrer</button>
        <button type="reset" class="btn">Annuler</button>
    </div>
</form>
