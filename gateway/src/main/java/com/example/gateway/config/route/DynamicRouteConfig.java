package com.example.gateway.config.route;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.example.gateway.config.NacosGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;


/**
 * @author hcq
 * @date 2020/11/4 19:31
 */
@Slf4j
@Component
@DependsOn({"nacosProperties"})
public class DynamicRouteConfig implements ApplicationEventPublisherAware {

    /**
     * 路由规则仓库
     */
    private final RouteDefinitionRepository definitionRepository;

    /**
     * 发布Application事件
     */
    private ApplicationEventPublisher publisher;

    /**
     * NacosConfig Client用于监听Nacos配置变更
     */
    private ConfigService configService;

    private DynamicRouteConfig(RouteDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void init() {
        // 初始化Nacos配置的路由规则，并获取NacosClient
        configService = initConfig();
        // 监听Nacos配置文件
        dynamicRouteByNacosListener(NacosGatewayProperties.NACOS_ROUTE_DATA_ID, NacosGatewayProperties.NACOS_ROUTE_GROUP);
    }

    /**
     * 初始化NacosRoutes
     */
    private ConfigService initConfig() {
        log.info("gateway route init...");
        ConfigService configService = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", NacosGatewayProperties.NACOS_SERVER_ADDR);
            properties.setProperty("namespace", NacosGatewayProperties.NACOS_NAMESPACE);
            configService = NacosFactory.createConfigService(properties);
            if (configService == null) {
                log.warn("initConfigService fail");
                throw new IllegalArgumentException("initConfigService fail");
            }
            String configInfo = configService.getConfig(NacosGatewayProperties.NACOS_ROUTE_DATA_ID, NacosGatewayProperties.NACOS_ROUTE_GROUP, NacosGatewayProperties.DEFAULT_TIMEOUT);
            // 初始化路由规则
            if (StringUtils.isNotBlank(configInfo)) {
                updateRouteRule(configInfo);
            }
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
        }
        return configService;
    }

    /**
     * 根据Nacos中配置的路由规则json串更新路由规则
     *
     * @param routesStr Nacos中配置的路由规则json串
     */
    private void updateRouteRule(String routesStr) {
        System.out.println(this);
        log.info("更新gateway路由规则\n\r{}", routesStr);
        definitionRepository.getRouteDefinitions().collectMap(RouteDefinition::getId).subscribe(oldRoutes -> {
            List<RouteDefinition> nowRoutes = JSONObject.parseArray(routesStr, RouteDefinition.class);
            // 删除原有规则
            for (RouteDefinition oldRoute : oldRoutes.values()) {
                log.info("gateway remove route {}", oldRoute);
                definitionRepository.delete(Mono.just(oldRoute.getId())).subscribe();
            }
            // 加载配置文件中的规则
            for (RouteDefinition route : nowRoutes) {
                log.info("gateway add route {}", route);
                definitionRepository.save(Mono.just(route)).subscribe();
            }
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
        });
    }

    /**
     * 监听Nacos下发的动态路由配置
     *
     * @param dataId 路由规则
     * @param group  路由规则group
     */
    private void dynamicRouteByNacosListener(String dataId, String group) {
        try {
            DynamicRouteConfig routeConfig = this;
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println(this);
                    System.out.println(routeConfig);
                    routeConfig.updateRouteRule(configInfo);
                }

                @Override
                public Executor getExecutor() {
                    log.info("getExecutor\n\r");
                    return null;
                }
            });
        } catch (NacosException e) {
            log.error("从nacos接收动态路由配置出错!!!", e);
        }
    }


}
