package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_attachments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id")
    private AttachmentCategoryMaster category;
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;
    @Column(name = "content_type", length = 120)
    private String contentType;
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    @Column(name = "version_number", nullable = false)
    @Builder.Default
    private int versionNumber = 1;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
