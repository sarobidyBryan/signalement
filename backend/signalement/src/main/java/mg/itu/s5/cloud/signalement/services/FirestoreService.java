package mg.itu.s5.cloud.signalement.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import mg.itu.s5.cloud.signalement.config.FirebaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    @Autowired
    private FirebaseConfig firebaseConfig;

    private Firestore getFirestore() {
        if (!firebaseConfig.isFirebaseInitialized()) {
            throw new IllegalStateException("Firebase is not initialized. Please configure Firebase credentials.");
        }
        return FirestoreClient.getFirestore();
    }

    /**
     * Crée ou met à jour un document dans Firestore
     * @param collection Nom de la collection
     * @param documentId ID du document (null pour générer automatiquement)
     * @param data Données du document
     * @return L'ID du document créé ou mis à jour
     */
    public String saveDocument(String collection, String documentId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef;
        
        if (documentId != null && !documentId.isEmpty()) {
            // Mise à jour ou création avec ID spécifique
            docRef = db.collection(collection).document(documentId);
            docRef.set(data, SetOptions.merge()).get();
            return documentId;
        } else {
            // Création avec ID auto-généré
            ApiFuture<DocumentReference> future = db.collection(collection).add(data);
            docRef = future.get();
            return docRef.getId();
        }
    }

    /**
     * Récupère un document par son ID
     */
    public Map<String, Object> getDocument(String collection, String documentId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(collection).document(documentId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.getData();
        }
        return null;
    }

    /**
     * Récupère tous les documents d'une collection
     */
    public List<QueryDocumentSnapshot> getAllDocuments(String collection) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(collection).get();
        return future.get().getDocuments();
    }

    /**
     * Récupère les documents modifiés après une certaine date
     * Utilise le champ updated_at ou synced_at pour filtrer
     */
    public List<QueryDocumentSnapshot> getDocumentsModifiedSince(String collection, LocalDateTime since) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        
        // Convertir LocalDateTime en Timestamp Firestore
        Date sinceDate = Date.from(since.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp firestoreTimestamp = Timestamp.of(sinceDate);
        
        // Essayer d'abord avec updated_at
        ApiFuture<QuerySnapshot> future = db.collection(collection)
                .whereGreaterThan("updated_at", firestoreTimestamp)
                .get();
        
        List<QueryDocumentSnapshot> results = future.get().getDocuments();
        
        // Si aucun résultat, essayer avec synced_at
        if (results.isEmpty()) {
            future = db.collection(collection)
                    .whereGreaterThan("synced_at", firestoreTimestamp)
                    .get();
            results = future.get().getDocuments();
        }
        
        return results;
    }

    /**
     * Supprime un document
     */
    public void deleteDocument(String collection, String documentId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        db.collection(collection).document(documentId).delete().get();
    }

    /**
     * Recherche des documents par champ
     */
    public List<QueryDocumentSnapshot> findByField(String collection, String field, Object value) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(collection)
                .whereEqualTo(field, value)
                .get();
        return future.get().getDocuments();
    }

    /**
     * Vérifie si un document existe avec un postgres_id donné
     */
    public String findDocumentIdByPostgresId(String collection, int postgresId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = findByField(collection, "postgres_id", postgresId);
        if (!docs.isEmpty()) {
            return docs.get(0).getId();
        }
        return null;
    }

    /**
     * Crée un map avec les champs communs pour la synchronisation
     */
    public Map<String, Object> createSyncMap(int postgresId, Map<String, Object> data) {
        Map<String, Object> syncData = new HashMap<>(data);
        syncData.put("postgres_id", postgresId);
        syncData.put("synced_at", FieldValue.serverTimestamp());
        return syncData;
    }
}
