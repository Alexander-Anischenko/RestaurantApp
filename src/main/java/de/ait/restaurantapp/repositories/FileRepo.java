package de.ait.restaurantapp.repositories;

import de.ait.restaurantapp.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepo extends JpaRepository<FileEntity, Long> {
}
