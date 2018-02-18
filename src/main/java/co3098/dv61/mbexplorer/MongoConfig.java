package co3098.dv61.mbexplorer;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;

// Cloud Mongo DB setup

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "<CENSORED>";
    }

    @Override
    public Mongo mongo() throws Exception {
        MongoCredential credential = MongoCredential.createCredential("<CENSORED>", "<CENSORED>", "<CENSORED>".toCharArray());
        return new MongoClient((new ServerAddress("ds139436.mlab.com:39436")), Arrays.asList(credential));
    }

    @Override
    protected String getMappingBasePackage() {
        return "<CENSORED>";
    }

    @Override public @Bean
    MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }
}