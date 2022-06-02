package com.filavents.mycoinbot.model.repository;

import com.filavents.mycoinbot.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

}
