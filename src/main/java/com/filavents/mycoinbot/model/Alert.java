package com.filavents.mycoinbot.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_price_alert", columnList = "chatId, isAlerted")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "alert_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String chatId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String triggerCondition;

    @Column(nullable = false)
    private boolean isAlerted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public boolean isAlerted() {
        return isAlerted;
    }

    public void setAlerted(boolean alerted) {
        isAlerted = alerted;
    }
}
