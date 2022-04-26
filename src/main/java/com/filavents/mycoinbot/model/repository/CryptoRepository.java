package com.filavents.mycoinbot.model.repository;

import com.filavents.mycoinbot.model.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    @Query("select c from Crypto c where c.id = ?1")
    Crypto findByIdEquals(Long id);

    @Query("select c from Crypto c where c.name = ?1")
    Crypto findByNameEquals(String name);


}