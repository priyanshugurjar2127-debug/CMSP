import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WebServer {
    private static final Gson gson = new Gson();
    private static ContactManager manager;
    private static FileHandler fileHandler;

    public static void main(String[] args) {
        int portNumber = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "10000")
        );

        String dataDir =
            System.getenv().getOrDefault("DATA_DIR", ".");

        String dataFile =
            Paths.get(dataDir, "contacts.txt").toString();

        ipAddress("0.0.0.0");
        port(portNumber);
        staticFiles.location("/public");

        fileHandler = new FileHandler(dataFile);
        manager = new ContactManager(fileHandler.load());

        get("/api/health", (req, res) -> {
            res.type("application/json");
            return "{\"status\":\"ok\"}";
        });

        get("/api/contacts", (req, res) -> {
            res.type("application/json");

            String query = req.queryParams("q");

            ArrayList<Contact> contacts =
                (query == null || query.isBlank())
                ? manager.getAllSorted()
                : manager.searchByName(query.trim());

            return gson.toJson(contacts);
        });

        post("/api/contacts", (req, res) -> {
            res.type("application/json");

            Contact c;

            try {
                c = gson.fromJson(req.body(), Contact.class);
            } catch (Exception e) {
                res.status(400);
                return error("Invalid JSON body.");
            }

            String validation = validate(c);

            if (validation != null) {
                res.status(400);
                return error(validation);
            }

            c = new Contact(
                c.getName().trim(),
                c.getPhone().trim(),
                c.getEmail() == null ? "" : c.getEmail().trim(),
                c.getAddress() == null ? "" : c.getAddress().trim()
            );

            if (!manager.addContact(c)) {
                res.status(409);
                return error("This phone number already exists.");
            }

            if (!fileHandler.save(manager.getContacts())) {
                manager.deleteContact(c.getPhone());
                res.status(500);
                return error("Could not save contact.");
            }

            res.status(201);
            return gson.toJson(c);
        });

        put("/api/contacts/:phone", (req, res) -> {
            res.type("application/json");

            String phone = req.params(":phone");

            if (manager.findByPhone(phone) == null) {
                res.status(404);
                return error("Contact not found.");
            }

            JsonObject body;

            try {
                body = gson.fromJson(req.body(), JsonObject.class);
            } catch (Exception e) {
                res.status(400);
                return error("Invalid JSON body.");
            }

            if (body == null) {
                res.status(400);
                return error("Request body is required.");
            }

            String name = getString(body, "name");
            String email = getString(body, "email");
            String address = getString(body, "address");

            if (!email.isBlank() && !isValidEmail(email)) {
                res.status(400);
                return error("Please enter a valid email address.");
            }

            manager.updateContact(phone, name, email, address);

            if (!fileHandler.save(manager.getContacts())) {
                res.status(500);
                return error("Could not save updated contact.");
            }

            return gson.toJson(manager.findByPhone(phone));
        });

        delete("/api/contacts/:phone", (req, res) -> {
            res.type("application/json");

            String phone = req.params(":phone");

            if (!manager.deleteContact(phone)) {
                res.status(404);
                return error("Contact not found.");
            }

            if (!fileHandler.save(manager.getContacts())) {
                res.status(500);
                return error("Could not save deleted contact.");
            }

            return "{\"success\":true}";
        });

        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.type("application/json");
            res.status(500);
            res.body(error("Unexpected server error."));
        });

        System.out.println(
            "Contact Management System listening on port "
            + portNumber
        );
    }

    private static String validate(Contact c) {
        if (c == null) {
            return "Contact data is required.";
        }

        if (c.getName() == null || c.getName().isBlank()) {
            return "Name is required.";
        }

        if (c.getPhone() == null ||
            !c.getPhone().trim().matches("\\d{10}")) {
            return "Phone must be exactly 10 digits.";
        }

        if (c.getEmail() != null &&
            !c.getEmail().isBlank() &&
            !isValidEmail(c.getEmail().trim())) {
            return "Please enter a valid email address.";
        }

        return null;
    }

    private static boolean isValidEmail(String email) {
        return email.matches(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
    }

    private static String getString(
        JsonObject body, String key
    ) {
        return body.has(key) && !body.get(key).isJsonNull()
            ? body.get(key).getAsString().trim()
            : "";
    }

    private static String error(String message) {
        JsonObject result = new JsonObject();
        result.addProperty("error", message);
        return gson.toJson(result);
    }
}
