package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "customer_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_code", unique = true, length = 60)
    private String customerCode;
    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;
    @Column(name = "customer_category", length = 60)
    private String customerCategory;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
