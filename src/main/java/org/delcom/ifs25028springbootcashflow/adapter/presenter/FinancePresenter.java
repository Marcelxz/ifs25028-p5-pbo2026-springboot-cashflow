package org.delcom.ifs25028springbootcashflow.adapter.presenter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;

public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /** Contoh: Rp 1,000,000.00 */
    public String formatMoney(long amount) {
        return String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount));
    }

    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Contoh: [1] Gaji | Rp 1,000,000.00 | [Pemasukan] */
    public String formatLine(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription()
                + " | " + formatMoney(t.getAmount())
                + " | [" + typeLabel(t.getType()) + "]";
    }

    /** Contoh: Saldo: Rp 4,975,000.00 */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatDateTime(LocalDateTime time) {
        return time == null ? "-" : DATE_TIME.format(time);
    }

    public String addedMessage(Transaction t) {
        return "Transaksi ditambahkan: " + formatLine(t);
    }

    public String deletedMessage(Transaction t) {
        return "Transaksi dihapus: " + formatLine(t);
    }

    public String errorMessage(String reason) {
        return "Gagal: " + reason;
    }
}