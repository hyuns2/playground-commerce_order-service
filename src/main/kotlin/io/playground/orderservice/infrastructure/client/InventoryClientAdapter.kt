package io.playground.orderservice.infrastructure.client

import feign.FeignException
import io.playground.orderservice.application.saga.dto.ClientDto
import io.playground.orderservice.application.saga.port.client.InventoryClientPort
import io.playground.orderservice.exception.BusinessDetailException
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.infrastructure.util.JsonUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InventoryClientAdapter(
    private val inventoryClient: InventoryFeignClient,
    private val jsonUtil: JsonUtil,
) : InventoryClientPort {
    @Value("\${hot-inventory.target-ids}")
    private lateinit var hotInventoryTargetIds: List<Long>

    private fun execute(action: () -> Unit) {
        try {
            action()
        } catch (fe: FeignException) {
            if (jsonUtil.isBusinessDetailError(fe.contentUTF8())) {
                throw BusinessDetailException(BusinessErrorCode.INVENTORY_SERVICE_FAILED, fe.contentUTF8())
            }
            fe.printStackTrace()
            throw fe
        }
    }

    override fun reserveStocks(orderExternalId: String, infos: List<ClientDto.ReservationRequest>) {
        if (infos.size == 1 && hotInventoryTargetIds.contains(infos[0].variantId)) {
            execute { inventoryClient.reserveHotStocks(orderExternalId, infos) }
            return
        }
        execute { inventoryClient.reserveStocks(orderExternalId, infos) }
    }

    override fun confirmStocks(orderExternalId: String) {
        execute { inventoryClient.confirmStocks(orderExternalId) }
    }
}
