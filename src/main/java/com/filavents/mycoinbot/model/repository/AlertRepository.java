package com.filavents.mycoinbot.model.repository;

import com.filavents.mycoinbot.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("select c from Alert c where c.isAlerted = false and c.chatId = ?1")
    List<Alert> findAllActiveAlerts(long id);

    @Query("select c from Alert c where c.isAlerted = false and c.chatId = ?1 and c.price = ?2 and c.triggerCondition = ?3")
    List<Alert> findDuplicateActiveAlerts(long id, BigDecimal price, String triggerCondition);
}
