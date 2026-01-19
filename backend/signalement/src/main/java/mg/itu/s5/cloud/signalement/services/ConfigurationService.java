package mg.itu.s5.cloud.signalement.services;

import mg.itu.s5.cloud.signalement.entities.Configuration;
import mg.itu.s5.cloud.signalement.repositories.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    public List<Configuration> getAllConfigurations() {
        return configurationRepository.findAll();
    }

    public Optional<Configuration> getConfigurationById(int id) {
        return configurationRepository.findById(id);
    }

    public Optional<Configuration> getConfigurationByKey(String key) {
        return configurationRepository.findByKey(key);
    }

    public String getConfigurationValue(String key) {
        return getConfigurationByKey(key).map(Configuration::getValue).orElse(null);
    }

    public String getConfigurationValue(String key, String defaultValue) {
        return getConfigurationByKey(key).map(Configuration::getValue).orElse(defaultValue);
    }

    public Configuration saveConfiguration(Configuration configuration) {
        return configurationRepository.save(configuration);
    }

    public Configuration createOrUpdateConfiguration(String key, String value, String type) {
        Optional<Configuration> existingConfig = getConfigurationByKey(key);
        Configuration config;

        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setValue(value);
            config.setType(type);
        } else {
            config = new Configuration();
            config.setKey(key);
            config.setValue(value);
            config.setType(type);
        }

        return saveConfiguration(config);
    }

    public void deleteConfiguration(int id) {
        configurationRepository.deleteById(id);
    }

    public void deleteConfigurationByKey(String key) {
        Optional<Configuration> config = getConfigurationByKey(key);
        config.ifPresent(c -> deleteConfiguration(c.getId()));
    }
}