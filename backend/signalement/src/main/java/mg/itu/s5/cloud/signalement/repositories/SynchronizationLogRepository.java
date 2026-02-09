package mg.itu.s5.cloud.signalement.repositories;

import mg.itu.s5.cloud.signalement.entities.SynchronizationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SynchronizationLogRepository extends JpaRepository<SynchronizationLog, Integer> {
    
    @Query("SELECT MAX(s.syncDate) FROM SynchronizationLog s WHERE s.tableName = :tableName AND s.syncType = :syncType")
    Optional<LocalDateTime> findLastSyncDateByTableNameAndSyncType(@Param("tableName") String tableName, @Param("syncType") String syncType);
    
    @Query("SELECT MAX(s.syncDate) FROM SynchronizationLog s WHERE s.syncType = :syncType")
    Optional<LocalDateTime> findLastSyncDateBySyncType(@Param("syncType") String syncType);
    
    Optional<SynchronizationLog> findTopByTableNameAndSyncTypeOrderBySyncDateDesc(String tableName, String syncType);
}
