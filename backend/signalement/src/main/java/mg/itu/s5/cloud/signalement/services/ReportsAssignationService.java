package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.entities.Status;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportsAssignationService {

    @Autowired
    private ReportsAssignationRepository repository;

    @Autowired
    private StatusService statusService;

    @Autowired
    private ReportsStatusService reportsStatusService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ConfigurationService configurationService;

    public List<ReportsAssignation> getAll() { return repository.findAll(); }
    public Optional<ReportsAssignation> getById(int id) { return repository.findById(id); }

    public ReportsAssignation save(ReportsAssignation r) {
        boolean isNewAssignation = (r.getId() == 0);
        if (isNewAssignation) {
            r.setCreatedAt(LocalDateTime.now());
        }

        // Auto-calculate budget = price_m2 * niveau * area
        try {
            int reportId = 0;
            if (r.getReport() != null) {
                reportId = r.getReport().getId();
            }
            if (reportId > 0) {
                Optional<Report> reportOpt = reportService.getReportById(reportId);
                if (reportOpt.isPresent()) {
                    Report report = reportOpt.get();
                    BigDecimal area = report.getArea();
                    Integer niveau = report.getNiveau();
                    String priceM2Str = configurationService.getConfigurationValue("price_m2");

                    if (area != null && niveau != null && priceM2Str != null) {
                        BigDecimal priceM2 = new BigDecimal(priceM2Str);
                        BigDecimal budget = priceM2.multiply(BigDecimal.valueOf(niveau)).multiply(area);
                        r.setBudget(budget);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul automatique du budget: " + e.getMessage());
        }

        ReportsAssignation saved = repository.save(r);
        
        if (isNewAssignation) {
            try {
                int reportId = 0;
                if (saved.getReport() != null) {
                    reportId = saved.getReport().getId();
                    System.out.println("UNNNNN");
                } else if (r.getReport() != null) {
                    reportId = r.getReport().getId();
                    System.out.println("DEUX");
                }

                if (reportId > 0) {
                    Optional<Status> assignedStatusOpt = statusService.getStatusByStatusCode("ASSIGNED");
                    if (assignedStatusOpt.isPresent()) {
                        Optional<Report> reportOpt = reportService.getReportById(reportId);
                        if (reportOpt.isPresent()) {
                            Report report = reportOpt.get();
                            report.setStatus(assignedStatusOpt.get());
                            reportService.saveReport(report);

                            reportsStatusService.addStatusToReport(
                                report.getId(),
                                assignedStatusOpt.get().getId(),
                                LocalDateTime.now()
                            );
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
            }
        }
        
        return saved;
    }
    public void delete(int id) { repository.deleteById(id); }

    public List<ReportsAssignation> findByReportId(int reportId) {
        return repository.findByReport_Id(reportId);
    }

    public List<ReportsAssignation> findByReportIdWithFilters(int reportId, java.time.LocalDate startDate, java.math.BigDecimal budgetMin, java.math.BigDecimal budgetMax) {
        return repository.findByReportIdWithFilters(reportId, startDate, budgetMin, budgetMax);
    }

    public java.math.BigDecimal sumBudgetByReportId(int reportId) {
        java.math.BigDecimal v = repository.sumBudgetByReportId(reportId);
        return v == null ? java.math.BigDecimal.ZERO : v;
    }

    public Optional<Integer> getReportIdByAssignationId(int assignationId) {
        return getById(assignationId).map(a -> a.getReport().getId());
    }

    public Optional<ReportsAssignation> getByFirebaseId(String firebaseId) {
        return repository.findByFirebaseId(firebaseId);
    }

    public List<ReportsAssignation> findModifiedSince(LocalDateTime since) {
        return repository.findModifiedSince(since);
    }
}

