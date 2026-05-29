package io.playground.orderservice.application.saga.port.client;

import io.playground.orderservice.application.saga.dto.ClientDto;

import java.util.List;

public interface InventoryClientPort {
    void reserveStocks(String orderExternalId,
                       List<ClientDto.ReservationRequest> infos);

    void confirmStocks(String orderExternalId);
}
