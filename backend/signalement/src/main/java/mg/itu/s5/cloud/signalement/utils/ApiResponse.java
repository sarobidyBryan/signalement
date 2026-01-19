package mg.itu.s5.cloud.signalement.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    private String status;
    private Object data;
    private ErrorDetails error;
    private LocalDateTime meta;

    // Constructeur privé pour forcer l'utilisation des méthodes statiques
    private ApiResponse(String status, Object data, ErrorDetails error) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.meta = LocalDateTime.now();
    }

    // Getters
    public String getStatus() { return status; }
    public Object getData() { return data; }
    public ErrorDetails getError() { return error; }
    public LocalDateTime getMeta() { return meta; }

    // Méthodes statiques pour créer des réponses de succès
    public static ApiResponse success(Object data) {
        return new ApiResponse("success", data, null);
    }

    public static ApiResponse success(String message) {
        Map<String, String> data = new HashMap<>();
        data.put("message", message);
        return new ApiResponse("success", data, null);
    }

    public static ApiResponse success(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return new ApiResponse("success", data, null);
    }

    // Méthodes statiques pour créer des réponses d'erreur
    public static ApiResponse error(String code, String message) {
        return new ApiResponse("error", null, new ErrorDetails(code, message, ""));
    }

    public static ApiResponse error(String code, String message, String details) {
        return new ApiResponse("error", null, new ErrorDetails(code, message, details));
    }

    // Classe interne pour les détails d'erreur
    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;

        public ErrorDetails(String code, String message, String details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }

    // Codes d'erreur prédéfinis
    public static class ErrorCodes {
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
        public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
        public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String CONFIGURATION_NOT_FOUND = "CONFIGURATION_NOT_FOUND";
        public static final String INVALID_REQUEST = "INVALID_REQUEST";
        public static final String INVALID_DATA = "INVALID_DATA";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String STATUS_TYPE_NOT_FOUND = "STATUS_TYPE_NOT_FOUND";
        public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    }
}