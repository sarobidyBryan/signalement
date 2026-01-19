package mg.itu.s5.cloud.signalement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.repositories.ReportsStatusRepository;
import java.math.*;
import java.util.*;

@Service
public class ReportsSummaryService {

    @Autowired
    private ReportsStatusRepository reportsStatusRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportsAssignationService reportsAssignationService;

    @Autowired
    private ReportsAssignationProgressService reportsAssignationProgressService;

    public Map<String, Object> getSummaryForStatus(String statusCode) {
        List<Integer> reportIds = reportsStatusRepository.findReportIdsWithLatestStatus(statusCode);

        List<Map<String, Object>> reports = new ArrayList<>();

        int nb = 0;
        BigDecimal totalSurface = BigDecimal.ZERO;
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalTreated = BigDecimal.ZERO;

        for (Integer rid : reportIds) {
            Optional<Report> opt = reportService.getReportById(rid);
            if (opt.isPresent()) {
                Report report = opt.get();
                Map<String, Object> rmap = new HashMap<>();
                rmap.put("id", report.getId());
                rmap.put("description", report.getDescription());
                BigDecimal area = report.getArea() != null ? report.getArea() : BigDecimal.ZERO;
                rmap.put("area", area);
                BigDecimal budget = reportsAssignationService.sumBudgetByReportId(rid);
                rmap.put("budget", budget);
                BigDecimal treated = reportsAssignationProgressService.sumTreatedAreaByReportId(rid);
                if (treated == null) treated = BigDecimal.ZERO;
                rmap.put("treatedArea", treated);
                int percent = 0;
                if (area != null && area.compareTo(BigDecimal.ZERO) > 0) {
                    percent = treated.multiply(BigDecimal.valueOf(100)).divide(area, 0, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100)).intValue();
                }
                rmap.put("progressPercent", percent);
                reports.add(rmap);

                // accumulate
                nb++;
                totalSurface = totalSurface.add(area);
                totalBudget = totalBudget.add(budget != null ? budget : BigDecimal.ZERO);
                totalTreated = totalTreated.add(treated != null ? treated : BigDecimal.ZERO);
            }
        }

        int overallPercent = 0;
        if (totalSurface.compareTo(BigDecimal.ZERO) > 0) {
            overallPercent = totalTreated.multiply(BigDecimal.valueOf(100)).divide(totalSurface, 0, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100)).intValue();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("nbPoints", nb);
        summary.put("totalSurface", totalSurface);
        summary.put("totalBudget", totalBudget);
        summary.put("overallProgressPercent", overallPercent);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("reports", reports);
        return result;
    }
}
