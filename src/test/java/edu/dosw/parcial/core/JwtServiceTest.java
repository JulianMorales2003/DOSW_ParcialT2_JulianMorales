package edu.dosw.parcial.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import edu.dosw.parcial.core.services.JwtService;

class JwtServiceTest {

    private static String secret(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static final String SECRET = secret("clave-de-prueba-con-mas-de-32-bytes-ok");

    @Test
    void tokenGeneradoEsValidoYConservaElSubject() {
        JwtService service = new JwtService(SECRET, 60_000);

        String token = service.generateToken("julian");

        assertTrue(service.isTokenValid(token));
        assertEquals("julian", service.extractSubject(token));
    }

    @Test
    void tokenExpiradoNoEsValido() {
        JwtService service = new JwtService(SECRET, -1_000);

        String token = service.generateToken("julian");

        assertFalse(service.isTokenValid(token));
    }

    @Test
    void tokenFirmadoConOtroSecretNoEsValido() {
        JwtService emisor = new JwtService(secret("otra-clave-de-prueba-con-mas-de-32-bytes"), 60_000);
        JwtService validador = new JwtService(SECRET, 60_000);

        String token = emisor.generateToken("julian");

        assertFalse(validador.isTokenValid(token));
    }

    @Test
    void textoQueNoEsUnTokenNoEsValido() {
        JwtService service = new JwtService(SECRET, 60_000);

        assertFalse(service.isTokenValid("esto-no-es-un-jwt"));
    }
}