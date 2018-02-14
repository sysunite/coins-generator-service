package com.weaverplatform.service;

import io.restassured.RestAssured;
import org.junit.BeforeClass;

/**
 * @author Mohamad Alamili
 */
public class BaseTest {
  @BeforeClass
  public static void setup(){
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 4567;
  }
}
