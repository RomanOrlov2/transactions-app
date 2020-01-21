package com.unicorn.power.service

import com.unicorn.power.data.Sample
import com.unicorn.power.data.Transaction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.Collections.singletonList
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.BiFunction

@Profile("inmemory")
@Service
class TransactionServiceImpl : TransactionService {
    private val transactions = ConcurrentSkipListMap<LocalDateTime, List<Transaction>>(LocalDateTime::compareTo)

    override fun saveTransaction(transaction: Transaction) {
        val transactionDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(transaction.timestamp), ZoneId.systemDefault())
        // Если такой транзакции не существует, пытаемся её туда положить (Но параллельно могут начать её туда ложить два потока)
        val putIfAbsent = transactions.putIfAbsent(
            transactionDateTime,
            singletonList(transaction)
        )
        // Если не успели положить, или транзакция с таким временем уже есть, то мержим значения
        if (putIfAbsent != null) {
            transactions.computeIfPresent(transactionDateTime, BiFunction { t, u -> u + transaction })
        }
    }

    override fun getSample(): Sample {
        val amounts =
            transactions.tailMap(now().minusMinutes(1), true)
                .flatMap(Map.Entry<LocalDateTime, List<Transaction>>::value)
                .map { it.amount }
                .toList()
        amounts.apply {
            return Sample(sum = sum(), avg = average(), max = max() ?: 0.0, min = min() ?: 0.0, count = size.toLong())
        }
    }

    fun removeOldRecords(olderThen: LocalDateTime) {
        val headMap = transactions.headMap(olderThen, true).forEach { transactions.remove(it.key) }
    }

}