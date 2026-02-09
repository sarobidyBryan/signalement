package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.SynchronizationLog;
import mg.itu.s5.cloud.signalement.repositories.SynchronizationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SynchronizationLogService {

    public static final String SYNC_TYPE_POSTGRES_TO_FIREBASE = "POSTGRES_TO_FIREBASE";
    public static final String SYNC_TYPE_FIREBASE_TO_POSTGRES = "FIREBASE_TO_POSTGRES";

    @Autowired
    private SynchronizationLogRepository repository;

    public List<SynchronizationLog> getAll() {
        return repository.findAll();
    }

    public Optional<SynchronizationLog> getById(int id) {
        return repository.findById(id);
    }

    public SynchronizationLog save(SynchronizationLog log) {
        return repository.save(log);
    }

    public SynchronizationLog logSync(String tableName, int recordsSynced, String syncType) {
        SynchronizationLog log = new SynchronizationLog(LocalDateTime.now(), tableName, recordsSynced, syncType);
        return repository.save(log);
    }

    public Optional<LocalDateTime> getLastSyncDate(String tableName, String syncType) {
        return repository.findLastSyncDateByTableNameAndSyncType(tableName, syncType);
    }

    public Optional<LocalDateTime> getLastSyncDate(String syncType) {
        return repository.findLastSyncDateBySyncType(syncType);
    }

    public LocalDateTime getLastSyncDateOrDefault(String tableName, String syncType) {
        return getLastSyncDate(tableName, syncType)
                .orElse(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    }
}
