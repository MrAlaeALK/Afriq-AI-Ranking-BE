package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.service.RankService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rank")
@AllArgsConstructor
public class RankController {
    private final RankService rankService;
    @PostMapping("/ranking")
    public ResponseEntity<?> getCountryRanking(@RequestBody Map<String, Integer> request) {
        Integer year = request.get("year");
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(rankService.findAllByYearOrderByRank(year)));
    }
}
