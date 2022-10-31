package com.backendtrack.final_;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class MainVerticle extends AbstractVerticle {

  EventBus bussin;

  JWTAuth jwtAuth;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    Verticle database = new Database();
    Verticle creditBureau = new CreditBureau();

    vertx.deployVerticle(creditBureau);
    vertx.deployVerticle(database);

    bussin = vertx.eventBus();

    Router router = Router.router(vertx);

    router.route("/*").handler(BodyHandler.create());

    router.post("/register").handler(this::handleRegister);
    router.post("/login").handler(this::handleLogin);

    router.route("/actions/*").handler(JWTAuthHandler.create(jwtAuth));

    router.post("/actions/charge").handler(this::handleCharge);
    router.post("/actions/apply").handler(this::handleApply);
    router.get("/actions/account").handler(this::handleAccount);

    Route route_Users = router.route("/admin/users");

    route_Users.method(HttpMethod.POST).handler(this::handleAdminUserPost);
    route_Users.method(HttpMethod.GET).handler(this::handleAdminUserGet);
    route_Users.method(HttpMethod.PUT).handler(this::handleAdminUserPut);

    Route route_Cards = router.route("/admin/cards");

    route_Cards.method(HttpMethod.POST).handler(this::handleAdminCardPost);
    route_Cards.method(HttpMethod.GET).handler(this::handleAdminCardGet);
    route_Cards.method(HttpMethod.PUT).handler(this::handleAdminCardPut);

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void handleRegister(RoutingContext routingContext) {
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
  }

  private void handleLogin(RoutingContext routingContext) {
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
  }

  private void handleCharge(RoutingContext routingContext) {

    User jwtUser = routingContext.user();

    JsonObject prince = jwtUser.principal();

    String username = prince.getString("username");

    JsonObject bus_Msg = new JsonObject()
      .put("action", "charge")
      .put("user", routingContext.body().asJsonObject()
        .put("username", username)
        .put("password", routingContext.body().asJsonObject().getString("card-number")));

    bussin.request("database",bus_Msg, messageAsyncResult -> {
      if (messageAsyncResult.succeeded()) {

        String[] body = messageAsyncResult.result().body().toString().split(" ");

        routingContext.end("Card has been charged. New Balance: " + body[0] + " available credit " + body[1]);
      } else {
        routingContext.end("Card Declined! " + messageAsyncResult.cause());
      }

    });
  }

  private void handleApply(RoutingContext routingContext) {

    User jwtUser = routingContext.user();

    JsonObject prince = jwtUser.principal();

    String username = prince.getString("username");

    JsonObject bus_Msg = new JsonObject()
      .put("action", "apply")
      .put("user", routingContext.body().asJsonObject()
        .put("username", username));

    bussin.request("database",bus_Msg, messageAsyncResult -> {
      if (messageAsyncResult.succeeded()) {

        String[] card = messageAsyncResult.result().body().toString().split(" ");

        routingContext.end("You have been approved for a credit limit of " + card[1] + " your card number is " + card[0]);
      } else {
        routingContext.end("Sorry we couldn't approve you today! " + messageAsyncResult.cause());
      }

    });
  }
  private void handleAccount(RoutingContext routingContext) {

    routingContext.end("Account");

  }

  private void handleAdminUserPost(RoutingContext routingContext) {

    routingContext.end("Added User");

  }

  private void handleAdminUserGet(RoutingContext routingContext) {
    routingContext.end("User:__");
  }

  private void handleAdminUserPut(RoutingContext routingContext) {
    routingContext.end("User Changed!");
  }

  private void handleAdminCardPost(RoutingContext routingContext) {
    routingContext.end("Card created!");
  }

  private void handleAdminCardGet(RoutingContext routingContext) {
    routingContext.end("Card:___");
  }

  private void handleAdminCardPut(RoutingContext routingContext) {
    routingContext.end("Card changed!");
  }

}
