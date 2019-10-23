package com.epam.learn;

import com.epam.learn.extension.HateoasClient;
import com.epam.learn.util.TestUtil;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;


public class ApiTest {

    private static final String baseUrl = TestUtil.getProperty("baseUrl");

    private HateoasClient client = new HateoasClient(baseUrl);

    @Test
    public void shouldGetRoot() {
        //rest assured vanilla
        given()
                .when()
                .get(baseUrl)
                .then()
                .statusCode(200)
                .log().body();
    }

    @Test
    public void rootResponseShouldContainProfileLink() {
        String relHref = client
                .discovery()
                .log().getRelHref("profile");
        assertEquals(baseUrl + "/profile", relHref);
    }
}
