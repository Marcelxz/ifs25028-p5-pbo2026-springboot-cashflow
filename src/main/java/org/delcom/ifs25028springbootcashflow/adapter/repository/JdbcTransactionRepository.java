package org.delcom.ifs25028springbootcashflow.adapter.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.domain.repository.ITransactionRepository;

public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS =
            "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        String sql = "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, new String[]{"id"})) {
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Transaction(keys.getInt(1), userId, description, amount, type, proofPath, now);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan transaksi", e);
        }
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        String sql = "SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        List<Transaction> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca transaksi", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM transactions ORDER BY id";
        List<Transaction> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca transaksi", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        String sql = "SELECT " + COLUMNS + " FROM transactions WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca transaksi", e);
        }
    }

    @Override
    public boolean update(Transaction t) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getDescription());
            ps.setLong(2, t.getAmount());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getProofPath());
            ps.setInt(5, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui transaksi", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus transaksi", e);
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}