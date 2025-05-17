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
    private final IndicatorCategoryService indicatorCategoryService;
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
     * @param indicatorCategory Indicator entity to add
     * @return The saved indicatorCategory
     * @throws CustomException if category already exists
     */
    public IndicatorCategory addIndicatorCategory(IndicatorCategory indicatorCategory) {
        IndicatorCategory existingIndicatorCategory = indicatorCategoryService.findByName(indicatorCategory.getName());
        if (existingIndicatorCategory != null) {
            throw new CustomException("Category already exists", HttpStatus.CONFLICT);
        }

        IndicatorCategory savedIndicatorCategory = indicatorCategoryService.save(indicatorCategory);
        return savedIndicatorCategory;
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
     * Generates a final score for a specific country in a given year.
     * @param dto DTO containing country ID and year
     * @return The generated rank entry
     * @throws CustomException if rank already exists or country not found
     */
    public Rank generateFinalScoreForCountry(GenerateFinalScoreForCountryDTO dto) {
        
        Rank existingRank = rankService.findByCountryIdAndYear(dto.countryId(), dto.year());

        if (existingRank != null) {
            throw new CustomException("Rank already exists", HttpStatus.CONFLICT);
        }

        Country country = countryService.findById(dto.countryId());
        double finalScore = scoreService.calculateFinalScore(dto.countryId(), dto.year());

        Rank newRank = Rank.builder()
                .country(country)
                .year(dto.year())
                .finalScore(finalScore)
                .rank(1) // Initial rank, will be updated later
                .build();

        Rank savedRank = rankService.save(newRank);

        return savedRank;
    }

    /**
     * Generates final scores for all countries in a specific year.
     * @param dto DTO containing the year
     * @return List of generated rank entries
     */
    public List<Rank> generateFinalScore(GenerateRankOrFinalScoreDTO dto) {

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
                } else {
                    // Log other errors but continue processing other countries
                    log.error("Error generating final score for country: {}, year: {}, error: {}",
                            country.getName(), dto.year(), e.getMessage());
                }
            }
        }

        return finalScores;
    }

    /**
     * Generates rankings for all countries in a specific year.
     * @param dto DTO containing the year
     * @return List of countries ordered by rank
     */
    public List<Country> generateRanking(GenerateRankOrFinalScoreDTO dto) {

        // Ensure all countries have final scores
        generateFinalScore(dto);

        // Let the RankService handle the position calculation
        rankService.updateRankPositions(dto.year());

        List<Rank> yearRanking = rankService.findByYearOrderByFinalScoreDesc(dto.year());

        List<Country> rankedCountries = new ArrayList<>();
        for (Rank rank : yearRanking) {
            rankedCountries.add(rank.getCountry());
        }

        return rankedCountries;
    }
}