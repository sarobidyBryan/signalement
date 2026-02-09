package mg.itu.s5.cloud.signalement.controllers;

import mg.itu.s5.cloud.signalement.entities.Company;
import mg.itu.s5.cloud.signalement.services.CompanyService;
import mg.itu.s5.cloud.signalement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Management", description = "API for managing companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieves a list of all companies")
    public ResponseEntity<ApiResponse> getAll() {
        List<Company> all = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success("companies", all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID", description = "Retrieves a specific company by its ID")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id) {
        return companyService.getCompanyById(id)
                .map(c -> ResponseEntity.ok(ApiResponse.success(c)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Company not found")));
    }

    @PostMapping
    @Operation(summary = "Create a new company", description = "Creates a new company")
    public ResponseEntity<ApiResponse> create(@RequestBody Company company) {
        Company saved = companyService.saveCompany(company);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company", description = "Updates an existing company")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @RequestBody Company company) {
        return companyService.getCompanyById(id)
                .map(existing -> {
                    existing.setName(company.getName());
                    existing.setEmail(company.getEmail());
                    Company saved = companyService.saveCompany(existing);
                    return ResponseEntity.ok(ApiResponse.success(saved));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(ApiResponse.ErrorCodes.USER_NOT_FOUND, "Company not found")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company", description = "Deletes a company")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted"));
    }
}
