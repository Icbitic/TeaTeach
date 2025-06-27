package org.bedrock.teateach.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.mappers.QuestionMapper;
import org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler;
import org.bedrock.teateach.typehandler.LongListJsonTypeHandler;
import org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class DatabaseConfig {

    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public DatabaseConfig(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        registry.register(int[].class, IntArrayJsonTypeHandler.class);
        
        // Register the custom type handler for List<Long>
        // This allows MyBatis to automatically convert between List<Long> and JSON strings in the database
        registry.register(List.class, LongListJsonTypeHandler.class);
        registry.register(String[].class, StringArrayJsonTypeHandler.class);

        // Make sure the QuestionMapper interface is properly scanned and initialized
        try {
            sqlSessionFactory.getConfiguration().addMapper(QuestionMapper.class);
        } catch (Exception e) {
            // If mapper is already registered, ignore the exception
        }
    }
}
