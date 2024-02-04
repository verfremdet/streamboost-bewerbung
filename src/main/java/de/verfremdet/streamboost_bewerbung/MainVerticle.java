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

  /**
   * The HTTP port used for the server.
   * It is retrieved from the system environment variable "HTTP_PORT".
   * If the variable is not set, the default port value is 8080.
   */
  private static final int httpPort = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));
  private RoutingContext context;

  /**
   * Starts the server and sets up the necessary routes and handlers.
   *
   * @param startPromise a Promise that will be completed when the server is successfully started
   */
  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);

    /**
     * The Router class is responsible for handling HTTP requests and routing them to the appropriate controller.
     * It uses a map to store the route patterns as keys and corresponding controller classes as values.
     *
     * Example usage:
     * Router router = new Router();
     * router.addRoute("/home", HomeController.class); // Maps the URL "/home" to the HomeController class
     * router.addRoute("/user/:id", UserController.class); // Maps the URL "/user/:id" to the UserController class
     *
     * The Router class supports both static and dynamic route patterns. Static route patterns are fixed URLs,
     * while dynamic route patterns include parameters that can vary based on the request.
     *
     * When a request comes in, the Router class analyzes the URL and attempts to match it with the registered route patterns.
     * If a match is found, it determines the corresponding controller class and forwards the request to it for further processing.
     *
     * In case of no matching route pattern, the Router class returns a 404 Not Found error.
     *
     * Note: This implementation is not thread-safe.
     *
     * @see HomeController
     * @see UserController
     */
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

  /**
   * Retrieves addresses from the MongoDB database and sends them as a JSON response.
   * Uses the Vert.x toolkit and the MongoClient library to connect to the database.
   *
   * @param context the routing context for the HTTP request and response
   */
  private void getAddresses(RoutingContext context) {
    MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
      .put("url", "localhost")
      .put("db_name", "streamboost"));
    JsonObject document = new JsonObject();

    /**
     *
     * GET ALL ADDRESSES
     *
     */
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

  /**
   * Inserts an address into the database.
   *
   * @param context the routing context
   */
  private void insertAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String firtName = body.getString("firstName");
    String lastName = body.getString("lastName");
    String birthday = body.getString("birthday");
    String telephone = body.getString("telephone");
    /**
     *
     *  Checking if any Value is null or empty
     *
     */
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

      Map<String, String> objectId = new HashMap<>();
      objectId.put("$oid", new ObjectId().toString());

      /**
       * Creates a new JsonObject document with the following fields:
       *
       * - "_id": the ObjectId of the document
       * - "firstName": the first name of the person
       * - "lastName": the last name of the person
       * - "birthday": the birthday of the person
       * - "telephone": the telephone number of the person
       *
       * @param objectId the ObjectId to be used for the "_id" field
       * @param firstName the first name of the person
       * @param lastName the last name of the person
       * @param birthday the birthday of the person
       * @param telephone the telephone number of the person
       * @return the created JsonObject document with the specified fields
       */
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

  /**
   * Edits an address in the database.
   *
   * @param context The routing context.
   */
  private void editAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String id = body.getString("id");
    String firtName = body.getString("firstName");
    String lastName = body.getString("lastName");
    String birthday = body.getString("birthday");
    String telephone = body.getString("telephone");

    /**
     *
     *  Checking if any Value is null or empty
     *
     */
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

      /**
       * Creates a new JsonObject document with the following fields:
       *
       * - "firstName": the first name of the person
       * - "lastName": the last name of the person
       * - "birthday": the birthday of the person
       * - "telephone": the telephone number of the person
       *
       * @param firstName the first name of the person
       * @param lastName the last name of the person
       * @param birthday the birthday of the person
       * @param telephone the telephone number of the person
       * @return the created JsonObject document with the specified fields
       */

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

  /**
   * Deletes an address from the database.
   *
   * @param context the routing context for handling the request
   */
  private void deleteAddress(RoutingContext context) {
    JsonObject body = context.getBodyAsJson().getJsonObject("address");
    String id = body.getString("id");
    MongoClient mongoClient = MongoClient.create(vertx, new JsonObject()
      .put("url", "localhost")
      .put("db_name", "streamboost"));

    /**
     * Creates a new JsonObject document.
     *
     * @param objectId the value to be assigned to the "_id" field of the document
     * @return a JsonObject document with the "_id" field set to the specified objectId
     */
    Map<String, String> objectId = new HashMap<>();
    objectId.put("$oid", id);

    JsonObject document = new JsonObject()
      .put("_id", objectId);

    /**
     *
     * Searches for Address Document and removes it.
     */
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
