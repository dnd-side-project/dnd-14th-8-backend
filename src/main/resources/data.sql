-- =====================================================
-- 초기 테스트 데이터: 모임 10개 (각 모임당 방장 1명)
-- =====================================================

-- Meeting 10개
INSERT INTO meeting (meeting_id, participant_count, created_at, updated_at) VALUES
('test-meeting-001', 5, NOW(), NOW()),
('test-meeting-002', 4, NOW(), NOW()),
('test-meeting-003', 6, NOW(), NOW()),
('test-meeting-004', 3, NOW(), NOW()),
('test-meeting-005', 8, NOW(), NOW()),
('test-meeting-006', 2, NOW(), NOW()),
('test-meeting-007', 7, NOW(), NOW()),
('test-meeting-008', 4, NOW(), NOW()),
('test-meeting-009', 5, NOW(), NOW()),
('test-meeting-010', 6, NOW(), NOW());

-- SchedulePoll 10개 (각 모임당 1개, 14일간 날짜 옵션)
INSERT INTO schedule_poll (meeting_id, date_options, start_time, end_time, confirmed_start_time, confirmed_end_time, poll_status, created_at, updated_at) VALUES
('test-meeting-001', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-002', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-003', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-004', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-005', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-006', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-007', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-008', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-009', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW()),
('test-meeting-010', '["2025-02-05","2025-02-06","2025-02-07","2025-02-08","2025-02-09","2025-02-10","2025-02-11","2025-02-12","2025-02-13","2025-02-14","2025-02-15","2025-02-16","2025-02-17","2025-02-18"]', 7, 24, NULL, NULL, 'INACTIVE', NOW(), NOW());

-- LocationPoll 10개 (각 모임당 1개)
INSERT INTO location_poll (meeting_id, poll_status, created_at, updated_at) VALUES
('test-meeting-001', 'INACTIVE', NOW(), NOW()),
('test-meeting-002', 'INACTIVE', NOW(), NOW()),
('test-meeting-003', 'INACTIVE', NOW(), NOW()),
('test-meeting-004', 'INACTIVE', NOW(), NOW()),
('test-meeting-005', 'INACTIVE', NOW(), NOW()),
('test-meeting-006', 'INACTIVE', NOW(), NOW()),
('test-meeting-007', 'INACTIVE', NOW(), NOW()),
('test-meeting-008', 'INACTIVE', NOW(), NOW()),
('test-meeting-009', 'INACTIVE', NOW(), NOW()),
('test-meeting-010', 'INACTIVE', NOW(), NOW());

INSERT INTO participant (meeting_id, local_storage_key, name, is_host, created_at, updated_at) VALUES
('test-meeting-001', 'host-key-001', '김민준', TRUE, NOW(), NOW()),
('test-meeting-002', 'host-key-002', '이서연', TRUE, NOW(), NOW()),
('test-meeting-003', 'host-key-003', '박도윤', TRUE, NOW(), NOW()),
('test-meeting-004', 'host-key-004', '최하은', TRUE, NOW(), NOW()),
('test-meeting-005', 'host-key-005', '정시우', TRUE, NOW(), NOW()),
('test-meeting-006', 'host-key-006', '강지아', TRUE, NOW(), NOW()),
('test-meeting-007', 'host-key-007', '조예준', TRUE, NOW(), NOW()),
('test-meeting-008', 'host-key-008', '윤수아', TRUE, NOW(), NOW()),
('test-meeting-009', 'host-key-009', '임건우', TRUE, NOW(), NOW()),
('test-meeting-010', 'host-key-010', '한지유', TRUE, NOW(), NOW());
