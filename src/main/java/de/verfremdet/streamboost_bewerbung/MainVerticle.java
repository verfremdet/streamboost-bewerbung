package de.verfremdet.streamboost_bewerbung;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
  private static final int httpPort = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowCredentials(true)
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Authorization")
      .allowedHeader("Access-Control-Allow-Method")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Content-Type"));


    router.route().handler(BodyHandler.create());
    router.get("/api/getAddresses").handler(this::getAddresses);
    router.post("/api/deleteAddress").handler(this::deleteAddress);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(httpPort)
      .onSuccess(ok -> {
        logger.info("Server is running on http://127.0.0.1:" + httpPort);
        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }

  private void getAddresses(RoutingContext context) {
    MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
      .put("url", "localhost")
      .put("db_name", "streamboost"));
    JsonObject document = new JsonObject();

    mongoClient.find("addresses", document, res -> {
      if (res.succeeded()) {
        List<JsonObject> list = res.result();
        context.response()
          .putHeader("Content-Type", "application/json")
          .setStatusCode(200)
          .end(list.stream().toList().toString());
      } else {
        res.cause().printStackTrace();
      }
    });

  }

  private void deleteAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String addressID = body.getString("addressID");
    MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
      .put("url", "localhost")
      .put("db_name", "streamboost"));

    Map<String,String> objectId = new HashMap<String,String>();
    objectId.put("$oid",addressID);

    JsonObject document = new JsonObject()
      .put("_id", objectId);
    mongoClient.removeDocument("addresses", document, res -> {
      if (res.succeeded()) {
     //   MongoClientDeleteResult result = res.result();
        MongoClientDeleteResult result = res.result();
        context.response()
          .end("ADDRESS DELETED");
      } else {
        context.response()
          .end("ERROR");
      }
    });
  }
}
