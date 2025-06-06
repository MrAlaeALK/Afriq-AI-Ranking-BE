package com.pfa.pfaproject.service;

import com.pfa.pfaproject.config.JWT.JwtUtil;
import com.pfa.pfaproject.dto.Admin.LoginDTO;
import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.dto.Upload.SpreadsheetDataDTO;
import com.pfa.pfaproject.dto.Weight.AddIndicatorWeightDTO;
import com.pfa.pfaproject.dto.Weight.AddWeightDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.*;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business service that orchestrates operations across multiple domain entities.
 * Handles high-level business workflows for administrators including authentication,
 * data management, and ranking generation.
 *
 * @since 1.0
 * @version 1.1
 */
@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AdminBusinessService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminService adminService;
    private final CountryService countryService;
    private final IndicatorService indicatorService;
    private final DimensionService dimensionService;
    private final ScoreService scoreService;
    private final RankService rankService;
    private final DimensionWeightService dimensionWeightService;
    private final IndicatorWeightService indicatorWeightService;
    private final JwtUtil jwtUtil;
    private final DimensionScoreService dimensionScoreService;

    /**
     * Registers a new admin user.
     * @param adminToRegisterDTO DTO containing registration details with password confirmation
     * @return JWT token for the newly registered admin
     * @throws CustomException if admin with username or email already exists
     */
    public String register(RegisterDTO adminToRegisterDTO) {

        if (adminService.existsByUsernameOrEmail(adminToRegisterDTO.username(), adminToRegisterDTO.email())) {
            throw new CustomException("Admin already exists", HttpStatus.CONFLICT);
        }

        Admin admin = Admin.builder()
                .email(adminToRegisterDTO.email())
                .username(adminToRegisterDTO.username())
                .firstName(adminToRegisterDTO.firstName())
                .lastName(adminToRegisterDTO.lastName())
                .password(passwordEncoder.encode(adminToRegisterDTO.password()))
                .build();

        adminService.save(admin);

        return jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());
    }

    /**
     * Authenticates an admin user.
     * @param adminToLogin DTO containing login credentials
     * @return JWT token for the authenticated admin
     * @throws CustomException if credentials are invalid
     */
    public String login(LoginDTO adminToLogin) {

        Admin admin = ValidationUtils.isValidEmail(adminToLogin.usernameOrEmail()) ?
                adminService.findByEmail(adminToLogin.usernameOrEmail()) :
                adminService.findByUsername(adminToLogin.usernameOrEmail());

//        Admin admin = (adminToLogin.usernameOrEmail().contains("@") && adminToLogin.usernameOrEmail().contains("."))?
//                adminService.findByEmail(adminToLogin.usernameOrEmail())
//                : adminService.findByUsername(adminToLogin.usernameOrEmail());

        if (admin == null) {
            throw new CustomException("Username or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(adminToLogin.password(), admin.getPassword())) {
            throw new CustomException("Username or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        return jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());
    }

    /**
     * Adds a new country to the system.
     * @param country Country entity to add
     * @return The saved country with ID
     * @throws CustomException if country already exists
     */
    public Country addCountry(Country country) {

        Country existingCountry = countryService.findByName(country.getName());
        if (existingCountry != null) {
            throw new CustomException("Country already exists", HttpStatus.CONFLICT);
        }

        Country savedCountry = countryService.save(country);
        return savedCountry;
    }

    /**
     * Adds a new indicator to the system.
     * @param indicator Indicator entity to add
     * @return The saved indicator with ID
     * @throws CustomException if indicator already exists
     */
    public Indicator addIndicator(Indicator indicator) {
        Indicator existingIndicator = indicatorService.findByName(indicator.getName());
        if (existingIndicator != null) {
            throw new CustomException("Indicator already exists", HttpStatus.CONFLICT);
        }

        Indicator savedIndicator = indicatorService.save(indicator);
        return savedIndicator;
    }

    /**
     * Adds a new indicator category to the system.
     * @param dimension Indicator entity to add
     * @return The saved indicatorCategory
     * @throws CustomException if category already exists
     */
    public Dimension addDimension(Dimension dimension) {
        Dimension existingDimension = dimensionService.findByName(dimension.getName());
        if (existingDimension != null) {
            throw new CustomException("Category already exists", HttpStatus.CONFLICT);
        }

        Dimension savedDimension = dimensionService.save(dimension);
        return savedDimension;
    }

    /**
     * Adds a new score for a country on a specific indicator.
     * @param addOrUpdateScoreDTO DTO containing score details
     * @return The saved score
     * @throws CustomException if score already exists or references invalid entities
     */
    public Score addScore(AddOrUpdateScoreDTO addOrUpdateScoreDTO){
        if(scoreService.findByCountryIdAndIndicatorIdAndYear(addOrUpdateScoreDTO.countryId(), addOrUpdateScoreDTO.indicatorId(), addOrUpdateScoreDTO.year()) == null) {
            Score newScore = Score.builder()
                    .score(addOrUpdateScoreDTO.score())
                    .year(addOrUpdateScoreDTO.year())
                    .country(countryService.findById(addOrUpdateScoreDTO.countryId()))
                    .indicator(indicatorService.findById(addOrUpdateScoreDTO.indicatorId()))
                    .build();
            return scoreService.save(newScore);
        }
        throw new CustomException("Score already exists", HttpStatus.CONFLICT);
    }

    /**
     * Updates an existing score.
     * @param dto DTO containing updated score details
     * @return The updated score
     * @throws CustomException if score doesn't exist
     */
    public Score updateScore(AddOrUpdateScoreDTO dto) {

        Score score = scoreService.findByCountryIdAndIndicatorIdAndYear(
                dto.countryId(), dto.indicatorId(), dto.year());

        if (score == null) {
            throw new CustomException("Score does not exist", HttpStatus.NOT_FOUND);
        }

        score.setScore(dto.score());
        Score updatedScore = scoreService.save(score);

        return updatedScore;
    }

    /**
     * Generates rankings for all countries in a specific year.
     * @param dto DTO containing the year
     * @return List of countries ordered by rank
     */
    public DimensionWeight addDimensionWeight(AddWeightDTO dto) {
        Dimension dimension = dimensionService.findById(dto.id());
        DimensionWeight dimensionWeight = DimensionWeight.builder()
                .year(dto.year())
                .dimensionWeight(dto.weight())
                .dimension(dimension)
                .build();
        return dimensionWeightService.save(dimensionWeight);
    }

    public IndicatorWeight addIndicatorWeight(AddIndicatorWeightDTO dto) {
        IndicatorWeight indicatorWeight = IndicatorWeight.builder()
                .indicator(indicatorService.findById(dto.indicatorId()))
                .weight(dto.weight())
                .dimensionWeight(dimensionWeightService.findByCategoryAndYear(dto.categoryId(), dto.year()))
                .build();
        return indicatorWeightService.save(indicatorWeight);
    }

    public void calculateDimensionScoresForCountry(int year, Country country){
        // find dimensions for that year simply
        List<DimensionWeight> dimensions = dimensionWeightService.findByYear(year);

        for(DimensionWeight dimension : dimensions){

            double dimensionScore = 0;
            double weightSum = 0;

            //get all indicators for that year with their weights which are related to the current dimension
            List<IndicatorWeight> indicators = dimension.getIndicatorWeights();

            //score calculation
            for(IndicatorWeight indicator : indicators){
                Score score = scoreService.findByCountryIdAndIndicatorIdAndYear(country.getId(), indicator.getIndicator().getId(), year);

                dimensionScore += score.getScore() * indicator.getWeight();
                weightSum += indicator.getWeight();
            }

            DimensionScore newDimensionScore = DimensionScore.builder()
                    .country(country)
                    .dimension(dimension.getDimension())
                    .year(year)
                    .score(dimensionScore/weightSum)
                    .build();

            dimensionScoreService.save(newDimensionScore);
        }
    }

    public void calculateDimensionScores(int year){
        List<Country> countries = countryService.findAll();

        for(Country country : countries){
            calculateDimensionScoresForCountry(year, country);
        }
    }

    public Rank calculateFinalScoreForCountry(int year, Country country){
        calculateDimensionScoresForCountry(year, country);

        List<DimensionWeight> dimensions = dimensionWeightService.findByYear(year);

        double finalScore = 0;
        double weightSum = 0;

        for(DimensionWeight dimension : dimensions){
            DimensionScore dimensionScore = dimensionScoreService.findByCountryIdAndDimensionIdAndYear(country.getId(), dimension.getDimension().getId(), year);

            finalScore += dimensionScore.getScore() * dimension.getDimensionWeight();
            weightSum += dimension.getDimensionWeight();
        }

        Rank newRank = Rank.builder()
                .country(country)
                .year(year)
                .finalScore(finalScore/ weightSum)
                .rank(1)
                .build();
        return rankService.save(newRank);
    }

    public  List<Rank> generateFinalScores(GenerateRankOrFinalScoreDTO dto){
        List<Country> countries = countryService.findAll();
        List<Rank> finalScores = new ArrayList<>();
        for(Country country : countries){
            finalScores.add(calculateFinalScoreForCountry(dto.year(), country));
        }
        return finalScores;
    }

    public List<Country> generateRanking(GenerateRankOrFinalScoreDTO dto){
        generateFinalScores(dto);

        List<Rank> ranksOrdered = rankService.findByYearOrderByFinalScoreDesc(dto.year());

        List<Country> countriesRanked = new ArrayList<>();

        int position = 1;
        double lastScore = -1;
        int lastPosition = 0;

        for (Rank rank : ranksOrdered) {
            // If score is the same as the previous country, assign the same rank (tie)
            if (position > 1 && rank.getFinalScore() == lastScore) {
                rank.setRank(lastPosition);
            } else {
                rank.setRank(position);
                lastPosition = position;
            }

            lastScore = rank.getFinalScore();
            position++;

            countriesRanked.add(rankService.save(rank).getCountry());
        }
        return countriesRanked;
    }

    /**
     * Processes bulk indicator data from Excel upload
     * @param extractedData List of data points from Excel file
     * @param overwriteExisting Whether to overwrite existing scores
     * @return Map with processing results and statistics
     */
    @Transactional
    public Map<String, Object> processBulkIndicatorData(List<SpreadsheetDataDTO> extractedData, Boolean overwriteExisting) {
        Map<String, Object> results = new HashMap<>();
        List<String> successfullyProcessed = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int totalRecords = extractedData.size();
        int successCount = 0;
        int errorCount = 0;

        for (SpreadsheetDataDTO data : extractedData) {
            try {
                // Process each record in a separate method to isolate transactions
                processIndividualRecord(data, overwriteExisting);
                successfullyProcessed.add(data.countryName() + " - " + data.indicatorName());
                successCount++;

            } catch (Exception e) {
                String errorMsg = data.countryName() + " - " + data.indicatorName() + ": " + e.getMessage();
                log.error("Error processing {}: {}", errorMsg, e.getMessage());
                errors.add(errorMsg);
                errorCount++;
                // Continue processing other records instead of stopping
            }
        }

        results.put("totalRecords", totalRecords);
        results.put("successCount", successCount);
        results.put("errorCount", errorCount);
        results.put("successfullyProcessed", successfullyProcessed);
        results.put("errors", errors);

        return results;
    }

    // Handle individual record processing
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processIndividualRecord(SpreadsheetDataDTO data, Boolean overwriteExisting) {
        try {
            Score savedScore = addOrUpdateScore(
                    data.countryName(),
                    data.indicatorName(),
                    data.value(),
                    data.year(),
                    overwriteExisting
            );

            if (savedScore == null) {
                throw new CustomException("Failed to save score", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            log.error("Error processing individual record for {} - {}: {}",
                    data.countryName(), data.indicatorName(), e.getMessage());
            throw e; // Re-throw to be caught by the calling method
        }
    }

    /**
     * Recalculates rankings for a specific year
     * This method recalculates dimension scores and final rankings for all countries in the given year
     * @param year The year to recalculate rankings for
     */
    public void recalculateRankingsForYear(Integer year) {

        try {
            // Step 1: Clear existing dimension scores and ranks for this year
            dimensionScoreService.deleteByYear(year);
            rankService.deleteByYear(year);

            // Step 2: Recalculate dimension scores for all countries
            calculateDimensionScores(year);

            // Step 3: Generate final rankings
            GenerateRankOrFinalScoreDTO dto = new GenerateRankOrFinalScoreDTO(year);
            List<Country> rankedCountries = generateRanking(dto);

        } catch (Exception e) {
            throw new CustomException("Failed to recalculate rankings: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to add or update a single score
     * This can be called from the bulk processing or used standalone
     * @param countryName Name of the country
     * @param indicatorName Name of the indicator
     * @param value The score value
     * @param year The year
     * @param overwrite Whether to overwrite existing scores
     * @return The saved/updated Score entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Score addOrUpdateScore(String countryName, String indicatorName, Double value, Integer year, Boolean overwrite) {
        try {
            // Find country with proper error handling
            Country country = countryService.findByName(countryName);
            if (country == null) {
                throw new CustomException("Country not found: " + countryName, HttpStatus.NOT_FOUND);
            }

            // Find indicator with proper error handling
            Indicator indicator = indicatorService.findByName(indicatorName);
            if (indicator == null) {
                throw new CustomException("Indicator not found: " + indicatorName, HttpStatus.NOT_FOUND);
            }

            // Check if score already exists
            Score existingScore = scoreService.findByCountryIdAndIndicatorIdAndYear(
                    country.getId(), indicator.getId(), year);

            if (existingScore != null) {
                if (overwrite) {
                    // Update existing score
                    existingScore.setScore(value);
                    existingScore.setRawValue(value);
                    return scoreService.save(existingScore);
                } else {
                    throw new CustomException("Score already exists for " + countryName + " - " + indicatorName + " - " + year, HttpStatus.CONFLICT);
                }
            } else {
                // Create new score
                Score newScore = Score.builder()
                        .score(value)
                        .rawValue(value)
                        .year(year)
                        .country(country)
                        .indicator(indicator)
                        .build();

                return scoreService.save(newScore);
            }

        } catch (Exception e) {
            log.error("Error in addOrUpdateScore for {} - {} - {}: {}",
                    countryName, indicatorName, year, e.getMessage());
            throw e;
        }
    }
}