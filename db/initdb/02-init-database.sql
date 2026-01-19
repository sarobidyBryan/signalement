CREATE TABLE configurations(
    id SERIAL PRIMARY KEY,
    key VARCHAR(100),
    value VARCHAR(100),
    type VARCHAR(100)
);

INSERT INTO configurations (key, value, type) VALUES
('app_name', 'Signalements Routes Tana', 'string'),
('max_report_area', '1000', 'integer'),
('default_deadline_days', '30', 'integer'),
('map_center_lat', '-18.8792', 'decimal'),
('map_center_lng', '47.5079', 'decimal'),
('budget_min', '500000', 'integer'),
('budget_max', '50000000', 'integer'),
('currency', 'MGA', 'string'),
('notification_email', 'contact@signalements-tana.mg', 'string'),
('max_attempt', '3', 'integer'),
('session_duration', '150', 'integer');


CREATE TABLE companies(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

INSERT INTO companies (name, email) VALUES
('COLAS Madagascar', 'contact@colas.mg'),
('Groupe ETI Construction', 'info@eti-construction.mg'),
('SOGEA SATOM Madagascar', 'sogeamadagascar@sogea-satom.com'),
('TRAVOTECH', 'administration@travotech.mg'),
('BTP Madagascar', 'btp@btpmadagascar.mg'),
('Mad''Artisan', 'contact@madartisan.mg'),
('Road Masters Tana', 'contact@roadmasters.mg');


CREATE TABLE status(
    id SERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE,
    label VARCHAR(100)
);


INSERT INTO status (status_code, label) VALUES
('SUBMITTED', 'Soumis'),
--('UNDER_REVIEW', 'En cours d''examen'),
('ASSIGNED', 'Assigné à une entreprise'),
('IN_PROGRESS', 'Travaux en cours'),
('COMPLETED', 'Terminé'),
--('CANCELLED', 'Annulé'),
('VERIFIED', 'Vérifié et validé');


CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_code VARCHAR(20),
    label VARCHAR(100)
);

INSERT INTO roles (role_code,label) VALUES
('USER','Utilisateur'),
('MANAGER','Manager');

CREATE TABLE user_status_types(
    id SERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE,
    label VARCHAR(100)
);

INSERT INTO user_status_types (status_code, label) VALUES
('ACTIVE', 'Actif'),
('INACTIVE', 'Inactif'),
('SUSPENDED', 'Suspendu'),
('PENDING', 'En attente de validation'),
('BANNED', 'Banni');


CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    firebase_uid VARCHAR(100),
    role_id INTEGER NOT NULL REFERENCES roles(id), -- Colonne denormalisee
    user_status_type_id INTEGER NOT NULL REFERENCES user_status_types(id) -- Colonne denormalisee
);


CREATE TABLE user_status(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    user_status_type INTEGER NOT NULL REFERENCES user_status_types(id),
    registration_date TIMESTAMP
);



CREATE TABLE role_attributions(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    attribution_date TIMESTAMP
);



CREATE TABLE reports(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    report_date TIMESTAMP,
    area DECIMAL(10,2), -- en m^2
    longitude DECIMAL(9,6),
    latitude DECIMAL(9,6),
    description TEXT
);

CREATE TABLE reports_assignation(
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(id),
    report_id INTEGER NOT NULL REFERENCES reports(id),
    budget DECIMAL(15,2),
    start_date DATE,
    deadline DATE  
);

CREATE TABLE reports_status(
    id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL REFERENCES reports(id),
    status_id INTEGER NOT NULL REFERENCES status(id),
    registration_date TIMESTAMP  
);

CREATE TABLE reports_assignation_progress(
    id SERIAL PRIMARY KEY,
    reports_assignation_id INTEGER NOT NULL REFERENCES reports_assignation(id),
    treated_area DECIMAL(10,2),
    comment TEXT,
    registration_date TIMESTAMP
);

INSERT INTO users (name, email, password, role_id, user_status_type_id)
VALUES ('Manager One','manager@example.com','managerpass',
  (SELECT id FROM roles WHERE role_code='MANAGER'),
  (SELECT id FROM user_status_types WHERE status_code='ACTIVE'));

INSERT INTO users (name, email, password, role_id, user_status_type_id)
VALUES ('Alice','alice@example.com','alicepass',
  (SELECT id FROM roles WHERE role_code='USER'),
  (SELECT id FROM user_status_types WHERE status_code='ACTIVE'));

INSERT INTO users (name, email, password, role_id, user_status_type_id)
VALUES ('Bob','bob@example.com','bobpass',
  (SELECT id FROM roles WHERE role_code='USER'),
  (SELECT id FROM user_status_types WHERE status_code='SUSPENDED'));