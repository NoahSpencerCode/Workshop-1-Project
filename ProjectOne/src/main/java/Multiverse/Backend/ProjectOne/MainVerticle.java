package Multiverse.Backend.ProjectOne;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  public void start(Future<Void> startFuture) {

    CompositeFuture.all(
      deployVerticle(RestVerticle.class.getName()),
      deployVerticle(CrudVerticle.class.getName())
    ).onComplete(f -> {
      if (f.succeeded()) {
        startFuture.succeeded();
      } else {
        startFuture.failed();
      }
    });

  }

  Future<Void> deployVerticle(String verticleName) {

    Promise<Void> resultPromise = Promise.promise();
    Future<Void> result = resultPromise.future();
    vertx.deployVerticle(verticleName, event -> {
      if (event.succeeded()) {
        result.succeeded();
      } else {
        result.failed();
      }
    });

    return result;
  }

}
