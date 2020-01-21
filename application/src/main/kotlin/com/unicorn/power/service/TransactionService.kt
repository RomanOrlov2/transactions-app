package com.unicorn.power.service

import com.unicorn.power.data.Sample
import com.unicorn.power.data.Transaction

interface TransactionService {
    fun saveTransaction(transaction: Transaction)
    fun getSample(): Sample
}