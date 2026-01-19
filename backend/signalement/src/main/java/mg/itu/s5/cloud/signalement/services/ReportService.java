package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Report;
import mg.itu.s5.cloud.signalement.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public List<Report> getAllReports() { return reportRepository.findAll(); }

    public Optional<Report> getReportById(int id) { return reportRepository.findById(id); }

    public Report saveReport(Report r) { return reportRepository.save(r); }

    public void deleteReport(int id) { reportRepository.deleteById(id); }
}
