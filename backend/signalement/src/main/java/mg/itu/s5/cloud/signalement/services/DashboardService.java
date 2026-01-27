package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.repositories.ReportRepository;
import mg.itu.s5.cloud.signalement.repositories.CompanyRepository;
import mg.itu.s5.cloud.signalement.repositories.UserRepository;
import mg.itu.s5.cloud.signalement.repositories.ReportsStatusRepository;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationRepository;
import mg.itu.s5.cloud.signalement.entities.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportsStatusRepository reportsStatusRepository;

    @Autowired
    private ReportsAssignationRepository reportsAssignationRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de signalements
        long totalReports = reportRepository.count();
        stats.put("totalReports", totalReports);

        // Nombre de signalements assignés
        long assignedReports = reportsAssignationRepository.count();
        stats.put("assignedReports", assignedReports);

        // Nombre de signalements en cours (avec status IN_PROGRESS)
        long inProgressReports = reportsStatusRepository.countByStatus_StatusCode("IN_PROGRESS");
        stats.put("inProgressReports", inProgressReports);

        // Nombre de signalements terminés (avec status COMPLETED ou VERIFIED)
        long completedReports = reportsStatusRepository.countByStatus_StatusCode("COMPLETED") 
            + reportsStatusRepository.countByStatus_StatusCode("VERIFIED");
        stats.put("completedReports", completedReports);

        // Nombre d'entreprises
        long totalCompanies = companyRepository.count();
        stats.put("totalCompanies", totalCompanies);

        // Nombre d'utilisateurs actifs
        long activeUsers = userRepository.count();
        stats.put("activeUsers", activeUsers);

        // Signalements en attente (PENDING)
        long pendingReports = reportsStatusRepository.countByStatus_StatusCode("PENDING");
        stats.put("pendingReports", pendingReports);

        return stats;
    }

    public List<Map<String, Object>> getRecentReports(int limit) {
        List<Report> reports = reportRepository.findAll()
            .stream()
            .sorted((r1, r2) -> r2.getReportDate().compareTo(r1.getReportDate()))
            .limit(limit)
            .collect(Collectors.toList());

        return reports.stream().map(report -> {
            Map<String, Object> reportMap = new HashMap<>();
            reportMap.put("id", report.getId());
            reportMap.put("description", report.getDescription());
            reportMap.put("reportDate", report.getReportDate());
            reportMap.put("latitude", report.getLatitude());
            reportMap.put("longitude", report.getLongitude());
            reportMap.put("area", report.getArea());
            reportMap.put("firebaseId", report.getFirebaseId());
            
            if (report.getUser() != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", report.getUser().getId());
                userMap.put("name", report.getUser().getName());
                userMap.put("email", report.getUser().getEmail());
                reportMap.put("user", userMap);
            }
            
            return reportMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Long> getReportsByStatus() {
        Map<String, Long> statusCounts = new HashMap<>();
        
        statusCounts.put("PENDING", reportsStatusRepository.countByStatus_StatusCode("PENDING"));
        statusCounts.put("IN_PROGRESS", reportsStatusRepository.countByStatus_StatusCode("IN_PROGRESS"));
        statusCounts.put("COMPLETED", reportsStatusRepository.countByStatus_StatusCode("COMPLETED"));
        statusCounts.put("VERIFIED", reportsStatusRepository.countByStatus_StatusCode("VERIFIED"));
        statusCounts.put("REJECTED", reportsStatusRepository.countByStatus_StatusCode("REJECTED"));
        
        return statusCounts;
    }

    public Map<String, Object> getDetailedDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        dashboard.put("stats", getDashboardStats());
        dashboard.put("recentReports", getRecentReports(10));
        dashboard.put("reportsByStatus", getReportsByStatus());
        
        return dashboard;
    }
}
