package io.playground.orderservice.infrastructure.client;

import io.playground.orderservice.application.saga.dto.ClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "catalogClient",
        url = "${api.internal.catalog-service.url}"
)
public interface CatalogFeignClient {
    @GetMapping
    ResponseEntity<List<ClientDto.Snapshot>> getSnapshots(@RequestParam List<Long> variantIds);

    @GetMapping
    ResponseEntity<List<ClientDto.Snapshot>> getHotSnapshots(@RequestParam List<Long> variantIds);
}
