package com.weaverplatform.service;

import org.junit.Test;

import static io.restassured.RestAssured.given;

public class ApplicationTest extends BaseTest {
  @Test
  public void rootTest() {
    given()
      .expect()
      .statusCode(200)
      .when()
      .get("http://localhost:4567/");

  }

  @Test
  public void swaggerTest() {
    given()
      .expect()
      .statusCode(200)
      .when()
      .get("http://localhost:4567/swagger");

  }
}
