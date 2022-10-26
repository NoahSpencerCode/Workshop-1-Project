package Multiverse.Backend.ProjectOne;

import io.vertx.core.json.JsonObject;

public class User {

  String username;

  String password;

  String token;

  public JsonObject toSendableJson() {

    JsonObject user = new JsonObject()
      .put("username", this.username)
      .put("password", this.password)
      .put("token", this.token);

    JsonObject send = new JsonObject()
      .put("user", user);

    return send;

  }

  public void setUser(String username, String password, String token) {

    this.username = username;
    this.password = password;
    this.token = token;

  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

}
