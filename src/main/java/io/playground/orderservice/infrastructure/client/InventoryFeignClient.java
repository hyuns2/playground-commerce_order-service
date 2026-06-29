package io.playground.orderservice.infrastructure.client;

import io.playground.orderservice.application.saga.dto.ClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "inventoryClient",
        url = "${api.internal.inventory-service.url}"
)
public interface InventoryFeignClient {
    @PostMapping("/reserve")
    ResponseEntity<Void> reserveStocks(@RequestParam String orderExternalId,
                                       @RequestBody List<ClientDto.ReservationRequest> infos);

    @PostMapping("/hot/reserve")
    ResponseEntity<Void> reserveHotStocks(@RequestParam String orderExternalId,
                                          @RequestBody List<ClientDto.ReservationRequest> infos);


    @PostMapping("/confirm")
    ResponseEntity<Void> confirmStocks(@RequestParam String orderExternalId);
}
