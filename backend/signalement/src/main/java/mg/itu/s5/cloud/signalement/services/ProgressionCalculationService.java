package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de calcul des progressions et pourcentages
 * Responsable de tous les calculs liés aux progressions des travaux
 */
@Service
public class ProgressionCalculationService {

    /**
     * Calcule le pourcentage de progression pour une progression donnée
     * @param treatedArea Surface traitée à cette date
     * @param totalArea Surface totale du rapport
     * @return Pourcentage arrondi à 2 décimales
     */
    public BigDecimal calculateProgressionPercentage(BigDecimal treatedArea, BigDecimal totalArea) {
        if (totalArea == null || totalArea.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (treatedArea == null) {
            return BigDecimal.ZERO;
        }
        
        return treatedArea
                .multiply(new BigDecimal("100"))
                .divide(totalArea, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule les totaux de progression pour une liste de progressions
     * @param progressions Liste des progressions
     * @param totalArea Surface totale du rapport
     * @return Map contenant totalTreatedArea et totalPercentage
     */
    public Map<String, Object> calculateProgressionTotals(List<ReportsAssignationProgress> progressions, BigDecimal totalArea) {
        Map<String, Object> totals = new HashMap<>();
        
        BigDecimal totalTreatedArea = BigDecimal.ZERO;
        
        for (ReportsAssignationProgress progress : progressions) {
            if (progress.getTreatedArea() != null) {
                totalTreatedArea = totalTreatedArea.add(progress.getTreatedArea());
            }
        }
        
        BigDecimal totalPercentage = calculateProgressionPercentage(totalTreatedArea, totalArea);
        
        totals.put("totalTreatedArea", totalTreatedArea);
        totals.put("totalPercentage", totalPercentage);
        
        return totals;
    }

    /**
     * Calcule le pourcentage cumulé jusqu'à une progression donnée
     * Utile pour voir l'évolution chronologique
     * @param progressions Liste ordonnée des progressions (par date)
     * @param currentProgressionIndex Index de la progression actuelle
     * @param totalArea Surface totale
     * @return Pourcentage cumulé
     */
    public BigDecimal calculateCumulativePercentage(List<ReportsAssignationProgress> progressions, 
                                                     int currentProgressionIndex, 
                                                     BigDecimal totalArea) {
        BigDecimal cumulativeTreatedArea = BigDecimal.ZERO;
        
        for (int i = 0; i <= currentProgressionIndex && i < progressions.size(); i++) {
            ReportsAssignationProgress progress = progressions.get(i);
            if (progress.getTreatedArea() != null) {
                cumulativeTreatedArea = cumulativeTreatedArea.add(progress.getTreatedArea());
            }
        }
        
        return calculateProgressionPercentage(cumulativeTreatedArea, totalArea);
    }

    /**
     * Vérifie si les travaux sont terminés (100% ou plus)
     * @param totalTreatedArea Surface totale traitée
     * @param totalArea Surface totale du rapport
     * @return true si terminé
     */
    public boolean isWorkCompleted(BigDecimal totalTreatedArea, BigDecimal totalArea) {
        if (totalArea == null || totalArea.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        if (totalTreatedArea == null) {
            return false;
        }
        
        return totalTreatedArea.compareTo(totalArea) >= 0;
    }

    /**
     * Calcule la surface restante à traiter
     * @param totalTreatedArea Surface totale déjà traitée
     * @param totalArea Surface totale du rapport
     * @return Surface restante
     */
    public BigDecimal calculateRemainingArea(BigDecimal totalTreatedArea, BigDecimal totalArea) {
        if (totalArea == null) {
            return BigDecimal.ZERO;
        }
        if (totalTreatedArea == null) {
            return totalArea;
        }
        
        BigDecimal remaining = totalArea.subtract(totalTreatedArea);
        return remaining.max(BigDecimal.ZERO); // Ne peut pas être négatif
    }
}
