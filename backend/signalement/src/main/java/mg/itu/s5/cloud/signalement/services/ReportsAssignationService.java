package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignation;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportsAssignationService {

    @Autowired
    private ReportsAssignationRepository repository;

    public List<ReportsAssignation> getAll() { return repository.findAll(); }
    public Optional<ReportsAssignation> getById(int id) { return repository.findById(id); }
    public ReportsAssignation save(ReportsAssignation r) { return repository.save(r); }
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
}

