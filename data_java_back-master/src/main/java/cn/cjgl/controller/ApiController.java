package cn.cjgl.controller;

import cn.cjgl.config.FitbitConfig;
import cn.cjgl.config.TokenInfo;
import cn.cjgl.constant.Constant;
import cn.cjgl.service.FitbitDataService;
import cn.cjgl.service.S3Service;
import cn.cjgl.util.PKCEUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("api")
public class ApiController {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private static String accessToken=null;
    private static String codeVerifier=null;

    @Resource
    private FitbitConfig fitbitConfig;

    private RestTemplate restTemplate = new RestTemplate();

    @Resource
    private FitbitDataService fitbitDataService;

    @Value("${upload.dir}")
    private String uploadDir;

    @Resource
    private S3Service s3Service;

/*    @Value("${front.url}")
    private  String frontUrl;*/

   /* @Resource
    private FirebaseStorageService storageService;*/
    @GetMapping("/getHeartRateData")
    @ResponseBody
    public String getHeartRate(@RequestParam String queryDate) {
        try {
            String queryDateNum=queryDate.replace("-","");
            //ret= fitbitDataService.getHeartRateIntradayData(queryDate);//storageService.readString(Constant.FIRBASE_BUCKET_NAME,queryDateNum+Constant.HeartRateFileName);
            //Cancel local data search
//            FitbitData fitbitData=new FitbitData();
//            fitbitData.setDaynum(Long.parseLong(queryDateNum));
//            fitbitData.setUserid(TokenInfo.getUser_id());
//            FitbitData old=fitbitDataService.isExist(fitbitData);
//            if(null!=old){
//                ret=old.getHeartdata();
//            }
            //Extract data from Amazon
            String key=TokenInfo.getUser_id()+"_"+Constant.S3_DATA_TYPE_HEARTRATE+"_"+queryDateNum;
            return s3Service.getStringFromAws(Constant.S3_BUCKET_NAME,key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    @GetMapping("/getEcgLogData")
    @ResponseBody
    public String getEcgLog(@RequestParam String queryDate) {
        String ret="";
        try {
            String queryDateNum=queryDate.replace("-","");
            //ret= fitbitDataService.getECGlogyData(queryDate);//storageService.readString(Constant.FIRBASE_BUCKET_NAME,queryDateNum+Constant.HeartRateFileName);
         /*   FitbitData fitbitData=new FitbitData();
            fitbitData.setDaynum(Long.parseLong(queryDateNum));
            fitbitData.setUserid(TokenInfo.getUser_id());
            FitbitData old=fitbitDataService.isExist(fitbitData);
            if(null!=old){
                ret=old.getEcgdata();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @RequestMapping("/getLoginState")
    @ResponseBody
    public String getLoginState(){
        return TokenInfo.getAccess_token()==null?"0":"1";
    }

    @RequestMapping("/version")
    @ResponseBody
    public String version(){
        int version=Integer.parseInt("3.2d");
        return version+"";
    }

    @RequestMapping("/logout")
    @ResponseBody
    public void logout(){
        TokenInfo.setAccess_token(null);
        return;
    }

    @RequestMapping("/getCodeChallenge")
    @ResponseBody
    public String getCodeChallenge() throws NoSuchAlgorithmException {
        //generate codeVerifier
        codeVerifier=PKCEUtil.generateCodeVerifier();

        return PKCEUtil.generateCodeChallenge(codeVerifier);
    }

    @RequestMapping("/fitbitCallBack")
    public String fitbitCallBack(String code) throws Exception {
        log.error("=================================================================================");
        if (code == null || code.isEmpty()) {
            throw new Exception("Authorization code is missing.");
        }
        log.error("code:"+code+";access_token:"+code);

        // Assuming you already have code_challenge and used it in a previous authorisation request
        // Here we don't need to compute code_challenge again, because the authorisation request is already complete

        // Build the token request
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("code", code);
        tokenRequest.add("client_id", fitbitConfig.getClientId());
        tokenRequest.add("client_secret", fitbitConfig.getClientSecret());
        tokenRequest.add("redirect_uri", fitbitConfig.getRedirectUrl());
        tokenRequest.add("code_verifier", codeVerifier); // Add code_verifier to token request

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // create HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);

        // Send a POST request to Fitbit's /oauth2/token endpoint
        //String tokenUrl = "https://api.fitbit.com/oauth2/token"; // Fitbit token endpoint URL
        ResponseEntity<Map> response = restTemplate.exchange(
                Constant.get_access_token,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Check the response status and return the access token (or other necessary information)
        if (response.getStatusCode().is2xxSuccessful()) {
            log.debug("response.getBody:"+response.getBody());
        } else {
            // Handle error response
            throw new RuntimeException("Failed to obtain access token from Fitbit");
        }

        //accessToken = (String) tokenResponse.get("access_token");
        // Here, you can store the accessToken in a session, a database, or use it for subsequent API calls.
        if(null!=response.getBody()&&response.getBody() instanceof Map){
            Map rets=response.getBody();
            TokenInfo.setAccess_token((String)rets.get("access_token"));
            TokenInfo.setExpires_in((Integer)rets.get("expires_in"));
            TokenInfo.setRefresh_token((String)rets.get("refresh_token"));
            TokenInfo.setScope((String)rets.get("scope"));
            TokenInfo.setToken_type((String)rets.get("token_type"));
            TokenInfo.setUser_id((String)rets.get("user_id"));
        }
        return "index";
    }

    @PostMapping("/uploadRecord")
    public ResponseEntity<String> uploadFile(@RequestParam("qtitle") String qtitle,@RequestParam("audioFile") MultipartFile audioFile) {
        if (audioFile.isEmpty()) {
            return new ResponseEntity<>("Please select a file to upload", HttpStatus.BAD_REQUEST);
        }

        try {
     /*       // Generate a unique filename
            String fileName = UUID.randomUUID().toString() + ".wav";
            // Specify the file save path
            File fileDir = new File(uploadDir);
            if(!fileDir.exists()){
                fileDir.mkdir();
            }
            String filePath =fileDir.getAbsolutePath()+ uploadDir + File.separator + fileName;

            // Save the file to the specified path
            File dest = new File(filePath);
            audioFile.transferTo(dest);
            System.out.println("==========================filePath:"+filePath);*/

            byte[] bytes = audioFile.getBytes();
          /*  File fileDir = new File(uploadDir);
            if(!fileDir.exists()){
                fileDir.mkdir();
            }
            String fileName=uploadDir +File.separator+TokenInfo.getUser_id()+"_"+ System.currentTimeMillis()+audioFile.getOriginalFilename();
            Path path = Paths.get(fileName);
            Files.write(path, bytes);
            log.error("====================================save audio file====================================");
            log.error(path.toString());

            // Save the file to the specified path
            AudioData audioData=new AudioData();
            audioData.setCreatetime(new Date());
            audioData.setUserid(TokenInfo.getUser_id());
            audioData.setQtitle(qtitle);
            audioData.setAudiofile(fileName);

            audioDataService.add(audioData);*/
            String key=TokenInfo.getUser_id()+"_"+Constant.S3_DATA_TYPE_AUDIO+"_"+qtitle;
            s3Service.saveBytes2aws(Constant.S3_BUCKET_NAME,key,bytes);

            return new ResponseEntity<>("File upload successful", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("save audio file error:"+e.getMessage());
            return new ResponseEntity<>("File upload fail", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}
