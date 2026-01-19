package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.ReportsAssignationProgress;
import mg.itu.s5.cloud.signalement.repositories.ReportsAssignationProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportsAssignationProgressService {

    @Autowired
    private ReportsAssignationProgressRepository repository;

    public List<ReportsAssignationProgress> getAll() { return repository.findAll(); }
    public Optional<ReportsAssignationProgress> getById(int id) { return repository.findById(id); }
    public ReportsAssignationProgress save(ReportsAssignationProgress r) { return repository.save(r); }
    public void delete(int id) { repository.deleteById(id); }

    public java.math.BigDecimal sumTreatedAreaByReportId(int reportId) {
        java.math.BigDecimal v = repository.sumTreatedAreaByReportId(reportId);
        return v == null ? java.math.BigDecimal.ZERO : v;
    }
}
