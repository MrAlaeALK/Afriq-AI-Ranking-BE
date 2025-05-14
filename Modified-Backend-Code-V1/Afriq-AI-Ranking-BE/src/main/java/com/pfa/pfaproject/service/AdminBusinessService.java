package com.pfa.pfaproject.service;

import com.pfa.pfaproject.config.JWT.JwtUtil;
import com.pfa.pfaproject.dto.Admin.LoginDTO;
import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.dto.Rank.GenerateFinalScoreForCountryDTO;
import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.*;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final ScoreService scoreService;
    private final RankService rankService;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new admin user.
     * @param adminToRegisterDTO DTO containing registration details with password confirmation
     * @return JWT token for the newly registered admin
     * @throws CustomException if admin with username or email already exists
     */
    public String register(RegisterDTO adminToRegisterDTO) {
        log.info("Attempting to register new admin: {}", adminToRegisterDTO.username());

        if (adminService.existsByUsernameOrEmail(adminToRegisterDTO.username(), adminToRegisterDTO.email())) {
            log.warn("Registration failed - username or email already exists: {}", adminToRegisterDTO.username());
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
        log.info("Successfully registered new admin: {}", admin.getUsername());

        return jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());
    }

    /**
     * Authenticates an admin user.
     * @param adminToLogin DTO containing login credentials
     * @return JWT token for the authenticated admin
     * @throws CustomException if credentials are invalid
     */
    public String login(LoginDTO adminToLogin) {
        log.info("Attempting login for user: {}", adminToLogin.usernameOrEmail());

        Admin admin = ValidationUtils.isValidEmail(adminToLogin.usernameOrEmail()) ?
                adminService.findByEmail(adminToLogin.usernameOrEmail()) :
                adminService.findByUsername(adminToLogin.usernameOrEmail());

        if (admin == null) {
            log.warn("Login failed - user not found: {}", adminToLogin.usernameOrEmail());
            throw new CustomException("Username or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(adminToLogin.password(), admin.getPassword())) {
            log.warn("Login failed - incorrect password for user: {}", adminToLogin.usernameOrEmail());
            throw new CustomException("Username or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        log.info("Login successful for user: {}", admin.getUsername());
        return jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());
    }

    /**
     * Adds a new country to the system.
     * @param country Country entity to add
     * @return The saved country with ID
     * @throws CustomException if country already exists
     */
    public Country addCountry(Country country) {
        ValidationUtils.validateNotEmpty(country.getName(), "Country name");
        log.info("Attempting to add new country: {}", country.getName());

        Country existingCountry = countryService.findByName(country.getName());
        if (existingCountry != null) {
            log.warn("Cannot add country - already exists: {}", country.getName());
            throw new CustomException("Country already exists", HttpStatus.CONFLICT);
        }

        Country savedCountry = countryService.save(country);
        log.info("Successfully added new country: {}", savedCountry.getName());
        return savedCountry;
    }

    /**
     * Adds a new indicator to the system.
     * @param indicator Indicator entity to add
     * @return The saved indicator with ID
     * @throws CustomException if indicator already exists
     */
    public Indicator addIndicator(Indicator indicator) {
        ValidationUtils.validateNotEmpty(indicator.getName(), "Indicator name");
        log.info("Attempting to add new indicator: {}", indicator.getName());

        Indicator existingIndicator = indicatorService.findByName(indicator.getName());
        if (existingIndicator != null) {
            log.warn("Cannot add indicator - already exists: {}", indicator.getName());
            throw new CustomException("Indicator already exists", HttpStatus.CONFLICT);
        }

        Indicator savedIndicator = indicatorService.save(indicator);
        log.info("Successfully added new indicator: {}", savedIndicator.getName());
        return savedIndicator;
    }

    /**
     * Adds a new score for a country on a specific indicator.
     * @param dto DTO containing score details
     * @return The saved score with ID
     * @throws CustomException if score already exists or references invalid entities
     */
    public Score addScore(AddOrUpdateScoreDTO dto) {
        log.info("Attempting to add score for country ID: {}, indicator ID: {}, year: {}",
                dto.countryId(), dto.indicatorId(), dto.year());

        ValidationUtils.validateYear(dto.year());
        
        Score existingScore = null;
        try {
            existingScore = scoreService.findByCountryIdAndIndicatorIdAndYear(
                    dto.countryId(), dto.indicatorId(), dto.year());
        } catch (CustomException e) {
            if (e.getStatus() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }

        if (existingScore != null) {
            log.warn("Cannot add score - already exists for country ID: {}, indicator ID: {}, year: {}",
                    dto.countryId(), dto.indicatorId(), dto.year());
            throw new CustomException("Score already exists", HttpStatus.CONFLICT);
        }

        // Verify country and indicator exist
        Country country = countryService.findById(dto.countryId());
        Indicator indicator = indicatorService.findById(dto.indicatorId());

        Score newScore = Score.builder()
                .score(dto.score())
                .year(dto.year())
                .country(country)
                .indicator(indicator)
                .build();

        Score savedScore = scoreService.save(newScore);
        log.info("Successfully added score for country: {}, indicator: {}, year: {}",
                country.getName(), indicator.getName(), dto.year());

        return savedScore;
    }

    /**
     * Updates an existing score.
     * @param dto DTO containing updated score details
     * @return The updated score
     * @throws CustomException if score doesn't exist
     */
    public Score updateScore(AddOrUpdateScoreDTO dto) {
        log.info("Attempting to update score for country ID: {}, indicator ID: {}, year: {}",
                dto.countryId(), dto.indicatorId(), dto.year());

        ValidationUtils.validateYear(dto.year());
        
        Score score = scoreService.findByCountryIdAndIndicatorIdAndYear(
                dto.countryId(), dto.indicatorId(), dto.year());

        if (score == null) {
            log.warn("Cannot update score - not found for country ID: {}, indicator ID: {}, year: {}",
                    dto.countryId(), dto.indicatorId(), dto.year());
            throw new CustomException("Score does not exist", HttpStatus.NOT_FOUND);
        }

        score.setScore(dto.score());
        Score updatedScore = scoreService.save(score);

        log.info("Successfully updated score for country ID: {}, indicator ID: {}, year: {}",
                dto.countryId(), dto.indicatorId(), dto.year());

        return updatedScore;
    }

    /**
     * Generates a final score for a specific country in a given year.
     * @param dto DTO containing country ID and year
     * @return The generated rank entry
     * @throws CustomException if rank already exists or country not found
     */
    public Rank generateFinalScoreForCountry(GenerateFinalScoreForCountryDTO dto) {
        log.info("Generating final score for country ID: {}, year: {}", dto.countryId(), dto.year());

        ValidationUtils.validateYear(dto.year());
        
        Rank existingRank = null;
        try {
            existingRank = rankService.findByCountryIdAndYear(dto.countryId(), dto.year());
        } catch (CustomException e) {
            if (e.getStatus() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }

        if (existingRank != null) {
            log.warn("Cannot generate final score - rank already exists for country ID: {}, year: {}",
                    dto.countryId(), dto.year());
            throw new CustomException("Rank already exists", HttpStatus.CONFLICT);
        }

        Country country = countryService.findById(dto.countryId());
        double finalScore = scoreService.calculateFinalScore(dto.countryId(), dto.year());

        Rank newRank = Rank.builder()
                .country(country)
                .year(dto.year())
                .finalScore(finalScore)
                .rank(0) // Initial rank, will be updated later
                .build();

        Rank savedRank = rankService.save(newRank);
        log.info("Successfully generated final score for country: {}, year: {}, score: {}",
                country.getName(), dto.year(), finalScore);

        return savedRank;
    }

    /**
     * Generates final scores for all countries in a specific year.
     * @param dto DTO containing the year
     * @return List of generated rank entries
     */
    public List<Rank> generateFinalScore(GenerateRankOrFinalScoreDTO dto) {
        ValidationUtils.validateYear(dto.year());
        
        log.info("Generating final scores for all countries for year: {}", dto.year());

        List<Country> countries = countryService.findAll();
        List<Rank> finalScores = new ArrayList<>();

        for (Country country : countries) {
            try {
                Rank finalScore = generateFinalScoreForCountry(
                        new GenerateFinalScoreForCountryDTO(country.getId(), dto.year()));
                finalScores.add(finalScore);
            } catch (CustomException e) {
                if (e.getStatus() == HttpStatus.CONFLICT) {
                    // If rank already exists, get the existing one
                    Rank existingRank = rankService.findByCountryIdAndYear(country.getId(), dto.year());
                    finalScores.add(existingRank);
                    log.info("Using existing rank for country: {}, year: {}", country.getName(), dto.year());
                } else {
                    // Log other errors but continue processing other countries
                    log.error("Error generating final score for country: {}, year: {}, error: {}",
                            country.getName(), dto.year(), e.getMessage());
                }
            }
        }

        log.info("Successfully generated final scores for {} countries for year: {}", finalScores.size(), dto.year());
        return finalScores;
    }

    /**
     * Generates rankings for all countries in a specific year.
     * @param dto DTO containing the year
     * @return List of countries ordered by rank
     */
    public List<Country> generateRanking(GenerateRankOrFinalScoreDTO dto) {
        ValidationUtils.validateYear(dto.year());
        
        log.info("Generating rankings for year: {}", dto.year());

        // Ensure all countries have final scores
        generateFinalScore(dto);

        // Let the RankService handle the position calculation
        rankService.updateRankPositions(dto.year());

        List<Rank> yearRanking = rankService.findByYearOrderByFinalScoreDesc(dto.year());

        List<Country> rankedCountries = new ArrayList<>();
        for (Rank rank : yearRanking) {
            rankedCountries.add(rank.getCountry());
        }

        log.info("Successfully generated rankings for {} countries for year: {}", rankedCountries.size(), dto.year());
        return rankedCountries;
    }
}