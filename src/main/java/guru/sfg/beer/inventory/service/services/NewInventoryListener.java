package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.beer.inventory.service.web.mappers.BeerInventoryMapper;
import guru.sfg.common.events.BeerDto;
import guru.sfg.common.events.NewInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewInventoryListener {

    private final BeerInventoryRepository inventoryRepository;
    private final BeerInventoryMapper mapper;

    @JmsListener(destination = JmsConfig.NEW_INVENTORY_QUEUE)
    private void createInventoryRecord(@Payload NewInventoryEvent event) {
        log.debug("Got inventory: {}", event.toString());

        BeerDto beerDto = event.getBeerDto();

        inventoryRepository.save(mapper.beerDtoToBeerInventory(beerDto));
    }
}
