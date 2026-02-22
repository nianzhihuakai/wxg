package com.nzhk.wxg.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Map;

public class JwtUtil {

    private static final String KEY = "nzhk_wxg";

    public static void main(String[] args) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", "ef2aabac10934bc0b5f9bd5515cc0c79");
//        String token = JwtUtil.generateToken(claims);
//        System.out.println(token);
//
//        Map<String, Object> claims1 = new HashMap<>();
//        claims1.put("userId", "c216b75f29e0467fbc0494a3b0d68823");
//        String token1 = JwtUtil.generateToken(claims1);
//        System.out.println(token1);

        System.out.println(parseToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbXMiOnsidXNlcklkIjoiZWYyYWFiYWMxMDkzNGJjMGI1ZjliZDU1MTVjYzBjNzkifX0.6CLTN_9LrX8CF1GUKboWwsHy0OH-x3FqLW3h47jV2R0"));
        System.out.println(parseToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbXMiOnsidXNlcklkIjoiYzIxNmI3NWYyOWUwNDY3ZmJjMDQ5NGEzYjBkNjg4MjMifX0.DKV-CsBoZwaVsfyG7K_PQSnVOP5kunpw6mMcjL8PC5s"));
    }

    //接收业务数据,生成token并返回
    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
//                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .sign(Algorithm.HMAC256(KEY));
    }

    //接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
