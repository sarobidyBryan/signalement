package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Status;
import mg.itu.s5.cloud.signalement.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusService {

    @Autowired
    private StatusRepository statusRepository;

    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    public Optional<Status> getStatusById(int id) {
        return statusRepository.findById(id);
    }

    public Optional<Status> getStatusByStatusCode(String statusCode) {
        return statusRepository.findByStatusCode(statusCode);
    }

    public Optional<Status> getStatusByFirebaseId(String firebaseId) {
        return statusRepository.findAll().stream()
                .filter(status -> firebaseId.equals(status.getFirebaseId()))
                .findFirst();
    }

    public Status saveStatus(Status status) {
        if (status.getId() == 0) {
            status.setCreatedAt(java.time.LocalDateTime.now());
        }
        status.setUpdatedAt(java.time.LocalDateTime.now());
        return statusRepository.save(status);
    }
}