package com.vikas.keycloak;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(allowedHeaders = "*")
public class AuthController {

	@Value("${keycloak.auth-server-url}")
	private String keycloakUrl;
	
	@Value("${keycloak.realm}")
	private String realm;
	
	@Value("${keycloak.client-id}")
	private String clientId;
	
	@Value("${keycloak.client-secret}")
	private String clientSecret;
	
	@Value("${keycloak.redirect-uri}")
	private String redirectUri;
	
	@GetMapping("/login")
	public void login(HttpServletResponse response) throws IOException {
		String authurl = keycloakUrl + "/protocol/openid-connect/auth?client_id="+clientId+"&response_type=code"+"&scope=openid&redirect_uri="+redirectUri;
		response.sendRedirect(authurl);
	}
	
	@GetMapping("/callback")
	public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form  = new LinkedMultiValueMap<>();
		form.add("code", code);
		form.add("grant_type", "authorization_code");
		form.add("redirect_uri", redirectUri);
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
		
		ResponseEntity<Map> tokenResponse = new RestTemplate().postForEntity(keycloakUrl+"/protocol/openid-connect/token", request, Map.class);
		Map body = tokenResponse.getBody();
		String accessToken = (String) body.get("access_token");
		String refreshToken = (String) body.get("refresh_token");
		String idToken = (String) body.get("id_token");
		
		
		ResponseCookie access = ResponseCookie.from("access_token", accessToken)
				.httpOnly(true).sameSite("None").path("/").secure(true).maxAge(1800).build();
		
		ResponseCookie refresh = ResponseCookie.from("refresh_token", refreshToken)
				.httpOnly(true).sameSite("None").path("/").secure(true).maxAge(1800).build();
		
		ResponseCookie idTok = ResponseCookie.from("id_token", idToken)
				.httpOnly(true).sameSite("None").path("/").secure(true).maxAge(1800).build();
		response.addHeader(HttpHeaders.SET_COOKIE, access.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, idTok.toString());
		
		response.sendRedirect("http://localhost:3000");
	}
	
	@GetMapping("/me")
	public ResponseEntity<?> me(@CookieValue("access_token") String token) throws JsonMappingException, JsonProcessingException, ParseException {
		JWT jwt = JWTParser.parse(token);
		JWTClaimsSet jwtClaimSet = jwt.getJWTClaimsSet();
		jwtClaimSet.getClaim("preferred_username");
//		Jwt decoded = JwtHelper.decode(token);
//		String claims = decoded.getClaims();
//		Map<String, Object> userInfo = new ObjectMapper().readValue(claims, new TypeReference<>() {});
//		return ResponseEntity.ok(Map.of("name", userInfo.get("preferred_username")));
		return ResponseEntity.ok(Map.of("name", jwtClaimSet.getClaim("preferred_username")));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
	    String idToken = null;
	    for (Cookie cookie : request.getCookies()) {
	        if ("id_token".equalsIgnoreCase(cookie.getName())) {
	            idToken = cookie.getValue();
	            break;
	        }
	    }

	    String logoutUrl = null;
	    if (idToken != null) {
	        logoutUrl = "http://localhost:8080/realms/reactsso/protocol/openid-connect/logout?id_token_hint=" 
	            + idToken + "&post_logout_redirect_uri=http://localhost:3000";
	    }

	    Stream.of("access_token", "refresh_token", "id_token").forEach(name -> {
	        Cookie cookie = new Cookie(name, "");
	        cookie.setPath("/");
	        cookie.setMaxAge(0);
	        cookie.setHttpOnly(true);
	        cookie.setSecure(true); // Only for local development
	        response.addCookie(cookie);
	    });

	    Map<String, String> responseBody = new HashMap<>();
	    responseBody.put("logoutUrl", logoutUrl);
	    return ResponseEntity.ok(responseBody);
	}
}
