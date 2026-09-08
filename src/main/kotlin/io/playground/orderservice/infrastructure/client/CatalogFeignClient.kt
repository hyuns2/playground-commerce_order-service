package io.playground.orderservice.infrastructure.client

import io.playground.orderservice.application.saga.dto.ClientDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "catalogClient",
    url = "\${api.internal.catalog-service.url}",
)
interface CatalogFeignClient {
    @GetMapping
    fun getSnapshots(@RequestParam variantIds: List<Long>): ResponseEntity<List<ClientDto.Snapshot>>

    @GetMapping
    fun getHotSnapshots(@RequestParam variantIds: List<Long>): ResponseEntity<List<ClientDto.Snapshot>>
}
