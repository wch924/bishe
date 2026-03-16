package com.chwww924.chwwwBackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis Plus 配置
 *
 * @author https://github.com/liyupi
 */
@Configuration
@MapperScan("com.chwww924.chwwwBackend.mapper")
public class MyBatisPlusConfig {

    /**
     * 拦截器配置
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * SQL拦截器，处理保留关键字表名
     */
    @Bean
    public Interceptor sqlInterceptor() {
        return new GroupsTableInterceptor();
    }

    /**
     * Groups表名拦截器（处理MySQL保留关键字）
     */
    @Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
    })
    static class GroupsTableInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            BoundSql boundSql = ms.getBoundSql(parameter);
            String sql = boundSql.getSql();
            
            // 如果SQL中包含 groups 表名（未加反引号），则添加反引号
            if (sql.contains("groups") && !sql.contains("`groups`")) {
                // 替换 INSERT INTO groups 为 INSERT INTO `groups`
                sql = sql.replaceAll("(?i)INSERT INTO groups", "INSERT INTO `groups`");
                // 替换 UPDATE groups 为 UPDATE `groups`
                sql = sql.replaceAll("(?i)UPDATE groups", "UPDATE `groups`");
                // 替换 DELETE FROM groups 为 DELETE FROM `groups`
                sql = sql.replaceAll("(?i)DELETE FROM groups", "DELETE FROM `groups`");
                // 替换 FROM groups 为 FROM `groups`（但避免重复替换）
                sql = sql.replaceAll("(?i)\\bFROM groups\\b", "FROM `groups`");
                // 替换 JOIN groups 为 JOIN `groups`
                sql = sql.replaceAll("(?i)\\bJOIN groups\\b", "JOIN `groups`");
                
                // 使用MetaObject修改BoundSql的sql字段
                MetaObject metaObject = SystemMetaObject.forObject(boundSql);
                metaObject.setValue("sql", sql);
            }
            
            return invocation.proceed();
        }

        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }

        @Override
        public void setProperties(Properties properties) {
            // 不需要配置属性
        }
    }
}
