package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "activity_type_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityTypeMaster {
    public static final String FRONTEND_CHANGES = "FRONTEND_CHANGES";
    public static final String BACKEND_CHANGES = "BACKEND_CHANGES";
    public static final String DATABASE_CHANGES = "DATABASE_CHANGES";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "type_code", nullable = false, unique = true, length = 60)
    private String typeCode;
    @Column(name = "type_name", nullable = false, length = 120)
    private String typeName;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
