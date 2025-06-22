package org.bedrock.teateach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("org.bedrock.teateach.mappers")
@EnableCaching
@EnableAspectJAutoProxy
public class TeaTeachApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeaTeachApplication.class, args);
    }

}
