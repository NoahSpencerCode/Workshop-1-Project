package com.Multiverse.BackendProject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MainVerticle extends AbstractVerticle {

  static <name> void saveUser(MongoClient mongoClient, String name) {
    JsonObject document = new JsonObject()
      .put("name", name);
    mongoClient.save("UserData", document, res -> {
      if (res.succeeded()) {
        String id = res.result();
        System.out.println("Saved user with id " + id);
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  static void insertUser(MongoClient mongoClient) {
    JsonObject document = new JsonObject()
      .put("title", "The Hobbit");
    mongoClient.insert("books", document, res -> {
      if (res.succeeded()) {
        String id = res.result();
        System.out.println("Inserted book with id " + id);
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  static void updateUser(MongoClient mongoClient) {
    JsonObject query = new JsonObject()
      .put("title", "The Hobbit");
    JsonObject update = new JsonObject().put("$set", new JsonObject()
      .put("author", "J. R. R. Tolkien"));
    mongoClient.updateCollection("books", query, update, res -> {
      if (res.succeeded()) {
        System.out.println("Book updated !");
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  static void findUser(MongoClient mongoClient,String name) {
    JsonObject query = new JsonObject()
      .put("name", name);
    mongoClient.find("UserData", query, res -> {
      if (res.succeeded()) {
        for (JsonObject json : res.result()) {
          System.out.println(json.encodePrettily());
        }
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  static void removeUser(MongoClient mongoClient) {
    JsonObject query = new JsonObject()
      .put("author", "J. R. R. Tolkien");
    mongoClient.removeDocuments("books", query, res -> {
      if (res.succeeded()) {
        System.out.println("Never much liked Tolkien stuff!");
      } else {
        res.cause().printStackTrace();
      }
    });
  }

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

    JsonObject config = new JsonObject();
    MongoClient client = MongoClient.createShared(vertx, config, "UserData");

    saveUser(client, "Noah");

    findUser(client,"Noah");
  }
}


