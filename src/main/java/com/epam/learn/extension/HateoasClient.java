package com.epam.learn.extension;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class HateoasClient {
    private Response currentResponse;
    private String currentUrl;
    private boolean isLoggingEnabled = false;
    private boolean ignoreFailureResponse = false;
    private final String baseUrl;

    public HateoasClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ---------------------------------
    // Links relation
    // ---------------------------------

    public HateoasClient rel(String rel)
    {
        this.currentUrl = getRelHref(rel);
        return this;
    }

    public HateoasClient rel(String field, String rel)
    {
        this.currentUrl = getRelHref(rel);
        return this;
    }

    public HateoasClient filterRel(String field, String condition, String rel)
    {
        this.currentUrl = getRelHref(field, condition, rel);
        return this;
    }

    public String getRelHref(String rel)
    {
        final String path = "_links." + rel + ".href";
        logPath(path);
        String url = currentResponse.then().extract().jsonPath().get(path);
        logUrl(url);
        return url;
    }

    public String getRelHref(String field, String condition, String rel)
    {
        final String path = field + ".find{" + field + " -> " + field + "." + condition + "}";
        logPath(path);
        final Map jsonObject = currentResponse.then().extract().path(path);
        return selectSelfHrefFromMap(jsonObject, rel);
    }

    // ---------------------------------
    // Links convenience methods
    // ---------------------------------

    public List<String> getSelfLinks(String field)
    {
        final List<String> hrefs = new ArrayList<>();
        logPath(field);
        final List<Map> array = currentResponse.then().extract().path(field);
        for (Map item : array)
        {
            final String url = selectSelfHrefFromMap(item);
            hrefs.add(url);
            logUrl(url);
        }
        return hrefs;
    }

    private String selectSelfHrefFromMap(Map map)
    {
        return selectSelfHrefFromMap(map, "self");
    }

    private String selectSelfHrefFromMap(Map map, String rel)
    {
        List links = (List) map.get("_links");
        for (Object link : links)
        {
            Map linkMap = (Map) link;
            if ((linkMap.get("rel")).equals(rel))
            {
                return (String) linkMap.get("href");
            }
        }
        log();
        throw new IllegalArgumentException("Could not find self link");
    }


    // ---------------------------------
    // Http requests
    // ---------------------------------

    public HateoasClient discovery()
    {
        url(baseUrl).get();
        return this;
    }

    public HateoasClient url(String url)
    {
        this.currentUrl = url;
        return this;
    }

    public HateoasClient post(String json)
    {
        currentResponse = given().contentType(ContentType.JSON).body(json).post(currentUrl);
        logResponse();
        assertOkResponse();
        return this;
    }

    public HateoasClient put(String json)
    {
        currentResponse = given().contentType(ContentType.JSON).body(json).put(currentUrl);
        logResponse();
        assertOkResponse();
        return this;
    }

    public HateoasClient delete()
    {
        currentResponse = RestAssured.delete(currentUrl);
        logResponse();
        assertOkResponse();
        return this;
    }

    public HateoasClient get()
    {
        currentResponse = RestAssured.get(currentUrl, Collections.emptyMap());
        logResponse();
        assertOkResponse();
        return this;
    }

    public HateoasClient patch(String json)
    {
        currentResponse = given().contentType(ContentType.JSON).body(json).patch(currentUrl);
        logResponse();
        assertOkResponse();
        return this;
    }

    // ------------------------
    // Logging
    // ------------------------

    public HateoasClient log()
    {
        isLoggingEnabled = true;
        return this;
    }

    private void logResponse()
    {
        if (isLoggingEnabled)
        {
            System.out.println(currentResponse.prettyPeek());
        }
    }

    private void logUrl(String url)
    {
        if (isLoggingEnabled)
        {
            System.out.println("self url = " + url);
        }
    }

    private void logPath(String path)
    {
        if (isLoggingEnabled)
        {
            System.out.println("Extracting from response using path: " + path);
        }
    }

    // ------------------------
    // Assertion
    // ------------------------
    public void statusCode(int statusCode)
    {
        getResponse().then().statusCode(statusCode);
    }

    private void assertOkResponse()
    {
        if (!ignoreFailureResponse)
        {
            final int status = currentResponse.statusCode();
            assertThat(status).isIn(200, 201);
        }
        ignoreFailureResponse = false;
    }

    public HateoasClient ignoreResponseCode()
    {
        ignoreFailureResponse = true;
        return this;
    }

    // ---------------------------------
    // RestAssured wrapper methods
    // ---------------------------------

    public Response getResponse()
    {
        return currentResponse;
    }

    public JsonPath getJsonPath()
    {
        return getResponse().body().jsonPath();
    }

}
