package cn.cjgl.service;

import cn.cjgl.config.FitbitConfig;
import cn.cjgl.config.TokenInfo;
import cn.cjgl.constant.Constant;
import cn.cjgl.controller.ApiController;
import cn.cjgl.util.DateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FitbitDataService {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private RestTemplate restTemplate=new RestTemplate();

    @Resource
    private FitbitConfig fitbitConfig;

    @Resource
    private S3Service s3Service;

    /**
     * Refreshes the Fitbit OAuth token using the refresh token
     * Updates TokenInfo with new access token, refresh token, and other token-related information
     */
    private void refreshToken(){
        // Build token request
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "refresh_token");
        tokenRequest.add("refresh_token", TokenInfo.getRefresh_token());
        tokenRequest.add("client_id", fitbitConfig.getClientId());

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        List<MediaType> list=new ArrayList<>();
        list.add(MediaType.APPLICATION_JSON);
        headers.setAccept(list);
        // Create HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);

        // Send POST request to Fitbit's /oauth2/token endpoint
        //String tokenUrl = "https://api.fitbit.com/oauth2/token";
        ResponseEntity<Map> response = restTemplate.exchange(
                Constant.get_access_token,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Check response status and return access token (or other necessary information)
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("response.getBody:"+response.getBody());
        } else {
            // Handle error response
            throw new RuntimeException("Failed to obtain access token from Fitbit");
        }

        if(null!=response.getBody()&&response.getBody() instanceof Map){
            Map rets=response.getBody();
            TokenInfo.setAccess_token((String)rets.get("access_token"));
            TokenInfo.setExpires_in((Integer)rets.get("expires_in"));
            TokenInfo.setRefresh_token((String)rets.get("refresh_token"));
            TokenInfo.setToken_type((String)rets.get("token_type"));
            TokenInfo.setUser_id((String)rets.get("user_id"));
        }
    }

    /**
     * Scheduled task to fetch heart rate intraday data from Fitbit API
     * Runs every 5 minutes
     * Fetches data for the current day and stores it in AWS S3
     * @throws IOException If there's an error during API call or data storage
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void fetchHeartRateIntradayData() throws IOException {
        if(TokenInfo.getAccess_token()==null){
            return ;
        }
        //先刷新token
        this.refreshToken();

        String today=getTodayStr();

        // Build request URL
        StringBuilder urlBuilder = new StringBuilder(Constant.FITBIT_API_BASE_URL);
        urlBuilder.append(TokenInfo.getUser_id())
                  .append("/activities/heart/date/")
                  .append(today)
                  .append("/")
                  .append(today)
                  .append("/")
                  .append("1min");

        urlBuilder.append(".json");

        String url = urlBuilder.toString();

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + TokenInfo.getAccess_token());

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        // Send GET request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
        String jsonData=response.getBody();
        String todayNum=today.replace("-","");

        int minnum= DateTimeUtil.calculateMinutesFromMidnight(LocalDateTime.now());
        //Cancel local storage
        /*FitbitData fitbitData=new FitbitData();
        fitbitData.setUserid(TokenInfo.getUser_id());
        fitbitData.setDaynum(Long.parseLong(todayNum));
        fitbitData.setMinnum(minnum);
        fitbitData.setCreatetime(new Date());
        fitbitData.setHeartdata(jsonData);

        FitbitData old=isExist(fitbitData);
        if(null!=old){
            fitbitData.setId(old.getId());
            dao.updateFitbitData(fitbitData);
        }
        else{
            dao.insertFitbitData(fitbitData);
        }*/
        log.debug("====================heartData fetch success====================\n"+jsonData);
        if (isExistIntraday(jsonData)){
            String key=TokenInfo.getUser_id()+"_"+Constant.S3_DATA_TYPE_HEARTRATE+"_"+todayNum;
            s3Service.saveString2aws(Constant.S3_BUCKET_NAME,key,jsonData);
            log.debug("====================activities-heart-intraday exist，saved aws s3====================");
        }
        else{
            log.error("********************activities-heart-intraday not exist or empty,ignore save aws s3********************");
        }

//        storageService.storeString(Constant.FIRBASE_BUCKET_NAME,todayNum+"_"+Constant.HeartRateFileName,response.getBody());
    }

    /**
     * Checks if the intraday heart rate data exists and is valid in the JSON response
     * @param jsonString The JSON response string from Fitbit API
     * @return true if valid intraday data exists, false otherwise
     */
    private boolean isExistIntraday(String jsonString){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // Check if "activities-heart-intraday" field exists
            if (rootNode.has("activities-heart-intraday")) {
                JsonNode intradayNode = rootNode.get("activities-heart-intraday");

                // Check if "dataset" field exists and is not empty
                if (intradayNode.has("dataset") && !intradayNode.get("dataset").isEmpty()) {
                    return true;
                } else {
                    System.out.println("\"activities-heart-intraday\" dataset field is empty or does not exist");
                    return false;
                }
            } else {
                System.out.println("\"activities-heart-intraday\" field does not exist");
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Scheduled task to fetch ECG data from Fitbit API
     * Runs every 5 minutes
     * Fetches ECG logs for the current day
     * @throws IOException If there's an error during API call or data storage
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void fetchECGlogyData() throws IOException {
        if(TokenInfo.getAccess_token()==null){
            return ;
        }
        //First refresh token
        this.refreshToken();


        String today=getTodayStr();
        String url = Constant.FITBIT_API_BASE_URL+TokenInfo.getUser_id()+"/ecg/list.json?" +
                "afterDate=" + today + "&" +
                "sort=asc&" +
                "limit=" + 1 + "&" +
                "offset=" + 0;

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + TokenInfo.getAccess_token());

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        // Send GET request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.debug("=============================EcgData fetch success====================================\n"+response.getBody());
        //storageService.storeString(Constant.FIRBASE_BUCKET_NAME,todayNum+"_"+Constant.ECGLogFileName,response.getBody());
    }

    /**
     * Gets today's date in the format "yyyy-MM-dd"
     * @return String representation of today's date
     */
    String getTodayStr(){
        // Get current date
        LocalDate currentDate = LocalDate.now();

        // Define date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format current date to string
        String formattedDate = currentDate.format(formatter);
        return formattedDate;
    }
}