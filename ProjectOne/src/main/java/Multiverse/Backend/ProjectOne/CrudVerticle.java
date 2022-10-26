package Multiverse.Backend.ProjectOne;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;

import java.security.AuthProvider;

public class CrudVerticle extends AbstractVerticle {

  private MongoClient mongoClient;

  private MongoAuth authProvider;

  @Override
  public void start(Promise<Void> startPromise) {

    mongoClient = MongoClient.createShared(
      vertx,
      new JsonObject()
        .put("db_name",config().getString("db_name", "backend_MV"))
        .put("connection_string", config().getString("connection_string", "mongodb://localhost:27017"))
    );

    authProvider = MongoAuth.create(mongoClient, new JsonObject());
    authProvider.setUsernameField("username");
    authProvider.setUsernameCredentialField("username");

    EventBus eventBus = vertx.eventBus();

    MessageConsumer<JsonObject> consumer = eventBus.consumer("crud-address");

    consumer.handler(message -> {

      String action = message.body().getString("action");

      switch (action) {
        case "register-user":
          registerUser(message);
          break;
        default:
          message.fail(1, "Unknown action: " + message.body());
      }

    });

    startPromise.complete();

  }

  private void registerUser(Message<JsonObject> message) {

    JsonObject userToRegister = message.body().getJsonObject("user");

    authProvider.insertUser(userToRegister.getString("username"), userToRegister.getString("password"), null, null, res -> {
      if (res.succeeded()) {

        String id = res.result();

        JsonObject query = new JsonObject()
          .put("_id", id);
        JsonObject update = new JsonObject()
          .put("$set", new JsonObject().put("username", userToRegister.getString("username")));

        mongoClient.updateCollection("user", query, update, reply -> {
          if (reply.succeeded()) {
            message.reply(Json.encode(userToRegister));
          } else {
            message.fail(2, "insert failed: " + reply.cause().getMessage());
          }
        });

      } else {
        message.fail(2, "insert failed: " + res.cause().getMessage());
      }
    });

  }


}
