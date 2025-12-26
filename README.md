# druid-multi-tenant-starter
<p align="center">
    <a  href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/license/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter/releases"><img src="https://badgen.net/github/release/osinn/druid-multi-tenant-starter?cache=300" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/stars/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/forks/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg?color=blue" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://img.shields.io/badge/Spring boot-2.x & 3.x-green.svg?color=blue" ></a>
</p>

> 目标多租户插件，快速集成，开箱即用，支持单个租户以及多个租户，基于alibaba druid 解析SQL，开发过程中只需要像平时一样写SQL语句即可，执行SQL最终会被拦截串改，最终把租户字段添加到SQL语句中，生成新的SQL语句，此过程你无需关心是如何添加。

**需要注意的是，插件最终会认为每个表都会存在租户ID字段，所以，每个表都应该出现租户ID字段，否则需要在配置中明确指出需要忽略的表，忽略的表将不会拼接租户ID字段查询条件，临时表亦是如此(否则临时表结果集中需要出现租户ID字段)**

- jdk1.8+
- 支持 Spring boot 2.x & Spring boot 3.x
- 支持JPA、Mybatis等ORM框架
- 项目地址：[https://github.com/osinn/druid-multi-tenant-starter](https://github.com/osinn/druid-multi-tenant-starter)
- 演示地址：[https://github.com/osinn/druid-multi-tenant-demo](https://github.com/osinn/druid-multi-tenant-demo)
- 测试用例：[https://github.com/osinn/druid-multi-tenant-starter/wiki](https://github.com/osinn/druid-multi-tenant-starter/wiki)

# 支持 SQL 拦截策略种类
- [x] mybatis_interceptor mybatis拦截器策略，需项目使用的是mybatis ORM框架, 如果项目使用mybatis ORM 框架，推荐使用此方式
- [x] jpa_interceptor JPA拦截器策略，需项目使用的是JPA ORM框架
- [x] druid_filter 过滤器策略，不管是哪种 ORM框架，只需要数据库连接使用的数据源是 DruidDataSource 即可

# 支持忽略tenantId策略
- [x] 支持原SQL语句中已有tenantId字段条件跳过
- [x] 支持忽略指定Mapper接口SQL语句添加tenantId字段条件
- [x] 支持指定`表名`忽略SQL语句添加tenantId字段条件
- [x] 支持指定`表名前缀`忽略SQL语句添加tenantId字段条件
- [x] 支持指定`别名`忽略SQL语句添加tenantId字段条件
- [x] 支持重写`skipParser`方法，通过获取当前线程变量判断是否`跳过SQL语句解析`添加tenantId字段条件，需要开发者自行实现逻辑判断
- [x] 支持指定`多数据源`忽略SQL语句添加tenantId字段条件，需要开发者重写`ignoreDynamicDatasource`方法提供获取当前执行的数据源名称
- [x] 支持`@IgnoreTenantIdField`注解从`1.5.6`开始支持使用在任意方法上，`1.5.5`及以下版本仅支持使用在mybatis Mapper接口方法上，需要在yml中配置`enable-pointcut-advisor-ignore-tenant-id: true`开启支持

# 快速开始
- 在`Spring Boot`项目中引入以下依赖

```
<dependency>
    <groupId>io.github.osinn</groupId>
    <artifactId>druid-multi-tenant-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

# 配置
```
--基础配置 1.5.6版本开始 mybatis.tenant.config 改为 multi-tenant.config
multi-tenant:
  config:
    # 是否启用多租户插件
    enable: true
    # 数据库表租户ID的列名
    tenant-id-column: tenant_id

--完整配置参数   
multi-tenant:
  config:
    # 是否使用MyBatis拦截器方式修改sql
    enable: true
    # 数据库表租户ID的列名字段
    tenant-id-column: tenant_id
    # 数据库方言，可选，如果不指定会从事务数据元中自动识别获取方言类型
    db-type: mysql
    # SQL 拦截策略，默认 mybatis 拦截器，如果不是mybatis,且使用的是 DruidDataSource 数据源，推荐使用 druid_filter 过滤器策略
    # druid_filter 只支持使用 DruidDataSource 数据数据源连接数据库
    sql-interceptor-strategy: mybatis_interceptor
    # 是否启用切面方式忽略租户ID字段，默认不开启，开启后 @IgnoreTenantIdField 注解不仅仅可以使用在mybatis Mapper接口方法上，也可以使用在Service任意接口上
    enable-pointcut-advisor-ignore-tenant-id: false
    # 是否忽略表按租户ID过滤,默认所有表都按租户ID过滤，指定表名称(区分大小写全等判断)
    ignore-table-name:
     - user_role # 内部全等判断
    ignore-match-table-alias
    # 匹配判断指定表别名称是否忽略表按租户ID过滤(区分大小写匹配判断)
      - temp # 内部判断 以 temp 为前缀的别名进行忽略过滤
    # 根据表名前缀判断是否忽略表按租户ID过滤
    ignore-table-name-prefix: 
      - act_ # 内部判断 以 act_ 为前缀的别名进行忽略过滤
    # 多数据源情况下指定忽略跳过的数据源名称（需要重写ITenantService接口中的ignoreDynamicDatasource方法自行提供获取当前执行的数据源名称）
    ignore-dynamic-datasource:
      - demoDataSource # 内部全等判断
    # 数据源包装类型, 默认是 null
    data-source-wrapper-type: baomidou_dynamic_data_source
    # 是否自定义数据源，默认是 false，true时会调用addCustomDataSourceProxySqlParser()方法，用于自定义数据源添加SQL解析器
    enable-custom-data-source: false
```
- 如果SQL中表名不在 `ignore-table-name` 中，则去`ignore-match-table-alias`匹配查找
- 执行SQL中存在临时表可以约束指定 `ignore-match-table-alias` 匹配临时表别名来忽略临时表添加租户ID查询条件

# 实现提供获取多租户值接口
- 1.5.5及以下版本需要实现`ITenantService`接口，1.5.6版本开始需要继承`TenantApplicationContext`抽象类提供获取多租户ID值

### 基础方法
```
/**
 * 演示：提供多租户ID服务接口
 * 
 * @author wency_cai
 * @since 1.5.6
 */
@Service
public class TenantServiceImpl extends TenantApplicationContext {

    @Override
    public List<Object> getTenantIds() {
        Long tenantId = 1L;
        List<Object> tenantIdList = new ArrayList<>();
        tenantIdList.add(tenantId);
        return tenantIdList;
    }

}
```
### 完整方法
```
/**
 * 演示：提供多租户ID服务接口
 *
 * @author wency_cai
 * 
 * @since 1.5.6 开始需要继承 TenantApplicationContext 抽象类提供获取多租户ID值
 */
@Service
public class TenantServiceImpl extends TenantApplicationContext {

    @Override
    public List<Object> getTenantIds() {
        // 查询系统多租户id,如果有多个返回多个值即可
        int tenantId = 1;
        return Lists.newArrayList(tenantId);
    }
    
    // 如果需要指定某个数据源跳过设置租户ID，可以重写此方法，并且在yml配置文件中配置 ignore-dynamic-datasource 指定忽略的数据源名称
    @Override
    public String ignoreDynamicDatasource() {
        // 过去当前执行的数据源名称, 空则需要执行解析设置租户ID
        return 返回当前执行的数据源名称;
    }
    
    // 如果开发者需要自行解析SQL，可以重新此方法，并返回新的SQL语句，返回值为空时则继续执行默认解析逻辑，有返回值则直接返回且不会执行默认的解析逻辑
    @Override
    public String customizeParser(String originalSQL, Object paramTenantId) {
        return null;
    }

    // 如果开发者想要在执行默认的解析逻辑前做点什么，可以重写此方法，如果有返回值则会覆盖原始SQL语句（originalSQL）
    @Override
    public String before(String originalSQL, Object paramTenantId) {
        System.out.println("执行默认解析逻辑前 执行了 before方法");
        return null;
    }

    // 如果开发者想要在执行默认的解析逻辑后做点什么，可以重写此方法，如果有返回值则会覆盖解析后的SQL语句（newSQL）
    @Override
    public String after(String newSQL, Object paramTenantId) {
        System.out.println("执行默认解析逻辑后 执行了 after方法");
//        return newSQL + " and tenant_id = 123456";
        return null;
    }
    
    /**
     * 是否跳过解析，可以重写此方法，(实现当前线程全局判断是否忽略tenant_id字段)
     * 此方法可以用在此场景，如：init()方法下执行的所有sql语句忽略添加tenant_id字段，可以自行实现 ThreadLocal
     * 并在此方法中获取当前线程变量判断是否需要忽略添加tenant_id字段，如果需要忽略添加tenant_id字段，则返回true即可
     *
     * @return 返回 true 跳过解析, false 继续解析
     */
    @Override
    public boolean skipParser() {
        return false;
    }
  
    /**
     * 只有 yml 配置 enable-pointcut-advisor = true 时 内部会使用到 ThreadLocal
     * 流程: 调用 threadLocalSkipParserSet 方法 设置是否跳过解析 ——> skipParser 从 ThreadLocal 中获取设置值判断 ——> threadLocalSkipParserClear 清理上下文
     * 可以重写此方法实现使用自定义 ThreadLocal 实现，对应 调用 ThreadLocal 的 set 方法，即为 myThreadLocal.set(xxxx)
     * 如果不实现此方法，会调用内部默认的 ThreadLocal 实现
     * <p>
     * 需要注意的是，内部使用的 ThreadLocal 不支持跨线程调用(即 方法上使用@IgnoreTenantIdField 注解，此方法内又有异步方法，在异步方法内操作数据库，此时不会生效，依然会解析设置租户ID)
     * 如果需要支持跨线程(异步方法)，你的框架可参考使用 TransmittableThreadLocal 库创建线程池，然后实现 skipParser、threadLocalSkipParserSet、threadLocalSkipParserClear这三个方法逻辑
     *
     * @since 1.5.6
     */  
    @Override
    public void threadLocalSkipParserSet() {
        IGNORE_TENANT.set(Boolean.TRUE);
    }

    /**
     * 只有 yml 配置 enable-pointcut-advisor = true 时 内部会使用到 ThreadLocal
     * 可以重写此方法实现使用自定义 ThreadLocal 实现，对应 调用 ThreadLocal 的 clear 方法，即为 myThreadLocal.clear()
     * 如果不实现此方法，会调用内部默认的 ThreadLocal 实现
     *
     * @since 1.5.6
     */
    @Override
    public void threadLocalSkipParserClear() {
        IGNORE_TENANT.remove();
    }
    
    /**
     * 预留方法，当bean初始化后调用，自定义数据源添加代理 sql 解析器
     * yml 配置 enable-custom-data-source = true 时 才会调用此方法
     *
     * @param dataSource       数据源
     * @param defaultSqlParser sql 解析器
     * @return 如果需要自定义添加数据源代理 sql 解析器，则返回调整后的 dataSource 数据源,否则返回 null
     * @since 1.5.6
     */
    default Object addCustomDataSourceProxySqlParser(Object dataSource, DefaultSqlParser defaultSqlParser) {

        return null;
    }
}
```
- 到此整合完成

# 租户ID从Mapper接口方法中传入
> 默认调用 ITenantService.getTenantIds() 接口方法获取租户ID

- 此方式传入tenantId值只支持 mybatis_interceptor 拦截策略(mybatis 拦截器)

```
public interface UserMapper {

    /**
     * 传入租户ID值，支持传入List或Set集合
     */
    User getUserInfoById(@Param("userId") Long userId, @Param("tenant_id") Long tenantId);
}
```
- @Param("tenant_id") 中的值 tenant_id <font color=#FF000 >**约定**</font>对应的是 yml 配置 tenant-id-column 的值
- 如果Mapper接口方法中传入租户ID，则不会调用 ITenantService.getTenantIds() 接口方法获取租户ID

# @IgnoreTenantIdField注解使用
### 多租户忽略Mapper方法
- mybatis Mapper接口方法上使用
```
public interface UserMapper {

    /**
     * 添加@IgnoreTenantIdField注解来忽略设置多租户字段
     */
    @IgnoreTenantIdField
    void deleteTestIgnoreTenantIdById(Long id);
}
```
### service 服务方法忽略租户ID
- yml配置为`enable-pointcut-advisor-ignore-tenant-id: true`，可在任意服务接口方法上使用`@IgnoreTenantIdField`注解来忽略设置多租户字段
```
@Service
public class DemoService {

    @IgnoreTenantIdField
    public Role getRole() {
        return null;
    }
}

-- 需要开启 enable-pointcut-advisor-ignore-tenant-id，如下
multi-tenant:
  config:
    # 需要将此项配置设置为 true
    enable-pointcut-advisor-ignore-tenant-id: true
```
- 需要注意的是，内部使用的`ThreadLocal`设置当前线程跳过SQL解析，不支持跨线程跳过解析(即 方法上使用`@IgnoreTenantIdField` 注解，此方法内又有异步方法，在异步方法内操作数据库，此时不会生效，依然会解析设置租户ID)
- 如果需要支持跨线程(异步方法)，你的框架可参考使用 `TransmittableThreadLocal` 库创建线程池，然后重写 `skipParser`、`threadLocalSkipParserSet`、`threadLocalSkipParserClear`这三个方法逻辑

# 以下是解析修改后的SQL语句效果
## select语句
```sql
SELECT id, name, tenant_id
FROM role
WHERE tenant_id = 11
------------------------------------- 

SELECT *
FROM user s
WHERE s.name = '333'
  AND s.tenant_id = 11
------------------------------------- 

SELECT tenant_id
FROM people
WHERE id IN (
    SELECT id
    FROM user s
    WHERE s.tenant_id = 11
)
  AND tenant_id = 11
------------------------------------- 

SELECT tenant_id
FROM people
WHERE id IN (
    SELECT id
    FROM user s
    WHERE s.tenant_id = 11
)
  AND id IN (
    SELECT u.id
    FROM user u
    WHERE u.tenant_id = 11
)
  AND tenant_id = 11
------------------------------------- 

SELECT tenant_id
FROM people
WHERE id IN (
    SELECT id
    FROM user s
    WHERE s.tenant_id = 11
)
  AND id IN (
    SELECT u.id
    FROM user u
    WHERE u.tenant_id = 11
)
  AND tenant_id IN (
    SELECT u.tenant_id
    FROM user u
    WHERE u.tenant_id = 11
)
  AND tenant_id = 11
------------------------------------- 

SELECT p.tenant_id
     , (
    SELECT u.name
    FROM user u
    WHERE u.id = p.user_id
      AND u.tenant_id = 11
)
FROM people p
WHERE p.tenant_id = 11
------------------------------------- 

SELECT p.tenant_id
     , (
    SELECT u.name
    FROM user u
    WHERE u.id = p.user_id
      AND u.tenant_id = 11
)
     , (
    SELECT s.name
    FROM user s
    WHERE s.id = p.user_id
      AND s.tenant_id = 11
)
FROM people p
WHERE p.tenant_id = 11
------------------------------------- 

SELECT p.tenant_id
     , (
    SELECT u.name
         , (
        SELECT s.name
        FROM user s
        WHERE s.id = p.user_id
          AND s.tenant_id = 11
    )
    FROM user u
    WHERE u.id = p.user_id
      AND u.tenant_id = 11
)
FROM people p
WHERE p.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         JOIN user_role ur
              ON ur.user_id = u.id
                  AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         JOIN user_role ur
              ON ur.user_id = u.id
                  AND u.id = 22
                  AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
     , (
    SELECT ur.name
    FROM role r
    WHERE r.id = ur.role_id
      AND r.tenant_id = 11
) AS r_name
FROM `user` u
         JOIN user_role ur
              ON ur.user_id = u.id
                  AND u.id = 22
                  AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         JOIN user_role ur
              ON ur.user_id = u.id
                  AND ur.role_id IN (
                      SELECT r.id
                      FROM role r
                      WHERE r.tenant_id = 11
                  )
                  AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         JOIN user_role ur
              ON ur.user_id = u.id
                  AND ur.role_id IN (
                      SELECT r.id
                      FROM role r
                      WHERE r.tenant_id = 11
                  )
                  AND ur.test_id IN (
                      SELECT r.test_id
                      FROM role r
                      WHERE r.tenant_id = 11
                  )
                  AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         LEFT JOIN user_role ur
                   ON ur.user_id = u.id
                       AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*
FROM `user` u
         RIGHT JOIN user_role ur
                    ON ur.user_id = u.id
                        AND ur.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT COUNT(*), id
FROM `user`
WHERE tenant_id = 11
GROUP BY id
HAVING COUNT(*) >= 1
------------------------------------- 

SELECT EXISTS (
    SELECT *
    FROM `user`
    WHERE username = ?
      AND tenant_id = 11
) AS d
------------------------------------- 

SELECT EXISTS (
    SELECT tenant_id
    FROM people
    WHERE id IN (
        SELECT id
        FROM user s
        WHERE s.tenant_id = 11
    )
      AND tenant_id = 11
) AS d
------------------------------------- 

SELECT username
FROM `user`
WHERE tenant_id = 11
UNION
SELECT username
FROM account
WHERE tenant_id = 11
------------------------------------- 

SELECT u.*, r.id AS r_id, r.NAME AS r_name
FROM `user` u
         LEFT JOIN user_role ur
                   ON ur.user_id = u.id
                       AND ur.tenant_id = 11
         LEFT JOIN role r
                   ON r.id = ur.role_id
                       AND u.id = 22
                       AND r.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT u.*, r.id AS r_id, r.NAME AS r_name
FROM `user` u
         LEFT JOIN user_role ur
                   ON ur.user_id = u.id
                       AND ur.tenant_id = 11
         LEFT JOIN role r
                   ON r.id = ur.role_id
                       AND u.id = 22
                       AND r.tenant_id = 11
         LEFT JOIN menu m
                   ON m.role_id = r.id
                       AND m.tenant_id = 11
WHERE u.tenant_id = 11
------------------------------------- 

SELECT a, b
FROM (
         SELECT *
         FROM table_a
         WHERE tenant_id = 11
     ) temp
WHERE temp.a = 'a';
------------------------------------- 

SELECT temp.*
FROM (
         SELECT *
         FROM `user`
         WHERE tenant_id = 11
     ) temp
         JOIN user_role ur
              ON ur.user_id = temp.id
                  AND ur.tenant_id = 11
WHERE temp.a = 'a'
  AND temp.tenant_id = 11;
------------------------------------- 

SELECT id, name, tenant_id
FROM role
WHERE tenant_id = 1
  AND tenant_id = 11
```
## update语句
```sql
UPDATE user u
SET ds = ?, u.name = ?, id = 'fdf', ddd = ?
WHERE id = ?
  AND u.tenant_id = 11
------------------------------------- 

UPDATE user u
SET ds = ?, u.name = ?, id = 'fdf', ddd = ?
WHERE id IN (1, 2, 3)
  AND u.tenant_id = 11
------------------------------------- 

UPDATE user u
SET ds = ?, u.name = ?, id = 'fdf', ddd = ?
WHERE id IN (
    SELECT ur.user_id
    FROM user_role ur
    WHERE ur.tenant_id = 11
)
  AND u.tenant_id = 11
------------------------------------- 

UPDATE user u
SET ds = ?, u.name = ?, id = 'fdf', ddd = ?
WHERE id IN (
    SELECT ur.user_id
    FROM user_role ur
    WHERE ur.id = 1
      AND ur.tenant_id = 11
)
  AND u.tenant_id = 11
------------------------------------- 

UPDATE `user` u
    JOIN user_role ur
ON ur.user_id = u.id
    AND u.id = 111
    AND ur.tenant_id = 11
    SET u.qr_code = '1212'
WHERE u.tenant_id = 11
------------------------------------- 

UPDATE `user` u
    JOIN user_role ur
ON ur.user_id = u.id
    AND u.id IN (
    SELECT urr.user_id
    FROM user_role urr
    WHERE urr.tenant_id = 11
    )
    AND ur.tenant_id = 11
    SET u.qr_code = '1212'
WHERE u.tenant_id = 11
------------------------------------- 

UPDATE user
SET name = CASE
               WHEN id = 1 THEN 'name1'
               WHEN id = 2 THEN 'name2'
               WHEN id = 3 THEN 'name3'
    END
WHERE id IN (11, 22, 33, 3)
  AND tenant_id = 11;
```
## insert语句
```sql
INSERT INTO `user` (`id`, `username`, `password`, tenant_id)
VALUES (?, ?, ?, 11),
       (?, ?, ?, 11),
       (?, ?, ?, 11)
```
## delete语句
```sql
DELETE FROM user
WHERE id = 1
  AND tenant_id = 11
------------------------------------- 

DELETE FROM user
WHERE id IN (
    SELECT id
    FROM user s
    WHERE s.tenant_id = 11
)
  AND tenant_id = 11
------------------------------------- 

DELETE FROM system_permission_data sp
    LEFT JOIN system_role_data_relation re
ON sp.id = re.data_id
    AND re.tenant_id = 11
    LEFT JOIN system_role sr
    ON sr.id = re.role_id
    AND sr.tenant_id = 11
WHERE sr.id = 1
  AND sp.tenant_id = 11
```
