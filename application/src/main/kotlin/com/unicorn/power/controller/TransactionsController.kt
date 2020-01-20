package com.unicorn.power.controller

import com.unicorn.power.data.Sample
import com.unicorn.power.data.Transaction
import com.unicorn.power.influxdb.DatabaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId


@RestController
class TransactionsController(@Autowired val databaseService: DatabaseService) {

    @PostMapping("/api/transactions", consumes = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun transaction(@RequestBody transaction: Transaction): ResponseEntity<Any> {
        val transactionDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(transaction.timestamp), ZoneId.systemDefault())
        return if (transactionDateTime.isAfter(now().minusMinutes(1))) {
            databaseService.saveTransaction(transaction)
            ResponseEntity(HttpStatus.CREATED)
        } else {
            ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }

    @GetMapping("/api/statistics", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun sample(): Sample = databaseService.getSample()
}