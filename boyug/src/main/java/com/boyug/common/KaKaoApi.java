package com.boyug.common;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class KaKaoApi {

    @Value("${kakao.api-key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.logout-uri}")
    private String kakaoLogoutUri;

    // map rest-key
    @Value("${kakao.rest-api-key}")
    private String kakaoMapApiKey;

    //인가 코드를 받아서 accessToken을 반환
    public String getAccessToken(String code){

        String reqUrl = "https://kauth.kakao.com/oauth/token";
        RestTemplate rt = new RestTemplate();

        // HttpHeader
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoApiKey);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        //http 바디(params)와 http 헤더(headers)를 가진 엔티티
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        //reqUrl로 Http 요청 , POST 방식
        ResponseEntity<String> response =
                rt.exchange(reqUrl, HttpMethod.POST, kakaoTokenRequest, String.class);

        String responseBody = response.getBody();

        // 파싱 후 추출
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        String accessToken = jsonObject.get("access_token").getAsString();
//        String connectedAt = jsonObject.get("connected_at").getAsString();
        return accessToken;
    }

    // accessToken을 받아서 UserInfo 반환
    public HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqUrl = "https://kapi.kakao.com/v2/user/me";
        try{
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            System.out.println(accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            System.out.println("유저 정보 요청 응답코드 = " + responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while((line = br.readLine()) != null){
                responseSb.append(line);
            }
            String result = responseSb.toString();

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
            String profileImage = properties.getAsJsonObject().get("profile_image").getAsString();
            String connectedAt = element.getAsJsonObject().get("connected_at").getAsString();
            String id = element.getAsJsonObject().get("id").getAsString();

            userInfo.put("nickname", nickname);
            userInfo.put("profileImage", profileImage);
            userInfo.put("connectedAt", connectedAt);
            userInfo.put("id", id);

            br.close();

        } catch (Exception e){
            e.printStackTrace();
        }
        return userInfo;
//        아래가 리펙토링 아래로 바꾸셈
//        String reqUrl = "https://kapi.kakao.com/v2/user/me";
//
//        RestTemplate rt = new RestTemplate();
//
//        //HttpHeader 오브젝트
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + accessToken);
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        //http 헤더(headers)를 가진 엔티티
//        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
//                new HttpEntity<>(headers);
//
//        //reqUrl로 Http 요청 , POST 방식
//        ResponseEntity<String> response =
//                rt.exchange(reqUrl, HttpMethod.POST, kakaoProfileRequest, String.class);
//
//        KakaoProfile kakaoProfile = new KakaoProfile(response.getBody());
//
//        return kakaoProfile;
    }

    //accessToken을 받아서 로그아웃 시키는 메서드
    public void kakaoLogout(String accessToken) {
        String reqUrl = "https://kapi.kakao.com/v1/user/logout";
        System.out.println(accessToken);
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("logout responseCode = " + responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while((line = br.readLine()) != null){
                responseSb.append(line);
            }
            String result = responseSb.toString();
            System.out.println("로그아웃 response 값 = " + result);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<String, Object> getKakaoSearch(String userAddr2) {

        // 주소 변환해야함 (~동) 빼는 작업
        String address = userAddr2.replaceAll("\\s*\\(.*?\\)", "");

        final String restAPIKey = "KakaoAK " + kakaoMapApiKey;

        //요청 URL과 검색어를 담음
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + address;

        //RestTemplate를 이용해
        RestTemplate restTemplate = new RestTemplate();

        //HTTPHeader를 설정해줘야 하기때문에 생성함
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", kakaoMapApiKey);
        headers.set("Authorization", restAPIKey);
        headers.set("Accept", "application/json");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //ResTemplate를 이용해 요청을 보내고 KakaoSearchDto로 받아 response에 담음

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        // 응답 본문을 문자열로 가져옴
        String jsonResponse = response.getBody();

        // JSON 문자열을 JsonObject로 파싱
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // documents 배열을 가져옴
        JsonArray documents = jsonObject.getAsJsonArray("documents");

        // 0번째 인덱스의 객체를 가져옴
        Map<String, Object> xy = new HashMap<>();

        if (documents != null && documents.size() > 0) {
            JsonObject firstDocument = documents.get(0).getAsJsonObject();

            // 경도(x), 위도(y) 추출
            xy.put("x", firstDocument.get("x").getAsString());
            xy.put("y", firstDocument.get("y").getAsString());
        } else {
            // x, y 없는 주소 만이빌딩으로 대체
            xy.put("x", "127.02675502058682");
            xy.put("y", "37.50077973287715");
        }
        return xy;

    }

    // 토큰 받아오는거 초기 코드
//        String accessToken = "";
//        String refreshToken = "";
//        String reqUrl = "https://kauth.kakao.com/oauth/token";
//
//        try{
//            URL url = new URL(reqUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            //필수 헤더 세팅
//            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//            conn.setDoOutput(true); //OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
//
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
//            StringBuilder sb = new StringBuilder();
//
//            //필수 쿼리 파라미터 세팅
//            sb.append("grant_type=authorization_code");
//            sb.append("&client_id=").append(kakaoApiKey);
//            sb.append("&redirect_uri=").append(kakaoRedirectUri);
//            sb.append("&code=").append(code);
//
//            bw.write(sb.toString());
//            bw.flush();
//
//            int responseCode = conn.getResponseCode();
//            System.out.println("토근 응답코드 = " + responseCode);
//
//            BufferedReader br;
//            if (responseCode >= 200 && responseCode < 300) {
//                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            } else {
//                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//            }
//
//            String line = "";
//            StringBuilder responseSb = new StringBuilder();
//            while((line = br.readLine()) != null){
//                responseSb.append(line);
//            }
//            String result = responseSb.toString();
////            log.info("responseBody = {}", result);
//            System.out.println("응답 토큰 = " + result);
//
//            // JSON 문자열을 파싱하여 필요한 토큰 정보 추출
//            JsonElement element = JsonParser.parseString(result);
//            JsonObject jsonObject = element.getAsJsonObject();
//
//            accessToken = jsonObject.get("access_token").getAsString();
//            refreshToken = jsonObject.get("refresh_token").getAsString();
//
//            br.close();
//            bw.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return accessToken;
}
