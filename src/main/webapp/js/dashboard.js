(function () {
    "use strict";

    var ctx = document.body.dataset.ctx || "";
    var prefixe = document.body.dataset.frag || "";
    var contenu = document.getElementById("contenu");
    var titre = document.getElementById("page-title");
    var sidebar = document.getElementById("sidebar");
    var overlay = document.getElementById("overlay");
    var sectionCourante = "";

    /* ==================== Navigation ==================== */

    function marquerActif(section) {
        document.querySelectorAll(".cat-menu a[data-section]").forEach(function (a) {
            a.classList.toggle("active", a.dataset.section === section);
        });
    }

    function erreurChargement(section, titrePage) {
        contenu.innerHTML =
            '<div class="panel"><div class="empty-state">' +
            '<svg class="ic"><use href="#i-alert"/></svg>' +
            "<p>Impossible de charger cette rubrique.</p>" +
            '<button class="btn btn-primary" id="retry">' +
            '<svg class="ic"><use href="#i-refresh"/></svg>Réessayer</button>' +
            "</div></div>";
        var retry = document.getElementById("retry");
        if (retry) {
            retry.onclick = function () {
                sectionCourante = "";
                charger(section, titrePage);
            };
        }
    }

    function recuperer(url) {
        return fetch(url, { cache: "no-store" }).then(function (rep) {
            if (!rep.ok) {
                throw new Error(rep.status);
            }
            return rep.text();
        });
    }

    function charger(section, titrePage) {
        if (section === sectionCourante) {
            return;
        }
        sectionCourante = section;
        marquerActif(section);
        titre.textContent = titrePage;
        contenu.innerHTML = '<div class="loader"><span class="spinner"></span>Chargement…</div>';

        var urlEspace = prefixe === "accueil/"
            ? ctx + "/accueil/" + section
            : prefixe === "admin/"
                ? ["patients", "rendezvous", "agenda", "historique"].indexOf(section) >= 0
                    ? ctx + "/accueil/" + section
                    : ctx + "/admin/" + section
            : prefixe === "medecin/"
                ? ctx + "/medecin/" + section
            : prefixe === "labo/"
                ? ctx + "/labo/" + section
            : prefixe === "caisse/"
                ? ctx + "/caisse/" + section
            : prefixe === "patient/"
                ? ctx + "/patient/" + section
            : section === "accueil" && !prefixe
                ? ctx + "/admin/accueil"
                : ctx + "/fragments/" + prefixe + section + ".jsp";
        var urlBase = ctx + "/fragments/" + section + ".jsp";

        var promesse = recuperer(urlEspace).catch(function () {
            // Repli sur le fragment partagé (racine) s'il n'existe pas dans l'espace
            if (prefixe && prefixe !== "admin/" && prefixe !== "accueil/" && prefixe !== "medecin/" && prefixe !== "labo/" && prefixe !== "caisse/" && prefixe !== "patient/" && urlEspace !== urlBase) {
                return recuperer(urlBase);
            }
            throw new Error("introuvable");
        });

        promesse
            .then(function (html) {
                contenu.innerHTML = html;
                contenu.querySelectorAll("[data-chart]").forEach(dessinerGraphique);
                window.scrollTo({ top: 0, behavior: "smooth" });
            })
            .catch(function () {
                erreurChargement(section, titrePage);
            });
    }

    /* ==================== Graphiques SVG ==================== */

    function dessinerGraphique(el) {
        var type = el.dataset.chart;
        var valeurs = (el.dataset.values || "").split(",").map(Number);
        var labels = (el.dataset.labels || "").split(",");
        var couleur = el.dataset.color || "#159B91";
        var W = 640, H = 230, gl = 44, gr = 14, gt = 14, gb = 30;
        var iw = W - gl - gr, ih = H - gt - gb;
        var max = Math.max.apply(null, valeurs) * 1.15 || 1;
        var svg = '<svg viewBox="0 0 ' + W + " " + H + '" xmlns="http://www.w3.org/2000/svg">';

        // grille + axe Y
        for (var i = 0; i <= 4; i++) {
            var y = gt + (ih / 4) * i;
            var val = Math.round(max - (max / 4) * i);
            svg += '<line x1="' + gl + '" y1="' + y + '" x2="' + (W - gr) + '" y2="' + y + '" stroke="#e8efed" stroke-width="1"/>';
            svg += '<text x="' + (gl - 8) + '" y="' + (y + 4) + '" text-anchor="end" font-size="11" fill="#77857f">' + val + "</text>";
        }

        var n = valeurs.length;
        var pas = n > 1 ? iw / (n - 1) : 0;
        var points = valeurs.map(function (v, i) {
            return [gl + pas * i, gt + ih - (v / max) * ih];
        });

        if (type === "bar") {
            var largeur = Math.min(46, (iw / n) * 0.55);
            points.forEach(function (p, i) {
                var h = gt + ih - p[1];
                svg += '<rect x="' + (p[0] - largeur / 2) + '" y="' + p[1] + '" width="' + largeur +
                    '" height="' + h + '" rx="4" fill="' + couleur + '" opacity="' + (i === n - 1 ? 1 : 0.78) + '"/>';
            });
        } else {
            var d = points.map(function (p, i) { return (i ? "L" : "M") + p[0].toFixed(1) + " " + p[1].toFixed(1); }).join(" ");
            svg += '<defs><linearGradient id="g-' + sectionCourante + '" x1="0" y1="0" x2="0" y2="1">' +
                '<stop offset="0%" stop-color="' + couleur + '" stop-opacity="0.30"/>' +
                '<stop offset="100%" stop-color="' + couleur + '" stop-opacity="0.02"/></linearGradient></defs>';
            svg += '<path d="' + d + " L" + points[n - 1][0] + " " + (gt + ih) + " L" + points[0][0] + " " + (gt + ih) +
                ' Z" fill="url(#g-' + sectionCourante + ')"/>';
            svg += '<path d="' + d + '" fill="none" stroke="' + couleur + '" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>';
            points.forEach(function (p) {
                svg += '<circle cx="' + p[0] + '" cy="' + p[1] + '" r="3.5" fill="#fff" stroke="' + couleur + '" stroke-width="2"/>';
            });
        }

        // axe X
        labels.forEach(function (lab, i) {
            var x = gl + pas * i;
            svg += '<text x="' + x + '" y="' + (H - 8) + '" text-anchor="middle" font-size="11" fill="#77857f">' + lab + "</text>";
        });

        svg += "</svg>";
        el.innerHTML = svg;
    }

    /* ==================== Recherche & filtres ==================== */

    function filtrer(panel) {
        if (!panel) return;
        var q = ((panel.querySelector("[data-search]") || {}).value || "").toLowerCase().trim();
        var statut = ((panel.querySelector("[data-filter]") || {}).value || "").toLowerCase();
        var visibles = 0;
        panel.querySelectorAll("tbody tr").forEach(function (tr) {
            var okQ = !q || tr.textContent.toLowerCase().indexOf(q) !== -1;
            var okS = !statut || (tr.dataset.status || "") === statut;
            var visible = okQ && okS;
            tr.hidden = !visible;
            if (visible) visibles++;
        });
        var vide = panel.querySelector(".empty-state");
        if (vide) vide.hidden = visibles > 0;
        var wrap = panel.querySelector(".table-wrap");
        if (wrap) wrap.hidden = visibles === 0;
    }

    /* ==================== Toasts ==================== */

    function toast(message, type) {
        var box = document.getElementById("toasts");
        var t = document.createElement("div");
        t.className = "toast" + (type ? " " + type : "");
        t.textContent = message;
        box.appendChild(t);
        setTimeout(function () { t.remove(); }, 3600);
    }
    window.toast = toast;

    /* ==================== Sidebar mobile ==================== */

    function fermerSidebar() {
        sidebar.classList.remove("open");
        overlay.classList.remove("show");
    }

    /* ==================== Écoute globale ==================== */

    document.addEventListener("click", function (e) {
        if (e.target.closest("#add-line")) {
            var list = document.getElementById("invoice-lines");
            if (list) { var row=list.querySelector(".invoice-line").cloneNode(true); row.querySelector("select").value=""; row.querySelector("input").value="1"; row.querySelector(".line-price").textContent="Sélectionnez une prestation"; list.appendChild(row); }
            return;
        }
        if (e.target.closest(".remove-line")) {
            var rows=document.querySelectorAll("#invoice-lines .invoice-line"); if(rows.length>1)e.target.closest(".invoice-line").remove();
            return;
        }
        var fragmentLink = e.target.closest("a[data-fragment-link]");
        if (fragmentLink) {
            e.preventDefault();
            contenu.innerHTML = '<div class="loader"><span class="spinner"></span>Chargement…</div>';
            fetch(fragmentLink.href, {cache:"no-store"}).then(function(r){if(!r.ok)throw new Error(r.status);return r.text();}).then(function(html){contenu.innerHTML=html;var path=new URL(fragmentLink.href,window.location.href).pathname;var area=prefixe.replace(/\/$/,"");var index=path.indexOf("/"+area+"/");var part=index<0?"":path.substring(index+area.length+2).split("/")[0];if(area==="caisse"){sectionCourante=part==="patient"?"patients":part==="facture"||part==="nouvelle-facture"?"facturation":part||"accueil";}else if(area==="medecin"){sectionCourante=part==="patient"?"patients":part||"patients";}else if(area==="labo"){sectionCourante=part==="demande"?"analyses":part||"accueil";}else{sectionCourante=part||"patients";}marquerActif(sectionCourante);titre.textContent=sectionCourante.replace(/-/g," ");}).catch(function(){toast("Impossible de charger la rubrique.","error");});
            return;
        }
        var lien = e.target.closest("a[data-section]");
        if (lien) {
            e.preventDefault();
            charger(lien.dataset.section, lien.dataset.title);
            fermerSidebar();
            return;
        }

        var toggle = e.target.closest(".cat-toggle");
        if (toggle) {
            var cat = toggle.closest(".cat");
            cat.classList.toggle("collapsed");
            toggle.setAttribute("aria-expanded", cat.classList.contains("collapsed") ? "false" : "true");
            try {
                localStorage.setItem("cat-" + cat.dataset.cat, cat.classList.contains("collapsed") ? "off" : "on");
            } catch (err) { /* ignore */ }
            return;
        }

        if (e.target.closest(".burger")) {
            sidebar.classList.toggle("open");
            overlay.classList.toggle("show");
            return;
        }
        if (e.target.closest("#overlay")) {
            fermerSidebar();
            return;
        }

        var demo = e.target.closest("[data-demo]");
        if (demo) {
            toast(demo.dataset.msg || "Action disponible une fois la base de données connectée.", demo.dataset.type || "");
            return;
        }

        var confirme = e.target.closest("[data-confirm]");
        if (confirme) {
            if (window.confirm(confirme.dataset.confirm)) {
                toast(confirme.dataset.msg || "Opération effectuée (démonstration).", "success");
            }
        }
    });

    document.addEventListener("input", function (e) {
        if (e.target.matches("[data-search]")) {
            filtrer(e.target.closest(".panel"));
        }
    });

    document.addEventListener("change", function (e) {
        if (e.target.matches("[data-filter]")) {
            filtrer(e.target.closest(".panel"));
        }
        if (e.target.matches("select[name=prestationId]")) {
            var prix=e.target.selectedOptions[0].dataset.price||"";var output=e.target.closest(".invoice-line").querySelector(".line-price");if(output)output.textContent=prix?"Tarif unitaire : "+prix:"Sélectionnez une prestation";
        }
    });

    document.addEventListener("submit", function (e) {
        var form = e.target.closest("form[data-ajax-form]");
        if (!form) return;
        e.preventDefault();
        var submit = form.querySelector("button[type=submit],button:not([type])");
        if (submit && submit.dataset.confirmSubmit && !window.confirm(submit.dataset.confirmSubmit)) return;
        var data = new FormData(form);
        var url = new URL(form.action, window.location.href);
        var method = (form.method || "GET").toUpperCase();
        var opts = { method: method, cache: "no-store" };
        if (method === "GET") data.forEach(function(v,k){url.searchParams.set(k,v);});
        else {
            // Ces formulaires ne téléversent aucun fichier. Envoyer du
            // application/x-www-form-urlencoded permet aux Servlets de lire
            // les champs et le jeton CSRF avec request.getParameter().
            opts.body = new URLSearchParams(data);
            opts.headers = { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" };
        }
        if(submit){submit.disabled=true;}
        fetch(url.toString(),opts).then(function(r){if(!r.ok)throw new Error(r.status);return r.text();}).then(function(html){
            contenu.innerHTML=html;
            contenu.querySelectorAll("[data-chart]").forEach(dessinerGraphique);
            sectionCourante="";
        }).catch(function(){toast("La requête a échoué. Rechargez la rubrique et réessayez.","error");if(submit)submit.disabled=false;});
    });

    /* ==================== Initialisation ==================== */

    document.querySelectorAll(".cat").forEach(function (cat) {
        var etat = null;
        try { etat = localStorage.getItem("cat-" + cat.dataset.cat); } catch (err) { /* ignore */ }
        if (etat === "off") {
            cat.classList.add("collapsed");
            cat.querySelector(".cat-toggle").setAttribute("aria-expanded", "false");
        }
    });

    charger(document.body.dataset.section || "accueil",
            document.body.dataset.title || "Dashboard");
})();
