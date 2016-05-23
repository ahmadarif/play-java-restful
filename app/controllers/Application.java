package controllers;

import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Task;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.List;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result taskGetAll() {
        List<Task> tasks = Task.find.all();
        return ok(Json.toJson(tasks));
    }

    public Result taskGetById(Long id) {
        Task task = Task.find.byId(id);

        if (task != null)
            return ok(Json.toJson(task));
        else {
            ObjectNode result = Json.newObject();
            result.put("success", false);
            result.put("message", "Task not found");

            return ok(result);
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public Result taskInsert() {
        // ambil variabel
        JsonNode json = request().body().asJson();

        // perbaharui task
        Task task = Json.fromJson(json, Task.class);
        task.save();

        // kirim response
        return created(Json.toJson(task));
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public Result taskUpdate() {
        // ambil variabel
        JsonNode json = request().body().asJson();

        // perbaharui task
        Task task = Json.fromJson(json, Task.class);
        task.update();

        // kirim response
        return ok(Json.toJson(task));
    }

    @Transactional
    public Result taskDelete(Long id) {
        // response
        ObjectNode result = Json.newObject();

        // ambil task berdasarkan id
        Task task = Task.find.byId(id);

        // cek apakah task ada pada database
        if (task == null) {
            result.put("success", false);
            result.put("message", "Task not found");
            return ok(result);
        }

        // hapus task
        task.delete();

        // periksa apakah task berhasil dihapus
        if (Task.find.byId(id) == null) {
            result.put("success", true);
            result.put("message", "Task successfully removed");
        } else {
            result.put("success", false);
            result.put("message", "Task is not successfully removed");
        }

        // kirim response
        return ok(result);
    }

}
