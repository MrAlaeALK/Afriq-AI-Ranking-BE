package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.getFinalScoreAndRankDTO;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.CountryRepository;
import com.pfa.pfaproject.service.DashboardService;
import com.pfa.pfaproject.service.DimensionService;
import com.pfa.pfaproject.service.IndicatorService;
import com.pfa.pfaproject.service.ScoreService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private DimensionService dimensionService;

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/countries")
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @GetMapping("/dimensions")
    public List<Dimension> getAllDimensions() {
        return dimensionService.findAll();
    }

    @GetMapping("/indicators")
    public List<Indicator> getAllIndicators() {
        return indicatorService.findAll();
    }

    @GetMapping("/scores")
    public List<Score> getAllScores() {
        return scoreService.findAll();
    }

    @GetMapping("/scores/country/{countryId}/indicator/{indicatorId}/year/{year}")
    public Score getScoreByCountryIndicatorYear(
            @PathVariable Long countryId,
            @PathVariable Long indicatorId,
            @PathVariable int year) {
        return scoreService.findByCountryIdAndIndicatorIdAndYear(countryId, indicatorId, year);
    }

    @GetMapping("/dashboard/top-countries")
    public List<DashboardService.CountryRankWithChange> getTopCountries() {
        return dashboardService.getTopCountries();
    }

    @GetMapping("/dashboard/top-countries/{year}")
    public List<getFinalScoreAndRankDTO> getTopCountriesByYear(@PathVariable int year) {
        return dashboardService.getCountriesRankingForYear(year);
    }

    @PostMapping("/dashboard/generate-ranks/{year}")
    public String generateRanksFromScores(@PathVariable int year) {
        return dashboardService.generateRanks(year);
    }

    @GetMapping("/debug/scores/{year}")
    public List<Score> getScoresByYear(@PathVariable int year) {
        return dashboardService.getScoresByYear(year);
    }

    @GetMapping("/debug/rank-score-changes")
    public Map<String, Object> debugRankScoreChanges() {
        int currentYear = Year.now().getValue();
        int previousYear = currentYear - 1;

        Map<String, Object> debug = new HashMap<>();
        debug.put("currentYear", currentYear);
        debug.put("previousYear", previousYear);
        debug.put("currentYearRanks", dashboardService.getCountriesRankingForYear(currentYear));
        debug.put("previousYearRanks", dashboardService.getCountriesRankingForYear(previousYear));
        debug.put("topCountriesWithChanges", dashboardService.getTopCountries());

        return debug;
    }

    /**
     * Nouveau endpoint pour obtenir des informations sur les années disponibles
     */
    @GetMapping("/dashboard/years-info")
    public Map<String, Object> getAvailableYearsInfo() {
        return dashboardService.getAvailableYearsInfo();
    }

    /**
     * Nouveau endpoint pour obtenir les données du dashboard avec contexte
     */
    @GetMapping("/dashboard/context")
    public Map<String, Object> getDashboardContext() {
        Map<String, Object> context = new HashMap<>();

        // Informations sur les années
        Map<String, Object> yearsInfo = dashboardService.getAvailableYearsInfo();
        context.put("yearsInfo", yearsInfo);

        // Top countries avec les changements
        List<DashboardService.CountryRankWithChange> topCountries = dashboardService.getTopCountries();
        context.put("topCountries", topCountries);

        // Informations générales
        context.put("totalCountries", countryRepository.findAll().size());
        context.put("totalDimensions", dimensionService.findAll().size());
        context.put("totalIndicators", indicatorService.findAll().size());
        context.put("totalScores", scoreService.findAll().size());

        return context;
    }
}