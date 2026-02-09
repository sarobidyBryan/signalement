package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.dto.ReportDelayStat;
import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import mg.itu.s5.cloud.signalement.repositories.ReportRepository;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationRepository;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DelayStatService {

    private static final Logger logger = LoggerFactory.getLogger(DelayStatService.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportsAssignationRepository assignationRepository;

    @Autowired
    private ReportsAssignationProgressRepository progressRepository;

    /**
     * Calcule le délai pour un seul report
     * Délai = temps entre le premier progress (treatedArea > 0) et:
     *   - Si terminé: dernier progress (treatedArea >= area)
     *   - Si en cours: date actuelle
     */
    public ReportDelayStat calculateDelayForReport(int reportId) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (!reportOpt.isPresent()) {
            return null;
        }

        Report report = reportOpt.get();
        
        // Récupérer l'assignation
        List<ReportsAssignation> assignations = assignationRepository.findByReport_Id(reportId);
        ReportsAssignation assignation = assignations.isEmpty() ? null : assignations.get(0);

        // Récupérer tous les progress triés par date
        List<ReportsAssignationProgress> progressList = progressRepository.findByReportsAssignation_Report_Id(reportId);
        progressList.sort(Comparator.comparing(ReportsAssignationProgress::getRegistrationDate));

        // Calculer le treatedArea total et le pourcentage
        BigDecimal totalTreatedArea = progressList.stream()
                .map(ReportsAssignationProgress::getTreatedArea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double progressPercentage = 0.0;
        if (report.getArea() != null && report.getArea().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = totalTreatedArea
                    .divide(report.getArea(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            progressPercentage = Math.min(100.0, progressPercentage);
        }

        // Calculer le délai
        Double delayInDays = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        boolean isCompleted = false;

        if (!progressList.isEmpty()) {
            // Date de début = premier progress
            startDate = progressList.get(0).getRegistrationDate();

            // Vérifier si le travail est terminé (treatedArea >= area)
            if (totalTreatedArea.compareTo(report.getArea()) >= 0) {
                // Terminé: date de fin = dernier progress
                endDate = progressList.get(progressList.size() - 1).getRegistrationDate();
                isCompleted = true;
            } else {
                // En cours: date de fin = maintenant
                endDate = LocalDateTime.now();
                isCompleted = false;
            }

            // Calculer le délai en jours
            if (startDate != null && endDate != null) {
                Duration duration = Duration.between(startDate, endDate);
                delayInDays = duration.toMinutes() / (60.0 * 24.0); // Conversion en jours
            }
        }

        // Construire le DTO
        ReportDelayStat stat = new ReportDelayStat();
        stat.setReportId(report.getId());
        stat.setDescription(report.getDescription());
        stat.setArea(report.getArea());
        stat.setLatitude(report.getLatitude());
        stat.setLongitude(report.getLongitude());
        
        if (assignation != null && assignation.getCompany() != null) {
            stat.setCompanyId(assignation.getCompany().getId());
            stat.setCompanyName(assignation.getCompany().getName());
        }
        
        if (report.getStatus() != null) {
            stat.setCurrentStatus(report.getStatus().getStatusCode());
            stat.setStatusLabel(report.getStatus().getLabel());
        }
        
        stat.setTreatedArea(totalTreatedArea);
        stat.setProgressPercentage(progressPercentage);
        stat.setDelayInDays(delayInDays);
        stat.setStartDate(startDate);
        stat.setEndDate(endDate);
        stat.setCompleted(isCompleted);

        return stat;
    }

    /**
     * Calcule les délais pour plusieurs reports
     */
    public List<ReportDelayStat> calculateDelayForReports(List<Integer> reportIds) {
        return reportIds.stream()
                .map(this::calculateDelayForReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les délais pour TOUS les reports
     */
    public List<ReportDelayStat> calculateDelayForAllReports() {
        List<Report> allReports = reportRepository.findAll();
        return allReports.stream()
                .map(r -> calculateDelayForReport(r.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les délais pour tous les reports, avec filtre par entreprise
     */
    public List<ReportDelayStat> calculateDelayByCompany(Integer companyId) {
        List<ReportDelayStat> allStats = calculateDelayForAllReports();
        
        if (companyId == null) {
            return allStats;
        }
        
        return allStats.stream()
                .filter(stat -> stat.getCompanyId() != null && stat.getCompanyId().equals(companyId))
                .collect(Collectors.toList());
    }

    /**
     * Calcule le délai moyen global pour une liste de reports
     */
    public Map<String, Object> calculateAverageDelay(List<ReportDelayStat> stats) {
        Map<String, Object> result = new HashMap<>();
        
        if (stats == null || stats.isEmpty()) {
            result.put("count", 0);
            result.put("averageDelayDays", null);
            result.put("completedCount", 0);
            result.put("inProgressCount", 0);
            return result;
        }

        // Filtrer les stats qui ont un délai (ont au moins un progress)
        List<ReportDelayStat> statsWithDelay = stats.stream()
                .filter(s -> s.getDelayInDays() != null)
                .collect(Collectors.toList());

        long completedCount = statsWithDelay.stream().filter(ReportDelayStat::isCompleted).count();
        long inProgressCount = statsWithDelay.stream().filter(s -> !s.isCompleted()).count();

        // Calculer la moyenne uniquement pour les reports terminés
        List<ReportDelayStat> completedStats = statsWithDelay.stream()
                .filter(ReportDelayStat::isCompleted)
                .collect(Collectors.toList());

        Double averageDelay = null;
        if (!completedStats.isEmpty()) {
            averageDelay = completedStats.stream()
                    .mapToDouble(ReportDelayStat::getDelayInDays)
                    .average()
                    .orElse(0.0);
        }

        result.put("count", stats.size());
        result.put("averageDelayDays", averageDelay);
        result.put("completedCount", completedCount);
        result.put("inProgressCount", inProgressCount);
        result.put("assignedCount", stats.size() - completedCount - inProgressCount); // Reports sans progress

        return result;
    }
}
