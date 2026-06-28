CREATE TABLE IF NOT EXISTS status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(255)
);

INSERT IGNORE INTO status (id, status) VALUES (1, 'Submitted'), (2, 'Approved'), (3, 'Rejected');

CREATE TABLE IF NOT EXISTS rpp (
    id INT AUTO_INCREMENT PRIMARY KEY,
    participant_id INT,
    work_title TEXT,
    group_role TEXT,
    task_description TEXT,
    start_date DATE,
    finish_date DATE,
    status INT,
    FOREIGN KEY (status) REFERENCES status(id)
);

CREATE TABLE IF NOT EXISTS logbook (
    id INT AUTO_INCREMENT PRIMARY KEY,
    participant_id INT,
    date DATE,
    project_name VARCHAR(255),
    project_manager VARCHAR(255),
    technical_leader VARCHAR(255),
    task TEXT,
    time_and_activity TEXT,
    tools TEXT,
    work_result TEXT,
    description TEXT,
    grade INT,
    encountered_problem VARCHAR(255),
    status INT,
    FOREIGN KEY (status) REFERENCES status(id)
);

CREATE TABLE IF NOT EXISTS laporan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    participant_id INT,
    uri_name VARCHAR(255),
    upload_date DATE,
    phase INT
);

-- Insert Dummy Data for participant 1
INSERT INTO rpp (participant_id, work_title, start_date, finish_date, status) VALUES 
(1, 'Proyek Dummy E-Commerce RPP 1', '2026-05-10', '2026-06-10', 1),
(1, 'Proyek Dummy Sistem Kasir RPP 2', '2026-06-11', '2026-07-11', 1),
(1, 'Proyek Dummy Monitoring API RPP 3', '2026-07-12', '2026-08-12', 1);

INSERT INTO logbook (participant_id, project_name, date, description, status) VALUES 
(1, 'E-Commerce App', '2026-06-25', 'Melakukan setup awal repository frontend dan backend.', 1),
(1, 'Sistem Kasir', '2026-06-26', 'Membuat skema database untuk sistem inventory dan kasir.', 1),
(1, 'Monitoring API', '2026-06-27', 'Mengintegrasikan API Gateway dan implementasi JWT auth.', 1);

INSERT INTO self_assessment (participant_id, start_date, finish_date) VALUES 
(1, '2026-05-01', '2026-05-31'),
(1, '2026-06-01', '2026-06-30'),
(1, '2026-07-01', '2026-07-31');

INSERT INTO laporan (participant_id, phase, upload_date, uri_name) VALUES 
(1, 1, '2026-05-30', 'https://dummy.link/laporan-fase-1.pdf'),
(1, 2, '2026-06-30', 'https://dummy.link/laporan-fase-2.pdf'),
(1, 3, '2026-07-30', 'https://dummy.link/laporan-fase-3.pdf');
