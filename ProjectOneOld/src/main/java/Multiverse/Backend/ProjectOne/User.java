package Multiverse.Backend.ProjectOne;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;

public class User {

  MongoClient client;
  MongoAuth authProvider;
  public User(Vertx vertx) {
    client = MongoClient.createShared(vertx, new JsonObject());
    authProvider = MongoAuth.create(client, new JsonObject());
  }

  public String create(JsonObject postedBody) {


    return "Unknown Error";
  };
}
