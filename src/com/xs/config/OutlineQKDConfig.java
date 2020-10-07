package com.xs.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement//开启spring对事务注解的支持
@PropertySource("classpath:veh.properties")//数据源properties文件
public class OutlineQKDConfig {
	
    private String driver;

    private String url;

    private String username;

    private String password;
    
    @Bean(name="qkdJdbcTemplate")
    public JdbcTemplate createJdbcTemplate(@Qualifier("qkdDataSource") DataSource dataSource) throws IOException{
    	
    	Properties prop = new Properties();
        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
    	
    	if(prop.getProperty("jdbc.qkd.driver")==null) {
    		return null;
    	}
    	
        return new JdbcTemplate(dataSource);
    }
    
    @Bean(name="qkdDataSource")
    public DataSource createDataSource() throws IOException{
        DriverManagerDataSource ds = new DriverManagerDataSource();
        
        Properties prop = new Properties();
        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
        
        if(prop.getProperty("jdbc.qkd.driver")==null) {
    		return null;
    	}
        
        driver=prop.getProperty("jdbc.qkd.driver");
        url=prop.getProperty("jdbc.qkd.url");
        username=prop.getProperty("jdbc.qkd.username");
        password=prop.getProperty("jdbc.qkd.password");
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }


}
