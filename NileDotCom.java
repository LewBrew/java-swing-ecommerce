/*
 Name: 
 Course: CNT 4714 – Spring 2026
 Assignment title: Project 1 – An Event-driven Enterprise Simulation
 Date: Sunday February 1, 2026
*/

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class NileDotCom {

    // Holds info for a single inventory item
    static class ItemInfo {
        String id;
        String name;
        boolean inStock;
        int stockAmount;
        double price;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Nile Dot Com - Project 1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 500);
            frame.setLayout(new BorderLayout(10, 10));

            // North panel
            JPanel northPanel = new JPanel(new GridLayout(6, 2, 5, 5));

            JTextField itemIdField = new JTextField();
            JTextField quantityField = new JTextField();

            JTextField detailsField = new JTextField();
            detailsField.setEditable(false);

            JTextField subtotalField = new JTextField();
            subtotalField.setEditable(false);

            northPanel.add(new JLabel("Enter Item ID:"));
            northPanel.add(itemIdField);

            northPanel.add(new JLabel("Enter Quantity:"));
            northPanel.add(quantityField);

            northPanel.add(new JLabel("Item Details:"));
            northPanel.add(detailsField);

            northPanel.add(new JLabel("Order Subtotal:"));
            northPanel.add(subtotalField);

            northPanel.add(new JLabel(""));
            northPanel.add(new JLabel(""));

            // Center panel
            JTextArea cartArea = new JTextArea();
            cartArea.setEditable(false);
            cartArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(cartArea);

            // South panel
            JPanel southPanel = new JPanel(new GridLayout(2, 3, 5, 5));

            JButton searchButton = new JButton("Search For Item #1");
            JButton addButton = new JButton("Add Item #1 To Cart");
            JButton deleteButton = new JButton("Delete Last Item From Cart");
            JButton checkoutButton = new JButton("Check Out");
            JButton emptyButton = new JButton("Empty Cart");
            JButton exitButton = new JButton("Exit");

            southPanel.add(searchButton);
            southPanel.add(addButton);
            southPanel.add(deleteButton);
            southPanel.add(checkoutButton);
            southPanel.add(emptyButton);
            southPanel.add(exitButton);

            // Order state + cart storage
            final int MAX_ITEMS = 5;

            // Variables to keep track of the current order
            int[] itemNumber = {1};
            int[] cartCount = {0};
            double[] subtotal = {0.0};

            ItemInfo[] lastFoundItem = {null};
            int[] lastFoundQty = {0};
            int[] lastFoundDiscount = {0};

            String[] cartItemId = new String[MAX_ITEMS];
            String[] cartItemName = new String[MAX_ITEMS];
            double[] cartUnitPrice = new double[MAX_ITEMS];
            int[] cartQty = new int[MAX_ITEMS];
            int[] cartDiscount = new int[MAX_ITEMS];
            double[] cartLineTotal = new double[MAX_ITEMS];
            String[] cartLines = new String[MAX_ITEMS];

            // starting buttons
            addButton.setEnabled(false);
            deleteButton.setEnabled(false);
            checkoutButton.setEnabled(false);

            // Exit
            exitButton.addActionListener(e -> System.exit(0));

            // Empty cart (reset)
            emptyButton.addActionListener(e -> {

                itemIdField.setText("");
                quantityField.setText("");
                detailsField.setText("");
                subtotalField.setText("");
                cartArea.setText("");

                itemNumber[0] = 1;
                cartCount[0] = 0;
                subtotal[0] = 0.0;

                lastFoundItem[0] = null;
                lastFoundQty[0] = 0;
                lastFoundDiscount[0] = 0;

                for (int i = 0; i < MAX_ITEMS; i++) {
                    cartItemId[i] = null;
                    cartItemName[i] = null;
                    cartUnitPrice[i] = 0.0;
                    cartQty[i] = 0;
                    cartDiscount[i] = 0;
                    cartLineTotal[i] = 0.0;
                    cartLines[i] = null;
                }

                searchButton.setText("Search For Item #1");
                addButton.setText("Add Item #1 To Cart");

                searchButton.setEnabled(true);
                addButton.setEnabled(false);
                deleteButton.setEnabled(false);
                checkoutButton.setEnabled(false);

                itemIdField.setEnabled(true);
                quantityField.setEnabled(true);

                itemIdField.requestFocusInWindow();
            });

            // Search
            searchButton.addActionListener(e -> {

                if (cartCount[0] >= MAX_ITEMS) {
                    JOptionPane.showMessageDialog(frame,
                            "Cart is full (max " + MAX_ITEMS + " items).",
                            "Cart Full",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String idTyped = itemIdField.getText().trim();
                String qtyTyped = quantityField.getText().trim();

                if (idTyped.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Please type an Item ID.",
                            "Missing Item ID",
                            JOptionPane.ERROR_MESSAGE);
                    itemIdField.requestFocusInWindow();
                    return;
                }

                int qtyWanted;
                try {
                    qtyWanted = Integer.parseInt(qtyTyped);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Quantity must be a whole number (like 1, 2, 3...).",
                            "Bad Quantity",
                            JOptionPane.ERROR_MESSAGE);

                    // clear only quantity
                    quantityField.setText("");
                    quantityField.requestFocusInWindow();
                    return;
                }

                if (qtyWanted <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Quantity must be 1 or more.",
                            "Bad Quantity",
                            JOptionPane.ERROR_MESSAGE);

                    quantityField.setText("");
                    quantityField.requestFocusInWindow();
                    return;
                }

                ItemInfo found = findItemInInventory(idTyped);

                if (found == null) {
                    JOptionPane.showMessageDialog(frame,
                            "Item ID " + idTyped + " was not found.",
                            "Not Found",
                            JOptionPane.ERROR_MESSAGE);

                    // clear both
                    itemIdField.setText("");
                    quantityField.setText("");
                    detailsField.setText("");
                    itemIdField.requestFocusInWindow();
                    return;
                }

                if (!found.inStock) {
                    JOptionPane.showMessageDialog(frame,
                            "Item is listed as NOT in stock.",
                            "Out of Stock",
                            JOptionPane.ERROR_MESSAGE);

                    // clear both
                    itemIdField.setText("");
                    quantityField.setText("");
                    detailsField.setText("");
                    itemIdField.requestFocusInWindow();
                    return;
                }

                if (qtyWanted > found.stockAmount) {
                    JOptionPane.showMessageDialog(frame,
                            "Not enough in stock.\n" +
                                    "You asked for: " + qtyWanted + "\n" +
                                    "We have: " + found.stockAmount,
                            "Not Enough Stock",
                            JOptionPane.ERROR_MESSAGE);

                    // keep ID, clear quantity
                    quantityField.setText("");
                    quantityField.requestFocusInWindow();
                    return;
                }

                int discountPercent = getDiscountPercent(qtyWanted);

                lastFoundItem[0] = found;
                lastFoundQty[0] = qtyWanted;
                lastFoundDiscount[0] = discountPercent;

                detailsField.setText(
                        found.id + " - " + found.name +
                                " | $" + String.format("%.2f", found.price) +
                                " | qty: " + qtyWanted +
                                " | discount: " + discountPercent + "%"
                );

                searchButton.setEnabled(false);
                addButton.setEnabled(true);
            });

            // Add
            addButton.addActionListener(e -> {

                if (lastFoundItem[0] == null) {
                    JOptionPane.showMessageDialog(frame,
                            "Please search for an item first.",
                            "No Item Selected",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int spot = cartCount[0];

                ItemInfo it = lastFoundItem[0];
                int qty = lastFoundQty[0];
                int disc = lastFoundDiscount[0];

                double priceBeforeDiscount = it.price * qty;
                double discountMoney = priceBeforeDiscount * (disc / 100.0);
                double finalPrice = priceBeforeDiscount - discountMoney;
                finalPrice = round2(finalPrice);

                cartItemId[spot] = it.id;
                cartItemName[spot] = it.name;
                cartUnitPrice[spot] = it.price;
                cartQty[spot] = qty;
                cartDiscount[spot] = disc;
                cartLineTotal[spot] = finalPrice;

                cartLines[spot] =
                        "Item #" + itemNumber[0] +
                                ": " + it.id +
                                "  \"" + it.name + "\"" +
                                "  qty=" + qty +
                                "  $" + String.format("%.2f", it.price) +
                                "  disc=" + disc + "%" +
                                "  lineTotal=$" + String.format("%.2f", finalPrice);

                subtotal[0] = round2(subtotal[0] + finalPrice);
                subtotalField.setText("$" + String.format("%.2f", subtotal[0]));

                cartArea.setText("");
                for (int i = 0; i <= spot; i++) {
                    cartArea.append(cartLines[i] + "\n");
                }

                cartCount[0]++;
                itemNumber[0]++;

                lastFoundItem[0] = null;
                lastFoundQty[0] = 0;
                lastFoundDiscount[0] = 0;

                searchButton.setText("Search For Item #" + itemNumber[0]);
                addButton.setText("Add Item #" + itemNumber[0] + " To Cart");

                addButton.setEnabled(false);
                searchButton.setEnabled(true);
                deleteButton.setEnabled(true);
                checkoutButton.setEnabled(true);

                if (cartCount[0] >= MAX_ITEMS) {
                    searchButton.setEnabled(false);
                    JOptionPane.showMessageDialog(frame,
                            "You reached the max of " + MAX_ITEMS + " items in the cart.",
                            "Cart Full",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                itemIdField.setText("");
                quantityField.setText("");
                detailsField.setText("");
                itemIdField.requestFocusInWindow();
            });

            // Delete last
            deleteButton.addActionListener(e -> {

                if (cartCount[0] <= 0) return;

                int lastSpot = cartCount[0] - 1;

                subtotal[0] = round2(subtotal[0] - cartLineTotal[lastSpot]);
                if (subtotal[0] < 0) subtotal[0] = 0.0;

                if (subtotal[0] == 0.0) subtotalField.setText("");
                else subtotalField.setText("$" + String.format("%.2f", subtotal[0]));

                cartItemId[lastSpot] = null;
                cartItemName[lastSpot] = null;
                cartUnitPrice[lastSpot] = 0.0;
                cartQty[lastSpot] = 0;
                cartDiscount[lastSpot] = 0;
                cartLineTotal[lastSpot] = 0.0;
                cartLines[lastSpot] = null;

                cartCount[0]--;
                itemNumber[0] = cartCount[0] + 1;

                cartArea.setText("");
                for (int i = 0; i < cartCount[0]; i++) {
                    cartArea.append(cartLines[i] + "\n");
                }

                searchButton.setText("Search For Item #" + itemNumber[0]);
                addButton.setText("Add Item #" + itemNumber[0] + " To Cart");

                searchButton.setEnabled(true);
                addButton.setEnabled(false);

                if (cartCount[0] == 0) {
                    deleteButton.setEnabled(false);
                    checkoutButton.setEnabled(false);
                }

                detailsField.setText("");
                itemIdField.requestFocusInWindow();
            });

            // Checkout
            checkoutButton.addActionListener(e -> {

                if (cartCount[0] == 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Cart is empty. Add something first.",
                            "Nothing To Check Out",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String transactionId = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());

                double roundedSubtotal = round2(subtotal[0]);
                double roundedTax = round2(roundedSubtotal * 0.06);
                double roundedTotal = round2(roundedSubtotal + roundedTax);

                String prettyDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new Date());

                StringBuilder invoice = new StringBuilder();
                invoice.append("Nile Dot Com - Invoice\n");
                invoice.append("Transaction ID: ").append(transactionId).append("\n");
                invoice.append("Date/Time: ").append(prettyDateTime).append("\n");
                invoice.append("------------------------------------------------------------\n");

                for (int i = 0; i < cartCount[0]; i++) {
                    invoice.append(cartLines[i]).append("\n");
                }

                invoice.append("------------------------------------------------------------\n");
                invoice.append("Subtotal: $").append(String.format("%.2f", roundedSubtotal)).append("\n");
                invoice.append("Tax (6%): $").append(String.format("%.2f", roundedTax)).append("\n");
                invoice.append("Total:    $").append(String.format("%.2f", roundedTotal)).append("\n");

                JOptionPane.showMessageDialog(frame,
                        invoice.toString(),
                        "Invoice",
                        JOptionPane.INFORMATION_MESSAGE);

                try (FileWriter fw = new FileWriter("transactions.csv", true)) {

                    for (int i = 0; i < cartCount[0]; i++) {
                        String line =
                                transactionId + "," +
                                        cartItemId[i] + "," +
                                        "\"" + cartItemName[i] + "\"," +
                                        String.format("%.2f", cartUnitPrice[i]) + "," +
                                        cartQty[i] + "," +
                                        cartDiscount[i] + "," +
                                        String.format("%.2f", cartLineTotal[i]) +
                                        "\n";
                        fw.write(line);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Could not write to transactions.csv\n" + ex.getMessage(),
                            "File Write Problem",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                searchButton.setEnabled(false);
                addButton.setEnabled(false);
                deleteButton.setEnabled(false);
                checkoutButton.setEnabled(false);

                itemIdField.setEnabled(false);
                quantityField.setEnabled(false);

                JOptionPane.showMessageDialog(frame,
                        "Saved to transactions.csv\nClick Empty Cart to start a new order.",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            frame.add(northPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(southPanel, BorderLayout.SOUTH);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static double round2(double money) {
        return Math.round(money * 100.0) / 100.0;
    }

    private static ItemInfo findItemInInventory(String idLookingFor) {
        try (Scanner sc = new Scanner(new File("inventory.csv"))) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                // This is just a small helper so descriptions with commas in quotes don't break
                String[] parts = splitCsvLine(line);
                if (parts.length < 5) continue;

                String id = parts[0].trim();
                String name = parts[1].trim();

                if (name.startsWith("\"") && name.endsWith("\"") && name.length() >= 2) {
                    name = name.substring(1, name.length() - 1);
                }

                boolean inStock = parts[2].trim().equalsIgnoreCase("true");
                int stockAmount = Integer.parseInt(parts[3].trim());
                double price = Double.parseDouble(parts[4].trim());

                if (id.equalsIgnoreCase(idLookingFor)) {
                    ItemInfo it = new ItemInfo();
                    it.id = id;
                    it.name = name;
                    it.inStock = inStock;
                    it.stockAmount = stockAmount;
                    it.price = price;
                    return it;
                }
            }

        } catch (Exception ex) {
            // ignore and treat as not found
        }

        return null;
    }

    private static int getDiscountPercent(int qty) {
        if (qty >= 15) return 20;
        if (qty >= 10) return 15;
        if (qty >= 5) return 10;
        return 0;
    }

    // Simple CSV split that doesn't break commas inside quotes
    private static String[] splitCsvLine(String line) {
        String[] temp = new String[10];
        int count = 0;

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                temp[count] = cur.toString();
                count++;
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }

        temp[count] = cur.toString();
        count++;

        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = temp[i];
        return out;
        //:)
    }
}
