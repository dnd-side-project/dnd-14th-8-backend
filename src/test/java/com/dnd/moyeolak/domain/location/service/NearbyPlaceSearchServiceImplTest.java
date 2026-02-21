package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.NearbyPlaceSearchResponse;
import com.dnd.moyeolak.domain.location.entity.NearbyPlace;
import com.dnd.moyeolak.domain.location.entity.NearbyPlaceHours;
import com.dnd.moyeolak.domain.location.entity.enums.PlaceCategory;
import com.dnd.moyeolak.domain.location.repository.NearbyPlaceRepository;
import com.dnd.moyeolak.domain.location.service.BusinessHoursCalculator.BusinessStatus;
import com.dnd.moyeolak.domain.location.service.impl.NearbyPlaceSearchServiceImpl;
import com.dnd.moyeolak.global.client.google.GooglePlacesClient;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.Close;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.Location;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.Open;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.Period;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.Place;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse.RegularOpeningHours;
import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;
import com.dnd.moyeolak.global.entity.BaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NearbyPlaceSearchServiceImplTest {

    @Mock
    private GooglePlacesClient googlePlacesClient;

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @Mock
    private NearbyPlaceRepository nearbyPlaceRepository;

    @Mock
    private BusinessHoursCalculator businessHoursCalculator;

    @InjectMocks
    private NearbyPlaceSearchServiceImpl nearbyPlaceSearchService;

    private static final String BASE_LAT = "37.5000000";
    private static final String BASE_LNG = "127.0000000";
    private static final BigDecimal BASE_LAT_BD = new BigDecimal(BASE_LAT);
    private static final BigDecimal BASE_LNG_BD = new BigDecimal(BASE_LNG);

    // ---- 헬퍼 메서드 ----

    private void setUpdatedAt(NearbyPlace place, LocalDateTime time) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(place, time);
    }

    private NearbyPlace createCachedPlace(PlaceCategory category, String googlePlaceId, String name) {
        return NearbyPlace.of(
                BASE_LAT_BD, BASE_LNG_BD, category, googlePlaceId,
                name, "서울시 강남구", new BigDecimal("37.5001"), new BigDecimal("127.0001"),
                "https://place.map.kakao.com/12345", 500
        );
    }

    private Place googlePlace(String id, String name, double lat, double lng) {
        return new Place(
                id,
                "서울시 강남구",
                new GooglePlacesResponse.DisplayName(name, "ko"),
                new Location(lat, lng),
                null
        );
    }

    private Place googlePlaceWithHours(String id, String name, double lat, double lng) {
        return new Place(
                id,
                "서울시 강남구",
                new GooglePlacesResponse.DisplayName(name, "ko"),
                new Location(lat, lng),
                new RegularOpeningHours(true, List.of(
                        new Period(
                                new Open(1, 9, 0),
                                new Close(1, 22, 0)
                        )
                ), null)
        );
    }

    private CategorySearchResponse kakaoResponse(String placeUrl) {
        CategorySearchResponse.Place kakaoPlace = new CategorySearchResponse.Place(
                "kakao-1", "테스트카페", "카페", "CE7", "카페",
                "02-1234-5678", "서울시 강남구", "서울시 강남구 역삼로",
                "127.0001", "37.5001", placeUrl, "500"
        );
        return new CategorySearchResponse(
                new CategorySearchResponse.Meta(1, 1, true, null),
                List.of(kakaoPlace)
        );
    }

    private CategorySearchResponse emptyKakaoResponse() {
        return new CategorySearchResponse(
                new CategorySearchResponse.Meta(0, 0, true, null),
                List.of()
        );
    }

    private void stubGoogleForAllCategories(GooglePlacesResponse response) {
        when(googlePlacesClient.searchText(any())).thenReturn(response);
    }

    private void stubKakaoForAllCalls(CategorySearchResponse response) {
        when(kakaoLocalClient.searchByKeyword(any())).thenReturn(response);
    }

    // ---- 테스트 ----

    @Nested
    @DisplayName("캐시 HIT 테스트")
    class CacheHitTest {

        @Test
        @DisplayName("신선한 캐시 데이터가 있으면 API 호출 없이 응답을 반환한다")
        void freshCache_returnsWithoutApiCall() throws Exception {
            // given
            NearbyPlace cached = createCachedPlace(PlaceCategory.CAFE, "google-1", "테스트카페");
            setUpdatedAt(cached, LocalDateTime.now().minusDays(10));

            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of(cached));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(null, null));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            assertThat(response.categories()).hasSize(1);
            assertThat(response.categories().getFirst().category()).isEqualTo("카페");
            assertThat(response.categories().getFirst().places()).hasSize(1);
            assertThat(response.categories().getFirst().places().getFirst().name()).isEqualTo("테스트카페");

            verify(googlePlacesClient, never()).searchText(any());
            verify(kakaoLocalClient, never()).searchByKeyword(any());
            verify(nearbyPlaceRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("만료된 캐시 데이터가 있으면 삭제 후 API를 호출한다")
        void expiredCache_deletesAndCallsApis() throws Exception {
            // given
            NearbyPlace cached = createCachedPlace(PlaceCategory.CAFE, "google-1", "테스트카페");
            setUpdatedAt(cached, LocalDateTime.now().minusDays(31));

            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of(cached));
            stubGoogleForAllCategories(new GooglePlacesResponse(null));

            // when
            nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            verify(nearbyPlaceRepository).deleteByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD);
            verify(googlePlacesClient, atLeastOnce()).searchText(any());
            verify(nearbyPlaceRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("캐시 MISS 테스트")
    class CacheMissTest {

        @Test
        @DisplayName("캐시가 비어있으면 API를 호출하고 결과를 저장한다")
        void emptyCache_callsApisAndSaves() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            Place gPlace = googlePlace("google-1", "테스트카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            stubKakaoForAllCalls(kakaoResponse("https://place.map.kakao.com/12345"));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(null, null));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            verify(googlePlacesClient, atLeastOnce()).searchText(any());
            verify(kakaoLocalClient, atLeastOnce()).searchByKeyword(any());
            verify(nearbyPlaceRepository).saveAll(any());
            assertThat(response.categories()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Google API 응답 테스트")
    class GoogleApiTest {

        @Test
        @DisplayName("Google API가 빈 결과를 반환하면 Kakao 호출 없이 빈 응답을 반환한다")
        void googleReturnsEmpty_noKakaoCall() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());
            stubGoogleForAllCategories(new GooglePlacesResponse(null));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            assertThat(response.categories()).isEmpty();
            verify(kakaoLocalClient, never()).searchByKeyword(any());
        }
    }

    @Nested
    @DisplayName("Kakao 검증 테스트")
    class KakaoVerificationTest {

        @Test
        @DisplayName("Kakao가 빈 documents를 반환하면 해당 장소를 스킵한다")
        void kakaoReturnsEmptyDocuments_skipsPlace() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            Place gPlace = googlePlace("google-1", "유령카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            stubKakaoForAllCalls(emptyKakaoResponse());

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            assertThat(response.categories()).isEmpty();
        }

        @Test
        @DisplayName("Kakao 호출 시 예외가 발생하면 해당 장소를 스킵하고 전체 예외를 전파하지 않는다")
        void kakaoThrowsException_skipsPlaceWithoutPropagation() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            Place gPlace = googlePlace("google-1", "테스트카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            when(kakaoLocalClient.searchByKeyword(any())).thenThrow(new RuntimeException("Kakao API 오류"));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            assertThat(response.categories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("응답 빌드 테스트")
    class ResponseBuildTest {

        @Test
        @DisplayName("정상 흐름에서 카테고리별 PlaceDetail이 포함된 응답을 반환한다")
        void normalFlow_returnsResponseWithCategoryPlaces() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            Place gPlace = googlePlace("google-1", "테스트카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            stubKakaoForAllCalls(kakaoResponse("https://place.map.kakao.com/12345"));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(true, "22:00에 영업 종료"));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            assertThat(response.categories()).isNotEmpty();

            NearbyPlaceSearchResponse.PlaceDetail firstPlace =
                    response.categories().getFirst().places().getFirst();
            assertThat(firstPlace.name()).isEqualTo("테스트카페");
            assertThat(firstPlace.kakaoPlaceUrl()).isEqualTo("https://place.map.kakao.com/12345");
            assertThat(firstPlace.isOpen()).isTrue();
            assertThat(firstPlace.businessStatusMessage()).isEqualTo("22:00에 영업 종료");
        }
    }

    @Nested
    @DisplayName("중복 제거 테스트")
    class DeduplicationTest {

        @Test
        @DisplayName("다른 카테고리에서 같은 googlePlaceId가 등장하면 두 번째는 스킵한다")
        void duplicateGooglePlaceId_skipsSecond() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            // 모든 카테고리에서 같은 ID의 장소가 반환됨
            Place duplicatePlace = googlePlace("same-google-id", "중복카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(duplicatePlace)));
            stubKakaoForAllCalls(kakaoResponse("https://place.map.kakao.com/12345"));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(null, null));

            // when
            NearbyPlaceSearchResponse response = nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then — 4개 카테고리 모두에서 같은 ID이므로 1건만 저장
            long totalPlaces = response.categories().stream()
                    .mapToLong(c -> c.places().size())
                    .sum();
            assertThat(totalPlaces).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("영업시간 테스트")
    class BusinessHoursTest {

        @Test
        @DisplayName("regularOpeningHours + periods가 있으면 NearbyPlaceHours가 생성된다")
        void withPeriods_createsHours() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            Place gPlace = googlePlaceWithHours("google-1", "테스트카페", 37.5001, 127.0001);
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            stubKakaoForAllCalls(kakaoResponse("https://place.map.kakao.com/12345"));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(true, "22:00에 영업 종료"));

            // when
            nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then — saveAll에 전달된 장소에 hours가 포함되었는지 검증
            @SuppressWarnings("unchecked")
            var captor = org.mockito.ArgumentCaptor.forClass((Class<List<NearbyPlace>>) (Class<?>) List.class);
            verify(nearbyPlaceRepository).saveAll(captor.capture());

            List<NearbyPlace> saved = captor.getValue();
            // 첫 번째 카테고리의 장소 (중복 제거로 1건만)
            NearbyPlace firstPlace = saved.getFirst();
            assertThat(firstPlace.getNearbyPlaceHours()).hasSize(1);

            NearbyPlaceHours hours = firstPlace.getNearbyPlaceHours().getFirst();
            assertThat(hours.getOpenDay()).isEqualTo(1);
            assertThat(hours.getOpenHour()).isEqualTo(9);
            assertThat(hours.getOpenMinute()).isEqualTo(0);
            assertThat(hours.getCloseDay()).isEqualTo(1);
            assertThat(hours.getCloseHour()).isEqualTo(22);
            assertThat(hours.getCloseMinute()).isEqualTo(0);
        }

        @Test
        @DisplayName("periods가 null이면 hours가 생성되지 않는다")
        void withNullPeriods_noHoursCreated() {
            // given
            when(nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(BASE_LAT_BD, BASE_LNG_BD))
                    .thenReturn(List.of());

            // regularOpeningHours는 있지만 periods가 null
            Place gPlace = new Place(
                    "google-1", "서울시 강남구",
                    new GooglePlacesResponse.DisplayName("테스트카페", "ko"),
                    new Location(37.5001, 127.0001),
                    new RegularOpeningHours(true, null, null)
            );
            stubGoogleForAllCategories(new GooglePlacesResponse(List.of(gPlace)));
            stubKakaoForAllCalls(kakaoResponse("https://place.map.kakao.com/12345"));
            when(businessHoursCalculator.calculateBusinessStatus(any()))
                    .thenReturn(new BusinessStatus(null, null));

            // when
            nearbyPlaceSearchService.nearbyPlaceSearch(BASE_LAT, BASE_LNG);

            // then
            @SuppressWarnings("unchecked")
            var captor = org.mockito.ArgumentCaptor.forClass((Class<List<NearbyPlace>>) (Class<?>) List.class);
            verify(nearbyPlaceRepository).saveAll(captor.capture());

            List<NearbyPlace> saved = captor.getValue();
            NearbyPlace firstPlace = saved.getFirst();
            assertThat(firstPlace.getNearbyPlaceHours()).isEmpty();
        }
    }
}
