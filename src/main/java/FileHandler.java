import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

public class FileHandler {
    private final String fileName;

    public FileHandler(String fileName) {
        this.fileName = fileName;
    }

    public synchronized boolean save(ArrayList<Contact> contacts) {
        try {
            Path path = Paths.get(fileName).toAbsolutePath();
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(
                    path, StandardCharsets.UTF_8
                )
            )) {
                for (Contact c : contacts) {
                    writer.println(c.toFileString());
                }
            }

            return true;

        } catch (IOException e) {
            System.err.println(
                "Could not save contacts: " + e.getMessage()
            );
            return false;
        }
    }

    public synchronized ArrayList<Contact> load() {
        ArrayList<Contact> contacts = new ArrayList<>();
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) return contacts;

        try (BufferedReader reader = Files.newBufferedReader(
            path, StandardCharsets.UTF_8
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                Contact c = Contact.fromFileString(line);

                if (c != null) {
                    contacts.add(c);
                }
            }

        } catch (IOException e) {
            System.err.println(
                "Could not load contacts: " + e.getMessage()
            );
        }

        return contacts;
    }
}
