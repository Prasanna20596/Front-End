package com.jwt.react.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.jwt.react.entity.TokenRequest;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String SECRET_KEY;

	@Value("${jwt.tokenvalidity}")
	private int TOKEN_VALIDITY;

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	private static String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public String generateToken(UserDetails userDetails) {

		Map<String, Object> claims = new HashMap<>();

		return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
	}

	public String getNewAccessToken(TokenRequest request) {
		String jwtToken = request.getJwtToken();
		Map<String, Object> claims = new HashMap<>();

		String[] parts = jwtToken.split("\\.");
		JSONObject payload = new JSONObject(decode(parts[1]));
		String subject = payload.getString("sub");
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();

	}

}
