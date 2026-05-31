package com.example.todoapp.controler;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.Task;
import com.example.todoapp.DAO.TaskDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.todoapp.DTO.TaskCreateDTO;
import com.example.todoapp.DTO.TaskUpdateDTO;
import com.example.todoapp.DTO.ErrorDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import java.util.List;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskDao dao = new TaskDao();

    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", Application::handleTasks);
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            //region Manage POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                com.example.todoapp.DTO.TaskCreateDTO input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), com.example.todoapp.DTO.TaskCreateDTO.class);

                if (input.getTitle() == null || input.getTitle().length() > 50) {
                    com.example.todoapp.DTO.ErrorDTO err = new com.example.todoapp.DTO.ErrorDTO("title", "La taille maximale du titre est de 50 caractères");
                    sendResponse(exchange, 400, JsonUtils.serialize(err));
                    return;
                }

                if (input.getDescription() != null && input.getDescription().length() > 255) {
                    com.example.todoapp.DTO.ErrorDTO err = new com.example.todoapp.DTO.ErrorDTO("description", "La taille maximale de la description est de 255 caractères");
                    sendResponse(exchange, 400, JsonUtils.serialize(err));
                    return;
                }


                Task taskToSave = new Task(0, input.getTitle(), input.getDescription(), false);
                Task createdTask = dao.save(taskToSave);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }
            //endregion

            //region Manage GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                boolean todoOnly = false;

                if (query != null && query.contains("todo-only=true")) {
                    todoOnly = true;
                }

                List<Task> tasks = dao.findAll(todoOnly);

                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                }
                return;
            }
            //endregion

            //region Manage GET /tasks/{id}
            Matcher m = ID_PATH.matcher(path);
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<Task> task = dao.findById(id);

                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            //endregion

            //region Manage PUT /tasks/{id}
            if ("PUT".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), UTF_8);


                com.example.todoapp.DTO.TaskUpdateDTO input = JsonUtils.deserialize(requestBody, com.example.todoapp.DTO.TaskUpdateDTO.class);


                if (input.getTitle() == null || input.getTitle().length() > 50) {
                    com.example.todoapp.DTO.ErrorDTO err = new com.example.todoapp.DTO.ErrorDTO("title", "La taille maximale du titre est de 50 caractères");
                    sendResponse(exchange, 400, JsonUtils.serialize(err));
                    return;
                }

                if (input.getDescription() != null && input.getDescription().length() > 255) {
                    com.example.todoapp.DTO.ErrorDTO err = new com.example.todoapp.DTO.ErrorDTO("description", "La taille maximale de la description est de 255 caractères");
                    sendResponse(exchange, 400, JsonUtils.serialize(err));
                    return;
                }

                Task taskToUpdate = new Task(id, input.getTitle(), input.getDescription(), input.getDone());

                boolean isUpdated = dao.update(id, taskToUpdate);
                if (isUpdated) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            //endregion

            //region Manage DELETE /tasks/{id}
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));

                boolean isDeleted = dao.deleteById(id);
                if (isDeleted) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            //endregion

            // Otherwise → 404
            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            log.error("Erreur interne du serveur", e);
            sendResponse(exchange, 500, null);
        }
    }
    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}
