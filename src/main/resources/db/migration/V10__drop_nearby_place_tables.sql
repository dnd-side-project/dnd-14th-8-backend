-- 주변 장소 검색 API를 지우면서 그 저장 테이블 두 개를 지운다(#192).
-- 카카오 키워드 검색 결과(장소 이름, 주소, 좌표)를 역 좌표마다 30일 저장해 다시 쓰던 테이블이다.
-- 카카오는 장소 ID와 장소 링크만 저장할 수 있다고 답했고, 주변 장소는 프론트가 카카오 지도 SDK로 바로 찾는다.
-- nearby_place_hours가 외래 키(fk_nearby_place_hours_nearby_place)로 nearby_place를 가리키므로 먼저 지운다.
-- 유니크 제약(uk_base_place)과 인덱스(idx_base_coords)는 테이블과 함께 지워진다.
DROP TABLE IF EXISTS nearby_place_hours;
DROP TABLE IF EXISTS nearby_place;
