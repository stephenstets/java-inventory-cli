import java.util.Locale;

/**
 * One thing in stock: a filament roll, a finished product, or a supply.
 *
 * <p>Items are stored one-per-line in a CSV file (see {@link Inventory}).
 * Fields never contain commas in this app — names are validated on input —
 * so a plain split on commas is a safe, dependency-free format.</p>
 */
public class InventoryItem {
    private final int id;
    private String name;
    private String category;   // e.g. FILAMENT, PRODUCT, SUPPLY
    private int quantity;
    private String unit;       // e.g. "roll", "pcs", "litre"
    private int lowStockAt;    // warn when quantity falls to this level
    private double unitPrice;  // dollars, for inventory valuation

    public InventoryItem(int id, String name, String category, int quantity,
                         String unit, int lowStockAt, double unitPrice) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.lowStockAt = lowStockAt;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public int getLowStockAt() { return lowStockAt; }
    public double getUnitPrice() { return unitPrice; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** True when this item needs reordering. */
    public boolean isLowStock() { return quantity <= lowStockAt; }

    /** Total value of this line of stock. */
    public double lineValue() { return quantity * unitPrice; }

    /** One CSV line: id,name,category,quantity,unit,lowStockAt,unitPrice */
    public String toCsv() {
        return String.format(Locale.ROOT, "%d,%s,%s,%d,%s,%d,%.2f",
                id, name, category, quantity, unit, lowStockAt, unitPrice);
    }

    /** Parse a line written by {@link #toCsv()}. */
    public static InventoryItem fromCsv(String line) {
        String[] f = line.split(",", -1);
        if (f.length != 7) {
            throw new IllegalArgumentException("Bad inventory line: " + line);
        }
        return new InventoryItem(
                Integer.parseInt(f[0].trim()),
                f[1].trim(), f[2].trim(),
                Integer.parseInt(f[3].trim()),
                f[4].trim(),
                Integer.parseInt(f[5].trim()),
                Double.parseDouble(f[6].trim()));
    }

    @Override
    public String toString() {
        String flag = isLowStock() ? "  <-- LOW STOCK" : "";
        return String.format(Locale.ROOT,
                "#%-3d %-28s %-9s %4d %-6s @ $%6.2f  = $%7.2f%s",
                id, name, category, quantity, unit, unitPrice, lineValue(), flag);
    }
}
