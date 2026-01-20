package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportsAssignationProgressService {

    @Autowired
    private ReportsAssignationProgressRepository repository;

    @Autowired
    private ReportsAssignationService reportsAssignationService;

    @Autowired
    private ReportService reportService;

    public List<ReportsAssignationProgress> getAll() { return repository.findAll(); }
    public Optional<ReportsAssignationProgress> getById(int id) { return repository.findById(id); }
    public ReportsAssignationProgress save(ReportsAssignationProgress r) {
        if (r.getId() == 0) {
            r.setCreatedAt(java.time.LocalDateTime.now());
        }
        r.setUpdatedAt(java.time.LocalDateTime.now());
        return repository.save(r);
    }
    public void delete(int id) { repository.deleteById(id); }

    public java.math.BigDecimal sumTreatedAreaByReportId(int reportId) {
        java.math.BigDecimal v = repository.sumTreatedAreaByReportId(reportId);
        return v == null ? java.math.BigDecimal.ZERO : v;
    }

    public List<Map<String, Object>> getAllWithPercentage() {
        List<ReportsAssignationProgress> all = repository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportsAssignationProgress p : all) {
            result.add(createProgressMap(p));
        }
        return result;
    }

    public Optional<Map<String, Object>> getByIdWithPercentage(int id) {
        return repository.findById(id).map(this::createProgressMap);
    }

    public Map<String, Object> saveWithPercentage(ReportsAssignationProgress p) {
        // Validation: vérifier que la surface traitée ne dépasse pas la surface du rapport
        if (p.getReportsAssignation() == null) {
            throw new IllegalArgumentException("ID d'assignation requis");
        }

        Optional<Integer> reportIdOpt = reportsAssignationService.getReportIdByAssignationId(p.getReportsAssignation().getId());
        if (!reportIdOpt.isPresent()) {
            throw new IllegalArgumentException("Assignation non trouvée");
        }

        int reportId = reportIdOpt.get();
        Optional<mg.itu.s5.cloud.signalement.entities.Report> reportOpt = reportService.getReportById(reportId);
        if (!reportOpt.isPresent()) {
            throw new IllegalArgumentException("Rapport non trouvé");
        }

        BigDecimal reportArea = reportOpt.get().getArea();
        if (reportArea == null) reportArea = BigDecimal.ZERO;

        BigDecimal currentTreated = sumTreatedAreaByReportId(reportId);
        BigDecimal newTreated = p.getTreatedArea() != null ? p.getTreatedArea() : BigDecimal.ZERO;

        if (currentTreated.add(newTreated).compareTo(reportArea) > 0) {
            BigDecimal remaining = reportArea.subtract(currentTreated);
            throw new IllegalArgumentException("La surface traitée ne peut pas dépasser la surface restante (" + remaining + " m²)");
        }

        // Gestion des timestamps
        if (p.getId() == 0) {
            p.setCreatedAt(java.time.LocalDateTime.now());
        }

        ReportsAssignationProgress saved = repository.save(p);
        return createProgressMap(saved);
    }

    private Map<String, Object> createProgressMap(ReportsAssignationProgress p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("reportsAssignation", p.getReportsAssignation());
        map.put("treatedArea", p.getTreatedArea());
        map.put("comment", p.getComment());
        map.put("registrationDate", p.getRegistrationDate());

        // Compute percentage
        BigDecimal percentage = BigDecimal.ZERO;
        try {
            Optional<Integer> reportIdOpt = reportsAssignationService.getReportIdByAssignationId(p.getReportsAssignation().getId());
            if (reportIdOpt.isPresent()) {
                Optional<mg.itu.s5.cloud.signalement.entities.Report> reportOpt = reportService.getReportById(reportIdOpt.get());
                if (reportOpt.isPresent() && reportOpt.get().getArea() != null && reportOpt.get().getArea().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal treated = p.getTreatedArea() != null ? p.getTreatedArea() : BigDecimal.ZERO;
                    percentage = treated.divide(reportOpt.get().getArea(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
            }
        } catch (Exception e) {
            // Keep percentage as 0
        }
        map.put("percentage", percentage);
        return map;
    }

    public Optional<ReportsAssignationProgress> getByFirebaseId(String firebaseId) {
        return repository.findByFirebaseId(firebaseId);
    }

    public List<ReportsAssignationProgress> findModifiedSince(java.time.LocalDateTime since) {
        return repository.findModifiedSince(since);
    }
}
