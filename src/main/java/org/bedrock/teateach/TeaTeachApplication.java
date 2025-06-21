package org.bedrock.teateach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.bedrock.teateach.mappers")
public class TeaTeachApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeaTeachApplication.class, args);
    }

}
