package com.filavents.mycoinbot.model;

import org.hibernate.Hibernate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "crypto", indexes = {
        @Index(name = "idx_crypto_price_name", columnList = "price, name")
})
public class Crypto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "crypto_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String name;

    @Column(updatable = false)
    private final Timestamp createdDate;

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getId() {
        return id;
    }

    public Crypto() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }


    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Crypto crypto = (Crypto) o;
        return id != null && Objects.equals(id, crypto.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "price = " + price + ", " +
                "name = " + name + ", " +
                "createdDate = " + createdDate + ")";
    }
}