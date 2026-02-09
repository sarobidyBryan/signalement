package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Company;
import mg.itu.s5.cloud.signalement.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(int id) {
        return companyRepository.findById(id);
    }

    public Optional<Company> getCompanyByFirebaseId(String firebaseId) {
        return companyRepository.findByFirebaseId(firebaseId);
    }

    public Company saveCompany(Company company) {
        if (company.getId() == 0) {
            company.setCreatedAt(LocalDateTime.now());
        }
        return companyRepository.save(company);
    }

    public void deleteCompany(int id) {
        companyRepository.deleteById(id);
    }

    public List<Company> findModifiedSince(LocalDateTime since) {
        return companyRepository.findModifiedSince(since);
    }
}
