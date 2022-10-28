package com.backendtrack.final_;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    Verticle database = new Database();

    Verticle creditBureau = new CreditBureau();

    vertx.deployVerticle(creditBureau);

    vertx.deployVerticle(database);

    EventBus bussin = vertx.eventBus();

    Router router = Router.router(vertx);

    router.route("/*").handler(BodyHandler.create());

    router.post("/register").handler(routingContext -> {

      JsonObject bus_Msg = new JsonObject()
        .put("action", "register")
        .put("user", routingContext.body().asJsonObject());

      bussin.request("database",bus_Msg, messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {
          routingContext.end("Thanks for Registering!");
        } else {
          routingContext.end("Registration failed! " + messageAsyncResult.cause());
        }

      });

    });

    router.post("/login").handler(routingContext -> {

      JsonObject bus_Msg = new JsonObject()
        .put("action", "login")
        .put("user", routingContext.body().asJsonObject());

      bussin.request("database",bus_Msg, messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {

          String token = jwtAuth.generateToken(
            new JsonObject()
              .put("username", routingContext.body().asJsonObject().getString("username")),
            new JWTOptions().setIgnoreExpiration(true));

          routingContext.end("Welcome Back! | token = " + token);
        } else {
          routingContext.end("Login failed! " + messageAsyncResult.cause());
        }

      });

    });

    //router.route("/actions/*").handler(JWTAuthHandler.create(jwtAuth));

    router.post("/actions/charge").handler(routingContext -> {

      JsonObject bus_Msg = new JsonObject()
        .put("action", "charge")
        .put("user", routingContext.body().asJsonObject());

      bussin.request("database",bus_Msg, messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {

          String[] body = messageAsyncResult.result().body().toString().split(" ");

          routingContext.end("Card has been charged. New Balance: " + body[0] + " available credit " + body[1]);
        } else {
          routingContext.end("Card Declined! " + messageAsyncResult.cause());
        }

      });

    });

    router.post("/actions/apply").handler(routingContext -> {

      JsonObject bus_Msg = new JsonObject()
        .put("action", "apply")
        .put("user", routingContext.body().asJsonObject());

      bussin.request("database",bus_Msg, messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {

          String[] card = messageAsyncResult.result().body().toString().split(" ");

          routingContext.end("You have been approved for a credit limit of " + card[1] + " your card number is " + card[0]);
        } else {
          routingContext.end("Sorry we couldn't approve you today! " + messageAsyncResult.cause());
        }

      });

    });

    router.get("/actions/account").handler(routingContext -> {

      routingContext.end("Account");

    });


    Route route_Users = router.route("/admin/users");

    route_Users.method(HttpMethod.POST).handler(routingContext -> {

      routingContext.end("Added User");

    });

    route_Users.method(HttpMethod.GET).handler(routingContext -> {

      routingContext.end("User:__");

    });

    route_Users.method(HttpMethod.PUT).handler(routingContext -> {

      routingContext.end("User Changed!");

    });

    Route route_Cards = router.route("/admin/cards");

    route_Cards.method(HttpMethod.POST).handler(routingContext -> {

      routingContext.end("Card created!");

    });

    route_Cards.method(HttpMethod.GET).handler(routingContext -> {

      routingContext.end("Card:___");

    });

    route_Cards.method(HttpMethod.PUT).handler(routingContext -> {

      routingContext.end("Card changed!");

    });

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
