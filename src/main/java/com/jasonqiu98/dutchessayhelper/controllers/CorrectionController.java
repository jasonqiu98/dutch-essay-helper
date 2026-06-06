package com.jasonqiu98.dutchessayhelper.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jasonqiu98.dutchessayhelper.requests.CorrectionRequest;
import com.jasonqiu98.dutchessayhelper.responses.CorrectionResponse;
import com.jasonqiu98.dutchessayhelper.services.CorrectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/essays")
@RequiredArgsConstructor
public class CorrectionController {
    
    private final CorrectionService correctionService;

    @PostMapping("/correct")
    public CorrectionResponse correctEssay(@Valid @RequestBody CorrectionRequest request) {
        String feedback = correctionService.correctEssay(request.taskType(), request.prompt(), request.essay());
        return new CorrectionResponse(feedback);
    }

}
