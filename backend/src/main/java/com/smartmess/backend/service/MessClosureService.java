package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.CreateMessClosureRequest;
import com.smartmess.backend.dto.request.UpdateMessClosureRequest;
import com.smartmess.backend.dto.response.MessClosureResponse;

public interface MessClosureService {

    MessClosureResponse createClosure(
            CreateMessClosureRequest request
    );

    MessClosureResponse updateClosure(
            Long closureId,
            UpdateMessClosureRequest request
    );

    void deleteClosure(
            Long closureId
    );

    List<MessClosureResponse> getCurrentAndUpcomingClosures();

    List<MessClosureResponse> getClosureHistory();
}