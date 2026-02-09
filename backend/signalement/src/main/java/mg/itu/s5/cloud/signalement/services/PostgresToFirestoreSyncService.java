package mg.itu.s5.cloud.signalement.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import mg.itu.s5.cloud.signalement.entities.*;
import mg.itu.s5.cloud.signalement.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class PostgresToFirestoreSyncService {

    private static final Logger logger = LoggerFactory.getLogger(PostgresToFirestoreSyncService.class);

    // Collections Firestore cibles
    public static final String COLLECTION_CONFIGURATIONS = "configurations";
    public static final String COLLECTION_COMPANIES = "companies";
    public static final String COLLECTION_STATUSES = "status";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_USER_TOKENS = "user_tokens";

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
    private SynchronizationLogService syncLogService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportsAssignationRepository reportsAssignationRepository;

    @Autowired
    private ReportsAssignationProgressRepository reportsAssignationProgressRepository;

    @Autowired
    private ProgressionCalculationService progressionCalculationService;

    @Autowired
    private UserTokenRepository userTokenRepository;

    /**
     * Synchronise toutes les tables vers Firestore
     */
    @Transactional
    public Map<String, Object> syncAllToFirestore() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            results.put("configurations", syncConfigurations());
            results.put("companies", syncCompanies());
            // statuses synchronization intentionally skipped
            results.put("users", syncUsers());
            results.put("userTokens", syncUserTokens());
            results.put("reports", syncReports());
            results.put("success", true);
            results.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Erreur lors de la synchronisation globale", e);
            results.put("success", false);
            results.put("error", e.getMessage());
        }
        
        return results;
    }

    /**
     * Synchronise les configurations
     */
    @Transactional
    public Map<String, Object> syncConfigurations() {
        String tableName = "configurations";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        
        List<Configuration> modifiedConfigs = configurationRepository.findModifiedSince(lastSync);
        int synced = 0;
        
        for (Configuration config : modifiedConfigs) {
            try {
                Map<String, Object> data = mapConfiguration(config);
                String firebaseId = syncToFirestore(COLLECTION_CONFIGURATIONS, config.getFirebaseId(), config.getId(), data);
                
                if (config.getFirebaseId() == null || !config.getFirebaseId().equals(firebaseId)) {
                    config.setFirebaseId(firebaseId);
                    configurationRepository.save(config);
                }
                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync configuration id={}", config.getId(), e);
            }
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        return createSyncResult(tableName, modifiedConfigs.size(), synced);
    }

    /**
     * Synchronise les entreprises
     */
    @Transactional
    public Map<String, Object> syncCompanies() {
        String tableName = "companies";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        
        List<Company> modifiedCompanies = companyRepository.findModifiedSince(lastSync);
        int synced = 0;
        
        for (Company company : modifiedCompanies) {
            try {
                Map<String, Object> data = mapCompany(company);
                String firebaseId = syncToFirestore(COLLECTION_COMPANIES, company.getFirebaseId(), company.getId(), data);
                
                if (company.getFirebaseId() == null || !company.getFirebaseId().equals(firebaseId)) {
                    company.setFirebaseId(firebaseId);
                    companyRepository.save(company);
                }
                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync company id={}", company.getId(), e);
            }
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        return createSyncResult(tableName, modifiedCompanies.size(), synced);
    }

    /**
     * Synchronise les statuts
     */
    @Transactional
    public Map<String, Object> syncStatuses() {
        String tableName = "status";
        
        // Note: status table has no updatedAt field, sync all statuses each time
        List<Status> modifiedStatuses = statusRepository.findAll();
        int synced = 0;
        
        for (Status status : modifiedStatuses) {
            try {
                Map<String, Object> data = mapStatus(status);
                String firebaseId = syncToFirestore(COLLECTION_STATUSES, null, status.getId(), data);
                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync status id={}", status.getId(), e);
            }
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        return createSyncResult(tableName, modifiedStatuses.size(), synced);
    }

    /**
     * Synchronise les utilisateurs
     * Crée les utilisateurs dans Firebase Authentication s'ils n'existent pas
     * Évite les duplications en utilisant l'email comme clé unique
     */
    @Transactional
    public Map<String, Object> syncUsers() {
        String tableName = "users";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);

        List<User> modifiedUsers = userRepository.findModifiedSince(lastSync);
        int synced = 0;
        int authCreated = 0;
        int authExisting = 0;

        for (User user : modifiedUsers) {
            try {
                // Obtenir ou créer l'utilisateur dans Firebase Authentication
                String firebaseUid = user.getFirebaseUid();

                if (firebaseUid == null || firebaseUid.isEmpty()) {
                    firebaseUid = createFirebaseAuthUser(user);
                    user.setFirebaseUid(firebaseUid);
                    userRepository.save(user);
                    authCreated++;
                    logger.info("Utilisateur créé dans Firebase Auth: {} (UID: {})", user.getEmail(), firebaseUid);
                } else {
                    // Vérifier que l'UID existe toujours dans Firebase Auth
                    try {
                        FirebaseAuth.getInstance().getUser(firebaseUid);
                        authExisting++;
                        logger.debug("Utilisateur existant confirmé dans Firebase Auth: {} (UID: {})", user.getEmail(), firebaseUid);
                    } catch (Exception e) {
                        // L'UID n'existe plus, recréer l'utilisateur
                        logger.warn("UID Firebase {} n'existe plus pour {}, recréation", firebaseUid, user.getEmail());
                        firebaseUid = createFirebaseAuthUser(user);
                        user.setFirebaseUid(firebaseUid);
                        userRepository.save(user);
                        authCreated++;
                        logger.info("Utilisateur recréé dans Firebase Auth: {} (nouvel UID: {})", user.getEmail(), firebaseUid);
                    }
                }

                // Synchroniser dans Firestore avec l'UID comme ID du document
                Map<String, Object> data = mapUser(user);
                String firebaseDocId = syncToFirestore(COLLECTION_USERS, firebaseUid, user.getId(), data);

                // Le document Firestore utilise l'UID Firebase comme ID
                if (!firebaseUid.equals(firebaseDocId)) {
                    logger.warn("Incohérence ID document Firestore pour user {}: attendu {}, obtenu {}", user.getEmail(), firebaseUid, firebaseDocId);
                }

                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync user id={}: {}", user.getId(), e.getMessage(), e);
            }
        }

        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        Map<String, Object> result = createSyncResult(tableName, modifiedUsers.size(), synced);
        result.put("authCreated", authCreated);
        result.put("authExisting", authExisting);
        return result;
    }

    /**
     * Synchronise les tokens utilisateurs
     */
    @Transactional
    public Map<String, Object> syncUserTokens() {
        String tableName = "user_tokens";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        
        List<UserToken> modifiedTokens = userTokenRepository.findModifiedSince(lastSync);
        int synced = 0;
        
        for (UserToken token : modifiedTokens) {
            try {
                Map<String, Object> data = mapUserToken(token);
                String firebaseId = syncToFirestore(COLLECTION_USER_TOKENS, null, token.getId(), data);
                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync user_token id={}", token.getId(), e);
            }
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        return createSyncResult(tableName, modifiedTokens.size(), synced);
    }

    /**
     * Synchronise les rapports avec leurs assignations et progressions imbriquées
     * Collection unique "reports" contenant:
     * - Données du report
     * - Assignation liée (avec son postgres_id)
     * - Progressions de l'assignation (chacune avec son postgres_id)
     */
    @Transactional
    public Map<String, Object> syncReports() {
        String tableName = "reports";
        LocalDateTime lastSync = syncLogService.getLastSyncDateOrDefault(tableName, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        
        List<Report> modifiedReports = reportRepository.findModifiedSince(lastSync);
        int synced = 0;
        
        for (Report report : modifiedReports) {
            try {
                // Créer le document complet avec assignation et progressions
                Map<String, Object> data = mapReportComplete(report);
                
                String firebaseId = syncToFirestore(COLLECTION_REPORTS, report.getFirebaseId(), report.getId(), data);
                
                if (report.getFirebaseId() == null || !report.getFirebaseId().equals(firebaseId)) {
                    report.setFirebaseId(firebaseId);
                    reportRepository.save(report);
                }
                synced++;
            } catch (Exception e) {
                logger.error("Erreur sync report id={}", report.getId(), e);
            }
        }
        
        syncLogService.logSync(tableName, synced, SynchronizationLogService.SYNC_TYPE_POSTGRES_TO_FIREBASE);
        return createSyncResult(tableName, modifiedReports.size(), synced);
    }

    // ===================== HELPERS =====================

    private String syncToFirestore(String collection, String existingFirebaseId, int postgresId, Map<String, Object> data) throws Exception {
        String firebaseId = existingFirebaseId;
        
        if (firebaseId == null || firebaseId.isEmpty()) {
            firebaseId = firestoreService.findDocumentIdByPostgresId(collection, postgresId);
        }
        
        data.put("syncedAt", new Date());
        
        return firestoreService.saveDocument(collection, firebaseId, data);
    }

    private Map<String, Object> createSyncResult(String tableName, int total, int synced) {
        Map<String, Object> result = new HashMap<>();
        result.put("table", tableName);
        result.put("totalModified", total);
        result.put("synced", synced);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    // ===================== MAPPERS COMPLETS =====================

    private Map<String, Object> mapConfiguration(Configuration config) {
        Map<String, Object> data = new HashMap<>();
        data.put("postgresId", config.getId());
        data.put("id", config.getId());
        data.put("key", config.getKey());
        data.put("value", config.getValue());
        data.put("type", config.getType());
        data.put("firebaseId", config.getFirebaseId());
        data.put("createdAt", toDate(config.getCreatedAt()));
        data.put("updatedAt", toDate(config.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapCompany(Company company) {
        Map<String, Object> data = new HashMap<>();
        data.put("postgresId", company.getId());
        data.put("id", company.getId());
        data.put("name", company.getName());
        data.put("email", company.getEmail());
        data.put("firebaseId", company.getFirebaseId());
        data.put("createdAt", toDate(company.getCreatedAt()));
        data.put("updatedAt", toDate(company.getUpdatedAt()));
        return data;
    }

    private Map<String, Object> mapStatus(Status status) {
        Map<String, Object> data = new HashMap<>();
        data.put("postgresId", status.getId());
        data.put("id", status.getId());
        data.put("statusCode", status.getStatusCode());
        data.put("label", status.getLabel());
        return data;
    }

    private Map<String, Object> mapRole(Role role) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", role.getId());
        data.put("roleCode", role.getRoleCode());
        data.put("label", role.getLabel());
        return data;
    }

    private Map<String, Object> mapUserStatusType(UserStatusType statusType) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", statusType.getId());
        data.put("statusCode", statusType.getStatusCode());
        data.put("label", statusType.getLabel());
        return data;
    }

    private Map<String, Object> mapUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("postgresId", user.getId());
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("password", user.getPassword());
        data.put("firebaseUid", user.getFirebaseUid());
        data.put("createdAt", toDate(user.getCreatedAt()));
        data.put("updatedAt", toDate(user.getUpdatedAt()));

        if (user.getRole() != null) {
            data.put("role", mapRole(user.getRole()));
        }

        if (user.getUserStatusType() != null) {
            data.put("userStatusType", mapUserStatusType(user.getUserStatusType()));
        }

        return data;
    }

    private Map<String, Object> mapUserToken(UserToken token) {
        Map<String, Object> data = new HashMap<>();
        data.put("postgresId", token.getId());
        data.put("id", token.getId());
        data.put("token", token.getToken());
        data.put("createdAt", toDate(token.getCreatedAt()));
        data.put("updatedAt", toDate(token.getUpdatedAt()));

        if (token.getUser() != null) {
            data.put("userId", token.getUser().getId());
            data.put("userEmail", token.getUser().getEmail());
            data.put("userName", token.getUser().getName());
        }

        return data;
    }

    /**
     * Mapper complet pour un report avec assignation et progressions globales
     * Structure:
     * {
     *   postgres_id: 1,
     *   id: 1,
     *   ...report fields...,
     *   user: { postgres_id, id, name, email, password, firebaseUid, role, userStatusType, ... },
     *   status: { id, statusCode, label },
     *   assignation: {
     *     postgres_id: 1,
     *     id: 1,
     *     budget: 5000000,
     *     startDate: "2024-01-15",
     *     deadline: "2024-03-15",
     *     firebaseId: "abc123",
     *     company: { postgres_id, id, name, email, ... }
     *   },
     *   progressions: {
     *     reportsAssignationId: 1,
     *     reportsAssignationFirebaseId: "abc123",
     *     totalTreatedArea: 120.50,
     *     totalPercentage: 80.33,
     *     remainingArea: 29.50,
     *     isCompleted: false,
     *     items: [
     *       { postgres_id: 1, id: 1, treatedArea: 50.00, percentage: 33.33, comment: "...", ... },
     *       { postgres_id: 2, id: 2, treatedArea: 70.50, percentage: 46.99, comment: "...", ... }
     *     ]
     *   }
     * }
     */
    private Map<String, Object> mapReportComplete(Report report) {
        Map<String, Object> data = new HashMap<>();
        
        // === REPORT (niveau racine) ===
        data.put("postgresId", report.getId());
        data.put("id", report.getId());
        data.put("reportDate", toDate(report.getReportDate()));
        data.put("area", report.getArea());
        data.put("longitude", report.getLongitude());
        data.put("latitude", report.getLatitude());
        data.put("description", report.getDescription());
        data.put("firebaseId", report.getFirebaseId());
        data.put("createdAt", toDate(report.getCreatedAt()));
        data.put("updatedAt", toDate(report.getUpdatedAt()));

        // User complet
        if (report.getUser() != null) {
            data.put("user", mapUser(report.getUser()));
        }

        // Status complet
        if (report.getStatus() != null) {
            data.put("status", mapStatus(report.getStatus()));
        }

        // === ASSIGNATION(S) (liée(s) au report) ===
        List<ReportsAssignation> assignations = reportsAssignationRepository.findByReport_Id(report.getId());
        
        if (!assignations.isEmpty()) {
            // Première assignation directement accessible (avec company complète)
            ReportsAssignation firstAssignation = assignations.get(0);
            data.put("assignation", mapAssignationBasic(firstAssignation));
            
            // === PROGRESSIONS GLOBALES (avec totaux calculés) ===
            data.put("progressions", mapProgressionsGlobal(firstAssignation, report.getArea()));
        } else {
            data.put("assignation", null);
            data.put("progressions", null);
        }

        return data;
    }

    /**
     * Mapper basique pour une assignation (sans progressions, avec company)
     */
    private Map<String, Object> mapAssignationBasic(ReportsAssignation assignation) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("postgresId", assignation.getId());
        data.put("id", assignation.getId());
        data.put("budget", assignation.getBudget());
        data.put("startDate", assignation.getStartDate() != null ? assignation.getStartDate().toString() : null);
        data.put("deadline", assignation.getDeadline() != null ? assignation.getDeadline().toString() : null);
        data.put("firebaseId", assignation.getFirebaseId());
        data.put("createdAt", toDate(assignation.getCreatedAt()));
        data.put("updatedAt", toDate(assignation.getUpdatedAt()));

        // Company complète
        if (assignation.getCompany() != null) {
            data.put("company", mapCompany(assignation.getCompany()));
        }

        return data;
    }

    /**
     * Mapper pour les progressions globales avec calculs
     */
    private Map<String, Object> mapProgressionsGlobal(ReportsAssignation assignation, BigDecimal totalArea) {
        Map<String, Object> data = new HashMap<>();
        
        // IDs de référence
        data.put("reportsAssignationId", assignation.getId());
        data.put("reportsAssignationFirebaseId", assignation.getFirebaseId());
        
        // Récupérer toutes les progressions
        List<ReportsAssignationProgress> progressions = reportsAssignationProgressRepository
                .findByReportsAssignation_Id(assignation.getId());
        
        // Calculer les totaux
        Map<String, Object> totals = progressionCalculationService.calculateProgressionTotals(progressions, totalArea);
        data.put("totalTreatedArea", totals.get("totalTreatedArea"));
        data.put("totalPercentage", totals.get("totalPercentage"));
        
        // Calculer la surface restante
        BigDecimal totalTreatedArea = (BigDecimal) totals.get("totalTreatedArea");
        data.put("remainingArea", progressionCalculationService.calculateRemainingArea(totalTreatedArea, totalArea));
        data.put("isCompleted", progressionCalculationService.isWorkCompleted(totalTreatedArea, totalArea));
        
        // Mapper chaque progression avec son pourcentage
        List<Map<String, Object>> progressionItems = new ArrayList<>();
        for (ReportsAssignationProgress progress : progressions) {
            progressionItems.add(mapProgressionWithPercentage(progress, totalArea));
        }
        data.put("items", progressionItems);
        
        return data;
    }

    /**
     * Mapper pour une progression individuelle avec pourcentage calculé
     */
    private Map<String, Object> mapProgressionWithPercentage(ReportsAssignationProgress progress, BigDecimal totalArea) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("postgresId", progress.getId());
        data.put("id", progress.getId());
        data.put("treatedArea", progress.getTreatedArea());
        
        // Calcul du pourcentage pour cette progression
        BigDecimal percentage = progressionCalculationService.calculateProgressionPercentage(
                progress.getTreatedArea(), totalArea);
        data.put("percentage", percentage);
        
        data.put("comment", progress.getComment());
        data.put("registrationDate", toDate(progress.getRegistrationDate()));
        data.put("firebaseId", progress.getFirebaseId());
        data.put("createdAt", toDate(progress.getCreatedAt()));
        data.put("updatedAt", toDate(progress.getUpdatedAt()));

        return data;
    }

    /**
     * Crée un utilisateur dans Firebase Authentication
     * @param user L'utilisateur PostgreSQL à créer dans Firebase Auth
     * @return L'UID Firebase de l'utilisateur créé ou existant
     */
    private String createFirebaseAuthUser(User user) throws Exception {
        try {
            // Vérifier si l'utilisateur existe déjà par email
            try {
                UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(user.getEmail());
                logger.info("Utilisateur déjà existant dans Firebase Auth: {} (UID: {})", user.getEmail(), existingUser.getUid());
                return existingUser.getUid();
            } catch (Exception e) {
                // L'utilisateur n'existe pas, on peut le créer
                logger.debug("Utilisateur {} n'existe pas encore dans Firebase Auth, création en cours", user.getEmail());
            }

            // Créer l'utilisateur dans Firebase Auth
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setPassword(user.getPassword()) // Le mot de passe hashé de PostgreSQL
                    .setDisplayName(user.getName())
                    .setEmailVerified(false); // Par défaut, non vérifié

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            logger.info("Utilisateur Firebase Auth créé avec succès: {} (UID: {})", user.getEmail(), userRecord.getUid());
            return userRecord.getUid();

        } catch (Exception e) {
            logger.error("Erreur lors de la création/récupération de l'utilisateur Firebase Auth: {}", user.getEmail(), e);
            throw e;
        }
    }

    private Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
