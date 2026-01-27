-- 03-insert-sample-report.sql
-- Insert a sample report (no assignation, no progression)
-- Adjust values as needed. Uses existing user email and status_code from the DB.

BEGIN;

-- Replace 'alice@example.com' with a user that exists in your DB if needed.
-- Replace 'SUBMITTED' with the desired status code if needed.
INSERT INTO reports (user_id, report_date, area, longitude, latitude, description, status_id, firebase_id, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE email = 'alice@example.com' LIMIT 1),
  NOW(),
  123.45, -- area in m^2
  47.519000, -- longitude
  -18.879000, -- latitude
  'Exemple: nids-de-poule et dégradation de la chaussée',
  (SELECT id FROM status WHERE status_code = 'SUBMITTED' LIMIT 1),
  NULL,
  NOW(),
  NOW()
);

COMMIT;

-- Quick verification (optional):
-- SELECT id, user_id, report_date, area, longitude, latitude, description, status_id FROM reports ORDER BY id DESC LIMIT 5;
