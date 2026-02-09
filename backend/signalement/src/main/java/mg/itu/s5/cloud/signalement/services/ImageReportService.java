package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.ImageReport;
import mg.itu.s5.cloud.signalement.repositories.ImageReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageReportService {

    @Autowired
    private ImageReportRepository imageReportRepository;

    public List<ImageReport> findByReportId(int reportId) {
        return imageReportRepository.findByReport_IdOrderByCreatedAtDesc(reportId);
    }
}
