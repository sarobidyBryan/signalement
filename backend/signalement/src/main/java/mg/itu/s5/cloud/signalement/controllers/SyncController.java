package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.services.FirestoreToPostgresSyncService;
import mg.itu.s5.cloud.signalement.services.PostgresToFirestoreSyncService;
import mg.itu.s5.cloud.signalement.services.SynchronizationLogService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Synchronization", description = "API for PostgreSQL <-> Firestore bidirectional synchronization")
public class SyncController {

    @Autowired
    private PostgresToFirestoreSyncService postgresToFirestoreSyncService;

    @Autowired
    private FirestoreToPostgresSyncService firestoreToPostgresSyncService;

    @Autowired
    private SynchronizationLogService syncLogService;

    // ===================== POSTGRES TO FIRESTORE =====================

    /**
     * Synchronisation complète Postgres vers Firebase
     * Crée les utilisateurs dans Firebase Auth et synchronise toutes les données
     */
    @PostMapping("/postgres-to-firebase")
    @Operation(summary = "Sync complet Postgres vers Firebase", description = "Synchronise toutes les données de PostgreSQL vers Firebase (crée les utilisateurs dans Auth, synchronise Firestore)")
    public ResponseEntity<ApiResponse> syncPostgresToFirebase() {
        try {
            Map<String, Object> results = postgresToFirestoreSyncService.syncAllToFirestore();
            return ResponseEntity.ok(ApiResponse.success("syncResults", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Sync Postgres vers Firebase échoué: " + e.getMessage()));
        }
    }

    // ===================== FIRESTORE TO POSTGRES =====================

    @PostMapping("/firebase-to-postgres")
    @Operation(summary = "Sync complet Firebase vers Postgres", description = "Synchronise toutes les données de Firebase vers PostgreSQL")
    public ResponseEntity<ApiResponse> syncFirebaseToPostgres() {
        try {
            Map<String, Object> results = firestoreToPostgresSyncService.syncAllFromFirestore();
            return ResponseEntity.ok(ApiResponse.success("syncResults", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Sync Firebase vers Postgres échoué: " + e.getMessage()));
        }
    }

    // ===================== BIDIRECTIONAL SYNC =====================

    @PostMapping("/bidirectional")
    @Operation(summary = "Sync bidirectionnel complet", description = "Effectue une synchronisation bidirectionnelle complète: Postgres → Firebase puis Firebase → Postgres")
    public ResponseEntity<ApiResponse> syncBidirectional() {
        try {
            Map<String, Object> results = new HashMap<>();
            results.put("postgresToFirebase", postgresToFirestoreSyncService.syncAllToFirestore());
            results.put("firebaseToPostgres", firestoreToPostgresSyncService.syncAllFromFirestore());
            results.put("success", true);
            return ResponseEntity.ok(ApiResponse.success("syncResults", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ApiResponse.ErrorCodes.INTERNAL_ERROR, "Sync bidirectionnel échoué: " + e.getMessage()));
        }
    }

    @GetMapping("/logs")
    @Operation(summary = "Get sync logs", description = "Retrieves all synchronization logs")
    public ResponseEntity<ApiResponse> getSyncLogs() {
        return ResponseEntity.ok(ApiResponse.success("logs", syncLogService.getAll()));
    }
}
