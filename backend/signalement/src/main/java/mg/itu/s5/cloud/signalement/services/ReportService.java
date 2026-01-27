package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private mg.itu.s5.cloud.signalement.repositories.StatusRepository statusRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    public Report updateReport(int id, Report reportData) {
        Optional<Report> existingReportOpt = reportRepository.findById(id);
        if (existingReportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report not found with id: " + id);
        }

        Report existingReport = existingReportOpt.get();

        // Mettre à jour les champs modifiables
        if (reportData.getArea() != null) {
            existingReport.setArea(reportData.getArea());
        }
        if (reportData.getLongitude() != null) {
            existingReport.setLongitude(reportData.getLongitude());
        }
        if (reportData.getLatitude() != null) {
            existingReport.setLatitude(reportData.getLatitude());
        }
        if (reportData.getDescription() != null) {
            existingReport.setDescription(reportData.getDescription());
        }
        if (reportData.getReportDate() != null) {
            existingReport.setReportDate(reportData.getReportDate());
        }

        // Mettre à jour la date de modification
        existingReport.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(existingReport);
    }

    public void deleteReport(int id) { reportRepository.deleteById(id); }

    public List<Report> findModifiedSince(LocalDateTime since) {
        return reportRepository.findModifiedSince(since);
    }

    public List<Report> searchReports(BigDecimal areaMin, BigDecimal areaMax, String statusCode, Integer userId, LocalDate reportDateFrom, LocalDate reportDateTo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Report> cq = cb.createQuery(Report.class);
        Root<Report> root = cq.from(Report.class);
        root.fetch("user", JoinType.LEFT);
        root.fetch("status", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();

        if (areaMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("area"), areaMin));
        }
        if (areaMax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("area"), areaMax));
        }
        if (statusCode != null && !statusCode.isBlank()) {
            Join<Object, Object> statusJoin = root.join("status", JoinType.LEFT);
            predicates.add(cb.equal(statusJoin.get("statusCode"), statusCode));
        }
        if (userId != null) {
            Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
            predicates.add(cb.equal(userJoin.get("id"), userId));
        }
        if (reportDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), reportDateFrom.atStartOfDay()));
        }
        if (reportDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), reportDateTo.atTime(23, 59, 59)));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("reportDate")));

        return entityManager.createQuery(cq).getResultList();
    }
}
