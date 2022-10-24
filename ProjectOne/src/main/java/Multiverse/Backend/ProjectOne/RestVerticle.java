package Multiverse.Backend.ProjectOne;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class RestVerticle extends AbstractVerticle {

  JWTAuth jwtAuth;

  public void start(Future<Void> startFuture) {

    jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setPath("keystore.jceks")
        .setPassword("secret")
      )
    );

    Router baseRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    baseRouter.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain").end("Multiverse backend project 2022");
    });

    apiRouter.route("/users*").handler(BodyHandler.create());
    apiRouter.post("/users").handler(this::registerUser);
    apiRouter.get("/user").handler(JWTAuthHandler.create(jwtAuth)).handler(this::getCurrentUser);

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx.createHttpServer().requestHandler(baseRouter).listen(8080, result -> {
      if (result.succeeded()) {
        startFuture.succeeded();
      } else {
        startFuture.failed();
      }
    });

  }

  private void getCurrentUser(RoutingContext routingContext) {
  }

  private void registerUser(RoutingContext routingContext) {

    JsonObject message = new JsonObject()
      .put("action", "register-user")
      .put("user", routingContext.body().asJsonObject().getJsonObject("user"));

  }

}
