package com.dnd.moyeolak.test.janghh.dto.response;

import java.util.List;

public record OptimalLocationResponse(
    CenterPoint centerPoint,
    List<EvaluatedPlace> recommendations
) {}
