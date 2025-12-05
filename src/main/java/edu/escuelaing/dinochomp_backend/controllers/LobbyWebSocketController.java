package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Slf4j
public class LobbyWebSocketController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ObjectMapper mapper;

    private static final String TOPIC = "/topic/lobbies/";

    @MessageMapping("/lobbies/{gameId}/join")
    public void joinLobby(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) throws Exception {

        log.info("JOIN cifrado recibido en lobby {}", gameId);

        // 1. Descifrar
        String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));

        // 2. Convertir JSON → Player
        Player player = mapper.readValue(json, Player.class);
        log.info("Jugador descifrado: {}", player.getName());

        if (player.getId() == null) {
            log.warn("ID de jugador inválido");
            return;
        }

        // 3. Procesar
        lobbyService.addPlayer(gameId, player);

        // 4. Cifrar respuesta
        String jsonResponse = mapper.writeValueAsString(lobbyService.getPlayers(gameId));
        AesCrypto.Encrypted encryptedResponse = AesCrypto.encrypt(jsonResponse);

        // 5. Enviar al frontend
        template.convertAndSend(TOPIC + gameId + "/players", encryptedResponse);
    }


    @MessageMapping("/lobbies/{gameId}/leave")
    public void leaveLobby(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) throws Exception {

        log.info("LEAVE cifrado recibido en lobby {}", gameId);

        // 1. Descifrar
        String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));

        // 2. Convertir JSON → Player
        Player player = mapper.readValue(json, Player.class);

        // 3. Procesar
        lobbyService.removePlayer(gameId, player.getId());

        // 4. Cifrar y enviar
        String jsonResponse = mapper.writeValueAsString(lobbyService.getPlayers(gameId));
        AesCrypto.Encrypted encryptedResponse = AesCrypto.encrypt(jsonResponse);

        template.convertAndSend(TOPIC + gameId + "/players", encryptedResponse);
    }

    @MessageMapping("/lobbies/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) throws Exception {

        log.info("START GAME solicitado en {}", gameId);

        String message = "Game starting...";

        // Cifrar antes de enviar
        AesCrypto.Encrypted encrypted = AesCrypto.encrypt(message);

        template.convertAndSend(TOPIC + gameId + "/start", encrypted);
    }
}
