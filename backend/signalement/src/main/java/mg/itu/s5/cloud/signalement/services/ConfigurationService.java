package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Configuration;
import mg.itu.s5.cloud.signalement.repositories.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    public Optional<Configuration> getConfigurationByKey(String key) {
        return configurationRepository.findByKey(key);
    }

    public String getConfigurationValue(String key) {
        return getConfigurationByKey(key).map(Configuration::getValue).orElse(null);
    }

    public String getConfigurationValue(String key, String defaultValue) {
        return getConfigurationByKey(key).map(Configuration::getValue).orElse(defaultValue);
    }
}