CREATE TABLE configurations(
    id SERIAL PRIMARY KEY,
    key VARCHAR(100),
    value VARCHAR(100),
    type VARCHAR(100),
    firebase_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO configurations (key, value, type) VALUES
('max_attempt', '3', 'integer'),
('session_duration', '1800', 'integer');


-- Trigger pour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_configurations_updated_at BEFORE UPDATE ON configurations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


CREATE TABLE companies(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    firebase_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO companies (name, email) VALUES
('COLAS Madagascar', 'contact@colas.mg'),
('Groupe ETI Construction', 'info@eti-construction.mg'),
('SOGEA SATOM Madagascar', 'sogeamadagascar@sogea-satom.com'),
('TRAVOTECH', 'administration@travotech.mg'),
('BTP Madagascar', 'btp@btpmadagascar.mg'),
('Mad''Artisan', 'contact@madartisan.mg'),
('Road Masters Tana', 'contact@roadmasters.mg');

CREATE TRIGGER update_companies_updated_at BEFORE UPDATE ON companies FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


CREATE TABLE status(
    id SERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE,
    label VARCHAR(100),
    firebase_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO status (status_code, label) VALUES
('SUBMITTED', 'Soumis'),
--('UNDER_REVIEW', 'En cours d''examen'),
('ASSIGNED', 'Assigné à une entreprise'),
('IN_PROGRESS', 'Travaux en cours'),
('COMPLETED', 'Terminé');
--('CANCELLED', 'Annulé'),
-- ('VERIFIED', 'Vérifié et validé');

CREATE TRIGGER update_status_updated_at BEFORE UPDATE ON status FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


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
    user_status_type_id INTEGER NOT NULL REFERENCES user_status_types(id), -- Colonne denormalisee
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    description TEXT,
    status_id INTEGER NOT NULL REFERENCES status(id),
    firebase_id VARCHAR(100),
    niveau INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_reports_updated_at BEFORE UPDATE ON reports FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE reports_assignation(
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(id),
    report_id INTEGER NOT NULL REFERENCES reports(id),
    budget DECIMAL(15,2),
    start_date DATE,
    deadline DATE,
    firebase_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_reports_assignation_updated_at BEFORE UPDATE ON reports_assignation FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

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
    registration_date TIMESTAMP,
    firebase_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE image_report(
    id SERIAL PRIMARY KEY,
    lien VARCHAR(1000) NOT NULL,
    report_id INTEGER NOT NULL REFERENCES reports(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_image_report_updated_at BEFORE UPDATE ON image_report FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE synchronization_logs(
    id SERIAL PRIMARY KEY,
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    table_name VARCHAR(100),
    records_synced INTEGER DEFAULT 0,
    sync_type VARCHAR(50) -- POSTGRES_TO_FIREBASE, FIREBASE_TO_POSTGRES
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

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE user_tokens(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    token TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_user_tokens_updated_at BEFORE UPDATE ON user_tokens FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();