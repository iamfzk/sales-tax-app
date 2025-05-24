package com.makkajai.salestax;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalesTaxApp {
    public static void main(String[] args) {
        List<String> input1 = Arrays.asList(
                "1 book at 12.49",
                "1 music CD at 14.99",
                "1 chocolate bar at 0.85"
        );

        List<String> input2 = Arrays.asList(
                "1 imported box of chocolates at 10.00",
                "1 imported bottle of perfume at 47.50"
        );

        List<String> input3 = Arrays.asList(
                "1 imported bottle of perfume at 27.99",
                "1 bottle of perfume at 18.99",
                "1 packet of headache pills at 9.75",
                "1 box of imported chocolates at 11.25"
        );

        System.out.println("Output 1:");
        printReceipt(processInput(input1));
        System.out.println("\nOutput 2:");
        printReceipt(processInput(input2));
        System.out.println("\nOutput 3:");
        printReceipt(processInput(input3));
    }

    private static List<Item> processInput(List<String> inputLines) {
        List<Item> items = new ArrayList<>();
        for (String line : inputLines) {
            Item item = ItemParser.parse(line);
            item.calculateTaxes();
            items.add(item);
        }
        return items;
    }

    private static void printReceipt(List<Item> items) {
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Item item : items) {
            System.out.println(item);
            totalTax = totalTax.add(item.getTax());
            total = total.add(item.getTotalPrice());
        }

        System.out.printf("Sales Taxes: %.2f\n", totalTax);
        System.out.printf("Total: %.2f\n", total);
    }
}

class Item {
    private final int quantity;
    private final String name;
    private final BigDecimal price;
    private BigDecimal tax = BigDecimal.ZERO;

    public Item(int quantity, String name, BigDecimal price) {
        this.quantity = quantity;
        this.name = name;
        this.price = price;
    }

    public void calculateTaxes() {
        BigDecimal taxRate = BigDecimal.ZERO;
        if (!isExempt()) {
            taxRate = taxRate.add(new BigDecimal("0.10"));
        }
        if (isImported()) {
            taxRate = taxRate.add(new BigDecimal("0.05"));
        }
        BigDecimal rawTax = price.multiply(taxRate);
        tax = roundUpToNearestPoint05(rawTax);
    }

    public BigDecimal getTax() {
        return tax.multiply(new BigDecimal(quantity));
    }

    public BigDecimal getTotalPrice() {
        return (price.add(tax)).multiply(new BigDecimal(quantity));
    }

    public boolean isImported() {
        return name.toLowerCase().contains("imported");
    }

    public boolean isExempt() {
        return name.matches("(?i).*(book|chocolate|pill).*"); // ✅ Fixed quote
    }

    private BigDecimal roundUpToNearestPoint05(BigDecimal value) {
        return new BigDecimal(Math.ceil(value.doubleValue() * 20) / 20).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%d %s: %.2f", quantity, name, getTotalPrice());
    }
}

class ItemParser {
    private static final Pattern pattern = Pattern.compile("(\\d+) (.+) at (\\d+\\.\\d{2})");

    public static Item parse(String line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid input line: " + line);
        }
        int quantity = Integer.parseInt(matcher.group(1));
        String name = matcher.group(2);
        BigDecimal price = new BigDecimal(matcher.group(3));
        return new Item(quantity, name, price);
    }
}
