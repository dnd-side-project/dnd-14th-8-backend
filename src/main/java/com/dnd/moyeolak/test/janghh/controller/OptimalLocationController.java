package com.dnd.moyeolak.test.janghh.controller;

import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest;
import com.dnd.moyeolak.test.janghh.dto.response.OptimalLocationResponse;
import com.dnd.moyeolak.test.janghh.service.OptimalLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test/janghh/optimal-location")
@RequiredArgsConstructor
public class OptimalLocationController {

    private final OptimalLocationService optimalLocationService;

    @PostMapping
    public ApiResponse<OptimalLocationResponse> findOptimalLocations(
        @Valid @RequestBody OptimalLocationRequest request
    ) {
        log.info("최적 만남 장소 추천 요청: 참가자 {}명", request.participants().size());

        OptimalLocationResponse response = optimalLocationService.findOptimalLocations(request);

        log.info("최적 만남 장소 추천 완료: {}개 장소", response.recommendations().size());

        return ApiResponse.success(response);
    }
}
