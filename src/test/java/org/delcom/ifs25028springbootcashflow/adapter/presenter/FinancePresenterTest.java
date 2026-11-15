package org.delcom.ifs25028springbootcashflow.adapter.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private Transaction sample() {
        return new Transaction(1, 1, "Gaji", 1_000_000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 15, 14, 5));
    }

    @Test
    void formatUang() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp -500.00", presenter.formatMoney(-500));
    }

    @Test
    void labelTipe() {
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));
    }

    @Test
    void formatBarisDanSaldo() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.formatLine(sample()));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
    }

    @Test
    void formatWaktu() {
        assertEquals("15-11-2026 14:05", presenter.formatDateTime(sample().getCreatedAt()));
        assertEquals("-", presenter.formatDateTime(null));
    }

    @Test
    void pesan() {
        assertEquals("Transaksi ditambahkan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.addedMessage(sample()));
        assertEquals("Transaksi dihapus: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.deletedMessage(sample()));
        assertEquals("Gagal: alasan", presenter.errorMessage("alasan"));
    }
}