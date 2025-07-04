package com.pfa.pfaproject.service;

import com.pfa.pfaproject.config.JWT.JwtUtil;
import com.pfa.pfaproject.dto.*;
import com.pfa.pfaproject.dto.Admin.LoginDTO;
import com.pfa.pfaproject.dto.Admin.LoginResponseDTO;
import com.pfa.pfaproject.dto.Admin.RefreshRequestDTO;
import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.dto.Score.AddScoreDTO;
import com.pfa.pfaproject.dto.Score.ScoreDTO;
import com.pfa.pfaproject.dto.Score.getScoresByYearDTO;
import com.pfa.pfaproject.dto.Weight.AddIndicatorWeightDTO;
import com.pfa.pfaproject.dto.Weight.AddWeightDTO;
import com.pfa.pfaproject.dto.indicator.GetYearIndicatorsDTO;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final DimensionWeightService categoryWeightService;
    private final IndicatorWeightService indicatorWeightService;
    private final JwtUtil jwtUtil;
    private final DimensionScoreService dimensionScoreService;
    private final DimensionWeightService dimensionWeightService;

    /**
     * Registers a new admin user.
     * @param adminToRegisterDTO DTO containing registration details with password confirmation
     * @return JWT token for the newly registered admin
     * @throws CustomException if admin with username or email already exists
     */
    public LoginResponseDTO register(RegisterDTO adminToRegisterDTO) {

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

        String refreshToken = jwtUtil.generateRefreshToken(admin.getUsername(), admin.getAuthorities());
        String accessToken =  jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    /**
     * Authenticates an admin user.
     * @param adminToLogin DTO containing login credentials
     * @return JWT token for the authenticated admin
     * @throws CustomException if credentials are invalid
     */
    public LoginResponseDTO login(LoginDTO adminToLogin) {

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
        String refreshToken = jwtUtil.generateRefreshToken(admin.getUsername(), admin.getAuthorities());
        String accessToken = jwtUtil.generateToken(admin.getUsername(), admin.getAuthorities());

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public String refreshAccessToken(RefreshRequestDTO refreshRequestDTO) {
        if(!jwtUtil.validateRefreshToken(refreshRequestDTO.refreshToken())){
            throw new CustomException("Refresh token is not valid", HttpStatus.UNAUTHORIZED);
        }
        String username = jwtUtil.extractUsername(refreshRequestDTO.refreshToken());
        Admin admin = adminService.findByUsername(username);
        if (admin == null) {
            throw new CustomException("admin not found", HttpStatus.UNAUTHORIZED);
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


    public DimensionWeight addDimensionWeight(AddWeightDTO dto) {
        Dimension dimension = dimensionService.findById(dto.id());
        DimensionWeight dimensionWeight = DimensionWeight.builder()
                .year(dto.year())
                .dimensionWeight(dto.weight())
                .dimension(dimension)
                .build();
        return categoryWeightService.save(dimensionWeight);
    }

    public IndicatorWeight addIndicatorWeight(AddIndicatorWeightDTO dto) {
        IndicatorWeight indicatorWeight = IndicatorWeight.builder()
                .indicator(indicatorService.findById(dto.indicatorId()))
                .weight(dto.weight())
                .dimensionWeight(categoryWeightService.findByCategoryAndYear(dto.categoryId(), dto.year()))
                .build();
        return indicatorWeightService.save(indicatorWeight);
    }


    public void calculateDimensionScoresForCountry(Integer  year, Country country){
        // find dimensions for that year simply
        List<DimensionWeight> dimensions = dimensionWeightService.findByYear(year);
        if(dimensions.isEmpty()){
            throw new CustomException(String.format("Aucune dimension pour l'année %s", year), HttpStatus.NOT_FOUND);
        }
        for(DimensionWeight dimension : dimensions){
            if(dimensionScoreService.findByCountryIdAndDimensionIdAndYear(country.getId(), dimension.getId(), year) != null){
                throw new CustomException(String.format("Le score existe déjà pour la dimension %s",dimension.getDimension().getName()), HttpStatus.CONFLICT);
            }

            double dimensionScore = 0;
            double weightSum = 0;

            //get all indicators for that year with their weights which are related to the current dimension
            List<IndicatorWeight> indicators = dimension.getIndicatorWeights();
            if(indicators.isEmpty()){
                throw new CustomException(String.format("Aucun indicateur pour l'année %s", year), HttpStatus.NOT_FOUND);
            }

            //score calculation
            for(IndicatorWeight indicator : indicators){
                Score score = scoreService.findByCountryIdAndIndicatorIdAndYear(country.getId(), indicator.getIndicator().getId(), year);
                if(score == null){
                    //since normally i already filtered countries with no scores at all, countries remaining are the ones which at least have a score so if they do not for some indicators then make it 0
                    Score newScore = Score.builder()
                            .year(year)
                            .country(country)
                            .indicator(indicator.getIndicator())
                            .score(0.0)
                            .build();
                    score = scoreService.save(newScore);
                }
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

    public void calculateDimensionScores(Integer  year){
        List<Country> countries = countryService.findAll();

        for(Country country : countries){
            calculateDimensionScoresForCountry(year, country);
        }
    }

    public Rank calculateFinalScoreForCountry(Integer  year, Country country){
        calculateDimensionScoresForCountry(year, country);

        List<DimensionWeight> dimensions = dimensionWeightService.findByYear(year);

        double finalScore = 0.0;
        double weightSum = 0.0;

        for(DimensionWeight dimension : dimensions){
            DimensionScore dimensionScore = dimensionScoreService.findByCountryIdAndDimensionIdAndYear(country.getId(), dimension.getDimension().getId(), year);
            //out of all the checks this is the least useful one (just keeping it in case)
            if (dimensionScore == null || dimensionScore.getScore() == null) {
                throw new CustomException(String.format("Le score de la dimension %s est manquant pour le pays %s",dimension.getDimension().getName(),country.getName()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            finalScore += dimensionScore.getScore() * dimension.getDimensionWeight();
            weightSum += dimension.getDimensionWeight();
        }

        if(rankService.existsByCountryIdAndYear(country.getId(), year)){
            throw new CustomException(String.format("Le classement existe déjà pour le pays %s", country.getName()), HttpStatus.CONFLICT);
        }

        Rank newRank = Rank.builder()
                .country(country)
                .year(year)
                .finalScore(finalScore/ weightSum)
                .rank(1)
                .build();
        return rankService.save(newRank);
    }

    public  List<Rank> generateFinalScores(YearRequestDTO dto){
        List<Country> countries = countryService.findAll();
        List<Rank> finalScores = new ArrayList<>();
        for(Country country : countries){
            List<Score> yearScores = country.getScores().stream()
                    .filter(score -> Objects.equals(score.getYear(), dto.year()))
                    .toList();
            // if the country does not have any scores for that year then we do not include it in ranking
            if(!yearScores.isEmpty()){
                finalScores.add(calculateFinalScoreForCountry(dto.year(), country));
            }
        }
        return finalScores;
    }

    public List<Country> generateRanking(YearRequestDTO dto){
        generateFinalScores(dto);

        List<Rank> ranksOrdered = rankService.findByYearOrderByFinalScoreDesc(dto.year());

        List<Country> countriesRanked = new ArrayList<>();

        int position = 1;
        double lastScore = -1;
        int lastPosition = 0;

        for (Rank rank : ranksOrdered) {
            // If score is the same as the previous country, assign the same rank (tie)
            if (position > 1 && Math.abs(rank.getFinalScore() - lastScore)<0.0000001) {
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

    public List<ScoreDTO> getAllScores(){
        List<Score> scores = scoreService.findAll();
        List<ScoreDTO> allScores = new ArrayList<>();
        for(Score score : scores){
            allScores.add(
                    new ScoreDTO(
                            score.getId(),
                            score.getYear(),
                            score.getCountry().getName(),
                            score.getIndicator().getName(),
                            score.getScore())
            );
        }
        return allScores;
    }

    public String deleteScore(Long id){
        Score score = scoreService.findById(id);
        scoreService.delete(id);//exception already handled in score service
        if(!rankService.findAllByYear(score.getYear()).isEmpty()){
            deleteRankingByYear(score.getYear());
            generateRanking(new YearRequestDTO(score.getYear()));
        }
        return "Le score a été supprimé";
    }

    public ScoreDTO updateScore(ScoreDTO dto){
        Score score = scoreService.findById(dto.id()); //exception already handled in score service
        score.setScore(dto.score()); //why score is getting converted from empty string to 0 (even before this)
        Score savedScore = scoreService.save(score);
        if(!rankService.findAllByYear(score.getYear()).isEmpty()){
            deleteRankingByYear(score.getYear());
            generateRanking(new YearRequestDTO(score.getYear()));
        }
        return new ScoreDTO(savedScore.getId(), savedScore.getYear(), savedScore.getCountry().getName(), savedScore.getIndicator().getName(), savedScore.getScore());
    }

    public List<Integer> getAllIndicatorYears(){
        return indicatorWeightService.findAll().stream()
                .map(i -> i.getDimensionWeight().getYear())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public List<GetYearIndicatorsDTO> getYearIndicators(Integer  year){
        return indicatorWeightService.getYearIndicators(year);
    }

    public List<Country> getAllCountries(){
        return countryService.findAll();
    }

    public ScoreDTO addScore(AddScoreDTO dto){
        if(scoreService.findByCountryNameAndIndicatorNameAndYear(dto.countryName(), dto.indicatorName(), dto.year()) != null){
            throw new CustomException("Le score existe déjà", HttpStatus.CONFLICT);
        }
            Score score = Score.builder()
                    .year(dto.year())
                    .score(dto.score())
                    .indicator(indicatorService.findByName(dto.indicatorName()))
                    .country(countryService.findByName(dto.countryName()))
                    .build();
            Score savedScore = scoreService.save(score);
            return new ScoreDTO(savedScore.getId(), savedScore.getYear(), savedScore.getCountry().getName(), savedScore.getIndicator().getName(), savedScore.getScore());
    }

    public void validateScores(ValidatedScoresDTO validatedScoresDTO){
        Integer  year = validatedScoresDTO.year();
        List<String> countryNames = new ArrayList<>();
        List<ScoresToValidateDTO> validatedScores = validatedScoresDTO.validatedScores();
        for(ScoresToValidateDTO validatedScore : validatedScores){
            Country country = countryService.findByCode(validatedScore.countryCode());
            Indicator indicator = indicatorService.findById(validatedScore.indicatorId());
            if(scoreService.findByCountryNameAndIndicatorNameAndYear(country.getName(), indicator.getName(), year) != null){
                countryNames.add(country.getName());
            }
        }
        if(!countryNames.isEmpty()){
            throw new CustomException("Les pays suivants ont déjà des scores pour l'année et l'indicateur spécifié. Veuillez donc supprimer les anciens scores avant de valider l'ensemble des scores" + countryNames, HttpStatus.CONFLICT);
        }
        else{
            for(ScoresToValidateDTO validatedScore : validatedScores) {
                Country country = countryService.findByCode(validatedScore.countryCode());
                Indicator indicator = indicatorService.findById(validatedScore.indicatorId());
                Score score = Score.builder()
                        .year(year)
                        .country(country)
                        .indicator(indicator)
                        .score(validatedScore.score())
                        .build();
                scoreService.save(score);
            }
        }
    }

    public List<getFinalScoreAndRankDTO> getYearRanking(Integer year){
        return rankService.findAllByYearOrderByRank(year);
    }

    public List<Integer> getYearsWithRanking(){
        return rankService.getDistinctYearsFromRanks();
    }

    public List<AllRanksDTO> getAllRanks(){
        List<Rank> ranks = rankService.findAll();
        List<AllRanksDTO> allRanks = new ArrayList<>();
        for(Rank rank : ranks){
            allRanks.add(new AllRanksDTO(rank.getYear(), rank.getRank(), rank.getCountry().getName()));
        }
        return allRanks;
    }

    public List<getScoresByYearDTO> getDimensionScoresByYear(Integer year){
        return dimensionScoreService.findByYear(year);
    }

    @Transactional //transactional helps us keep consistency: all succeed together or all fail together
    public String deleteRankingByYear(Integer year){
        List<Rank> ranks = rankService.findAllByYear(year);
        if(ranks.isEmpty()){
            throw new CustomException(String.format("le classement n'existe pas pour l'année %s", year), HttpStatus.CONFLICT);
        }
        for(Rank rank : ranks){
            rankService.delete(rank.getId());
        }
        List<DimensionScore> dimensionScores = dimensionScoreService.findScoresByYear(year);
        if(dimensionScores.isEmpty()){
            throw new CustomException(String.format("Aucun score de dimension trouvé pour l'année %s", year) , HttpStatus.CONFLICT);
        }
        for (DimensionScore dimensionScore : dimensionScores) {
            dimensionScoreService.delete(dimensionScore.getId());
        }
        return "Le classement a été supprimé";
    }

}