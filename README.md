# java-inventory-cli

A command-line inventory manager for a small maker business, in plain Java —
no frameworks, no build tool, no dependencies. It tracks filament rolls,
finished products, and supplies in a CSV file, so the data opens in any
spreadsheet app.

## Build and run

```bash
javac -d out src/*.java   # compile (Java 17+)
java -cp out Main         # run
```

Or on Windows, `run.bat` does both steps.

## Commands

```
add              add a new item (prompts for each field)
list             show everything in stock
find <text>      search by name
set <id> <qty>   set an item's quantity
del <id>         remove an item
low              show items at or below their reorder level
value            total inventory value
quit             save and exit
```

## Example session

```
> add
Name (no commas): PLA Black 1kg
Category [FILAMENT/PRODUCT/SUPPLY]: FILAMENT
Quantity: 12
Unit (roll/pcs/litre...): roll
Warn when stock falls to: 3
Unit price ($): 18.99
Added: #1   PLA Black 1kg                FILAMENT    12 roll   @ $ 18.99  = $ 227.88

> low
Nothing at reorder level. Well stocked.

> set 1 2
Updated: #1   PLA Black 1kg                FILAMENT     2 roll   @ $ 18.99  = $  37.98  <-- LOW STOCK
```

## How it's put together

| File | What it does |
|---|---|
| `src/InventoryItem.java` | One stock line: fields, low-stock check, CSV read/write |
| `src/Inventory.java` | The collection: load/save, search, low-stock report, total value |
| `src/Main.java` | The command loop and all user interaction |

Data lives in `inventory.csv` (created on first save, gitignored). Every change
saves immediately, so quitting mid-session loses nothing.

## License

MIT — see `LICENSE`.
