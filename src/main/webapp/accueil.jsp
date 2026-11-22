<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Clinique Miracle : découvrez les services et créez votre espace patient sécurisé.">
    <meta name="theme-color" content="#ffffff">
    <title>Clinique Miracle — Bienvenue</title>
    <link rel="stylesheet" href="<%= ctx %>/css/accueil.css">
</head>
<body>
    <div class="utility-bar">
        <div class="container utility-inner">
            <div class="utility-links"><a href="<%= ctx %>/accueil.jsp">Accueil</a><span aria-hidden="true">|</span><a href="#services">Services</a><span aria-hidden="true">|</span><a href="#patients">Patients et visiteurs</a></div>
            <div class="utility-links utility-right"><a href="<%= ctx %>/connexion.jsp">Connexion du personnel</a><span aria-hidden="true">|</span><a href="<%= ctx %>/inscription.jsp">Créer un compte patient</a></div>
        </div>
    </div>

    <header class="site-header">
        <div class="container header-inner">
            <a class="brand" href="<%= ctx %>/accueil.jsp" aria-label="Clinique Miracle, accueil">
                <img class="brand-logo-img" src="<%= ctx %>/img/logo-clinique.png" alt="Clinique Miracle — Votre santé, notre priorité">
            </a>
            <button class="menu-toggle" type="button" aria-expanded="false" aria-controls="main-navigation" aria-label="Ouvrir le menu"><span></span><span></span><span></span></button>
            <nav class="main-navigation" id="main-navigation" aria-label="Navigation principale">
                <a href="#services">Nos services</a>
                <a href="#clinique">La clinique</a>
                <a href="#patients">Patients et visiteurs</a>
                <a href="<%= ctx %>/connexion.jsp">Connexion</a>
                <a class="nav-cta" href="<%= ctx %>/inscription.jsp">Créer mon compte</a>
            </nav>
        </div>
    </header>

    <main>
        <section class="hero" aria-labelledby="hero-title">
            <div class="hero-photo" role="img" aria-label="Équipe médicale de la Clinique Miracle"></div>
            <div class="hero-shade"></div>
            <div class="container hero-content">
                <p class="hero-kicker">CLINIQUE MIRACLE <span>·</span> À VOS CÔTÉS</p>
                <h1 id="hero-title">Nous sommes là quand vous avez besoin de nous.</h1>
                <div class="hero-links">
                    <a href="#services">Nos services</a>
                    <a href="#patients">Votre suivi</a>
                    <a href="<%= ctx %>/inscription.jsp">Créer un compte patient</a>
                </div>
            </div>
        </section>

        <section class="featured-services" id="services">
            <div class="container">
                <div class="section-title-row"><div><p class="eyebrow">CLINIQUE MIRACLE</p><h2>Nos services</h2></div><a class="section-more" href="<%= ctx %>/inscription.jsp">Accéder à l’espace patient</a></div>
                <div class="service-grid">
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/consultation-patient.jpg" alt="Médecin en consultation avec une patiente">
                        <span class="service-copy"><b>Consultations</b><span>Consultez l’historique de vos consultations et retrouvez les informations de suivi communiquées par votre médecin dans votre espace patient.</span></span>
                    </a>
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/medecin-accueil.jpg" alt="Médecin accueillant un patient">
                        <span class="service-copy"><b>Rendez-vous</b><span>Demandez un rendez-vous, suivez son statut et retrouvez les créneaux planifiés avec les médecins de la clinique.</span></span>
                    </a>
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/laboratoire-microscope.jpg" alt="Analyse médicale réalisée au microscope">
                        <span class="service-copy"><b>Analyses médicales</b><span>Suivez les analyses demandées et consultez vos résultats dans votre espace dès leur validation par le laboratoire.</span></span>
                    </a>
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/patient-soins.jpg" alt="Patient suivi dans un établissement de santé">
                        <span class="service-copy"><b>Dossier médical</b><span>Retrouvez les informations de santé et les antécédents associés à votre dossier médical, dans un espace personnel sécurisé.</span></span>
                    </a>
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/medecin-suivi.jpg" alt="Médecin assurant le suivi d’un patient">
                        <span class="service-copy"><b>Prescriptions</b><span>Consultez les prescriptions enregistrées par votre médecin et retrouvez les indications disponibles pour votre traitement.</span></span>
                    </a>
                    <a class="service-card" href="<%= ctx %>/inscription.jsp">
                        <img class="service-photo" src="<%= ctx %>/img/clinique/equipe-clinique.jpg" alt="Équipe de la clinique accompagnant les patients">
                        <span class="service-copy"><b>Factures et paiements</b><span>Consultez vos factures, l’historique de vos paiements et le solde restant lié à vos prestations.</span></span>
                    </a>
                </div>
            </div>
        </section>

        <section class="feature-panels" id="patients">
            <div class="container feature-grid">
                <article class="feature-panel feature-cold">
                    <img src="<%= ctx %>/img/clinique/patient-soins.jpg" alt="Patient pris en charge dans une chambre de soins">
                    <div class="feature-overlay"></div>
                    <div class="feature-content"><p class="eyebrow">VOTRE SUIVI</p><h2>Votre parcours de soins, au même endroit.</h2><p>Retrouvez dans votre espace patient les informations liées à votre dossier et à vos consultations.</p><a href="<%= ctx %>/inscription.jsp">Découvrir mon espace</a></div>
                </article>
                <article class="feature-panel feature-where">
                    <img src="<%= ctx %>/img/clinique/suivi-infirmier.jpg" alt="Soignante accompagnant une patiente pendant son suivi">
                    <div class="feature-overlay"></div>
                    <div class="feature-content"><p class="eyebrow">ACCÈS PATIENT</p><h2>Besoin d’organiser votre suivi&nbsp;?</h2><p>Créez votre compte pour demander un rendez-vous et consulter les mises à jour de la clinique.</p><a href="<%= ctx %>/inscription.jsp">Créer mon compte patient</a></div>
                </article>
            </div>
        </section>

        <section class="care-gallery" aria-labelledby="care-gallery-title">
            <div class="container">
                <div class="why-intro"><p class="eyebrow">NOS ÉQUIPES</p><h2 id="care-gallery-title">Des professionnels présents à chaque étape.</h2><p>De l’accueil aux analyses, nos équipes vous accompagnent avec attention.</p></div>
                <div class="care-gallery-grid">
                    <article class="care-gallery-card"><img src="<%= ctx %>/img/clinique/laboratoire-microscope.jpg" alt="Laborantine examinant un échantillon au microscope"><h3>Analyses médicales</h3></article>
                    <article class="care-gallery-card"><img src="<%= ctx %>/img/clinique/laboratoire-technicien.jpg" alt="Technicien de laboratoire réalisant une analyse"><h3>Un laboratoire équipé</h3></article>
                    <article class="care-gallery-card"><img src="<%= ctx %>/img/clinique/infirmiere.jpg" alt="Infirmière accueillant les patients"><h3>Une équipe à votre écoute</h3></article>
                    <article class="care-gallery-card"><img src="<%= ctx %>/img/clinique/medecin-experience.jpg" alt="Médecin de la clinique"><h3>Un suivi médical attentif</h3></article>
                </div>
            </div>
        </section>

        <section class="action-strip">
            <div class="container action-grid">
                <article class="action-card"><span class="action-symbol" aria-hidden="true">✚</span><div><h2>Accéder aux soins</h2><p>Créez votre compte patient pour démarrer votre parcours en ligne.</p><a href="<%= ctx %>/inscription.jsp">Créer un compte</a></div></article>
                <article class="action-card"><span class="action-symbol action-symbol-alt" aria-hidden="true">⌂</span><div><h2>Besoin d’aide&nbsp;?</h2><p>Connectez-vous à votre espace ou contactez l’accueil de la clinique.</p><a href="<%= ctx %>/connexion.jsp">Ouvrir mon espace</a></div></article>
            </div>
        </section>

        <section class="why-section" id="clinique">
            <div class="container">
                <div class="why-intro"><p class="eyebrow">LA CLINIQUE MIRACLE</p><h2>Pourquoi choisir la Clinique Miracle&nbsp;?</h2><p>Une attention portée à chaque patient, un suivi organisé et un accès simple à vos informations personnelles.</p></div>
                <div class="why-grid">
                    <article class="why-card"><img src="<%= ctx %>/img/clinique/medecin-accueil.jpg" alt="Médecin accueillant les patients"><div class="why-card-copy"><h3>Une attention personnelle</h3><p>Chaque étape de votre parcours commence par un accueil attentif.</p></div></article>
                    <article class="why-card"><img src="<%= ctx %>/img/clinique/consultation-patient.jpg" alt="Médecins échangeant avec un patient hospitalisé"><div class="why-card-copy"><h3>Un suivi coordonné</h3><p>Les informations utiles à votre suivi sont réunies dans votre espace.</p></div></article>
                    <article class="why-card"><img src="<%= ctx %>/img/clinique/medecin-suivi.jpg" alt="Médecin assurant le suivi d’une patiente"><div class="why-card-copy"><h3>Un espace confidentiel</h3><p>Consultez vos informations personnelles après connexion à votre compte.</p></div></article>
                </div>
            </div>
        </section>

        <section class="health-info">
            <div class="container health-info-inner"><div><p class="eyebrow">ESPACE PATIENT</p><h2>Gardez votre suivi à portée de main.</h2><p>Vos rendez-vous, consultations et documents accessibles dans votre espace personnel.</p><a class="text-link" href="<%= ctx %>/inscription.jsp">Créer mon compte patient</a></div><img src="<%= ctx %>/img/clinique/consultation-equipe.jpg" alt="Équipe médicale discutant du suivi d’un patient"></div>
        </section>
    </main>

    <footer class="site-footer">
        <div class="container footer-columns">
            <div class="footer-brand"><a class="brand" href="<%= ctx %>/accueil.jsp" aria-label="Clinique Miracle, accueil"><img class="brand-logo-img" src="<%= ctx %>/img/logo-clinique.png" alt="Clinique Miracle — Votre santé, notre priorité"></a><p>Prendre soin de chaque patient,<br>à chaque étape du parcours.</p></div>
            <div class="footer-group"><h2>Accès patient</h2><a href="<%= ctx %>/inscription.jsp">Créer un compte</a><a href="<%= ctx %>/connexion.jsp">Connexion</a><a href="#services">Nos services</a></div>
            <div class="footer-group"><h2>La clinique</h2><a href="#clinique">À propos</a><a href="#patients">Votre suivi</a><a href="#services">Rendez-vous et consultations</a></div>
            <div class="footer-group"><h2>Accès professionnel</h2><a href="<%= ctx %>/connexion.jsp">Connexion du personnel</a><a href="#patients">Informations patients</a></div>
        </div>
        <div class="container footer-bottom"><span>© <%= java.time.Year.now() %> Clinique Miracle</span><span>La santé au cœur de nos soins</span></div>
    </footer>

    <script>
        const menuButton = document.querySelector('.menu-toggle');
        const navigation = document.querySelector('.main-navigation');
        menuButton.addEventListener('click', () => {
            const open = menuButton.getAttribute('aria-expanded') === 'true';
            menuButton.setAttribute('aria-expanded', String(!open));
            navigation.classList.toggle('is-open', !open);
        });
        navigation.addEventListener('click', event => {
            if (event.target.closest('a')) {
                menuButton.setAttribute('aria-expanded', 'false');
                navigation.classList.remove('is-open');
            }
        });
    </script>
</body>
</html>
