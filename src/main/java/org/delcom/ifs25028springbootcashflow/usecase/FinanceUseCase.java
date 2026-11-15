package org.delcom.ifs25028springbootcashflow.usecase;

import java.util.List;
import java.util.Locale;
import org.delcom.ifs25028springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25028springbootcashflow.domain.repository.ITransactionRepository;

public class FinanceUseCase {

    /** Ringkasan keuangan satu user. */
    public record Summary(int count, long income, long expense) {
        public long balance() {
            return income - expense;
        }
    }

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction addIncome(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, TransactionType.EXPENSE, proofPath);
    }

    public Transaction add(int userId, String description, String amountText,
                           TransactionType type, String proofPath) throws ValidationException {
        String desc = validateDescription(description);
        long amount = parseAmount(amountText);
        return repository.save(userId, desc, amount, type, proofPath);
    }

    public List<Transaction> list(int userId) {
        return repository.findByUserId(userId);
    }

    public List<Transaction> search(int userId, String keyword, SortOption sortOption) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        SortOption option = sortOption == null ? SortOption.NEWEST_FIRST : sortOption;
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(key))
                .sorted(option.getComparator())
                .toList();
    }

    public Transaction update(int userId, int id, String description, String amountText, TransactionType type)
            throws DomainException {
        Transaction transaction = findOwned(userId, id);
        transaction.setDescription(validateDescription(description));
        transaction.setAmount(parseAmount(amountText));
        transaction.setType(type);
        repository.update(transaction);
        return transaction;
    }

    /** Mengembalikan transaksi yang dihapus (agar file bukti bisa ikut dihapus). */
    public Transaction delete(int userId, int id) throws DomainException {
        Transaction transaction = findOwned(userId, id);
        repository.deleteById(id);
        return transaction;
    }

    public Summary getSummary(int userId) {
        List<Transaction> all = repository.findByUserId(userId);
        long income = 0;
        long expense = 0;
        for (Transaction t : all) {
            if (t.isIncome()) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }
        return new Summary(all.size(), income, expense);
    }

    private Transaction findOwned(int userId, int id) throws DomainException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi ini bukan milik Anda");
        }
        return transaction;
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        String text = amountText.trim();
        if (!text.matches("-?\\d+")) {
            throw new ValidationException("Jumlah harus berupa angka bulat, contoh: 150000");
        }
        long amount;
        try {
            amount = Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah terlalu besar");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return amount;
    }
}