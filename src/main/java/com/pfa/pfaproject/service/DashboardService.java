package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.getFinalScoreAndRankDTO;
import com.pfa.pfaproject.model.*;
import com.pfa.pfaproject.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private RankService rankService;

    public record CountryRankWithChange(
            Long countryId,
            String countryName,
            String countryCode,
            String countryRegion,
            Double finalScore,
            Integer rank,
            String rankChange,
            String scoreChange,
            Integer displayYear, // Année des données affichées
            boolean isCurrentYear // Indique si ce sont les données de l'année courante
    ) {}

    public List<CountryRankWithChange> getTopCountries() {
        int currentYear = Year.now().getValue();

        // Trouver l'année la plus récente avec des données
        int displayYear = findMostRecentYearWithData(currentYear);
        int comparisonYear = displayYear - 1;

        System.out.println("Année d'affichage: " + displayYear + ", Année de comparaison: " + comparisonYear);

        List<getFinalScoreAndRankDTO> displayYearRanks = getCountriesRankingForYear(displayYear);
        List<getFinalScoreAndRankDTO> comparisonYearRanks = getCountriesRankingForYear(comparisonYear);

        Map<Long, Integer> comparisonRankMap = comparisonYearRanks.stream()
                .collect(Collectors.toMap(getFinalScoreAndRankDTO::countryId, getFinalScoreAndRankDTO::rank));
        Map<Long, Double> comparisonScoreMap = comparisonYearRanks.stream()
                .collect(Collectors.toMap(getFinalScoreAndRankDTO::countryId, getFinalScoreAndRankDTO::finalScore));

        return displayYearRanks.stream().limit(5).map(current -> {
            String rankChange = calculateRankChange(current.countryId(), current.rank(), comparisonRankMap);
            String scoreChange = calculateScoreChange(current.countryId(), current.finalScore(), comparisonScoreMap);
            return new CountryRankWithChange(
                    current.countryId(),
                    current.countryName(),
                    current.countryCode(),
                    current.countryRegion(),
                    current.finalScore(),
                    current.rank(),
                    rankChange,
                    scoreChange,
                    displayYear,
                    displayYear == currentYear
            );
        }).collect(Collectors.toList());
    }

    /**
     * Trouve l'année la plus récente avec des données, en commençant par l'année courante
     */
    private int findMostRecentYearWithData(int startYear) {
        for (int year = startYear; year >= startYear - 5; year--) { // Cherche jusqu'à 5 ans en arrière
            if (hasDataForYear(year)) {
                return year;
            }
        }
        return startYear; // Retourne l'année courante par défaut
    }

    /**
     * Vérifie si des données existent pour une année donnée
     */
    private boolean hasDataForYear(int year) {
        // Vérifier d'abord dans les rangs
        List<getFinalScoreAndRankDTO> ranks = rankService.findAllByYearOrderByRank(year);
        if (!ranks.isEmpty()) {
            return true;
        }

        // Vérifier dans les scores
        List<Score> scores = scoreService.findAll().stream()
                .filter(score -> score.getYear() == year)
                .collect(Collectors.toList());

        return !scores.isEmpty();
    }

    public List<getFinalScoreAndRankDTO> getCountriesRankingForYear(int year) {
        List<getFinalScoreAndRankDTO> ranks = rankService.findAllByYearOrderByRank(year);
        return ranks.isEmpty() ? calculateTopCountriesFromScores(year) : ranks;
    }

    public List<getFinalScoreAndRankDTO> calculateTopCountriesFromScores(int year) {
        List<Country> countries = countryRepository.findAll();
        List<Indicator> indicators = indicatorService.findAll();
        Map<Long, Integer> weights = indicators.stream()
                .collect(Collectors.toMap(Indicator::getId, Indicator::getWeight));

        List<Score> scoresForYear = scoreService.findAll().stream()
                .filter(score -> score.getYear() == year)
                .collect(Collectors.toList());

        if (scoresForYear.isEmpty()) {
            System.out.println("Aucun score trouvé pour l'année " + year);
            return new ArrayList<>();
        }

        Map<Long, List<Score>> grouped = scoresForYear.stream()
                .collect(Collectors.groupingBy(score -> score.getCountry().getId()));

        List<getFinalScoreAndRankDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Score>> entry : grouped.entrySet()) {
            Country c = countries.stream().filter(ctry -> ctry.getId().equals(entry.getKey())).findFirst().orElse(null);
            if (c == null) continue;
            double sum = 0.0, total = 0.0;
            for (Score s : entry.getValue()) {
                int w = weights.getOrDefault(s.getIndicator().getId(), 0);
                sum += s.getScore() * w;
                total += w;
            }
            double avg = total > 0 ? sum / total : 0.0;
            result.add(new getFinalScoreAndRankDTO(c.getId(), c.getName(), c.getCode(), c.getRegion(), avg, 0));
        }

        result.sort((a, b) -> Double.compare(b.finalScore(), a.finalScore()));
        for (int i = 0; i < result.size(); i++) {
            getFinalScoreAndRankDTO d = result.get(i);
            result.set(i, new getFinalScoreAndRankDTO(
                    d.countryId(), d.countryName(), d.countryCode(), d.countryRegion(), d.finalScore(), i + 1));
        }

        return result;
    }

    public String generateRanks(int year) {
        List<getFinalScoreAndRankDTO> topCountries = calculateTopCountriesFromScores(year);
        if (topCountries.isEmpty()) return "Aucun score trouvé pour l'année " + year;

        int saved = 0;
        for (getFinalScoreAndRankDTO dto : topCountries) {
            if (!rankService.existsByCountryIdAndYear(dto.countryId(), year)) {
                Country c = countryRepository.findById(dto.countryId()).orElse(null);
                if (c != null) {
                    Rank r = new Rank();
                    r.setCountry(c);
                    r.setYear(year);
                    r.setFinalScore(dto.finalScore());
                    r.setRank(dto.rank());
                    rankService.save(r);
                    saved++;
                }
            }
        }

        return "Rangs générés avec succès pour " + saved + " pays pour l'année " + year;
    }

    public List<Score> getScoresByYear(int year) {
        return scoreService.findAll().stream()
                .filter(score -> score.getYear() == year)
                .collect(Collectors.toList());
    }

    /**
     * Méthode utilitaire pour obtenir des informations sur les années disponibles
     */
    public Map<String, Object> getAvailableYearsInfo() {
        int currentYear = Year.now().getValue();
        int mostRecentYear = findMostRecentYearWithData(currentYear);

        Map<String, Object> info = new HashMap<>();
        info.put("currentYear", currentYear);
        info.put("mostRecentYearWithData", mostRecentYear);
        info.put("isUsingCurrentYearData", mostRecentYear == currentYear);
        info.put("yearsDifference", currentYear - mostRecentYear);

        // Liste des années avec des données
        List<Integer> yearsWithData = new ArrayList<>();
        for (int year = currentYear; year >= currentYear - 10; year--) {
            if (hasDataForYear(year)) {
                yearsWithData.add(year);
            }
        }
        info.put("availableYears", yearsWithData);

        return info;
    }

    private String calculateRankChange(Long id, Integer currentRank, Map<Long, Integer> previousRanks) {
        Integer old = previousRanks.get(id);
        if (old == null) return "NEW";
        int diff = old - currentRank;
        return diff > 0 ? "+" + diff : (diff < 0 ? String.valueOf(diff) : "=");
    }

    private String calculateScoreChange(Long id, Double currentScore, Map<Long, Double> previousScores) {
        Double old = previousScores.get(id);
        if (old == null || currentScore == null) return "NEW";
        double diff = currentScore - old;
        if (Math.abs(diff) < 0.1) return "=";
        return diff > 0 ? "+" + String.format("%.1f", diff) : String.format("%.1f", diff);
    }
}