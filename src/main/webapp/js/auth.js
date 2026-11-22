(function () {
    "use strict";

    var MESSAGES = {
        requis: "Ce champ est obligatoire.",
        email: "Adresse email invalide.",
        telephone: "Numéro de téléphone invalide (chiffres, espaces, + et -).",
        motDePasse: "Le mot de passe doit contenir au moins 8 caractères.",
        confirmation: "Les deux mots de passe ne correspondent pas.",
        dateNaissance: "Date de naissance invalide.",
        conditions: "Vous devez accepter les conditions d'utilisation."
    };

    function afficherErreur(champ, message) {
        champ.classList.add("invalid");
        var span = document.querySelector('.field-error[data-for="' + champ.id + '"]');
        if (span) {
            span.textContent = message;
        }
    }

    function effacerErreur(champ) {
        champ.classList.remove("invalid");
        var span = document.querySelector('.field-error[data-for="' + champ.id + '"]');
        if (span) {
            span.textContent = "";
        }
    }

    function estVide(champ) {
        if (champ.type === "checkbox") {
            return !champ.checked;
        }
        return champ.value.trim() === "";
    }

    function validerChamp(champ) {
        effacerErreur(champ);

        if (estVide(champ)) {
            afficherErreur(champ, MESSAGES.requis);
            return false;
        }

        switch (champ.id) {
            case "email":
                if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(champ.value.trim())) {
                    afficherErreur(champ, MESSAGES.email);
                    return false;
                }
                break;
            case "telephone":
                if (!/^[0-9+\-\s]{8,20}$/.test(champ.value.trim())) {
                    afficherErreur(champ, MESSAGES.telephone);
                    return false;
                }
                break;
            case "motDePasse":
                if (champ.value.length < 8) {
                    afficherErreur(champ, MESSAGES.motDePasse);
                    return false;
                }
                break;
            case "confirmation":
                var mdp = document.getElementById("motDePasse");
                if (mdp && champ.value !== mdp.value) {
                    afficherErreur(champ, MESSAGES.confirmation);
                    return false;
                }
                break;
            case "dateNaissance":
                var date = new Date(champ.value);
                if (isNaN(date.getTime()) || date > new Date()) {
                    afficherErreur(champ, MESSAGES.dateNaissance);
                    return false;
                }
                break;
            case "conditions":
                if (!champ.checked) {
                    afficherErreur(champ, MESSAGES.conditions);
                    return false;
                }
                break;
        }
        return true;
    }

    function initialiserFormulaire(form) {
        if (!form) {
            return;
        }

        var champs = form.querySelectorAll("input[required], select[required]");

        champs.forEach(function (champ) {
            champ.addEventListener("blur", function () {
                if (!estVide(champ) || champ.type === "checkbox") {
                    validerChamp(champ);
                }
            });
            champ.addEventListener("input", function () {
                if (champ.classList.contains("invalid")) {
                    effacerErreur(champ);
                }
            });
            if (champ.type === "checkbox") {
                champ.addEventListener("change", function () {
                    validerChamp(champ);
                });
            }
        });

        form.addEventListener("submit", function (event) {
            var valide = true;
            champs.forEach(function (champ) {
                if (!validerChamp(champ)) {
                    valide = false;
                }
            });

            if (!valide) {
                event.preventDefault();
                var premierInvalide = form.querySelector(".invalid");
                if (premierInvalide) {
                    premierInvalide.focus();
                }
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initialiserFormulaire(document.getElementById("formConnexion"));
        initialiserFormulaire(document.getElementById("formInscription"));
    });
})();
