package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "attachment_category_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttachmentCategoryMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category_code", nullable = false, unique = true, length = 60)
    private String categoryCode;
    @Column(name = "category_name", nullable = false, length = 120)
    private String categoryName;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
