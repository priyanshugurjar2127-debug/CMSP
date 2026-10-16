import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactManager {
    private final ArrayList<Contact> contacts;

    public ContactManager(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public boolean addContact(Contact c) {
        if (c == null || c.getPhone() == null ||
            findByPhone(c.getPhone()) != null) {
            return false;
        }

        contacts.add(c);
        return true;
    }

    public Contact findByPhone(String phone) {
        if (phone == null) return null;

        for (Contact c : contacts) {
            if (phone.equals(c.getPhone())) {
                return c;
            }
        }

        return null;
    }

    public ArrayList<Contact> searchByName(String keyword) {
        ArrayList<Contact> result = new ArrayList<>();

        String key = keyword == null
            ? ""
            : keyword.toLowerCase();

        for (Contact c : contacts) {
            if (c.getName() != null &&
                c.getName().toLowerCase().contains(key)) {
                result.add(c);
            }
        }

        return result;
    }

    public boolean updateContact(
        String phone,
        String newName,
        String newEmail,
        String newAddress
    ) {
        Contact c = findByPhone(phone);

        if (c == null) return false;

        if (newName != null && !newName.isBlank()) {
            c.setName(newName.trim());
        }

        if (newEmail != null && !newEmail.isBlank()) {
            c.setEmail(newEmail.trim());
        }

        if (newAddress != null && !newAddress.isBlank()) {
            c.setAddress(newAddress.trim());
        }

        return true;
    }

    public boolean deleteContact(String phone) {
        Contact c = findByPhone(phone);
        return c != null && contacts.remove(c);
    }

    public ArrayList<Contact> getAllSorted() {
        ArrayList<Contact> sorted =
            new ArrayList<>(contacts);

        Collections.sort(
            sorted,
            Comparator.comparing(
                Contact::getName,
                Comparator.nullsFirst(
                    String.CASE_INSENSITIVE_ORDER
                )
            )
        );

        return sorted;
    }
}
