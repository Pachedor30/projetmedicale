<div class="page-head">
    <div>
        <h2>Mon profil</h2>
        <p class="muted">Informations personnelles de l'administrateur</p>
    </div>
</div>

<form class="panel" onsubmit="return false;">
    <div class="panel-head"><h3>Informations générales</h3></div>
    <div class="form-grid">
        <div class="form-group">
            <label for="f-nom">Nom</label>
            <input type="text" class="input" id="f-nom" placeholder="Nom">
        </div>
        <div class="form-group">
            <label for="f-prenom">Prénom</label>
            <input type="text" class="input" id="f-prenom" placeholder="Prénom">
        </div>
        <div class="form-group">
            <label for="f-email">Adresse email</label>
            <input type="email" class="input" id="f-email" placeholder="votre@email.com">
        </div>
        <div class="form-group">
            <label for="f-tel">Téléphone</label>
            <input type="tel" class="input" id="f-tel" placeholder="00 00 00 00 00">
        </div>
        <div class="form-group full">
            <label for="f-role">Rôle</label>
            <input type="text" class="input" id="f-role" placeholder="Rôle attribué" readonly>
            <small>Le rôle est attribué par l'administration du système.</small>
        </div>
    </div>
    <div class="form-footer">
        <button type="button" class="btn btn-primary" data-demo data-msg="Profil indisponible tant que la base de données n'est pas connectée.">Enregistrer les modifications</button>
    </div>
</form>

<form class="panel" onsubmit="return false;">
    <div class="panel-head"><h3>Changer le mot de passe</h3></div>
    <div class="form-grid">
        <div class="form-group full">
            <label for="f-anc">Mot de passe actuel</label>
            <input type="password" class="input" id="f-anc" placeholder="••••••••">
        </div>
        <div class="form-group">
            <label for="f-nouv">Nouveau mot de passe</label>
            <input type="password" class="input" id="f-nouv" placeholder="8 caractères minimum">
        </div>
        <div class="form-group">
            <label for="f-conf">Confirmer le nouveau mot de passe</label>
            <input type="password" class="input" id="f-conf" placeholder="Retapez le mot de passe">
        </div>
    </div>
    <div class="form-footer">
        <button type="button" class="btn btn-primary" data-demo data-msg="Changement de mot de passe indisponible tant que la base de données n'est pas connectée.">Mettre à jour le mot de passe</button>
    </div>
</form>
