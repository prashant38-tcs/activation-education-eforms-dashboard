package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "import_batch_rows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImportBatchRow {
    public static final String NEW = "NEW";
    public static final String EXISTING_CHANGED = "EXISTING_CHANGED";
    public static final String EXISTING_UNCHANGED = "EXISTING_UNCHANGED";
    public static final String INVALID = "INVALID";
    public static final String DUPLICATE_IN_FILE = "DUPLICATE_IN_FILE";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "batch_id", nullable = false)
    private ImportBatch batch;
    @Column(name = "`row_number`", nullable = false)
    private int rowNumber;
    @Column(name = "ticket_number", length = 60)
    private String ticketNumber;
    @Column(name = "crm_id", length = 60)
    private String crmId;
    @Column(name = "raw_data_json", columnDefinition = "TEXT")
    private String rawDataJson;
    @Column(name = "row_classification", nullable = false, length = 30)
    private String rowClassification;
    @Column(name = "error_reason", length = 500)
    private String errorReason;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resulting_ticket_id")
    private Ticket resultingTicket;
    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;
}
