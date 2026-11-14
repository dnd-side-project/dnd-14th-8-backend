-- 출발지로 장소를 고를 때 본 주소. 장소 이름으로 고른 경우에만 값이 있고, 기존 출발지는 NULL로 남는다.
ALTER TABLE location_vote
    ADD COLUMN departure_address varchar(255);
