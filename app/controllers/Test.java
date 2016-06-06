package controllers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;

import play.mvc.*;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Ahmad Arif on 03-Jun-16.
 */
public class Test extends Controller {

    public Result hello(String name, int age) {
        return ok(views.html.hello.render(name, age));
    }


    // ==================================================================
    // calling web service
    // ==================================================================

    @Inject
    WSClient ws;

    String url1 = "http://lumen.ahmadarif.com/api/v1/article";
    String url2 = "https://api.github.com/users/ahmadarif";

    public Result testWS() {
        // cara 1
        Logger.info("Sebelum WS 1");
        Promise<JsonNode> jsonPromise1 = ws.url(url1).get().map(toJson);
        Logger.info("Sesudah WS 1");

        // cara 2 dengan map
        try {
            Logger.info("Sebelum WS 2");
            Promise<JsonNode> jsonPromise2 = ws.url(url2).get().map(response -> {
                Logger.info("Di dalam WS 2");
                return response.asJson();
            }).recover(throwable -> {
                JsonNode node = Json.newObject()
                        .put("status", "gagal");
                return Json.toJson(node);
            });
            Logger.info("Sesudah WS 2");
            return ok(jsonPromise1.get(3, TimeUnit.MILLISECONDS));
        } catch (F.PromiseTimeoutException e) {
            JsonNode node = Json.newObject()
                    .put("success", false)
                    .put("message", "Request time out");
            return ok(Json.toJson(node));
        }

        // cara 3
//        Logger.info("Sebelum WS 3");
//        Promise<WSResponse> responsePromise = ws.url(url1)
//        Logger.info("Sesudah WS 3");

//        return ok(jsonPromise1.get(3, TimeUnit.MILLISECONDS));
    }

    private Function<WSResponse, JsonNode> toJson =
            new Function<WSResponse, JsonNode>() {
                public JsonNode apply(WSResponse response) {
                    Logger.info("Di dalam WS 1");
                    return response.asJson();
                }
            };

    public Promise<Result> testWS2() {
        Promise<Result> promise = ws.url(url1).setRequestTimeout(2000).get().map(response -> ok(response.asJson()));
        return promise;
    }

    // ====================================================================================
    // parallel
    public Promise<Timing> timed(final String url) {
        final long start = System.currentTimeMillis();
        return WS.url(url).get().map(time -> {
            return new Timing(url, System.currentTimeMillis() - start);
        });
    }

    public class Timing {
        public String url;
        public long latency;

        public Timing(String url, long latency) {
            this.url = url;
            this.latency = latency;
        }
    }

    public Result parallel() {
        final Promise<Timing> yahoo = timed("http://www.yahoo.com");
        final Promise<Timing> google = timed("http://www.google.com");
        final Promise<Timing> bing = timed("http://www.bing.com");

        Promise<List<Timing>> all = Promise.sequence(bing, yahoo, google);

        return ok(Json.toJson(all.get(10000)));
    }
    //> parallel
    //> ====================================================================================


    // ====================================================================================
    // with parameter
    public Promise<Result> params() {
        Promise<Result> promise = ws.url(url1)
                .setQueryParameter("title", "Title WS")
                .setQueryParameter("content", "Content WS")
                .post("content")
                .map(response -> ok(response.asJson()));

        return promise;
    }


    //> with parameter
    //> ====================================================================================

}