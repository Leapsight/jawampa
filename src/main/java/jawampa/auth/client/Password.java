package jawampa.auth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jawampa.WampMessages.AuthenticateMessage;
import jawampa.WampMessages.ChallengeMessage;

public class Password implements ClientSideAuthentication {
	public static final String AUTH_METHOD = "password";

	private final String password;

	public Password(String password) {
		this.password = password;
	}

	@Override
	public String getAuthMethod() {
		return AUTH_METHOD;
	}

	@Override
	public AuthenticateMessage handleChallenge(ChallengeMessage message,
			ObjectMapper objectMapper) {
		return new AuthenticateMessage(password, objectMapper.createObjectNode());
	}
}
