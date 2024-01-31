package de.verfremdet.streamboost_bewerbung;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
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
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.OPTIONS)
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
    router.post("/api/insertAddress").handler(this::insertAddress);
    router.post("/api/editAddress").handler(this::editAddress);

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

  private void insertAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String firtName = body.getString("firstName");
    String lastName = body.getString("lastName");
    String birthday = body.getString("birthday");
    String telephone = body.getString("telephone");

    if (firtName == null || firtName == "") {
      context.response()
        .end("VORNAME IST LEER!");
    } else if (lastName == null || lastName == "") {
      context.response()
        .end("NACHNAME IST LEER!");
    } else if (birthday == null || birthday == "") {
      context.response()
        .end("GEBURSTAG IST LEER!");
    } else if (telephone == null || telephone == "") {
      context.response()
        .end("TELEFONNUMMER IST LEER!");
    } else {
      MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
        .put("url", "localhost")
        .put("db_name", "streamboost"));

      Map<String,String> objectId = new HashMap<>();
      objectId.put("$oid", new ObjectId().toString());

      JsonObject document = new JsonObject()
        .put("_id", objectId)
        .put("firstName", firtName)
        .put("lastName", lastName)
        .put("birthday", birthday)
        .put("telephone", telephone);

      mongoClient.insert("addresses", document, res -> {
        if (res.succeeded()) {
          context.response()
            .end("ADDRESS INSERTED");
        } else {
          context.response()
            .end("ERROR");
        }
      });
    }
  }
  private void editAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String id = body.getString("id");
    String firtName = body.getString("firstName");
    String lastName = body.getString("lastName");
    String birthday = body.getString("birthday");
    String telephone = body.getString("telephone");

    if (id == null || id == "") {
      context.response()
        .end("ERROR");
    } else if (firtName == null || firtName == "") {
      context.response()
        .end("VORNAME IST LEER!");
    } else if (lastName == null || lastName == "") {
      context.response()
        .end("NACHNAME IST LEER!");
    } else if (birthday == null || birthday == "") {
      context.response()
        .end("GEBURSTAG IST LEER!");
    } else if (telephone == null || telephone == "") {
      context.response()
        .end("TELEFONNUMMER IST LEER!");
    } else {
      MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
        .put("url", "localhost")
        .put("db_name", "streamboost"));

      Map<String, String> objectId = new HashMap<>();
      objectId.put("$oid", id);

      JsonObject query = new JsonObject()
        .put("_id", objectId);

      JsonObject document = new JsonObject()
        .put("firstName", firtName)
        .put("lastName", lastName)
        .put("birthday", birthday)
        .put("telephone", telephone);

      mongoClient.replaceDocuments("addresses", query, document, res -> {
        if (res.succeeded()) {
          context.response()
            .end("ADDRESS SAVED");
        } else {
          context.response()
            .end("ERROR");
        }
      });
    }
  }

  private void deleteAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String id = body.getString("id");
    MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
      .put("url", "localhost")
      .put("db_name", "streamboost"));

    Map<String,String> objectId = new HashMap<>();
    objectId.put("$oid", id);

    JsonObject document = new JsonObject()
      .put("_id", objectId);
    mongoClient.removeDocument("addresses", document, res -> {
      if (res.succeeded()) {
        context.response()
          .end("ADDRESS DELETED");
      } else {
        context.response()
          .end("ERROR");
      }
    });
  }
}
