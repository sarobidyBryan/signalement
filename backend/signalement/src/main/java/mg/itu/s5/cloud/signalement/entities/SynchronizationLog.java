package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "synchronization_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class SynchronizationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "records_synced")
    private int recordsSynced;

    @Column(name = "sync_type", length = 50)
    private String syncType; // POSTGRES_TO_FIREBASE, FIREBASE_TO_POSTGRES

    public SynchronizationLog() {}

    public SynchronizationLog(LocalDateTime syncDate, String tableName, int recordsSynced, String syncType) {
        this.syncDate = syncDate;
        this.tableName = tableName;
        this.recordsSynced = recordsSynced;
        this.syncType = syncType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getSyncDate() { return syncDate; }
    public void setSyncDate(LocalDateTime syncDate) { this.syncDate = syncDate; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public int getRecordsSynced() { return recordsSynced; }
    public void setRecordsSynced(int recordsSynced) { this.recordsSynced = recordsSynced; }

    public String getSyncType() { return syncType; }
    public void setSyncType(String syncType) { this.syncType = syncType; }
}
