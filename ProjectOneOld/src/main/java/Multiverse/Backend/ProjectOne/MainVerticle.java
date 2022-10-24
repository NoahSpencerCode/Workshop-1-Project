package Multiverse.Backend.ProjectOne;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.post("/Register").handler(ctx -> {

      HttpServerResponse response = ctx.response();
      response.putHeader("content-type", "text/plain");

      String body = ctx.body().asString();

      response.end("Registering");

      System.out.println(body);
    });

    router.post("/Login").handler(ctx -> {

      HttpServerResponse response = ctx.response();
      response.putHeader("content-type", "text/plain");

      String body = ctx.body().asString();

      response.end("Logging in");

      System.out.println(body);
    });

    router.route("/").handler(ctx -> {

      // This handler will be called for every request
      HttpServerResponse response = ctx.response();
      response.putHeader("content-type", "text/plain");

      // Write to the response and end it
      response.end("Backend project by Noah Spencer.");
    });

    server.requestHandler(router).listen(8080);

  }
}
