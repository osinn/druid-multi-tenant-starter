# druid-multi-tenant-starter
<p align="center">
    <a  href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/license/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter/releases"><img src="https://badgen.net/github/release/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/stars/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://badgen.net/github/forks/osinn/druid-multi-tenant-starter" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg?color=blue" ></a>
    <a href="https://github.com/osinn/druid-multi-tenant-starter"><img src="https://img.shields.io/badge/Spring boot-2.x & 3.x-green.svg?color=blue" ></a>
</p>

> 目标多租户插件，快速集成，开箱即用，支持单个租户以及多个租户，基于alibaba druid 解析SQL，开发过程中只需要像平时一样写SQL语句即可，执行SQL最终会被拦截串改，最终把租户字段添加到SQL语句中，生成新的SQL语句，此过程你无需关心是如何添加。

**需要注意的是，插件最终会认为每个表都会存在租户ID字段，所以，每个表都应该出现租户ID字段，否则需要在配置中明确指出需要忽略的表，忽略的表将不会拼接租户ID字段查询条件，临时表亦是如此(否则临时表结果集中需要出现租户ID字段)**

- jdk1.8+
- 支持 Spring boot 2.x & Spring boot 3.x
- 项目地址：[https://github.com/osinn/druid-multi-tenant-starter](https://github.com/osinn/druid-multi-tenant-starter)
- 演示地址：[https://github.com/osinn/druid-multi-tenant-demo](https://github.com/osinn/druid-multi-tenant-demo)
- 测试用例：[https://github.com/osinn/druid-multi-tenant-starter/wiki](https://github.com/osinn/druid-multi-tenant-starter/wiki)

# 支持忽略tenantId策略
- [x] 支持原SQL语句中已有tenantId字段条件跳过
- [x] 支持忽略指定表名添加tenantId字段条件
- [x] 支持忽略指定Mapper接口SQL语句添加tenantId字段条件
- [x] 支持指定`表名`忽略SQL语句添加tenantId字段条件
- [x] 支持指定`别名`忽略SQL语句添加tenantId字段条件

# 快速开始
- 在已经集成`Mybatis`项目中引入以下依赖

```
<dependency>
    <groupId>io.github.osinn</groupId>
    <artifactId>druid-multi-tenant-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

# 配置
```
--基础配置
mybatis:
  tenant:
    config:
      # 是否启用多租户插件
      enable: true
      # 数据库中租户ID的列名
      tenant-id-column: tenant_id

--完整配置参数   
mybatis:
  tenant:
    config:
      # 是否使用MyBatis拦截器方式修改sql
      enable: true
      # 数据库方言
      db-type: mysql
      # 是否忽略表按租户ID过滤,默认所有表都按租户ID过滤，指定表名称(区分大小写全等判断)
      ignore-table-name:
        - user_role
      ignore-match-table-alias
      # 匹配判断指定表别名称是否忽略表按租户ID过滤(区分大小写匹配判断)
        - temp # tempTableAlias
      # 根据表名前缀判断是否忽略表按租户ID过滤
      ignore-table-name-prefix: 
        - act_
      # 多数据源情况下指定忽略跳过的数据源名称（需要重写ITenantService接口中的ignoreDynamicDatasource方法自行提供获取当前执行的数据源名称）
      ignore-dynamic-datasource:
        - demoDataSource
      # 数据库中租户ID的列名
      tenant-id-column: tenant_id
```
- 如果SQL中表名不在 `ignore-table-name` 中，则去`ignore-match-table-alias`匹配查找
- 执行SQL中存在临时表可以约束指定 `ignore-match-table-alias` 匹配临时表别名来忽略临时表添加租户ID查询条件

# 实现提供获取多租户值接口
- 需要实现ITenantService接口提供获取多租户ID值

```
/**
 * 演示：提供多租户ID服务接口
 *
 * @author wency_cai
 */
@Service
public class TenantServiceImpl implements ITenantService<Integer>{

    @Override
    public List<Integer> getTenantIds() {
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
}
```
- 到此整合完成

# 租户ID从Mapper接口方法中传入
> 默认调用 ITenantService.getTenantIds() 接口方法获取租户ID
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

# 多租户忽略Mapper方法
```
public interface UserMapper {

    /**
     * 添加@IgnoreTenantIdField注解来忽略设置多租户字段
     */
    @IgnoreTenantIdField
    void deleteTestIgnoreTenantIdById(Long id);
}
```
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
