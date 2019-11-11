package com.dl.shop.payment.configurer;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;

@Configuration
public class DataSourceConfig {
	@Autowired
    WallFilter wallFilter;
	
	@Bean(name = "dataSource")
	@Primary
	// application.properteis中对应属性的前缀
	@ConfigurationProperties(prefix = "spring.datasource.druid")
	public DataSource dataSource1() {
		DruidDataSource dateSource = new DruidDataSource();
		 // filter
        List<Filter> filters = new ArrayList<>();
        filters.add(wallFilter);
        dateSource.setProxyFilters(filters);
		return dateSource;
	}

    @Bean(name = "wallConfig")
    WallConfig wallFilterConfig(){
        WallConfig wc = new WallConfig ();
        wc.setMultiStatementAllow(true);
        return wc;
    }

    @Bean(name = "wallFilter")
    @DependsOn("wallConfig")
    WallFilter wallFilter(WallConfig wallConfig){
        WallFilter wfilter = new WallFilter ();
        wfilter.setConfig(wallConfig);
        return wfilter;
    }
}
