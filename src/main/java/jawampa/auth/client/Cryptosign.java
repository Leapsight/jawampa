package jawampa.auth.client;

import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Sign;
import com.goterl.lazysodium.utils.Key;

import jawampa.WampMessages.AuthenticateMessage;
import jawampa.WampMessages.ChallengeMessage;

public class Cryptosign implements ClientSideAuthentication {

	private static final LazySodiumJava LAZY_SODIUM = new LazySodiumJava(new SodiumJava("libsodium.so"),
			StandardCharsets.UTF_8);
	private final Sign.Native cryptoSignNative;

	public static final String AUTH_METHOD = "cryptosign";

	private final ObjectNode authextra;
	private final Key secretKey;

	// it could fail if for example the privkey format or lenght is invalid
	public Cryptosign(String privkey, String pubkey) {
		// cast to the proper native interface
		this.cryptoSignNative = (Sign.Native) LAZY_SODIUM;

		// build the secret key
		this.secretKey = Key.fromHexString(privkey);

		// build the authextra for HELLO message
		final ObjectNode authextra = new ObjectMapper().createObjectNode();
		authextra.put("pubkey", pubkey);
		this.authextra = authextra;
	}

	@Override
	public String getAuthMethod() {
		return AUTH_METHOD;
	}

	@Override
	public ObjectNode getAuthExtra() {
		return authextra;
	};

	/*
	 * Only for reference:
	 * // Autobahn|JS
	 * // const privkey = autobahn.util.htob(privkeyHex);
	 * // var challenge = autobahn.util.htob(extra.challenge);
	 * // var signature = nacl.sign.detached(challenge, privkey);
	 * // return autobahn.util.btoh(signature);
	 * 
	 * // Autobahn|Java
	 * // String hexChallenge = (String) challenge.extra.get("challenge");
	 * // byte[] rawChallenge = AuthUtil.toBinary(hexChallenge);
	 * // SigningKey key = new SigningKey(privateKeyRaw);
	 * // byte[] signed = key.sign(rawChallenge);
	 * // String signatureHex = AuthUtil.toHexString(signed);
	 * // return signatureHex + hexChallenge;
	 */
	@Override
	public AuthenticateMessage handleChallenge(ChallengeMessage message,
			ObjectMapper objectMapper) {
		// get the message to sign
		final String challengeHex = (String) message.extra.get("challenge").asText();
		final byte[] challengeRaw = LazySodiumJava.toBin(challengeHex);

		// try to sign message with the secret key
		byte[] signatureBytes = new byte[Sign.BYTES];

		// to avoid any logger we ignore if the signing was successfully
		cryptoSignNative.cryptoSignDetached(signatureBytes, challengeRaw, challengeRaw.length,
				secretKey.getAsBytes());

		return new AuthenticateMessage(LAZY_SODIUM.sodiumBin2Hex(signatureBytes), objectMapper.createObjectNode());
	}
}
