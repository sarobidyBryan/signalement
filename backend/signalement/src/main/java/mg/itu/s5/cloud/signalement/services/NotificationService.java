package mg.itu.s5.cloud.signalement.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import mg.itu.s5.cloud.signalement.dto.NotificationRequest;
import mg.itu.s5.cloud.signalement.entities.UserToken;
import mg.itu.s5.cloud.signalement.repositories.UserTokenRepository;

@Service
public class NotificationService {
    

    @Autowired 
    UserTokenRepository userTokenRepository;

    public String sendNotification(NotificationRequest request) {
        try {
            Message message = buildMessage(request);
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification envoyée avec succès: {}:\n"+response);
            return response;
        } catch (FirebaseMessagingException e) {
            System.out.println("Erreur lors de l'envoi de la notification Firebase");
            throw new RuntimeException("Échec de l'envoi de la notification", e);
        }
    }

    public void sendNotificationToUser(NotificationRequest request,Integer userId) {
        
        List<UserToken> userTokens = userTokenRepository.findByUserId(userId);
        for (UserToken userToken : userTokens) {
            request.setToken(userToken.getToken());
            sendNotification(request);
        }
    }

    

    private Message buildMessage(NotificationRequest request) {
        Message.Builder messageBuilder = Message.builder();

        // Configurer la notification
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        messageBuilder.setNotification(notification);

        // Ajouter le token ou le topic
        if (request.getToken() != null && !request.getToken().isEmpty()) {
            messageBuilder.setToken(request.getToken());
        } else if (request.getTopic() != null && !request.getTopic().isEmpty()) {
            messageBuilder.setTopic(request.getTopic());
        } else {
            throw new IllegalArgumentException("Token ou topic requis");
        }

        // Configurer les options Android
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setColor("#FF0000")
                        .build())
                .build();
        messageBuilder.setAndroidConfig(androidConfig);

        // Configurer les options APNs (iOS)
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .build())
                .build();
        messageBuilder.setApnsConfig(apnsConfig);

        return messageBuilder.build();
    }

}
