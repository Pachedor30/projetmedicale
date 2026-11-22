-- =====================================================================
--  CLINIQUE MIRACLE — Gestion des carnets médicaux
--  Script d'initialisation PostgreSQL
--  Base : gestion_carnets_medicaux
--  Conforme au cahier des charges §14 (modèle de données)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. BASE DE DONNÉES
-- ---------------------------------------------------------------------
DROP DATABASE IF EXISTS gestion_carnets_medicaux;
CREATE DATABASE gestion_carnets_medicaux
    WITH OWNER = postgres
         ENCODING = 'UTF8'
         CONNECTION LIMIT = -1;

\c gestion_carnets_medicaux

-- ---------------------------------------------------------------------
-- 1. RÔLES ET PERMISSIONS
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    code        VARCHAR(30)  PRIMARY KEY,
    libelle     VARCHAR(60)  NOT NULL,
    description TEXT,
    est_interne BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE permissions (
    code        VARCHAR(60)  PRIMARY KEY,
    libelle     VARCHAR(120) NOT NULL,
    module      VARCHAR(40)  NOT NULL
);

CREATE TABLE role_permissions (
    role_code       VARCHAR(30) NOT NULL REFERENCES roles(code) ON DELETE CASCADE,
    permission_code VARCHAR(60) NOT NULL REFERENCES permissions(code) ON DELETE CASCADE,
    PRIMARY KEY (role_code, permission_code)
);

-- ---------------------------------------------------------------------
-- 2. SERVICES MÉDICAUX
-- ---------------------------------------------------------------------
CREATE TABLE services (
    id            SERIAL PRIMARY KEY,
    nom           VARCHAR(80)  NOT NULL,
    description   TEXT,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_services_nom UNIQUE (nom)
);

-- ---------------------------------------------------------------------
-- 3. UTILISATEURS (table unique d'authentification — 6 rôles)
-- ---------------------------------------------------------------------
CREATE TABLE utilisateurs (
    id                        SERIAL PRIMARY KEY,
    nom                       VARCHAR(60)  NOT NULL,
    prenoms                   VARCHAR(80)  NOT NULL,
    email                     VARCHAR(120) NOT NULL,
    mot_de_passe_hash         VARCHAR(60)  NOT NULL,   -- BCrypt (60 car.)
    role_code                 VARCHAR(30)  NOT NULL REFERENCES roles(code),
    service_id                INTEGER      REFERENCES services(id) ON DELETE SET NULL,
    statut                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIF'
                              CHECK (statut IN ('ACTIF','INACTIF','VERROUILLE')),
    doit_changer_mot_de_passe BOOLEAN      NOT NULL DEFAULT FALSE,
    tentatives_echouees       INTEGER      NOT NULL DEFAULT 0,
    verrou_jusqu_a            TIMESTAMPTZ,
    date_derniere_connexion   TIMESTAMPTZ,
    date_creation             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    date_modification         TIMESTAMPTZ,
    cree_par                  INTEGER      REFERENCES utilisateurs(id) ON DELETE SET NULL,
    -- informations d'état civil partagées
    sexe                      VARCHAR(1)   CHECK (sexe IN ('M','F')),
    date_naissance            DATE,
    telephone                 VARCHAR(20),
    adresse                   TEXT,
    CONSTRAINT uk_utilisateurs_email UNIQUE (email),
    CONSTRAINT ck_utilisateurs_email_format CHECK (position('@' IN email) > 1),
    CONSTRAINT ck_utilisateurs_tentatives CHECK (tentatives_echouees >= 0)
);

CREATE INDEX idx_utilisateurs_role   ON utilisateurs(role_code);
CREATE INDEX idx_utilisateurs_service ON utilisateurs(service_id);
CREATE INDEX idx_utilisateurs_nom    ON utilisateurs(nom, prenoms);

-- ---------------------------------------------------------------------
-- 4. PERSONNEL (informations professionnelles — héritage par jointure)
-- ---------------------------------------------------------------------
CREATE TABLE personnel (
    utilisateur_id INTEGER PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE CASCADE,
    matricule      VARCHAR(30) NOT NULL,
    fonction       VARCHAR(80),
    specialite     VARCHAR(80),
    date_embauche  DATE,
    ordre_inscription VARCHAR(40),   -- n° ordre du conseil de l'ordre (médecins)
    CONSTRAINT uk_personnel_matricule UNIQUE (matricule)
);

-- ---------------------------------------------------------------------
-- 5. PATIENTS (héritage par jointure + rattachement au dossier)
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    utilisateur_id      INTEGER PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE CASCADE,
    numero_dossier      VARCHAR(20) NOT NULL,
    rattachement_statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                        CHECK (rattachement_statut IN ('EN_ATTENTE','VERIFIE','REJETE')),
    verifie_par         INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    verifie_le          TIMESTAMPTZ,
    profession          VARCHAR(80),
    contact_urgence     VARCHAR(120),
    telephone_urgence   VARCHAR(20),
    groupe_sanguin      VARCHAR(5),
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_patients_numero_dossier UNIQUE (numero_dossier)
);

CREATE INDEX idx_patients_rattachement ON patients(rattachement_statut);

-- ---------------------------------------------------------------------
-- 6. JOURNAL D'ACTIVITÉ (traçabilité — §17)
-- ---------------------------------------------------------------------
CREATE TABLE journal_activites (
    id             BIGSERIAL PRIMARY KEY,
    utilisateur_id INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    action         VARCHAR(40) NOT NULL,   -- CONNEXION, DECONNEXION, CREATION, MODIFICATION...
    entite         VARCHAR(40),            -- utilisateur, patient, rendez_vous...
    entite_id      BIGINT,
    details        TEXT,
    adresse_ip     VARCHAR(45),
    succes         BOOLEAN NOT NULL DEFAULT TRUE,
    date_action    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_journal_date     ON journal_activites(date_action DESC);
CREATE INDEX idx_journal_user     ON journal_activites(utilisateur_id);
CREATE INDEX idx_journal_entite   ON journal_activites(entite, entite_id);

-- ---------------------------------------------------------------------
-- 7. NOTIFICATIONS
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    id             BIGSERIAL PRIMARY KEY,
    utilisateur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    titre          VARCHAR(120) NOT NULL,
    message        TEXT NOT NULL,
    type           VARCHAR(30) NOT NULL DEFAULT 'INFO'
                   CHECK (type IN ('INFO','SUCCESS','ALERTE','RAPPEL')),
    lien           VARCHAR(160),
    lue            BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications(utilisateur_id, lue);

-- ---------------------------------------------------------------------
-- 8. DOSSIER MÉDICAL NUMÉRIQUE (§11)
-- ---------------------------------------------------------------------
CREATE TABLE dossiers_medicaux (
    id                  SERIAL PRIMARY KEY,
    patient_id          INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    antecedents_generaux TEXT,
    allergies           TEXT,
    traitements_en_cours TEXT,
    notes_administratives TEXT,
    date_ouverture      TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_modification   TIMESTAMPTZ,
    modifie_par         INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT uk_dossier_patient UNIQUE (patient_id)
);

-- ---------------------------------------------------------------------
-- 9. ANTECÉDENTS MÉDICAUX
-- ---------------------------------------------------------------------
CREATE TABLE antecedents_medicaux (
    id           SERIAL PRIMARY KEY,
    patient_id   INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL
                 CHECK (type IN ('PERSONNEL','FAMILIALE','CHIRURGICAL','ALLERGIQUE','AUTRE')),
    libelle      VARCHAR(160) NOT NULL,
    description  TEXT,
    date_debut   DATE,
    date_fin     DATE,
    date_enregistrement TIMESTAMPTZ NOT NULL DEFAULT now(),
    saisi_par    INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL
);

CREATE INDEX idx_antecedents_patient ON antecedents_medicaux(patient_id);

-- ---------------------------------------------------------------------
-- 10. RENDEZ-VOUS (§12 — anti double réservation)
-- ---------------------------------------------------------------------
CREATE TABLE rendez_vous (
    id             BIGSERIAL PRIMARY KEY,
    patient_id     INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    medecin_id     INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    service_id     INTEGER REFERENCES services(id) ON DELETE SET NULL,
    date_rdv       DATE    NOT NULL,
    heure_debut    TIME    NOT NULL,
    heure_fin      TIME    NOT NULL,
    motif          TEXT,
    statut         VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                   CHECK (statut IN ('EN_ATTENTE','CONFIRME','TERMINE','ANNULE','NON_HONORE')),
    cree_par       INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    annule_par     INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    motif_annulation TEXT,
    date_creation  TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_modification TIMESTAMPTZ,
    CONSTRAINT ck_rdv_intervalles CHECK (heure_fin > heure_debut),
    CONSTRAINT ck_rdv_date_future CHECK (date_rdv >= DATE '2020-01-01')
);

-- Anti double réservation : un médecin ne peut avoir 2 RDV actifs
-- sur le même créneau (les annulations restent autorisées).
CREATE UNIQUE INDEX uk_rdv_creneau_medecin
    ON rendez_vous(medecin_id, date_rdv, heure_debut)
    WHERE statut <> 'ANNULE';

-- Anti collision : un patient ne peut être au même instant 2 fois.
CREATE UNIQUE INDEX uk_rdv_creneau_patient
    ON rendez_vous(patient_id, date_rdv, heure_debut)
    WHERE statut <> 'ANNULE';

CREATE INDEX idx_rdv_medecin_date ON rendez_vous(medecin_id, date_rdv);
CREATE INDEX idx_rdv_patient_date ON rendez_vous(patient_id, date_rdv);
CREATE INDEX idx_rdv_statut       ON rendez_vous(statut, date_rdv);

-- ---------------------------------------------------------------------
-- 11. CONSULTATIONS
-- ---------------------------------------------------------------------
CREATE TABLE consultations (
    id                BIGSERIAL PRIMARY KEY,
    patient_id        INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    medecin_id        INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    rendez_vous_id    BIGINT  REFERENCES rendez_vous(id) ON DELETE SET NULL,
    service_id        INTEGER REFERENCES services(id) ON DELETE SET NULL,
    date_consultation TIMESTAMPTZ NOT NULL DEFAULT now(),
    motif             TEXT,
    observations      TEXT,
    diagnostic        TEXT,
    conduite_a_tenir  TEXT,
    statut            VARCHAR(20) NOT NULL DEFAULT 'EN_COURS'
                      CHECK (statut IN ('EN_COURS','TERMINEE','ANNULEE')),
    date_modification TIMESTAMPTZ,
    CONSTRAINT uk_consultation_rdv UNIQUE (rendez_vous_id)
);

CREATE INDEX idx_consult_patient ON consultations(patient_id, date_consultation DESC);
CREATE INDEX idx_consult_medecin ON consultations(medecin_id, date_consultation DESC);

-- ---------------------------------------------------------------------
-- 12. CONSTANTES VITALES
-- ---------------------------------------------------------------------
CREATE TABLE constantes_vitales (
    id                 BIGSERIAL PRIMARY KEY,
    consultation_id    BIGINT REFERENCES consultations(id) ON DELETE CASCADE,
    patient_id         INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    tension_arterielle VARCHAR(12),      -- ex. '12/8'
    pouls              INTEGER,
    temperature_c      NUMERIC(4,1),
    poids_kg           NUMERIC(5,2),
    taille_cm          NUMERIC(5,1),
    spo2               INTEGER,
    glycemie           NUMERIC(6,2),
    date_mesure        TIMESTAMPTZ NOT NULL DEFAULT now(),
    mesure_par         INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT ck_constantes_pouls    CHECK (pouls IS NULL OR (pouls > 0 AND pouls < 300)),
    CONSTRAINT ck_constantes_temp     CHECK (temperature_c IS NULL OR (temperature_c > 20 AND temperature_c < 45)),
    CONSTRAINT ck_constantes_spo2     CHECK (spo2 IS NULL OR (spo2 > 0 AND spo2 <= 100)),
    CONSTRAINT ck_constantes_poids    CHECK (poids_kg IS NULL OR poids_kg > 0),
    CONSTRAINT ck_constantes_taille   CHECK (taille_cm IS NULL OR taille_cm > 0)
);

CREATE INDEX idx_constantes_patient ON constantes_vitales(patient_id, date_mesure DESC);

-- ---------------------------------------------------------------------
-- 13. PRESCRIPTIONS
-- ---------------------------------------------------------------------
CREATE TABLE prescriptions (
    id                BIGSERIAL PRIMARY KEY,
    consultation_id   BIGINT  REFERENCES consultations(id) ON DELETE CASCADE,
    patient_id        INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    medecin_id        INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    date_prescription TIMESTAMPTZ NOT NULL DEFAULT now(),
    notes             TEXT,
    statut            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (statut IN ('ACTIVE','ANNULEE','TERMINEE')),
    annule_par        INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL
);

CREATE INDEX idx_prescription_patient ON prescriptions(patient_id, date_prescription DESC);

CREATE TABLE prescription_details (
    id              BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicament      VARCHAR(160) NOT NULL,
    dosage          VARCHAR(60),
    posologie       VARCHAR(160) NOT NULL,
    duree           VARCHAR(60),
    quantite        VARCHAR(60),
    instructions    TEXT
);

CREATE INDEX idx_prescription_details ON prescription_details(prescription_id);

-- ---------------------------------------------------------------------
-- 14. TYPES D'ANALYSES + DEMANDES + RÉSULTATS (§8)
-- ---------------------------------------------------------------------
CREATE TABLE types_analyses (
    id            SERIAL PRIMARY KEY,
    code          VARCHAR(30)  NOT NULL,
    libelle       VARCHAR(120) NOT NULL,
    categorie     VARCHAR(60),
    prix          NUMERIC(12,2) NOT NULL DEFAULT 0,
    unite         VARCHAR(30),
    valeurs_reference TEXT,
    actif         BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_types_analyses_code UNIQUE (code),
    CONSTRAINT ck_types_analyses_prix CHECK (prix >= 0)
);

CREATE TABLE demandes_analyses (
    id                  BIGSERIAL PRIMARY KEY,
    patient_id          INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE CASCADE,
    medecin_id          INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    type_analyse_id     INTEGER NOT NULL REFERENCES types_analyses(id) ON DELETE RESTRICT,
    consultation_id     BIGINT  REFERENCES consultations(id) ON DELETE SET NULL,
    date_demande        TIMESTAMPTZ NOT NULL DEFAULT now(),
    priorite            VARCHAR(20) NOT NULL DEFAULT 'NORMALE'
                        CHECK (priorite IN ('BASSE','NORMALE','URGENTE')),
    statut              VARCHAR(20) NOT NULL DEFAULT 'DEMANDEE'
                        CHECK (statut IN ('DEMANDEE','EN_COURS','SIS','VALIDE','ANNULEE')),
    observations_medecin TEXT,
    traite_par          INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    date_prise_en_charge TIMESTAMPTZ,
    CONSTRAINT uk_demande_analyse UNIQUE (patient_id, type_analyse_id, date_demande)
);

CREATE INDEX idx_demandes_statut ON demandes_analyses(statut, date_demande);
CREATE INDEX idx_demandes_patient ON demandes_analyses(patient_id);
CREATE INDEX idx_demandes_medecin ON demandes_analyses(medecin_id);

CREATE TABLE resultats_analyses (
    id                BIGSERIAL PRIMARY KEY,
    demande_id        BIGINT NOT NULL REFERENCES demandes_analyses(id) ON DELETE CASCADE,
    valeur            TEXT,
    unite             VARCHAR(30),
    valeurs_reference TEXT,
    observations      TEXT,
    statut            VARCHAR(20) NOT NULL DEFAULT 'SAISI'
                      CHECK (statut IN ('SAISI','VALIDE','REJETE')),
    saisi_par         INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    date_saisie       TIMESTAMPTZ,
    valide_par        INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    date_validation   TIMESTAMPTZ,
    CONSTRAINT uk_resultat_demande UNIQUE (demande_id)
);

CREATE INDEX idx_resultats_statut ON resultats_analyses(statut);

-- ---------------------------------------------------------------------
-- 15. PRESTATIONS ET TARIFS (§5 — admin)
-- ---------------------------------------------------------------------
CREATE TABLE prestations (
    id            SERIAL PRIMARY KEY,
    code          VARCHAR(30)  NOT NULL,
    libelle       VARCHAR(160) NOT NULL,
    categorie     VARCHAR(60),
    prix_unitaire NUMERIC(12,2) NOT NULL DEFAULT 0,
    actif         BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_prestations_code UNIQUE (code),
    CONSTRAINT ck_prestations_prix CHECK (prix_unitaire >= 0)
);

-- ---------------------------------------------------------------------
-- 16. FACTURATION ET PAIEMENTS (§9)
-- ---------------------------------------------------------------------
CREATE TABLE factures (
    id              BIGSERIAL PRIMARY KEY,
    numero_facture  VARCHAR(30) NOT NULL,
    patient_id      INTEGER NOT NULL REFERENCES patients(utilisateur_id) ON DELETE RESTRICT,
    date_facture    DATE NOT NULL DEFAULT CURRENT_DATE,
    echeance        DATE,
    statut          VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'
                    CHECK (statut IN ('EN_ATTENTE','PARTIELLEMENT_PAYEE','PAYEE','ANNULEE')),
    montant_total   NUMERIC(12,2) NOT NULL DEFAULT 0,
    montant_paye    NUMERIC(12,2) NOT NULL DEFAULT 0,
    montant_restant NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes           TEXT,
    cree_par        INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    date_creation   TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_modification TIMESTAMPTZ,
    CONSTRAINT uk_factures_numero UNIQUE (numero_facture),
    CONSTRAINT ck_factures_montants CHECK (montant_total >= 0
                                           AND montant_paye >= 0
                                           AND montant_restant >= 0),
    CONSTRAINT ck_factures_coherence CHECK (montant_paye <= montant_total)
);

CREATE INDEX idx_factures_patient ON factures(patient_id, date_facture DESC);
CREATE INDEX idx_factures_statut  ON factures(statut, date_facture DESC);

CREATE TABLE lignes_facture (
    id             BIGSERIAL PRIMARY KEY,
    facture_id     BIGINT NOT NULL REFERENCES factures(id) ON DELETE CASCADE,
    prestation_id  INTEGER REFERENCES prestations(id) ON DELETE SET NULL,
    libelle        VARCHAR(160) NOT NULL,
    quantite       NUMERIC(10,2) NOT NULL DEFAULT 1,
    prix_unitaire  NUMERIC(12,2) NOT NULL DEFAULT 0,
    montant_ligne  NUMERIC(12,2) NOT NULL,
    CONSTRAINT ck_lignes_quantite CHECK (quantite > 0),
    CONSTRAINT ck_lignes_prix     CHECK (prix_unitaire >= 0 AND montant_ligne >= 0)
);

CREATE INDEX idx_lignes_facture ON lignes_facture(facture_id);

CREATE TABLE paiements (
    id             BIGSERIAL PRIMARY KEY,
    facture_id     BIGINT NOT NULL REFERENCES factures(id) ON DELETE RESTRICT,
    montant        NUMERIC(12,2) NOT NULL,
    mode_paiement  VARCHAR(20) NOT NULL DEFAULT 'ESPECES'
                   CHECK (mode_paiement IN ('ESPECES','CARTE','VIREMENT','CHEQUE','MOBILE_MONEY')),
    date_paiement  TIMESTAMPTZ NOT NULL DEFAULT now(),
    reference      VARCHAR(60),
    notes          TEXT,
    encaisse_par   INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT ck_paiements_montant CHECK (montant > 0)
);

CREATE INDEX idx_paiements_facture ON paiements(facture_id, date_paiement DESC);
CREATE INDEX idx_paiements_date    ON paiements(date_paiement DESC);

-- =====================================================================
--  DONNÉES INITIALES (seed)
-- =====================================================================

-- Rôles (§4 du cahier des charges)
INSERT INTO roles (code, libelle, description, est_interne) VALUES
 ('ADMIN',             'Administrateur',        'Supervision globale de l''application', TRUE),
 ('ACCUEIL',           'Agent d''accueil',      'Enregistrement patients et rendez-vous', TRUE),
 ('MEDECIN',           'Médecin',               'Consultations, diagnostics, prescriptions', TRUE),
 ('LABORANTIN',        'Laborantin',            'Analyses médicales et résultats', TRUE),
 ('CAISSIER_COMPTABLE','Caissier / Comptable',  'Facturation et paiements', TRUE),
 ('PATIENT',           'Patient',               'Accès à son propre dossier (externe)', FALSE);

-- Permissions (module + action)
INSERT INTO permissions (code, libelle, module) VALUES
 ('UTILISATEUR.LIRE',   'Consulter les utilisateurs',   'UTILISATEUR'),
 ('UTILISATEUR.CREER',  'Créer un compte du personnel','UTILISATEUR'),
 ('UTILISATEUR.MODIFIER','Modifier un utilisateur',     'UTILISATEUR'),
 ('UTILISATEUR.STATUT', 'Activer / désactiver un compte','UTILISATEUR'),
 ('UTILISATEUR.REINIT', 'Réinitialiser un mot de passe','UTILISATEUR'),
 ('ROLE.GERER',         'Gérer rôles et permissions',   'SECURITE'),
 ('PATIENT.LIRE',       'Consulter la liste des patients','PATIENT'),
 ('PATIENT.CREER',      'Enregistrer un patient',       'PATIENT'),
 ('PATIENT.MODIFIER',   'Modifier un patient',          'PATIENT'),
 ('PATIENT.RATTACHER',  'Vérifier et rattacher un dossier','PATIENT'),
 ('SERVICE.GERER',      'Gérer les services',           'SERVICE'),
 ('PRESTATION.GERER',   'Gérer prestations et tarifs',  'PRESTATION'),
 ('RDV.LIRE',           'Consulter les rendez-vous',    'RDV'),
 ('RDV.CREER',          'Créer un rendez-vous',         'RDV'),
 ('RDV.MODIFIER',       'Modifier / annuler un rendez-vous','RDV'),
 ('CONSULTATION.CREER', 'Saisir une consultation',      'CONSULTATION'),
 ('CONSULTATION.LIRE',  'Consulter les consultations',  'CONSULTATION'),
 ('ANALYSE.DEMANDER',   'Demander une analyse',         'ANALYSE'),
 ('ANALYSE.TRAITER',    'Saisir et valider un résultat','ANALYSE'),
 ('ANALYSE.LIRE',       'Consulter les analyses',       'ANALYSE'),
 ('FACTURE.CREER',      'Établir une facture',          'FACTURE'),
 ('FACTURE.LIRE',       'Consulter les factures',       'FACTURE'),
 ('PAIEMENT.ENCAISSER', 'Encaisser un paiement',        'FACTURE'),
 ('RAPPORT.GENERER',    'Générer des rapports',         'RAPPORT'),
 ('JOURNAL.LIRE',       'Consulter le journal','SECURITE'),
 ('PARAMETRE.GERER',    'Gérer les paramètres',         'PARAMETRE'),
 ('SAUVEGARDE.GERER',   'Sauvegarder / restaurer',      'MAINTENANCE');

-- Matrice rôle → permissions
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'ADMIN', code FROM permissions;

INSERT INTO role_permissions (role_code, permission_code) VALUES
 ('ACCUEIL','PATIENT.LIRE'), ('ACCUEIL','PATIENT.CREER'), ('ACCUEIL','PATIENT.MODIFIER'),
 ('ACCUEIL','RDV.LIRE'), ('ACCUEIL','RDV.CREER'), ('ACCUEIL','RDV.MODIFIER'),
 ('ACCUEIL','CONSULTATION.LIRE'), ('ACCUEIL','ANALYSE.LIRE'), ('ACCUEIL','FACTURE.LIRE'),

 ('MEDECIN','PATIENT.LIRE'), ('MEDECIN','CONSULTATION.LIRE'), ('MEDECIN','CONSULTATION.CREER'),
 ('MEDECIN','ANALYSE.DEMANDER'), ('MEDECIN','ANALYSE.LIRE'), ('MEDECIN','RDV.LIRE'),

 ('LABORANTIN','PATIENT.LIRE'), ('LABORANTIN','ANALYSE.LIRE'), ('LABORANTIN','ANALYSE.TRAITER'),

 ('CAISSIER_COMPTABLE','PATIENT.LIRE'), ('CAISSIER_COMPTABLE','FACTURE.LIRE'),
 ('CAISSIER_COMPTABLE','FACTURE.CREER'), ('CAISSIER_COMPTABLE','PAIEMENT.ENCAISSER'),

 ('PATIENT','CONSULTATION.LIRE'), ('PATIENT','ANALYSE.LIRE'),
 ('PATIENT','RDV.LIRE'), ('PATIENT','RDV.CREER'), ('PATIENT','FACTURE.LIRE');

-- Services médicaux de base
INSERT INTO services (nom, description) VALUES
 ('Médecine générale', 'Consultations de médecine générale'),
 ('Cardiologie',       'Suivi et explorations cardiovasculaires'),
 ('Pédiatrie',         'Médecine de l''enfant'),
 ('Laboratoire',       'Analyses et examens biologiques');

-- Types d'analyses de base
INSERT INTO types_analyses (code, libelle, categorie, prix, unite, valeurs_reference) VALUES
 ('NFS',   'NFS complet',                 'Hématologie', 5000, '',   ''),
 ('GLY',   'Glycémie à jeun',             'Biochimie',   3000, 'g/L','0.70 - 1.10'),
 ('CREA',  'Créatinine',                  'Biochimie',   4000, 'mg/L','6 - 13'),
 ('CHOL',  'Cholestérol total',           'Biochimie',   5000, 'g/L','< 2.00'),
 ('URI',   'Examen cytobactériologique',  'Urologie',    4500, '',   '');

-- Prestations de base
INSERT INTO prestations (code, libelle, categorie, prix_unitaire) VALUES
 ('CONSULT_GENE', 'Consultation médecine générale', 'Consultation', 10000),
 ('CONSULT_SPEC', 'Consultation spécialisée',       'Consultation', 20000),
 ('CONTROL_TENS','Contrôle tensionnel',             'Acte',          2500),
 ('VACCIN',      'Vaccination',                     'Acte',         15000);

-- Compte administrateur initial (mot de passe : Admin12345)
-- Hash BCrypt généré pour ce mot de passe — à réinitialiser après la 1re connexion.
INSERT INTO utilisateurs
    (nom, prenoms, email, mot_de_passe_hash, role_code, statut,
     doit_changer_mot_de_passe, sexe, date_naissance, telephone, adresse)
VALUES
    ('Administrateur', 'Admin', 'admin@clinique.com',
     '$2a$12$OYKAjzvpzhTIQbyq1pJUo.tMjaJCbc/Vc9cDEXArBoAVU8zjijh2K',
     'ADMIN', 'ACTIF', FALSE, 'M', DATE '1980-01-01', '0100000000', 'Clinique Miracle');

-- Journalisation de la création initiale
INSERT INTO journal_activites (utilisateur_id, action, entite, entite_id, details, succes)
VALUES (NULL, 'INITIALISATION', 'base', NULL, 'Création du schéma et seed administrateur', TRUE);

-- =====================================================================
--  FIN DU SCRIPT
-- =====================================================================
