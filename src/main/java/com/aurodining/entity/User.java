package com.aurodining.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Mobile User Entity.
 * Login and verification code are by email (AWS SES). Phone is optional for contact.
 */
@Data
@Entity
@Table(name = "\"user\"")
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** Optional; used for contact. Login identity is email. */
    private String phone;

    /** Login identity; verification code sent via AWS SES. */
    private String email;

    // Map the database field 'id_number' to the Java field
    @Column(name = "id_number")
    private String idNumber;

    private String avatar;

    // Status: 0 Disabled, 1 Enabled
    private Integer status;

    private String password;
}
