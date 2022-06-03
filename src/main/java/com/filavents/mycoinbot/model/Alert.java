package com.filavents.mycoinbot.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_price_alert", columnList = "chatId, isAlerted")
})
public class Alert {

    public Alert() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "alert_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private long chatId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String triggerCondition;

    @Column(nullable = false)
    private boolean isAlerted;

    @Column(updatable = false)
    private final Timestamp createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setChatId(long chatId) {
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
