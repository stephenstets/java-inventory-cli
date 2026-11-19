import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The in-memory inventory, backed by a CSV file on disk.
 *
 * <p>Load once at startup ({@link #load()}), save after every change
 * ({@link #save()}). No database, no dependencies — the file is the
 * database, and it opens in any spreadsheet app.</p>
 */
public class Inventory {
    private final Path file;
    private final List<InventoryItem> items = new ArrayList<>();
    private int nextId = 1;

    public Inventory(Path file) {
        this.file = file;
    }

    /** Load items from the CSV file. A missing file just means "empty". */
    public void load() throws IOException {
        items.clear();
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank()) continue;
            InventoryItem item = InventoryItem.fromCsv(line);
            items.add(item);
            nextId = Math.max(nextId, item.getId() + 1);
        }
    }

    /** Write all items back to the CSV file. */
    public void save() throws IOException {
        List<String> lines = items.stream()
                .sorted(Comparator.comparingInt(InventoryItem::getId))
                .map(InventoryItem::toCsv)
                .collect(Collectors.toList());
        Files.write(file, lines);
    }

    public InventoryItem add(String name, String category, int quantity,
                             String unit, int lowStockAt, double unitPrice) {
        InventoryItem item = new InventoryItem(
                nextId++, name, category, quantity, unit, lowStockAt, unitPrice);
        items.add(item);
        return item;
    }

    public Optional<InventoryItem> findById(int id) {
        return items.stream().filter(i -> i.getId() == id).findFirst();
    }

    /** Case-insensitive substring search on the name. */
    public List<InventoryItem> search(String text) {
        String needle = text.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(i -> i.getName().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toList());
    }

    public boolean remove(int id) {
        return items.removeIf(i -> i.getId() == id);
    }

    public List<InventoryItem> all() {
        return items.stream()
                .sorted(Comparator.comparingInt(InventoryItem::getId))
                .collect(Collectors.toList());
    }

    public List<InventoryItem> lowStock() {
        return items.stream()
                .filter(InventoryItem::isLowStock)
                .collect(Collectors.toList());
    }

    public double totalValue() {
        return items.stream().mapToDouble(InventoryItem::lineValue).sum();
    }

    public int size() {
        return items.size();
    }
}
