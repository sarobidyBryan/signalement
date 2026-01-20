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

    @Autowired
    private ReportService reportService;

    @Autowired
    private mg.itu.s5.cloud.signalement.repositories.StatusRepository statusRepository;

    public List<ReportsStatus> getAll() { return repository.findAll(); }
    public Optional<ReportsStatus> getById(int id) { return repository.findById(id); }
    public ReportsStatus save(ReportsStatus r) { return repository.save(r); }
    public void delete(int id) { repository.deleteById(id); }

    public List<ReportsStatus> findByReportId(int reportId) {
        return repository.findByReport_Id(reportId);
    }

    public ReportsStatus addStatusToReport(int reportId, int statusId, LocalDateTime registrationDate) {
        // Récupérer le report
        Optional<Report> reportOpt = reportService.getReportById(reportId);
        if (!reportOpt.isPresent()) {
            throw new IllegalArgumentException("Report non trouvé");
        }
        Report report = reportOpt.get();

        // Récupérer le statut
        Optional<Status> statusOpt = statusRepository.findById(statusId);
        if (!statusOpt.isPresent()) {
            throw new IllegalArgumentException("Statut non trouvé");
        }
        Status status = statusOpt.get();

        // Mettre à jour le statut du report
        report.setStatus(status);
        reportService.saveReport(report);

        // Créer l'entrée dans l'historique
        ReportsStatus rs = new ReportsStatus();
        rs.setReport(report);
        rs.setStatus(status);
        rs.setRegistrationDate(registrationDate);
        return repository.save(rs);
    }
    
}
