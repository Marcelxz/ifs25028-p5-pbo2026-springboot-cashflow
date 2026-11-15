package org.delcom.ifs25028springbootcashflow.domain.repository;

import java.util.List;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    List<Transaction> findByUserId(int userId);
}