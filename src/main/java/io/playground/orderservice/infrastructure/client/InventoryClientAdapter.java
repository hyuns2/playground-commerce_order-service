package io.playground.orderservice.infrastructure.client;

import feign.FeignException;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.InventoryClientPort;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryClientPort {
    private final InventoryFeignClient inventoryClient;
    private final JsonUtil jsonUtil;
    @Value("${hot-inventory.target-ids}")
    private List<Long> hotInventoryTargetIds;

    private void execute(Runnable action) {
        try {
            action.run();
        } catch (FeignException fe) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8()))
                throw new BusinessDetailException(
                        BusinessErrorCode.INVENTORY_SERVICE_FAILED,
                        fe.contentUTF8()
                );

            fe.printStackTrace();
            throw fe;
        }
    }

    @Override
    public void reserveStocks(String orderExternalId,
                              List<ClientDto.ReservationRequest> infos) {
        if (infos.size() == 1 &&
                hotInventoryTargetIds.contains(infos.get(0).variantId())) {
            execute(() -> inventoryClient
                    .reserveHotStocks(orderExternalId, infos));
            return;
        }

        execute(() -> inventoryClient
                .reserveStocks(orderExternalId, infos));
    }

    @Override
    public void confirmStocks(String orderExternalId) {
        execute(() -> inventoryClient
                .confirmStocks(orderExternalId));
    }
}
