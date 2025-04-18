package de.ait.restaurantapp.services;

import de.ait.restaurantapp.model.RestaurantTable;
import de.ait.restaurantapp.repositories.RestaurantTableRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RestaurantTableServiceImpl implements RestaurantTableService {

    // Edit application.properties restaurant.table-count for change table count
    @Value("${restaurant.table-count:0}")
    private int tableCount;

    private final RestaurantTableRepo restaurantTableRepo;

    public RestaurantTableServiceImpl(RestaurantTableRepo restaurantTableRepo) {
        this.restaurantTableRepo = restaurantTableRepo;
    }

    @PostConstruct // to run fixed table count before dependency injection. it initializes our tables in database
    public void initTables() {
        if(restaurantTableRepo.count() == 0) {
            List<RestaurantTable> tables = IntStream
                    .rangeClosed(1, tableCount).mapToObj(count -> RestaurantTable.builder()
                            .capacity(count)
                            .build())
                    .collect(Collectors.toList());

            restaurantTableRepo.saveAll(tables);
        }
    }

    @Override
    public Optional<RestaurantTable> getTableById(Long id) {
        return restaurantTableRepo.findById(id);
    }

    @Override
    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepo.findAll();
    }
}
