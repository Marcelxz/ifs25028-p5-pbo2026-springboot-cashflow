package org.delcom.ifs25028springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25028springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25028springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void tambahPemasukanDanPengeluaran() throws Exception {
        Transaction masuk = useCase.addIncome(1, "  Gaji  ", "5000000", "bukti.png");
        Transaction keluar = useCase.addExpense(1, "Makan", "25000", null);

        assertEquals("Gaji", masuk.getDescription());
        assertEquals(5_000_000, masuk.getAmount());
        assertTrue(masuk.isIncome());
        assertEquals("bukti.png", masuk.getProofPath());
        assertFalse(keluar.isIncome());
    }

    @Test
    void keteranganTidakValid() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, null, "100", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "   ", "100", null));
    }

    @Test
    void jumlahTidakValid() {
        String[] salah = {null, "", "  ", "abc", "12.5", "0", "-5", "99999999999999999999"};
        for (String jumlah : salah) {
            assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", jumlah, null));
        }
    }

    @Test
    void daftarDiscopePerUser() throws Exception {
        useCase.addIncome(1, "A", "100", null);
        useCase.addIncome(2, "B", "200", null);
        assertEquals(1, useCase.list(1).size());
        assertEquals(1, useCase.list(2).size());
    }

    @Test
    void cariDanUrut() throws Exception {
        useCase.addIncome(1, "Gaji Bulanan", "5000000", null);
        useCase.addExpense(1, "Makan Siang", "25000", null);
        useCase.addIncome(2, "Milik user lain", "1", null);

        assertEquals(2, useCase.search(1, null, null).size());
        assertEquals(1, useCase.search(1, "  GAJI ", SortOption.NEWEST_FIRST).size());
        assertEquals("Makan Siang", useCase.search(1, "", SortOption.NEWEST_FIRST).get(0).getDescription());
        assertEquals("Makan Siang", useCase.search(1, "", SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("Gaji Bulanan", useCase.search(1, "", SortOption.INCOME_FIRST).get(0).getDescription());
    }

    @Test
    void updateBerhasil() throws Exception {
        Transaction t = useCase.addIncome(1, "Gaji", "100", null);
        Transaction hasil = useCase.update(1, t.getId(), " Bonus ", "250", TransactionType.EXPENSE);
        assertEquals("Bonus", hasil.getDescription());
        assertEquals(250, hasil.getAmount());
        assertEquals(TransactionType.EXPENSE, hasil.getType());
    }

    @Test
    void updateGagal() throws Exception {
        Transaction t = useCase.addIncome(1, "Gaji", "100", null);
        int id = t.getId();
        assertThrows(ValidationException.class, () -> useCase.update(2, id, "x", "1", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.update(1, 0, "x", "1", TransactionType.INCOME));
        assertThrows(EntityNotFoundException.class, () -> useCase.update(1, 99, "x", "1", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.update(1, id, " ", "1", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.update(1, id, "ok", "abc", TransactionType.INCOME));
    }

    @Test
    void hapusBerhasil() throws Exception {
        Transaction t = useCase.addIncome(1, "Gaji", "100", "bukti.png");
        Transaction dihapus = useCase.delete(1, t.getId());
        assertEquals("bukti.png", dihapus.getProofPath());
        assertEquals(0, useCase.list(1).size());
    }

    @Test
    void hapusGagal() throws Exception {
        Transaction t = useCase.addIncome(1, "Gaji", "100", null);
        int id = t.getId();
        assertThrows(ValidationException.class, () -> useCase.delete(2, id));
        assertThrows(ValidationException.class, () -> useCase.delete(1, 0));
        assertThrows(EntityNotFoundException.class, () -> useCase.delete(1, 99));
    }

    @Test
    void ringkasanKeuangan() throws Exception {
        FinanceUseCase.Summary kosong = useCase.getSummary(1);
        assertEquals(0, kosong.count());
        assertEquals(0, kosong.balance());

        useCase.addIncome(1, "Gaji", "5000000", null);
        useCase.addExpense(1, "Makan", "25000", null);

        FinanceUseCase.Summary s = useCase.getSummary(1);
        assertEquals(2, s.count());
        assertEquals(5_000_000, s.income());
        assertEquals(25_000, s.expense());
        assertEquals(4_975_000, s.balance());
    }
}