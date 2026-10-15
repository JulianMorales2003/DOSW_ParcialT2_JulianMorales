package edu.dosw.parcial;

import java.util.Map;

import edu.dosw.parcial.core.services.JwtService;

public class TokenTemporal {

    public static void main(String[] args) {
        JwtService jwtService = new JwtService(System.getenv("JWT_SECRET"), 3600000);
        System.out.println(jwtService.generateToken("admin@correo.com", Map.of("role", "ADMIN")));
    }
}