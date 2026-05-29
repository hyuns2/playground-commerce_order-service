package io.playground.orderservice.application.saga.port.client;

import io.playground.orderservice.application.saga.dto.ClientDto;

import java.util.List;

public interface CatalogClientPort {
    List<ClientDto.Snapshot> getSnapshots(List<Long> variantIds);
}
