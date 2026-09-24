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
 * Address Book Entity
 */
@Data
@Entity
@Table(name = "address_book")
@EntityListeners(AuditingEntityListener.class)
public class AddressBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User ID
    private Long userId;

    // Consignee Name (Receiver)
    private String consignee;

    // Phone Number
    private String phone;

    // Street Address (e.g., "123 Main St, Apt 4B")
    private String streetAddress;

    // City
    private String city;

    // State (2-letter abbreviation, e.g., "CA", "NY", "TX")
    private String state;

    // ZIP Code (5-digit or 9-digit format)
    private String zipCode;

    // Label (e.g., Home, Company)
    private String label;

    // Is Default Address: 0 No, 1 Yes
    private Integer isDefault = 0;

    // Creation Time
    @CreatedDate
    private LocalDateTime createTime;

    // Update Time
    @LastModifiedDate
    private LocalDateTime updateTime;

    // Creator User ID
    @CreatedBy
    private Long createUser;

    // Updater User ID
    @LastModifiedBy
    private Long updateUser;

    // Logic Delete Status (0: not deleted, 1: deleted)
    // Note: JPA uses physical deletion by default, this is kept for compatibility
    private Integer isDeleted = 0;
}