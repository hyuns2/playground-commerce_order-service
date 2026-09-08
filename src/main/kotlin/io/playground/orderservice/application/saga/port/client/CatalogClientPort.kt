package io.playground.orderservice.application.saga.port.client

import io.playground.orderservice.application.saga.dto.ClientDto

interface CatalogClientPort {
    fun getSnapshots(variantIds: List<Long>): List<ClientDto.Snapshot>
}
