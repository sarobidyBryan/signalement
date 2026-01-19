package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.entities.ReportsStatus;
import mg.itu.s5.cloud.signalement.entities.Status;
import mg.itu.s5.cloud.signalement.repositories.ReportsStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportsStatusService {

    @Autowired
    private ReportsStatusRepository repository;

    public List<ReportsStatus> getAll() { return repository.findAll(); }
    public Optional<ReportsStatus> getById(int id) { return repository.findById(id); }
    public ReportsStatus save(ReportsStatus r) { return repository.save(r); }
    public void delete(int id) { repository.deleteById(id); }

    public List<ReportsStatus> findByReportId(int reportId) {
        return repository.findByReport_Id(reportId);
    }

    public ReportsStatus addStatusToReport(int reportId, int statusId, LocalDateTime registrationDate) {
        ReportsStatus rs = new ReportsStatus();
        Report report = new Report();
        report.setId(reportId);
        rs.setReport(report);
        Status status = new Status();
        status.setId(statusId);
        rs.setStatus(status);
        rs.setRegistrationDate(registrationDate);
        return repository.save(rs);
    }
    
}
