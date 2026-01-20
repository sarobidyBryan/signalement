-- Script SQL pour insérer des données de test avec progressions
-- Permet de tester la synchronisation et les calculs de pourcentage

-- Insérer un signalement pour Alice (surface totale : 150 m²)
INSERT INTO reports (user_id, report_date, area, longitude, latitude, description, status_id)
VALUES (
    (SELECT id FROM users WHERE email = 'alice@example.com'),
    '2024-01-15 10:30:00',
    150.00,
    47.5162,
    -18.8792,
    'Route principale endommagée sur 150m², nécessite réparation urgente',
    (SELECT id FROM status WHERE status_code = 'IN_PROGRESS')
);

-- Récupérer l'ID du report créé
DO $$
DECLARE
    v_report_id INTEGER;
    v_assignation_id INTEGER;
BEGIN
    -- Récupérer le dernier report créé
    SELECT id INTO v_report_id FROM reports ORDER BY id DESC LIMIT 1;
    
    -- Assigner à COLAS Madagascar avec budget de 5 000 000 Ar
    INSERT INTO reports_assignation (company_id, report_id, budget, start_date, deadline)
    VALUES (
        (SELECT id FROM companies WHERE name = 'COLAS Madagascar'),
        v_report_id,
        5000000.00,
        '2024-01-20',
        '2024-03-20'
    )
    RETURNING id INTO v_assignation_id;
    
    -- Ajouter 3 progressions
    
    -- Progression 1 : 50 m² traités (33.33%)
    INSERT INTO reports_assignation_progress (reports_assignation_id, treated_area, comment, registration_date)
    VALUES (
        v_assignation_id,
        50.00,
        'Première phase terminée : réparation du tronçon nord',
        '2024-01-25 15:00:00'
    );
    
    -- Progression 2 : 70.50 m² traités (47.00%)
    INSERT INTO reports_assignation_progress (reports_assignation_id, treated_area, comment, registration_date)
    VALUES (
        v_assignation_id,
        70.50,
        'Deuxième phase : réparation du tronçon central en cours',
        '2024-02-01 16:30:00'
    );
    
    -- Progression 3 : 20 m² traités (13.33%)
    INSERT INTO reports_assignation_progress (reports_assignation_id, treated_area, comment, registration_date)
    VALUES (
        v_assignation_id,
        20.00,
        'Troisième phase : début des finitions sur le tronçon sud',
        '2024-02-05 09:15:00'
    );
    
    -- Total traité : 140.50 m² sur 150 m² = 93.67%
    -- Restant : 9.50 m²
    
    RAISE NOTICE 'Report ID: %, Assignation ID: %', v_report_id, v_assignation_id;
    RAISE NOTICE 'Total traité : 140.50 m² / 150 m² = 93.67%%';
    RAISE NOTICE 'Restant : 9.50 m²';
END $$;

-- Insérer un signalement pour Bob (surface totale : 200 m²)
INSERT INTO reports (user_id, report_date, area, longitude, latitude, description, status_id)
VALUES (
    (SELECT id FROM users WHERE email = 'bob@example.com'),
    '2024-01-18 14:20:00',
    200.00,
    47.5180,
    -18.8810,
    'Nid de poule important sur avenue principale, 200m² à traiter',
    (SELECT id FROM status WHERE status_code = 'IN_PROGRESS')
);

-- Assigner ce report à Groupe ETI Construction
DO $$
DECLARE
    v_report_id INTEGER;
    v_assignation_id INTEGER;
BEGIN
    SELECT id INTO v_report_id FROM reports ORDER BY id DESC LIMIT 1;
    
    INSERT INTO reports_assignation (company_id, report_id, budget, start_date, deadline)
    VALUES (
        (SELECT id FROM companies WHERE name = 'Groupe ETI Construction'),
        v_report_id,
        8500000.00,
        '2024-01-22',
        '2024-04-15'
    )
    RETURNING id INTO v_assignation_id;
    
    -- Progression 1 : 100 m² traités (50%)
    INSERT INTO reports_assignation_progress (reports_assignation_id, treated_area, comment, registration_date)
    VALUES (
        v_assignation_id,
        100.00,
        'Moitié des travaux effectués, excavation terminée',
        '2024-02-01 10:00:00'
    );
    
    -- Progression 2 : 60 m² traités (30%)
    INSERT INTO reports_assignation_progress (reports_assignation_id, treated_area, comment, registration_date)
    VALUES (
        v_assignation_id,
        60.00,
        'Suite des travaux, pose de la nouvelle couche en cours',
        '2024-02-08 11:30:00'
    );
    
    -- Total traité : 160 m² sur 200 m² = 80%
    -- Restant : 40 m²
    
    RAISE NOTICE 'Report ID: %, Assignation ID: %', v_report_id, v_assignation_id;
    RAISE NOTICE 'Total traité : 160 m² / 200 m² = 80%%';
    RAISE NOTICE 'Restant : 40 m²';
END $$;

-- Afficher un résumé des données insérées
SELECT 
    r.id as report_id,
    u.name as user_name,
    r.area as total_area,
    s.label as status,
    c.name as company,
    ra.budget,
    COUNT(rap.id) as nb_progressions,
    COALESCE(SUM(rap.treated_area), 0) as total_treated,
    ROUND(COALESCE(SUM(rap.treated_area), 0) / r.area * 100, 2) as percentage
FROM reports r
JOIN users u ON r.user_id = u.id
JOIN status s ON r.status_id = s.id
LEFT JOIN reports_assignation ra ON ra.report_id = r.id
LEFT JOIN companies c ON ra.company_id = c.id
LEFT JOIN reports_assignation_progress rap ON rap.reports_assignation_id = ra.id
WHERE r.id IN (
    SELECT id FROM reports ORDER BY id DESC LIMIT 2
)
GROUP BY r.id, u.name, r.area, s.label, c.name, ra.budget
ORDER BY r.id DESC;
