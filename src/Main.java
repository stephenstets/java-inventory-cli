import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Inventory CLI for a small maker business.
 *
 * <p>Tracks filament rolls, finished products, and supplies in a CSV file
 * (inventory.csv). Commands are typed at the prompt; type "help" for the
 * list. Every change is saved immediately, so quitting mid-session loses
 * nothing.</p>
 *
 * <p>Build: {@code javac -d out src/*.java}<br>
 * Run: {@code java -cp out Main}</p>
 */
public class Main {
    private static final String HELP =
            """
            Commands:
              add     - add a new item (prompts for each field)
              list    - show everything in stock
              find <text> - search by name
              set <id> <qty> - set an item's quantity
              del <id> - remove an item
              low     - show items at or below their reorder level
              value   - total inventory value
              help    - this list
              quit    - save and exit
            """;

    private final Inventory inventory;
    private final Scanner in = new Scanner(System.in);

    public Main(Path file) throws IOException {
        inventory = new Inventory(file);
        inventory.load();
        System.out.printf("Loaded %d item(s) from %s.%n",
                inventory.size(), file);
    }

    public static void main(String[] args) {
        Path file = Paths.get("inventory.csv");
        try {
            new Main(file).run();
        } catch (IOException e) {
            System.err.println("Could not load inventory: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run() {
        System.out.println("Maker inventory - type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : "";

            try {
                switch (cmd) {
                    case "add" -> doAdd();
                    case "list" -> doList();
                    case "find" -> doFind(arg);
                    case "set" -> doSet(arg);
                    case "del" -> doDelete(arg);
                    case "low" -> doLow();
                    case "value" -> System.out.printf(
                            "Total inventory value: $%.2f%n", inventory.totalValue());
                    case "help" -> System.out.print(HELP);
                    case "quit", "exit" -> {
                        inventory.save();
                        System.out.println("Saved. Goodbye.");
                        return;
                    }
                    default -> System.out.println("Unknown command - type 'help'.");
                }
            } catch (IOException e) {
                System.err.println("Could not save: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("That number didn't parse - try again.");
            }
        }
    }

    private void doAdd() throws IOException {
        System.out.print("Name (no commas): ");
        String name = readNonEmpty();
        System.out.print("Category [FILAMENT/PRODUCT/SUPPLY]: ");
        String category = readNonEmpty().toUpperCase();
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(in.nextLine().trim());
        System.out.print("Unit (roll/pcs/litre...): ");
        String unit = readNonEmpty();
        System.out.print("Warn when stock falls to: ");
        int low = Integer.parseInt(in.nextLine().trim());
        System.out.print("Unit price ($): ");
        double price = Double.parseDouble(in.nextLine().trim());

        InventoryItem item = inventory.add(name, category, qty, unit, low, price);
        inventory.save();
        System.out.println("Added: " + item);
    }

    private void doList() {
        List<InventoryItem> all = inventory.all();
        if (all.isEmpty()) {
            System.out.println("(empty - use 'add' to put something in stock)");
            return;
        }
        all.forEach(System.out::println);
        System.out.printf("Total value: $%.2f%n", inventory.totalValue());
    }

    private void doFind(String text) {
        if (text.isBlank()) {
            System.out.println("Usage: find <text>");
            return;
        }
        List<InventoryItem> hits = inventory.search(text);
        if (hits.isEmpty()) {
            System.out.println("No matches for '" + text + "'.");
        } else {
            hits.forEach(System.out::println);
        }
    }

    private void doSet(String arg) throws IOException {
        String[] f = arg.split("\\s+");
        if (f.length != 2) {
            System.out.println("Usage: set <id> <qty>");
            return;
        }
        int id = Integer.parseInt(f[0]);
        int qty = Integer.parseInt(f[1]);
        Optional<InventoryItem> item = inventory.findById(id);
        if (item.isEmpty()) {
            System.out.println("No item #" + id + ".");
            return;
        }
        item.get().setQuantity(qty);
        inventory.save();
        System.out.println("Updated: " + item.get());
    }

    private void doDelete(String arg) throws IOException {
        int id = Integer.parseInt(arg.trim());
        if (inventory.remove(id)) {
            inventory.save();
            System.out.println("Removed item #" + id + ".");
        } else {
            System.out.println("No item #" + id + ".");
        }
    }

    private void doLow() {
        List<InventoryItem> low = inventory.lowStock();
        if (low.isEmpty()) {
            System.out.println("Nothing at reorder level. Well stocked.");
        } else {
            System.out.println("Needs reordering:");
            low.forEach(System.out::println);
        }
    }

    /** Read a non-empty line; commas are rejected (they would break the CSV). */
    private String readNonEmpty() {
        while (true) {
            String s = in.nextLine().trim();
            if (s.isEmpty()) {
                System.out.print("Can't be empty - try again: ");
            } else if (s.contains(",")) {
                System.out.print("No commas, please - try again: ");
            } else {
                return s;
            }
        }
    }
}
