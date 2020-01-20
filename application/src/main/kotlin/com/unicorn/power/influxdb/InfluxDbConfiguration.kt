package com.unicorn.power.influxdb

import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.influx.InfluxDbProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val database: String = "transactions"
const val retentionPolicy: String = "one_hour"

@Configuration
@EnableConfigurationProperties(InfluxDbProperties::class)
class InfluxDbConfiguration {

    @Bean
    fun configureInfluxDb(@Autowired properties: InfluxDbProperties): InfluxDB {
        val influxDB = InfluxDBFactory.connect(properties.url, properties.user, properties.password)
        influxDB.query(Query("CREATE DATABASE \"$database\"", database))
        influxDB.query(Query("CREATE RETENTION POLICY \"$retentionPolicy\" ON \"$database\" DURATION 1h REPLICATION 1 DEFAULT", database))
        influxDB.setDatabase(database)
        influxDB.setRetentionPolicy(retentionPolicy)
        return influxDB
    }
}