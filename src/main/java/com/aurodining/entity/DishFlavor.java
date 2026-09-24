package com.aurodining.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Dish Flavor Entity
 */
@Data
@Entity
@Table(name = "dish_flavor")
@EntityListeners(AuditingEntityListener.class)
public class DishFlavor implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dish ID (Foreign Key logic)
    private Long dishId;

    // Flavor Name (e.g., "Spiciness")
    private String name;

    // Flavor Value (e.g., "Medium Spicy")
    private String value;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @CreatedBy
    private Long createUser;

    @LastModifiedBy
    private Long updateUser;

    private Integer isDeleted;
}