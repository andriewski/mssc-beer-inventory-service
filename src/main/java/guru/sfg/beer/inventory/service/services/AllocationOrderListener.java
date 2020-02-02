package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocationOrderRequest;
import guru.sfg.brewery.model.events.AllocationOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationOrderListener {

    private final JmsTemplate jmsTemplate;
    private final AllocationService allocationService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    private void allocateOrder(@Payload AllocationOrderRequest request) {
        log.debug("Get allocation request {}", request);

        BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        try {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE,
                    new AllocationOrderResult(beerOrderDto, false, !allocationService.allocateOrder(beerOrderDto)));
        } catch (Exception e) {
            log.error("Allocation failed for Order Id: {}", request.getBeerOrderDto().getId());

            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE,
                    new AllocationOrderResult(beerOrderDto, true, false));
        }
    }
}
