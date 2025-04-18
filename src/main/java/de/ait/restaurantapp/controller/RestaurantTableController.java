package de.ait.restaurantapp.controller;

import de.ait.restaurantapp.model.RestaurantTable;
import de.ait.restaurantapp.services.RestaurantTableServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
public class пRestaurantTableController {

    private final RestaurantTableServiceImpl restaurantTableServiceImpl;

    public RestaurantTableController(RestaurantTableServiceImpl restaurantTableServiceImpl) {
        this.restaurantTableServiceImpl = restaurantTableServiceImpl;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(restaurantTableServiceImpl.getAllTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTable> getTableById(@PathVariable Long id) {
        return restaurantTableServiceImpl.getTableById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RestaurantTable> addTable(@RequestParam int capacity) {
        RestaurantTable newTable = restaurantTableServiceImpl.addTable(capacity);
        return ResponseEntity.ok(newTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTable> updateTable(@PathVariable Long id,
                                                       @RequestParam int capacity) {
        Optional<RestaurantTable> updatedTable = restaurantTableServiceImpl.changeTableCapacityById(id, capacity);
        return updatedTable
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        restaurantTableServiceImpl.deleteTableById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTables() {
        restaurantTableServiceImpl.deleteAllTables();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/init")
    public ResponseEntity<List<RestaurantTable>> initTables(@RequestParam int count) {
        return ResponseEntity.ok(restaurantTableServiceImpl.initTables(count));
    }
}