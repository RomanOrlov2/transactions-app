package com.unicorn.power.influxdb

import com.unicorn.power.data.Sample
import com.unicorn.power.data.Transaction
import com.unicorn.power.service.TransactionService
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@Profile("database")
@Service
class DatabaseService(@Autowired val influxDB: InfluxDB): TransactionService {

    override fun saveTransaction(transaction: Transaction) {
        val point = Point.measurement("transactions")
            .time(transaction.timestamp, TimeUnit.MILLISECONDS)
            .addField("amount", transaction.amount)
            .build()
        influxDB.write(point)
    }

    override fun getSample(): Sample {
        val query =
            "SELECT SUM(amount),MEAN(amount),MAX(amount),MIN(amount),COUNT(amount) FROM \"$database\" WHERE time > now() - 1m"
        val queryResult = influxDB.query(Query(query, database))
        val results = queryResult.results[0].series?.get(0)?.values?.get(0)
        return if (results != null)
            Sample(
                sum = results[1] as Double,
                avg = results[2] as Double,
                max = results[3] as Double,
                min = results[4] as Double,
                count = (results[5] as Double).toLong()
            )
        else
            Sample(0.0, 0.0, 0.0, 0.0, 0)
    }
}