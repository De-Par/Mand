const axios = require("axios");
const { JwtGenerator } = require("virgil-sdk");
const { initCrypto, VirgilCrypto, VirgilAccessTokenSigner } = require("virgil-crypto");

const PARSE_APP_ID = "TmupEet4p9zS5GGKMhXPJkqL9sLzFt0g5Qy7CGfd";
const PARSE_REST_API_KEY = "emXvXGq9HutJfRSNCfmS2bYpgEaA2E4JuBIhXB4w";
const APP_ID = "833f1c4ef639451cabdaa49ca8165c58";
const APP_KEY_ID = "d5b757da841dabaca5db7b5cffc6d6d1";
const APP_KEY = "MC4CAQAwBQYDK2VwBCIEIDNorMD4nSXmPKLvb+A/2ZG1uGgn7kHFO9RhFZeNYLVJ";

initCrypto().then(() => {
  const crypto = new VirgilCrypto();
  const generator = new JwtGenerator({
      appId: APP_ID,
      apiKeyId: APP_KEY_ID,
      apiKey: crypto.importPrivateKey(APP_KEY),
      accessTokenSigner: new VirgilAccessTokenSigner(crypto)
  });

  Parse.Cloud.define("virgil-jwt", (request) => {
    const { sessionToken } = request.params;
    return axios
      .get("https://parseapi.back4app.com/users/me", {
        headers: {
          "X-Parse-Application-Id": PARSE_APP_ID,
          "X-Parse-REST-API-Key": PARSE_REST_API_KEY,
          "X-Parse-Session-Token": sessionToken
        }
      })
      .then(resp => {
        const identity = resp.data.objectId;
        const virgilJwtToken = generator.generateToken(identity);
        const tokenStr = virgilJwtToken.toString();

        return { token: tokenStr };
      })
      .catch(error => {
        throw new Error(error.message);
      });
  });
});