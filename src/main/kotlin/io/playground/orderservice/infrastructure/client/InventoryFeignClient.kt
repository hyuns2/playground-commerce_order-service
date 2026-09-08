package io.playground.orderservice.infrastructure.client

import io.playground.orderservice.application.saga.dto.ClientDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "inventoryClient",
    url = "\${api.internal.inventory-service.url}",
)
interface InventoryFeignClient {
    @PostMapping("/reserve")
    fun reserveStocks(
        @RequestParam orderExternalId: String,
        @RequestBody infos: List<ClientDto.ReservationRequest>,
    ): ResponseEntity<Void>

    @PostMapping("/hot/reserve")
    fun reserveHotStocks(
        @RequestParam orderExternalId: String,
        @RequestBody infos: List<ClientDto.ReservationRequest>,
    ): ResponseEntity<Void>

    @PostMapping("/confirm")
    fun confirmStocks(@RequestParam orderExternalId: String): ResponseEntity<Void>
}
