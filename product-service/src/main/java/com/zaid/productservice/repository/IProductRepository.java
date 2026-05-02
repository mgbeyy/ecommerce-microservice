package com.zaid.productservice.repository;

import com.zaid.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    // N+1 sorgu problemini engellemek için category ilişkisini join fetch ile tek sorguda çekiyoruz.
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);
}