package com.cropdeal.crop.repository;

import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.CropCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CropRepository extends JpaRepository<Crop, UUID> {

    List<Crop> findByFarmerId(UUID farmerId);

    List<Crop> findByCategory(CropCategory category);

    List<Crop> findByStatus(Crop.CropStatus status);

    @Query("SELECT c FROM Crop c WHERE c.status IN " +
            "('AVAILABLE', 'PARTIALLY_AVAILABLE') " +
            "AND c.availableQuantity > 0")
     List<Crop> findAllWithAvailableStock();
}