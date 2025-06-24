package com.pfa.pfaproject.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.pfaproject.dto.DetectedColumnsDTO;
import com.pfa.pfaproject.dto.FetchScoresDTO;
import com.pfa.pfaproject.dto.ScoresToValidateDTO;
import com.pfa.pfaproject.dto.WantedColumnsDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.validation.ValidationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FastApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final DocumentService documentService;
    private final IndicatorService indicatorService;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    public FastApiService(DocumentService documentService, IndicatorService indicatorService) {
        this.documentService = documentService;
        this.indicatorService = indicatorService;
    }

    // Send file to FastAPI /detect-columns
    public DetectedColumnsDTO sendFileToFastApi(MultipartFile multipartFile) {
        try{
            //Validating the file
            ValidationUtils.validateFile(multipartFile);

            String url = fastApiBaseUrl + "/detect-columns";

            File file = convertMultiPartToFile(multipartFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            FileSystemResource fileResource = new FileSystemResource(file);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DetectedColumnsDTO> response = restTemplate.postForEntity(url, requestEntity, DetectedColumnsDTO.class);

            return response.getBody();
        }
        catch(IOException e){
            throw new CustomException("erreur dans la lecture du fichier", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(RestClientException e){
            throw new CustomException("erreur de communication avec le service fastapi", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<ScoresToValidateDTO> sendJsonToFastApi(MultipartFile multipartFile, WantedColumnsDTO dto) {
        try{

            ValidationUtils.validateFile(multipartFile);
        documentService.uploadDocument(dto.year(), multipartFile);
            List<FetchScoresDTO.IndicatorColumn> indicatorColumns = new ArrayList<>();
            String url = fastApiBaseUrl + "/process-confirmed";
            if(!dto.isNormalized()){
                for (WantedColumnsDTO.IndicatorColumn ic : dto.indicatorColumns()) {
                    Long indicatorIdLong;
                    try {
                        indicatorIdLong = Long.parseLong(ic.indicatorId());
                    } catch (NumberFormatException e) {
                        throw new CustomException("Invalid indicatorId: " + ic.indicatorId(), HttpStatus.BAD_REQUEST);
                    }
                    Indicator indicator = indicatorService.findById(indicatorIdLong);
                    String normalization = indicator.getNormalizationType();
                    indicatorColumns.add(new FetchScoresDTO.IndicatorColumn(ic.columnName(), ic.indicatorId(), normalization));
                }
            }
            else{
                for (WantedColumnsDTO.IndicatorColumn ic : dto.indicatorColumns()) {
                    indicatorColumns.add(new FetchScoresDTO.IndicatorColumn(ic.columnName(), ic.indicatorId(), null));
                }
            }
            FetchScoresDTO fetchScoresDTO = new FetchScoresDTO(dto.countryColumn(), indicatorColumns);
            System.out.println("fetchScoresDTO = " + fetchScoresDTO);
            System.out.println("file name" + multipartFile.getOriginalFilename());

            // Multipart headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Convert MultipartFile to ByteArrayResource
            ByteArrayResource fileResource = new ByteArrayResource(multipartFile.getBytes()) {
                @Override
                public String getFilename() {
                    return multipartFile.getOriginalFilename();
                }
            };

            body.add("file", fileResource);

            // JSON part
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(fetchScoresDTO);

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> jsonPart = new HttpEntity<>(json, jsonHeaders);

            body.add("columns", jsonPart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            System.out.println("Request Body: " + body);
            System.out.println("Headers: " + headers);
            ParameterizedTypeReference<List<ScoresToValidateDTO>> responseType =
                    new ParameterizedTypeReference<List<ScoresToValidateDTO>>() {};

            ResponseEntity<List<ScoresToValidateDTO>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);

            return response.getBody();

        } catch (IOException e) {
            throw new CustomException("Failed to read uploaded file", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RestClientException e) {
            throw new CustomException("Failed to call FastAPI backend", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}
