package jawampa.auth.client;

import jawampa.WampMessages.AuthenticateMessage;
import jawampa.WampMessages.ChallengeMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ClientSideAuthentication {
    String getAuthMethod();
    AuthenticateMessage handleChallenge( ChallengeMessage message, ObjectMapper objectMapper );
    default ObjectNode getAuthExtra() {
        return null;
    };
}
