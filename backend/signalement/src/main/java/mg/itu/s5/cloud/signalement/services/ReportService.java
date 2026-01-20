package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private mg.itu.s5.cloud.signalement.repositories.StatusRepository statusRepository;

    public List<Report> getAllReports() { return reportRepository.findAll(); }

    public Optional<Report> getReportById(int id) { return reportRepository.findById(id); }

    public Optional<Report> getReportByFirebaseId(String firebaseId) { 
        return reportRepository.findByFirebaseId(firebaseId); 
    }

    public Report saveReport(Report r) {
        if (r.getId() == 0) {
            // Nouveau report
            r.setCreatedAt(java.time.LocalDateTime.now());
            // Si pas de statut défini, utiliser SUBMITTED par défaut
            if (r.getStatus() == null) {
                statusRepository.findByStatusCode("SUBMITTED").ifPresent(r::setStatus);
            }
        }
        return reportRepository.save(r);
    }

    public void deleteReport(int id) { reportRepository.deleteById(id); }

    public List<Report> findModifiedSince(LocalDateTime since) {
        return reportRepository.findModifiedSince(since);
    }
}
