package com.sfmc.inventory_service.repository;

import com.sfmc.inventory_service.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByWarehouseId(Long warehouseId);

    List<Stock> findByProductId(Long productId);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
