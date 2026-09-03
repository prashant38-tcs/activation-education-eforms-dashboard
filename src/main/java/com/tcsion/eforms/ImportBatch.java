package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_batches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImportBatch {
    public static final String PENDING_PREVIEW = "PENDING_PREVIEW";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    @Column(name = "stored_file_name")
    private String storedFileName;
    @Column(name = "checksum", length = 128)
    private String checksum;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
    @Column(name = "row_count") @Builder.Default private int rowCount = 0;
    @Column(name = "inserted_count") @Builder.Default private int insertedCount = 0;
    @Column(name = "updated_count") @Builder.Default private int updatedCount = 0;
    @Column(name = "rejected_count") @Builder.Default private int rejectedCount = 0;
    @Column(name = "duplicate_count") @Builder.Default private int duplicateCount = 0;
    @Column(name = "processing_status", nullable = false, length = 30)
    @Builder.Default
    private String processingStatus = PENDING_PREVIEW;
    @Column(name = "processing_result", length = 500)
    private String processingResult;
    @Column(name = "committed_at")
    private LocalDateTime committedAt;
}
