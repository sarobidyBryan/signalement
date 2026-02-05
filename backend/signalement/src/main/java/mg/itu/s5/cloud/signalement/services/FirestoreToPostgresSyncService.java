package mg.itu.s5.cloud.signalement.services;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import mg.itu.s5.cloud.signalement.entities.*;
import mg.itu.s5.cloud.signalement.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class FirestoreToPostgresSyncService {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreToPostgresSyncService.class);

    // Collections Firestore
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_REPORTS = "reports";

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
    private SynchronizationLogService syncLogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserStatusTypeRepository userStatusTypeRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ReportsStatusService reportsStatusService;

    /**
     * Synchronise toutes les tables depuis Firestore vers PostgreSQL
     */
    @Transactional
    public Map<String, Object> syncAllFromFirestore() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            results.put("users", syncUsersFromFirestore());
            results.put("reports", syncReportsFromFirestore());
            results.put("success", true);
            results.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Erreur lors de la synchronisation globale depuis Firestore", e);
            results.put("success", false);
            results.put("error", e.getMessage());
        }
        
        return results;
    }

    /**
     * Synchronise les utilisateurs depuis Firestore vers PostgreSQL
     */
    @Transactional
    public Map<String, Object> syncUsersFromFirestore() {
        String tableName = "users";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_FIREBASE_TO_POSTGRES);
        
        int synced = 0;
        int created = 0;
        int updated = 0;
        int total = 0;
        
        try {
            List<QueryDocumentSnapshot> documents = firestoreService.getAllDocuments(COLLECTION_USERS);
            total = documents.size();
            
            for (QueryDocumentSnapshot doc : documents) {
                try {
                    Map<String, Object> data = doc.getData();
                    
                    // Vérifier si le document a été modifié après le dernier sync
                    LocalDateTime docUpdatedAt = getDocumentUpdatedAt(data);
                    if (docUpdatedAt != null && docUpdatedAt.isBefore(lastSync)) {
                        continue; // Skip si pas modifié depuis le dernier sync
                    }
                    
                    // Chercher l'utilisateur existant par postgres_id ou firebase_uid
                    User existingUser = findExistingUser(data, doc.getId());
                    
                    if (existingUser != null) {
                        // Update
                        updateUserFromFirestore(existingUser, data, doc.getId());
                        userRepository.save(existingUser);
                        updated++;
                    } else {
                        // Create new user
                        User newUser = createUserFromFirestore(data, doc.getId());
                        if (newUser != null) {
                            userRepository.save(newUser);
                            created++;
                        }
                    }
                    synced++;
                } catch (Exception e) {
                    logger.error("Erreur sync user depuis Firestore doc={}", doc.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur récupération documents Firestore users", e);
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_FIREBASE_TO_POSTGRES);
        return createSyncResult(tableName, total, synced, created, updated);
    }

    /**
     * Synchronise les rapports depuis Firestore vers PostgreSQL
     * Lit uniquement les données de base du report (pas les assignations/progressions)
     */
    @Transactional
    public Map<String, Object> syncReportsFromFirestore() {
        String tableName = "reports";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_FIREBASE_TO_POSTGRES);
        
        int synced = 0;
        int created = 0;
        int updated = 0;
        int total = 0;
        
        try {
            List<QueryDocumentSnapshot> documents = firestoreService.getAllDocuments(COLLECTION_REPORTS);
            total = documents.size();
            
            for (QueryDocumentSnapshot doc : documents) {
                try {
                    Map<String, Object> data = doc.getData();
                    
                    // Vérifier si le document a été modifié après le dernier sync
                    LocalDateTime docUpdatedAt = getDocumentUpdatedAt(data);
                    if (docUpdatedAt != null && docUpdatedAt.isBefore(lastSync)) {
                        continue; // Skip si pas modifié depuis le dernier sync
                    }
                    
                    // Chercher le rapport existant par postgres_id ou firebase_id
                    Report existingReport = findExistingReport(data, doc.getId());
                    
                    if (existingReport != null) {
                        // Update
                        updateReportFromFirestore(existingReport, data, doc.getId());
                        reportRepository.save(existingReport);
                        updated++;
                    } else {
                        // Create new report
                        Report newReport = createReportFromFirestore(data, doc.getId());
                        if (newReport != null) {
                            Report savedReport = reportRepository.save(newReport);
                            
                            // Créer une entrée dans reports_status pour l'historique
                            try {
                                reportsStatusService.recordStatusHistory(
                                    savedReport.getId(),
                                    savedReport.getStatus().getId(),
                                    LocalDateTime.now()
                                );
                            } catch (Exception e) {
                                logger.error("Erreur lors de la création de l'entrée reports_status pour le rapport {}", savedReport.getId(), e);
                            }
                            
                            created++;
                        }
                    }
                    synced++;
                } catch (Exception e) {
                    logger.error("Erreur sync report depuis Firestore doc={}", doc.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur récupération documents Firestore reports", e);
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_FIREBASE_TO_POSTGRES);
        return createSyncResult(tableName, total, synced, created, updated);
    }

    // ===================== USER HELPERS =====================

    private User findExistingUser(Map<String, Object> data, String firebaseDocId) {
        // 1. Chercher par postgresId
        Object postgresIdObj = data.get("postgresId");
        if (postgresIdObj != null) {
            int postgresId = convertToInt(postgresIdObj);
            if (postgresId > 0) {
                Optional<User> user = userRepository.findById(postgresId);
                if (user.isPresent()) {
                    return user.get();
                }
            }
        }
        
        // 2. Chercher par firebase_uid (document ID)
        Optional<User> userByUid = userRepository.findByFirebaseUid(firebaseDocId);
        if (userByUid.isPresent()) {
            return userByUid.get();
        }
        
        // 3. Chercher par email (pour éviter les doublons)
        Object emailObj = data.get("email");
        if (emailObj != null) {
            Optional<User> userByEmail = userRepository.findByEmail(emailObj.toString());
            if (userByEmail.isPresent()) {
                return userByEmail.get();
            }
        }
        
        return null;
    }

    private User createUserFromFirestore(Map<String, Object> data, String firebaseDocId) {
        User user = new User();
        
        // Champs obligatoires
        user.setName(getStringValue(data, "name"));
        user.setEmail(getStringValue(data, "email"));
        user.setPassword(getStringValue(data, "password", "firebase_user")); // Default password
        user.setFirebaseUid(firebaseDocId);
        
        // Role
        Role role = getRoleFromData(data);
        if (role == null) {
            // Default role si non trouvé
            role = roleRepository.findByRoleCode("USER").orElse(null);
        }
        if (role == null) {
            logger.error("Role USER non trouvé, impossible de créer l'utilisateur");
            return null;
        }
        user.setRole(role);
        
        // User Status Type
        UserStatusType statusType = getUserStatusTypeFromData(data);
        if (statusType == null) {
            // Default status si non trouvé
            statusType = userStatusTypeRepository.findByStatusCode("ACTIVE").orElse(null);
        }
        if (statusType == null) {
            logger.error("UserStatusType ACTIVE non trouvé, impossible de créer l'utilisateur");
            return null;
        }
        user.setUserStatusType(statusType);
        
        // Dates
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return user;
    }

    private void updateUserFromFirestore(User user, Map<String, Object> data, String firebaseDocId) {
        // Mettre à jour les champs modifiables
        String name = getStringValue(data, "name");
        if (name != null) {
            user.setName(name);
        }
        
        String email = getStringValue(data, "email");
        if (email != null) {
            user.setEmail(email);
        }
        
        // Mettre à jour firebase_uid si nécessaire
        if (user.getFirebaseUid() == null || !user.getFirebaseUid().equals(firebaseDocId)) {
            user.setFirebaseUid(firebaseDocId);
        }
        
        // Role
        Role role = getRoleFromData(data);
        if (role != null) {
            user.setRole(role);
        }
        
        // User Status Type
        UserStatusType statusType = getUserStatusTypeFromData(data);
        if (statusType != null) {
            user.setUserStatusType(statusType);
        }
        
        user.setUpdatedAt(LocalDateTime.now());
    }

    private Role getRoleFromData(Map<String, Object> data) {
        Object roleObj = data.get("role");
        if (roleObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> roleMap = (Map<String, Object>) roleObj;
            
            // Chercher par id
            Object roleIdObj = roleMap.get("id");
            if (roleIdObj != null) {
                int roleId = convertToInt(roleIdObj);
                if (roleId > 0) {
                    return roleRepository.findById(roleId).orElse(null);
                }
            }
            
            // Chercher par roleCode
            Object roleCodeObj = roleMap.get("roleCode");
            if (roleCodeObj != null) {
                return roleRepository.findByRoleCode(roleCodeObj.toString()).orElse(null);
            }
        }
        return null;
    }

    private UserStatusType getUserStatusTypeFromData(Map<String, Object> data) {
        Object statusObj = data.get("userStatusType");
        if (statusObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statusMap = (Map<String, Object>) statusObj;
            
            // Chercher par id
            Object statusIdObj = statusMap.get("id");
            if (statusIdObj != null) {
                int statusId = convertToInt(statusIdObj);
                if (statusId > 0) {
                    return userStatusTypeRepository.findById(statusId).orElse(null);
                }
            }
            
            // Chercher par statusCode
            Object statusCodeObj = statusMap.get("statusCode");
            if (statusCodeObj != null) {
                return userStatusTypeRepository.findByStatusCode(statusCodeObj.toString()).orElse(null);
            }
        }
        return null;
    }

    // ===================== REPORT HELPERS =====================

    private Report findExistingReport(Map<String, Object> data, String firebaseDocId) {
        // 1. Chercher par postgresId
        Object postgresIdObj = data.get("postgresId");
        if (postgresIdObj != null) {
            int postgresId = convertToInt(postgresIdObj);
            if (postgresId > 0) {
                Optional<Report> report = reportRepository.findById(postgresId);
                if (report.isPresent()) {
                    return report.get();
                }
            }
        }
        
        // 2. Chercher par firebase_id
        Optional<Report> reportByFirebaseId = reportRepository.findByFirebaseId(firebaseDocId);
        if (reportByFirebaseId.isPresent()) {
            return reportByFirebaseId.get();
        }
        
        return null;
    }

    private Report createReportFromFirestore(Map<String, Object> data, String firebaseDocId) {
        Report report = new Report();
        
        // User
        User user = getUserFromData(data);
        if (user == null) {
            logger.error("User non trouvé pour le report, impossible de créer");
            return null;
        }
        report.setUser(user);
        
        // Status
        Status status = getStatusFromData(data);
        if (status == null) {
            // Default status si non trouvé
            status = statusRepository.findByStatusCode("SUBMITTED").orElse(null);
        }
        if (status == null) {
            logger.error("Status SUBMITTED non trouvé, impossible de créer le report");
            return null;
        }
        report.setStatus(status);
        
        // Autres champs
        report.setDescription(getStringValue(data, "description"));
        report.setArea(getBigDecimalValue(data, "area"));
        report.setLongitude(getBigDecimalValue(data, "longitude"));
        report.setLatitude(getBigDecimalValue(data, "latitude"));
        report.setReportDate(getLocalDateTimeValue(data, "reportDate"));
        report.setFirebaseId(firebaseDocId);
        
        // Dates
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        return report;
    }

    private void updateReportFromFirestore(Report report, Map<String, Object> data, String firebaseDocId) {
        // User (ne pas modifier l'utilisateur une fois créé)
        
        // Status
        Status status = getStatusFromData(data);
        if (status != null) {
            report.setStatus(status);
        }
        
        // Autres champs
        String description = getStringValue(data, "description");
        if (description != null) {
            report.setDescription(description);
        }
        
        BigDecimal area = getBigDecimalValue(data, "area");
        if (area != null) {
            report.setArea(area);
        }
        
        BigDecimal longitude = getBigDecimalValue(data, "longitude");
        if (longitude != null) {
            report.setLongitude(longitude);
        }
        
        BigDecimal latitude = getBigDecimalValue(data, "latitude");
        if (latitude != null) {
            report.setLatitude(latitude);
        }
        
        LocalDateTime reportDate = getLocalDateTimeValue(data, "reportDate");
        if (reportDate != null) {
            report.setReportDate(reportDate);
        }
        
        // Mettre à jour firebase_id si nécessaire
        if (report.getFirebaseId() == null || !report.getFirebaseId().equals(firebaseDocId)) {
            report.setFirebaseId(firebaseDocId);
        }
        
        report.setUpdatedAt(LocalDateTime.now());
    }

    private User getUserFromData(Map<String, Object> data) {
        Object userObj = data.get("user");
        if (userObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            
            // Chercher par id
            Object userIdObj = userMap.get("id");
            if (userIdObj != null) {
                int userId = convertToInt(userIdObj);
                if (userId > 0) {
                    return userRepository.findById(userId).orElse(null);
                }
            }
            
            // Chercher par email
            Object emailObj = userMap.get("email");
            if (emailObj != null) {
                return userRepository.findByEmail(emailObj.toString()).orElse(null);
            }
        }
        return null;
    }

    private Status getStatusFromData(Map<String, Object> data) {
        Object statusObj = data.get("status");
        if (statusObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statusMap = (Map<String, Object>) statusObj;
            
            // Chercher par id
            Object statusIdObj = statusMap.get("id");
            if (statusIdObj != null) {
                int statusId = convertToInt(statusIdObj);
                if (statusId > 0) {
                    return statusRepository.findById(statusId).orElse(null);
                }
            }
            
            // Chercher par statusCode
            Object statusCodeObj = statusMap.get("statusCode");
            if (statusCodeObj != null) {
                return statusRepository.findByStatusCode(statusCodeObj.toString()).orElse(null);
            }
        }
        return null;
    }

    // ===================== UTILITY METHODS =====================

    private LocalDateTime getDocumentUpdatedAt(Map<String, Object> data) {
        // Vérifier updated_at ou synced_at
        Object updatedAt = data.get("updated_at");
        if (updatedAt != null) {
            return convertToLocalDateTime(updatedAt);
        }
        
        Object syncedAt = data.get("synced_at");
        if (syncedAt != null) {
            return convertToLocalDateTime(syncedAt);
        }
        
        return null;
    }

    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) value;
            return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneId.systemDefault()
            );
        }
        
        if (value instanceof Date) {
            Date date = (Date) value;
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        
        if (value instanceof Long) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) value), ZoneId.systemDefault());
        }
        
        return null;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        return getStringValue(data, key, null);
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return convertToLocalDateTime(value);
    }

    private int convertToInt(Object value) {
        if (value == null) {
            return 0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Map<String, Object> createSyncResult(String tableName, int total, int synced, int created, int updated) {
        Map<String, Object> result = new HashMap<>();
        result.put("table", tableName);
        result.put("totalFirestoreDocs", total);
        result.put("synced", synced);
        result.put("created", created);
        result.put("updated", updated);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
}
