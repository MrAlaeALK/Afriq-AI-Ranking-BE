package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.service.DimensionService;
import com.pfa.pfaproject.service.DimensionWeightService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dimension")
@AllArgsConstructor
public class DimensionController {
    private final DimensionService dimensionService;
    private final DimensionWeightService dimensionWeightService;

    @GetMapping("/alldimensions")
    public ResponseEntity<?> findAllDimensions() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionService.findAll()));
    }

    @GetMapping("/dimensions")
    public ResponseEntity<?> getAllDimensions() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionService.getAllDimensions()));
    }

    @PostMapping("/year_dimensions")
    public ResponseEntity<?> getYearDimensions(@RequestBody Map<String, Integer> year) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionWeightService.getYearDimensions(year.get("year"))));
    }
}
