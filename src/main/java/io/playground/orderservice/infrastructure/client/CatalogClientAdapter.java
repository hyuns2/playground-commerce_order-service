package io.playground.orderservice.infrastructure.client;

import feign.FeignException;
import io.playground.orderservice.application.saga.dto.ClientDto;
import io.playground.orderservice.application.saga.port.client.CatalogClientPort;
import io.playground.orderservice.exception.BusinessDetailException;
import io.playground.orderservice.exception.BusinessErrorCode;
import io.playground.orderservice.infrastructure.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogClientAdapter implements CatalogClientPort {
    private final CatalogFeignClient catalogClient;
    private final JsonUtil jsonUtil;
    @Value("${hot-inventory.target-ids}")
    private List<Long> hotInventoryTargetIds;

    @Override
    public List<ClientDto.Snapshot> getSnapshots(List<Long> variantIds) {
        try {
            if (variantIds.size() == 1 &&
                    hotInventoryTargetIds.contains(variantIds.get(0)))
                return catalogClient.getHotSnapshots(variantIds)
                        .getBody();

            return catalogClient.getSnapshots(variantIds)
                    .getBody();
        } catch (FeignException fe) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8()))
                throw new BusinessDetailException(
                        BusinessErrorCode.CATALOG_SERVICE_FAILED,
                        fe.contentUTF8()
                );

            fe.printStackTrace();
            throw fe;
        }
    }
}
