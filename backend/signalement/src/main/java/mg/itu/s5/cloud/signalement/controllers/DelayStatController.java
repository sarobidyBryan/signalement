package mg.itu.s5.cloud.signalement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mg.itu.s5.cloud.signalement.dto.ReportDelayStat;
import mg.itu.s5.cloud.signalement.services.DelayStatService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delay-stats")
@Tag(name = "Delay Statistics", description = "API pour les statistiques de délais de traitement des signalements")
public class DelayStatController {

    @Autowired
    private DelayStatService delayStatService;

    /**
     * GET /api/delay-stats
     * Récupère les statistiques de délai pour tous les reports
     * Query params optionnels: companyId, reportId
     */
    @GetMapping
    @Operation(summary = "Get delay statistics", description = "Récupère les statistiques de délai avec filtres optionnels")
    public ResponseEntity<ApiResponse> getDelayStats(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer reportId) {
        
        try {
            List<ReportDelayStat> stats;
            
            if (reportId != null) {
                // Statistique pour un seul report
                ReportDelayStat stat = delayStatService.calculateDelayForReport(reportId);
                if (stat == null) {
                    return ResponseEntity.status(404)
                            .body(ApiResponse.error("NOT_FOUND", "Report non trouvé avec id=" + reportId));
                }
                stats = List.of(stat);
            } else if (companyId != null) {
                // Statistiques filtrées par entreprise
                stats = delayStatService.calculateDelayByCompany(companyId);
            } else {
                // Toutes les statistiques
                stats = delayStatService.calculateDelayForAllReports();
            }
            
            // Calculer les statistiques globales
            Map<String, Object> summary = delayStatService.calculateAverageDelay(stats);
            
            Map<String, Object> result = new HashMap<>();
            result.put("stats", stats);
            result.put("summary", summary);
            
            return ResponseEntity.ok(ApiResponse.success("delayStats", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "Erreur lors du calcul des délais: " + e.getMessage()));
        }
    }

    /**
     * GET /api/delay-stats/report/{id}
     * Récupère les statistiques de délai pour un report spécifique
     */
    @GetMapping("/report/{id}")
    @Operation(summary = "Get delay stat for one report", description = "Statistiques de délai pour un report spécifique")
    public ResponseEntity<ApiResponse> getDelayStatForReport(@PathVariable int id) {
        try {
            ReportDelayStat stat = delayStatService.calculateDelayForReport(id);
            
            if (stat == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("NOT_FOUND", "Report non trouvé avec id=" + id));
            }
            
            return ResponseEntity.ok(ApiResponse.success("delayStat", stat));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "Erreur lors du calcul du délai: " + e.getMessage()));
        }
    }

    /**
     * GET /api/delay-stats/company/{id}
     * Récupère les statistiques de délai pour tous les reports d'une entreprise
     */
    @GetMapping("/company/{id}")
    @Operation(summary = "Get delay stats for company", description = "Statistiques de délai pour tous les reports d'une entreprise")
    public ResponseEntity<ApiResponse> getDelayStatsByCompany(@PathVariable int id) {
        try {
            List<ReportDelayStat> stats = delayStatService.calculateDelayByCompany(id);
            Map<String, Object> summary = delayStatService.calculateAverageDelay(stats);
            
            Map<String, Object> result = new HashMap<>();
            result.put("stats", stats);
            result.put("summary", summary);
            result.put("companyId", id);
            
            return ResponseEntity.ok(ApiResponse.success("delayStats", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "Erreur lors du calcul des délais: " + e.getMessage()));
        }
    }

    /**
     * GET /api/delay-stats/summary
     * Récupère uniquement le résumé global (délai moyen, etc.)
     */
    @GetMapping("/summary")
    @Operation(summary = "Get global delay summary", description = "Résumé global des délais")
    public ResponseEntity<ApiResponse> getDelaySummary(
            @RequestParam(required = false) Integer companyId) {
        try {
            List<ReportDelayStat> stats;
            
            if (companyId != null) {
                stats = delayStatService.calculateDelayByCompany(companyId);
            } else {
                stats = delayStatService.calculateDelayForAllReports();
            }
            
            Map<String, Object> summary = delayStatService.calculateAverageDelay(stats);
            
            return ResponseEntity.ok(ApiResponse.success("summary", summary));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "Erreur lors du calcul du résumé: " + e.getMessage()));
        }
    }
}
