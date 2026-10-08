package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component//把这个类交给 Spring 管理，变成一个 Bean，其他地方可以 @Autowired 注入使用
@ConfigurationProperties(prefix = "sky.jwt")//告诉 Spring：从 YAML 里读取 sky.jwt 开头的配置，自动匹配到这个类的属性上
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
