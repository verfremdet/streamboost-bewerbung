package de.verfremdet.streamboost_bewerbung;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class MainClass {

  private static final Logger logger = LoggerFactory.getLogger(MainClass.class);

  /**
   * This is the entry point of the application.
   * It initializes a Vertx instance and deploys the MainVerticle.
   */
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    logger.info("Starting...");
    vertx.deployVerticle(new MainVerticle());
  }
}
