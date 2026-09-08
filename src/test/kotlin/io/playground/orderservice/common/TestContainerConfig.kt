package io.playground.orderservice.common

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer

@TestConfiguration
class TestContainerConfig {
    @Bean
    @ServiceConnection(name = "mysql")
    fun mysqlContainer(): MySQLContainer<*> =
        MySQLContainer("mysql:9.4.0")
            .withReuse(true)

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> =
        GenericContainer("redis:8.2.1-alpine")
            .withExposedPorts(6379)
            .withReuse(true)
}
