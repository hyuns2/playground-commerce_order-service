package io.playground.orderservice.application.saga.port.client

import io.playground.orderservice.application.saga.dto.ClientDto

interface InventoryClientPort {
    fun reserveStocks(orderExternalId: String, infos: List<ClientDto.ReservationRequest>)

    fun confirmStocks(orderExternalId: String)
}
