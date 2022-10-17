package Multiverse.Backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });

    WebClient client = WebClient.create(vertx);

    // Send a GET request
    client
      .get(8888, "localhost", "/test")
      .send()
      .onSuccess(response -> System.out
        .println("Received response with status code " + response.statusCode()))
      .onFailure(err ->
        System.out.println("Something went wrong " + err.getMessage()));
  }
}


