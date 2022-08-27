package com.orm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.orm.annotation.Column;
import com.orm.annotation.Id;
import com.orm.annotation.Table;

import lombok.Data;

@Table("products")
@Data
public class Product {

    @Id
    private Integer id;

    private String name;

    private BigDecimal price;

    @Column("created_at")
    private LocalDateTime dc;
}
