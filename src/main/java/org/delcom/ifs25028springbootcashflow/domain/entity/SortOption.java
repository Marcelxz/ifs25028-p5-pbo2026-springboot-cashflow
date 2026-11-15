package org.delcom.ifs25028springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {

    NEWEST_FIRST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparing(Transaction::getId)
                    .reversed()),

    AMOUNT_ASC("Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount)),

    AMOUNT_DESC("Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).reversed()),

    INCOME_FIRST("Pemasukan dulu",
            Comparator.<Transaction>comparingInt(t -> t.isIncome() ? 0 : 1)
                    .thenComparing(Comparator.comparing(Transaction::getCreatedAt).reversed())),

    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.<Transaction>comparingInt(t -> t.isIncome() ? 1 : 0)
                    .thenComparing(Comparator.comparing(Transaction::getCreatedAt).reversed()));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}