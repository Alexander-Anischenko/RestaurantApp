package de.ait.restaurantapp.services;

import de.ait.restaurantapp.model.RestaurantTable;

import java.util.List;
import java.util.Optional;

public interface RestaurantTableService {

    List<RestaurantTable> getAllTables();
    Optional<RestaurantTable> getTableById(Long id);
}
