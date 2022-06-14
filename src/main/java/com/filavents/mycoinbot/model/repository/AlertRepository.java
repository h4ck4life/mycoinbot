package com.filavents.mycoinbot.model.repository;

import com.filavents.mycoinbot.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("select c from Alert c where c.isAlerted = false and c.chatId = ?1")
    List<Alert> findAllActiveAlertsByChatId(long id);

    @Query("select c from Alert c where c.isAlerted = false")
    List<Alert> findAllActiveAlerts();

    @Query("select c from Alert c where c.isAlerted = false and c.chatId = ?1 and c.price = ?2 and c.triggerCondition = ?3")
    List<Alert> findDuplicateActiveAlerts(long id, BigDecimal price, String triggerCondition);

    @Transactional
    @Modifying
    @Query("update Alert c set c.isAlerted = true where c.chatId = ?1")
    void clearAllAlertsByChatId(long id);
}
