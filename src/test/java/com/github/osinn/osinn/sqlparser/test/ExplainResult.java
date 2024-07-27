package com.github.osinn.osinn.sqlparser.test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述
 *
 * @author wency_cai
 */
public class ExplainResult {

    public static final String sql1 = "SELECT id, name,tenant_id FROM role";
    public static final String sql2 = "select * from user s where s.name='333'";
    public static final String sql3 = "select tenant_id from people where id in (select id from user s)";
    public static final String sql4 = "select tenant_id from people where id in (select id from user s) and id in (select u.id from user u)";
    public static final String sql5 = "select tenant_id from people where id " +
            "in (select id from user s) " +
            "and id in (select u.id from user u) " +
            "and tenant_id in (select u.tenant_id from user u)";
    public static final String sql6 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id) from people p";
    public static final String sql7 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id), (select s.name from user s where s.id=p.user_id) from people p";
    public static final String sql8 = "select p.tenant_id, (select u.name, (select s.name from user s where s.id=p.user_id) from user u where u.id=p.user_id) from people p";
    public static final String sql9 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id";
    public static final String sql10 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    public static final String sql11 = "SELECT u.*, (select ur.name from role r where r.id=ur.role_id) as r_name FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    public static final String sql12 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r)";
    public static final String sql13 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r) AND ur.test_id in (select r.test_id from role r)";
    public static final String sql14 = "SELECT u.* FROM `user` u LEFT JOIN user_role ur ON ur.user_id = u.id";
    public static final String sql15 = "SELECT u.* FROM `user` u RIGHT JOIN user_role ur ON ur.user_id = u.id";
    public static final String sql16 = "SELECT COUNT( * ), id FROM `user` GROUP BY id HAVING COUNT( * ) >= 1";
    public static final String sql17 = "SELECT EXISTS ( SELECT * FROM `user` WHERE username = ?) d";
    public static final String sql18 = "SELECT EXISTS ( select tenant_id from people where id in (select id from user s)) d";
    public static final String sql19 = "SELECT username FROM `user`  union SELECT username FROM account";
    public static final String sql20 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22";
    public static final String sql21 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22 LEFT JOIN menu m on m.role_id = r.id";
    public static final String sql22 = "select a,b from (select * from table_a) temp where temp.a = 'a';";
    public static final String sql23 = "SELECT temp.* FROM (SELECT * FROM `user`) temp JOIN user_role ur ON ur.user_id=temp.id WHERE temp.a='a';";

    public static final String sql24 = "SELECT id, name,tenant_id FROM role where tenant_id=1";


    public static final String sql25 = "delete from user u where u.id = 1";
    public static final String sql26 = "delete from user where id in ( select id from user s )";
    public static final String sql27 = "DELETE FROM system_permission_data sp LEFT JOIN system_role_data_relation re ON sp.id=re.data_id LEFT JOIN system_role sr ON sr.id=re.role_id WHERE sr.id=1";

    public static final String sql28 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?)";
    public static final String sql29 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?) , (?,?,?) , (?,?,?)";

    public static final String sql30 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id =?";
    public static final String sql31 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(1,2,3)";
    public static final String sql32 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur)";
    public static final String sql33 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur where ur.id=1)";
    public static final String sql34 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 111 SET u.qr_code='1212'";
    public static final String sql35 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id in(SELECT urr.user_id from user_role urr) SET u.qr_code='1212'";
    public static final String sql36 = "update user" +
            "    set name =" +
            "    case " +
            "        when id = 1 then 'name1' " +
            "        when id = 2 then 'name2' " +
            "        when id = 3 then 'name3' " +
            "    end " +
            "    where id in (11,22,33,3);";


    public static final String sql37 = "SELECT vm.*FROM tbl_vehicle_maintenance vm JOIN tbl_vehicle v ON v.id=vm.vehicle_id AND v.plate LIKE concat('%','test111','%') WHERE vm.deleted=0 AND vm.id IN (\n" +
            "SELECT MAX(vl.id) FROM tbl_vehicle_maintenance vl INNER JOIN (\n" +
            "SELECT vehicle_id AS vid,MAX(created_time) AS time1 FROM tbl_vehicle_maintenance WHERE deleted=0 GROUP BY vehicle_id) vl1 ON vl.vehicle_id=vl1.vid AND vl.created_time=vl1.time1 WHERE vl1.vid IN ('11122','3333') GROUP BY vl.vehicle_id,vl.created_time)";


    public static final String sql38 = "SELECT t2.*FROM (\n" +
            "SELECT @r AS _id,(\n" +
            "SELECT @r :=parent_id FROM tbl_dept WHERE id=_id) AS parent_id,@s :=@s+1 AS sort FROM (\n" +
            "SELECT @r :=32,@s :=0) temp,tbl_dept WHERE @r> 0) temp1 JOIN tbl_dept t2 ON temp1._id=t2.id AND t2.cp_delete=0 ORDER BY temp1.sort DESC";

    public static final String sql39 = "SELECT id, name,tenant_id FROM role temp";

    public static final String sql40 = "SELECT *\n" +
            "        from\n" +
            "                (SELECT * from test_t1) a \n" +
            "                LEFT JOIN test_t2 pd ON a.depository_id = pd.id\n" +
            "                LEFT JOIN test_t3 pi ON a.order_no = pi.order_no\n" +
            "        ORDER BY a.part_no";

    public static final String sql41 = "SELECT *\n" +
            "        from\n" +
            "                (SELECT * from act_test_t1) a \n" +
            "                LEFT JOIN test_t2 pd ON a.depository_id = pd.id\n" +
            "                LEFT JOIN test_t3 pi ON a.order_no = pi.order_no\n" +
            "        ORDER BY a.part_no";

    public static final String sql42 = "DELETE a \n" +
            "FROM\n" +
            "\tact_test_t1 a\n" +
            "\tJOIN act_test_t2 b ON a.message_id = b.message_id \n" +
            "WHERE\n" +
            "\tb.test_id = '92fa9ed3-b0f8-11ee-9df6-f21898798332'";

    public static final String sql43 = "SELECT t.* FROM (\n" +
            "\tSELECT u.username FROM `user`  u\n" +
            "\tunion \n" +
            "\tSELECT a.username FROM account a\n" +
            ") t";
    public static final String sql44 = "SELECT t.* FROM (\n" +
            "\t\t SELECT u.username FROM `user`  u\n" +
            "\t\t\tunion \n" +
            "\t\t\tSELECT a.username FROM account a\n" +
            ") t JOIN  role r ON r.id = t.role_id";

    public static final String sql45 = "select a,b from test1 temp where temp.a = 'a' AND temp.id in(\n" +
            "\t\t SELECT u.id FROM `user`  u\n" +
            "\t\t\tunion \n" +
            "\t\t\tSELECT a.id FROM account a\n" +
            ")";

    public static final String sql46 = "SELECT\n" +
            "\tu.*,\n" +
            "\tr.id AS r_id,\n" +
            "\tr.NAME AS r_name \n" +
            "FROM\n" +
            "\t`user` u\n" +
            "\tJOIN user_role ur ON ur.user_id = u.id\n" +
            "\tJOIN role r ON r.id = ur.role_id \n" +
            "\tAND u.id = 22\n" +
            "\tJOIN menu m ON m.role_id = r.id\n" +
            "\tJOIN menut mt ON mt.role_id = m.id\n" +
            "\tJOIN menur mt2 ON mt2.role_id = m.id";
    public static final String sql47 = "INSERT INTO role SELECT * FROM role_test";
    public static final String sql48 = "INSERT INTO role SELECT id,`name`,tenant_id FROM role_test";
    public static final String sql49 = "INSERT INTO role SELECT id,`name` FROM role_test";
    public static final String sql50 = "INSERT INTO role (id,`name`) SELECT id,`name`,tenant_id FROM role_test";
    public static final String sql51 = "INSERT INTO role (id,`name`) SELECT id,`name` FROM role_test";

    public static final String sql52 = "SELECT id, name,tenant_id FROM role;SELECT id, name,tenant_id FROM role";

    public static final String sql53 = "INSERT INTO role (id, `name`, tenant_id)\n" +
            "SELECT rt.id, rt.`name`, rt.tenant_id\n" +
            "FROM role_test rt JOIN user_role ur ON ur.role_id = rt.id AND ur.user_id = 1";
    public static final Map<String, String> EXPLAIN_RESULT = new LinkedHashMap<>();

    static {
        EXPLAIN_RESULT.put(sql1,
                "SELECT id, name, tenant_id\n" +
                        "FROM role\n" +
                        "WHERE tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql2,
                "SELECT *\n" +
                        "FROM user s\n" +
                        "WHERE s.name = '333'\n" +
                        "\tAND s.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql3,
                "SELECT tenant_id\n" +
                        "FROM people\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT id\n" +
                        "\t\tFROM user s\n" +
                        "\t\tWHERE s.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql4,
                "SELECT tenant_id\n" +
                        "FROM people\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT id\n" +
                        "\t\tFROM user s\n" +
                        "\t\tWHERE s.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND id IN (\n" +
                        "\t\tSELECT u.id\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND tenant_id = 1540616714700591104");


        EXPLAIN_RESULT.put(sql5,
                "SELECT tenant_id\n" +
                        "FROM people\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT id\n" +
                        "\t\tFROM user s\n" +
                        "\t\tWHERE s.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND id IN (\n" +
                        "\t\tSELECT u.id\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND tenant_id IN (\n" +
                        "\t\tSELECT u.tenant_id\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\t)");

        EXPLAIN_RESULT.put(sql6,
                "SELECT p.tenant_id\n" +
                        "\t, (\n" +
                        "\t\tSELECT u.name\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.id = p.user_id\n" +
                        "\t\t\tAND u.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "FROM people p\n" +
                        "WHERE p.tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql7,
                "SELECT p.tenant_id\n" +
                        "\t, (\n" +
                        "\t\tSELECT u.name\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.id = p.user_id\n" +
                        "\t\t\tAND u.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\t, (\n" +
                        "\t\tSELECT s.name\n" +
                        "\t\tFROM user s\n" +
                        "\t\tWHERE s.id = p.user_id\n" +
                        "\t\t\tAND s.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "FROM people p\n" +
                        "WHERE p.tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql8,
                "SELECT p.tenant_id\n" +
                        "\t, (\n" +
                        "\t\tSELECT u.name\n" +
                        "\t\t\t, (\n" +
                        "\t\t\t\tSELECT s.name\n" +
                        "\t\t\t\tFROM user s\n" +
                        "\t\t\t\tWHERE s.id = p.user_id\n" +
                        "\t\t\t\t\tAND s.tenant_id = 1540616714700591104\n" +
                        "\t\t\t)\n" +
                        "\t\tFROM user u\n" +
                        "\t\tWHERE u.id = p.user_id\n" +
                        "\t\t\tAND u.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "FROM people p\n" +
                        "WHERE p.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql9,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql10,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND u.id = 22\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql11,
                "SELECT u.*\n" +
                        "\t, (\n" +
                        "\t\tSELECT ur.name\n" +
                        "\t\tFROM role r\n" +
                        "\t\tWHERE r.id = ur.role_id\n" +
                        "\t\t\tAND r.tenant_id = 1540616714700591104\n" +
                        "\t) AS r_name\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND u.id = 22\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql12,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.role_id IN (\n" +
                        "\t\t\tSELECT r.id\n" +
                        "\t\t\tFROM role r\n" +
                        "\t\t\tWHERE r.tenant_id = 1540616714700591104\n" +
                        "\t\t)\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql13,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.role_id IN (\n" +
                        "\t\t\tSELECT r.id\n" +
                        "\t\t\tFROM role r\n" +
                        "\t\t\tWHERE r.tenant_id = 1540616714700591104\n" +
                        "\t\t)\n" +
                        "\t\tAND ur.test_id IN (\n" +
                        "\t\t\tSELECT r.test_id\n" +
                        "\t\t\tFROM role r\n" +
                        "\t\t\tWHERE r.tenant_id = 1540616714700591104\n" +
                        "\t\t)\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql14,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tLEFT JOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql15,
                "SELECT u.*\n" +
                        "FROM `user` u\n" +
                        "\tRIGHT JOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql16,
                "SELECT COUNT(*), id\n" +
                        "FROM `user`\n" +
                        "WHERE tenant_id = 1540616714700591104\n" +
                        "GROUP BY id\n" +
                        "HAVING COUNT(*) >= 1");
        EXPLAIN_RESULT.put(sql17,
                "SELECT EXISTS (\n" +
                        "\t\tSELECT *\n" +
                        "\t\tFROM `user`\n" +
                        "\t\tWHERE username = ?\n" +
                        "\t\t\tAND tenant_id = 1540616714700591104\n" +
                        "\t) AS d");
        EXPLAIN_RESULT.put(sql18,
                "SELECT EXISTS (\n" +
                        "\t\tSELECT tenant_id\n" +
                        "\t\tFROM people\n" +
                        "\t\tWHERE id IN (\n" +
                        "\t\t\t\tSELECT id\n" +
                        "\t\t\t\tFROM user s\n" +
                        "\t\t\t\tWHERE s.tenant_id = 1540616714700591104\n" +
                        "\t\t\t)\n" +
                        "\t\t\tAND tenant_id = 1540616714700591104\n" +
                        "\t) AS d");
        EXPLAIN_RESULT.put(sql19,
                "SELECT username\n" +
                        "FROM `user`\n" +
                        "WHERE tenant_id = 1540616714700591104\n" +
                        "UNION\n" +
                        "SELECT username\n" +
                        "FROM account\n" +
                        "WHERE tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql20,
                "SELECT u.*, r.id AS r_id, r.NAME AS r_name\n" +
                        "FROM `user` u\n" +
                        "\tLEFT JOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN role r\n" +
                        "\tON r.id = ur.role_id\n" +
                        "\t\tAND u.id = 22\n" +
                        "\t\tAND r.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql21,
                "SELECT u.*, r.id AS r_id, r.NAME AS r_name\n" +
                        "FROM `user` u\n" +
                        "\tLEFT JOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN role r\n" +
                        "\tON r.id = ur.role_id\n" +
                        "\t\tAND u.id = 22\n" +
                        "\t\tAND r.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN menu m\n" +
                        "\tON m.role_id = r.id\n" +
                        "\t\tAND m.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql22,
                "SELECT a, b\n" +
                        "FROM (\n" +
                        "\tSELECT *\n" +
                        "\tFROM table_a\n" +
                        "\tWHERE tenant_id = 1540616714700591104\n" +
                        ") temp\n" +
                        "WHERE temp.a = 'a';");
        EXPLAIN_RESULT.put(sql23,
                "SELECT temp.*\n" +
                        "FROM (\n" +
                        "\tSELECT *\n" +
                        "\tFROM `user`\n" +
                        "\tWHERE tenant_id = 1540616714700591104\n" +
                        ") temp\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = temp.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE temp.a = 'a';");
        EXPLAIN_RESULT.put(sql24,
                "SELECT id, name, tenant_id\n" +
                        "FROM role\n" +
                        "WHERE tenant_id = 1");
        EXPLAIN_RESULT.put(sql25,
                "DELETE FROM user u\n" +
                        "WHERE u.id = 1\n" +
                        "\tAND u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql26,
                "DELETE FROM user\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT id\n" +
                        "\t\tFROM user s\n" +
                        "\t\tWHERE s.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql27,
                "DELETE FROM system_permission_data sp\n" +
                        "\tLEFT JOIN system_role_data_relation re\n" +
                        "\tON sp.id = re.data_id\n" +
                        "\t\tAND re.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN system_role sr\n" +
                        "\tON sr.id = re.role_id\n" +
                        "\t\tAND sr.tenant_id = 1540616714700591104\n" +
                        "WHERE sr.id = 1\n" +
                        "\tAND sp.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql28,
                "INSERT INTO `user` (`id`, `username`, `password`, tenant_id)\n" +
                        "VALUES (?, ?, ?, 1540616714700591104)");
        EXPLAIN_RESULT.put(sql29,
                "INSERT INTO `user` (`id`, `username`, `password`, tenant_id)\n" +
                        "VALUES (?, ?, ?, 1540616714700591104),\n" +
                        "\t(?, ?, ?, 1540616714700591104),\n" +
                        "\t(?, ?, ?, 1540616714700591104)");
        EXPLAIN_RESULT.put(sql30,
                "UPDATE user u\n" +
                        "SET ds = ?, u.name = ?, id = 'fdf', ddd = ?\n" +
                        "WHERE id = ?\n" +
                        "\tAND u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql31,
                "UPDATE user u\n" +
                        "SET ds = ?, u.name = ?, id = 'fdf', ddd = ?\n" +
                        "WHERE id IN (1, 2, 3)\n" +
                        "\tAND u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql32,
                "UPDATE user u\n" +
                        "SET ds = ?, u.name = ?, id = 'fdf', ddd = ?\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT ur.user_id\n" +
                        "\t\tFROM user_role ur\n" +
                        "\t\tWHERE ur.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql33,
                "UPDATE user u\n" +
                        "SET ds = ?, u.name = ?, id = 'fdf', ddd = ?\n" +
                        "WHERE id IN (\n" +
                        "\t\tSELECT ur.user_id\n" +
                        "\t\tFROM user_role ur\n" +
                        "\t\tWHERE ur.id = 1\n" +
                        "\t\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "\t)\n" +
                        "\tAND u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql34,
                "UPDATE `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND u.id = 111\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "SET u.qr_code = '1212'\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql35,
                "UPDATE `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND u.id IN (\n" +
                        "\t\t\tSELECT urr.user_id\n" +
                        "\t\t\tFROM user_role urr\n" +
                        "\t\t\tWHERE urr.tenant_id = 1540616714700591104\n" +
                        "\t\t)\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "SET u.qr_code = '1212'\n" +
                        "WHERE u.tenant_id = 1540616714700591104");
        EXPLAIN_RESULT.put(sql36,
                "UPDATE user\n" +
                        "SET name = CASE \n" +
                        "\tWHEN id = 1 THEN 'name1'\n" +
                        "\tWHEN id = 2 THEN 'name2'\n" +
                        "\tWHEN id = 3 THEN 'name3'\n" +
                        "END\n" +
                        "WHERE id IN (11, 22, 33, 3)\n" +
                        "\tAND tenant_id = 1540616714700591104;");


        EXPLAIN_RESULT.put(sql37,
                "SELECT vm.*\n" +
                        "FROM tbl_vehicle_maintenance vm\n" +
                        "\tJOIN tbl_vehicle v\n" +
                        "\tON v.id = vm.vehicle_id\n" +
                        "\t\tAND v.plate LIKE concat('%', 'test111', '%')\n" +
                        "\t\tAND v.tenant_id = 1540616714700591104\n" +
                        "WHERE vm.deleted = 0\n" +
                        "\tAND vm.id IN (\n" +
                        "\t\tSELECT MAX(vl.id)\n" +
                        "\t\tFROM tbl_vehicle_maintenance vl\n" +
                        "\t\t\tINNER JOIN (\n" +
                        "\t\t\t\tSELECT vehicle_id AS vid, MAX(created_time) AS time1\n" +
                        "\t\t\t\tFROM tbl_vehicle_maintenance\n" +
                        "\t\t\t\tWHERE deleted = 0\n" +
                        "\t\t\t\t\tAND tenant_id = 1540616714700591104\n" +
                        "\t\t\t\tGROUP BY vehicle_id\n" +
                        "\t\t\t) vl1\n" +
                        "\t\t\tON vl.vehicle_id = vl1.vid\n" +
                        "\t\t\t\tAND vl.created_time = vl1.time1\n" +
                        "\t\t\t\tAND vl1.tenant_id = 1540616714700591104\n" +
                        "\t\tWHERE vl1.vid IN ('11122', '3333')\n" +
                        "\t\t\tAND vl.tenant_id = 1540616714700591104\n" +
                        "\t\tGROUP BY vl.vehicle_id, vl.created_time\n" +
                        "\t)\n" +
                        "\tAND vm.tenant_id = 1540616714700591104");


        EXPLAIN_RESULT.put(sql38,
                "SELECT t2.*\n" +
                        "FROM (\n" +
                        "\tSELECT @r AS _id\n" +
                        "\t\t, (\n" +
                        "\t\t\tSELECT @r := parent_id\n" +
                        "\t\t\tFROM tbl_dept\n" +
                        "\t\t\tWHERE id = _id\n" +
                        "\t\t\t\tAND tenant_id = 1540616714700591104\n" +
                        "\t\t) AS parent_id, @s := @s + 1 AS sort\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT @r := 32, @s := 0\n" +
                        "\t) temp\n" +
                        "\t\tCROSS JOIN tbl_dept ON tenant_id = 1540616714700591104\n" +
                        "\tWHERE @r > 0\n" +
                        ") temp1\n" +
                        "\tJOIN tbl_dept t2\n" +
                        "\tON temp1._id = t2.id\n" +
                        "\t\tAND t2.cp_delete = 0\n" +
                        "\t\tAND t2.tenant_id = 1540616714700591104\n" +
                        "WHERE temp1.tenant_id = 1540616714700591104\n" +
                        "ORDER BY temp1.sort DESC");

        EXPLAIN_RESULT.put(sql39,
                "SELECT id, name, tenant_id\n" +
                        "FROM role temp");

        EXPLAIN_RESULT.put(sql40,
                "SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT *\n" +
                        "\tFROM test_t1\n" +
                        "\tWHERE tenant_id = 1540616714700591104\n" +
                        ") a\n" +
                        "\tLEFT JOIN test_t2 pd\n" +
                        "\tON a.depository_id = pd.id\n" +
                        "\t\tAND pd.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN test_t3 pi\n" +
                        "\tON a.order_no = pi.order_no\n" +
                        "\t\tAND pi.tenant_id = 1540616714700591104\n" +
                        "WHERE a.tenant_id = 1540616714700591104\n" +
                        "ORDER BY a.part_no");

        EXPLAIN_RESULT.put(sql41,
                "SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT *\n" +
                        "\tFROM act_test_t1\n" +
                        ") a\n" +
                        "\tLEFT JOIN test_t2 pd\n" +
                        "\tON a.depository_id = pd.id\n" +
                        "\t\tAND pd.tenant_id = 1540616714700591104\n" +
                        "\tLEFT JOIN test_t3 pi\n" +
                        "\tON a.order_no = pi.order_no\n" +
                        "\t\tAND pi.tenant_id = 1540616714700591104\n" +
                        "WHERE a.tenant_id = 1540616714700591104\n" +
                        "ORDER BY a.part_no");

        EXPLAIN_RESULT.put(sql42,
                "DELETE a\n" +
                        "FROM act_test_t1 a\n" +
                        "\tJOIN act_test_t2 b ON a.message_id = b.message_id\n" +
                        "WHERE b.test_id = '92fa9ed3-b0f8-11ee-9df6-f21898798332'");

        EXPLAIN_RESULT.put(sql43,
                "SELECT t.*\n" +
                        "FROM (\n" +
                        "\tSELECT u.username\n" +
                        "\tFROM `user` u\n" +
                        "\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\tUNION\n" +
                        "\tSELECT a.username\n" +
                        "\tFROM account a\n" +
                        "\tWHERE a.tenant_id = 1540616714700591104\n" +
                        ") t");

        EXPLAIN_RESULT.put(sql44,
                "SELECT t.*\n" +
                        "FROM (\n" +
                        "\tSELECT u.username\n" +
                        "\tFROM `user` u\n" +
                        "\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\tUNION\n" +
                        "\tSELECT a.username\n" +
                        "\tFROM account a\n" +
                        "\tWHERE a.tenant_id = 1540616714700591104\n" +
                        ") t\n" +
                        "\tJOIN role r\n" +
                        "\tON r.id = t.role_id\n" +
                        "\t\tAND r.tenant_id = 1540616714700591104\n" +
                        "WHERE t.tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql45,
                "SELECT a, b\n" +
                        "FROM test1 temp\n" +
                        "WHERE temp.a = 'a'\n" +
                        "\tAND temp.id IN (\n" +
                        "\t\tSELECT u.id\n" +
                        "\t\tFROM `user` u\n" +
                        "\t\tWHERE u.tenant_id = 1540616714700591104\n" +
                        "\t\tUNION\n" +
                        "\t\tSELECT a.id\n" +
                        "\t\tFROM account a\n" +
                        "\t\tWHERE a.tenant_id = 1540616714700591104\n" +
                        "\t)");




        EXPLAIN_RESULT.put(sql46,
                "SELECT u.*, r.id AS r_id, r.NAME AS r_name\n" +
                        "FROM `user` u\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.user_id = u.id\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "\tJOIN role r\n" +
                        "\tON r.id = ur.role_id\n" +
                        "\t\tAND u.id = 22\n" +
                        "\t\tAND r.tenant_id = 1540616714700591104\n" +
                        "\tJOIN menu m\n" +
                        "\tON m.role_id = r.id\n" +
                        "\t\tAND m.tenant_id = 1540616714700591104\n" +
                        "\tJOIN menut mt\n" +
                        "\tON mt.role_id = m.id\n" +
                        "\t\tAND mt.tenant_id = 1540616714700591104\n" +
                        "\tJOIN menur mt2\n" +
                        "\tON mt2.role_id = m.id\n" +
                        "\t\tAND mt2.tenant_id = 1540616714700591104\n" +
                        "WHERE u.tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql47,
                "INSERT INTO role\n" +
                        "SELECT *\n" +
                        "FROM role_test\n" +
                        "WHERE tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql48,
                "INSERT INTO role\n" +
                        "SELECT id, `name`, tenant_id\n" +
                        "FROM role_test\n" +
                        "WHERE tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql49,
                "INSERT INTO role\n" +
                        "SELECT id, `name`, tenant_id\n" +
                        "FROM role_test\n" +
                        "WHERE tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql50,
                "INSERT INTO role (id, `name`, tenant_id)\n" +
                        "SELECT id, `name`, tenant_id\n" +
                        "FROM role_test\n" +
                        "WHERE tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql51,
                "INSERT INTO role (id, `name`, tenant_id)\n" +
                        "SELECT id, `name`, tenant_id\n" +
                        "FROM role_test\n" +
                        "WHERE tenant_id = 1540616714700591104");

        EXPLAIN_RESULT.put(sql52,
                "SELECT id, name, tenant_id\n" +
                        "FROM role\n" +
                        "WHERE tenant_id = 1540616714700591104;SELECT id, name, tenant_id\n" +
                        "FROM role\n" +
                        "WHERE tenant_id = 1540616714700591104");


        EXPLAIN_RESULT.put(sql53,
                "INSERT INTO role (id, `name`, tenant_id)\n" +
                        "SELECT rt.id, rt.`name`, rt.tenant_id\n" +
                        "FROM role_test rt\n" +
                        "\tJOIN user_role ur\n" +
                        "\tON ur.role_id = rt.id\n" +
                        "\t\tAND ur.user_id = 1\n" +
                        "\t\tAND ur.tenant_id = 1540616714700591104\n" +
                        "WHERE rt.tenant_id = 1540616714700591104");
    }

    public static Map<String, String> getExplainResult() {
        return EXPLAIN_RESULT;
    }

}
