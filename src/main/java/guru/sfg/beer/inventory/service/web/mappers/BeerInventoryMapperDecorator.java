package guru.sfg.beer.inventory.service.web.mappers;

import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.brewery.model.events.BeerDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerInventoryMapperDecorator implements BeerInventoryMapper {

    @Autowired
    private BeerInventoryMapper beerInventoryMapper;

    @Override
    public BeerInventory beerDtoToBeerInventory(BeerDto beerDto) {
        BeerInventory beerInventory = beerInventoryMapper.beerDtoToBeerInventory(beerDto);
        beerInventory.setBeerId(beerDto.getId());

        return beerInventory;
    }
}
