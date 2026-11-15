package org.delcom.ifs25028springbootcashflow.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SortOptionTest {

    private Transaction tx(int id, long amount, TransactionType type, int hour) {
        return new Transaction(id, 1, "t" + id, amount, type, null, LocalDateTime.of(2026, 11, 15, hour, 0));
    }

    private final List<Transaction> data = List.of(
            tx(1, 300, TransactionType.INCOME, 8),
            tx(2, 100, TransactionType.EXPENSE, 9),
            tx(3, 200, TransactionType.INCOME, 10),
            tx(4, 400, TransactionType.EXPENSE, 11));

    private List<Integer> urutan(SortOption option) {
        return data.stream().sorted(option.getComparator()).map(Transaction::getId).toList();
    }

    @Test
    void terbaruDulu() {
        assertEquals(List.of(4, 3, 2, 1), urutan(SortOption.NEWEST_FIRST));
    }

    @Test
    void jumlahNaikDanTurun() {
        assertEquals(List.of(2, 3, 1, 4), urutan(SortOption.AMOUNT_ASC));
        assertEquals(List.of(4, 1, 3, 2), urutan(SortOption.AMOUNT_DESC));
    }

    @Test
    void pemasukanDanPengeluaranDulu() {
        assertEquals(List.of(3, 1, 4, 2), urutan(SortOption.INCOME_FIRST));
        assertEquals(List.of(4, 2, 3, 1), urutan(SortOption.EXPENSE_FIRST));
    }

    @Test
    void labelOpsi() {
        assertEquals("Terbaru", SortOption.NEWEST_FIRST.getLabel());
        assertEquals("Jumlah terkecil", SortOption.AMOUNT_ASC.getLabel());
        assertEquals("Jumlah terbesar", SortOption.AMOUNT_DESC.getLabel());
        assertEquals("Pemasukan dulu", SortOption.INCOME_FIRST.getLabel());
        assertEquals("Pengeluaran dulu", SortOption.EXPENSE_FIRST.getLabel());
    }
}