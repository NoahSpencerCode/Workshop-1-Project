package Multiverse.Backend.ProjectOne;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {

    CompositeFuture.all(
      deployVerticle(RestVerticle.class.getName()),
      deployVerticle(CrudVerticle.class.getName())
    ).onComplete(f -> {
      if (f.succeeded()) {
        startPromise.complete();
        System.out.println("Completed all");
      } else {
        startPromise.fail(f.cause());
      }
    });

  }

  Future<Void> deployVerticle(String verticleName) {

    Promise<Void> resultPromise = Promise.promise();
    Future<Void> result = resultPromise.future();
    vertx.deployVerticle(verticleName, event -> {
      if (event.succeeded()) {
        result.isComplete();
        System.out.println("Vert deployed");
      } else {
        result.failed();
      }
    });

    return result;
  }

}
