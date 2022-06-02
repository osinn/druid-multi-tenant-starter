# druid-multi-tenant-starter
> 目标多租户插件，开箱即用，支持单个租户以及多个租户

- 未完待续...

# select语句
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
# update语句
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
# insert语句
```sql
INSERT INTO `user` (`id`, `username`, `password`, tenant_id)
VALUES (?, ?, ?, 11),
	(?, ?, ?, 11),
	(?, ?, ?, 11)
```
# delete语句
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