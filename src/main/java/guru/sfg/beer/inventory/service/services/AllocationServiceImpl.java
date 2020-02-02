package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating orderId: {}", beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLine -> {
            int orderQuantity = beerOrderLine.getOrderQuantity() != null ? beerOrderLine.getOrderQuantity() : 0;
            int quantityAllocated = beerOrderLine.getQuantityAllocated() != null ? beerOrderLine.getQuantityAllocated() : 0;

            if ((orderQuantity - quantityAllocated) > 0) {
                allocateBeerOrderLine(beerOrderLine);
            }

            totalOrdered.set(totalOrdered.get() + orderQuantity);
            totalAllocated.set(totalAllocated.get() + quantityAllocated);
        });

        log.debug("Total Ordered: {} Total Allocated: {}", totalOrdered.get(), totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLine) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLine.getUpc());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = beerInventory.getQuantityOnHand() != null ? beerInventory.getQuantityOnHand() : 0;
            int orderedQuantity = beerOrderLine.getOrderQuantity() != null ? beerOrderLine.getOrderQuantity() : 0;
            int allocatedQuantity = beerOrderLine.getQuantityAllocated() != null ? beerOrderLine.getQuantityAllocated() : 0;
            int quantityToAllocate = orderedQuantity - allocatedQuantity;

            if (inventory >= quantityToAllocate) { //full allocation
                inventory = inventory - quantityToAllocate;
                beerOrderLine.setQuantityAllocated(orderedQuantity);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) { //partial allocation
                beerOrderLine.setQuantityAllocated(allocatedQuantity + inventory);
                beerInventory.setQuantityOnHand(0);

                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
