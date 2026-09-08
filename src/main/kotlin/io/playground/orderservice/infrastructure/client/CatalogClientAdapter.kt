package io.playground.orderservice.infrastructure.client

import feign.FeignException
import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.client.CatalogClientPort
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CatalogClientAdapter(
    private val catalogClient: CatalogFeignClient,
    private val jsonUtil: JsonUtil,
) : CatalogClientPort {
    @Value("\${hot-inventory.target-ids}")
    private lateinit var hotInventoryTargetIds: List<Long>

    override fun getSnapshots(variantIds: List<Long>): List<ClientDto.Snapshot> {
        return try {
            if (variantIds.size == 1 && hotInventoryTargetIds.contains(variantIds[0])) {
                catalogClient.getHotSnapshots(variantIds).body!!
            } else {
                catalogClient.getSnapshots(variantIds).body!!
            }
        } catch (fe: FeignException) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8())) {
                throw BusinessDetailException(BusinessErrorCode.CATALOG_SERVICE_FAILED, fe.contentUTF8())
            }
            fe.printStackTrace()
            throw fe
        }
    }
}
