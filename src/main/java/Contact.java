/**
 * Stores one contact. Phone number is used as the unique identifier.
 */
public class Contact {
    private String name;
    private String phone;
    private String email;
    private String address;

    public Contact() {}

    public Contact(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }

    public String toFileString() {
        return safe(name) + "|" + safe(phone) + "|" +
               safe(email) + "|" + safe(address);
    }

    private static String safe(String value) {
        return value == null ? "" :
            value.replace("|", " ")
                  .replace("\r", " ")
                  .replace("\n", " ");
    }

    public static Contact fromFileString(String line) {
        if (line == null) return null;

        String[] parts = line.split("\\|", -1);

        if (parts.length != 4) return null;

        return new Contact(
            parts[0], parts[1], parts[2], parts[3]
        );
    }

    @Override
    public String toString() {
        return String.format(
            "%-20s %-12s %-28s %s",
            name, phone, email, address
        );
    }
}
