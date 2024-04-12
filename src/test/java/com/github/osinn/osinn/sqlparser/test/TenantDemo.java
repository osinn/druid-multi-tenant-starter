package com.github.osinn.osinn.sqlparser.test;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcConstants;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author wency_cai
 */
public class TenantDemo {

    private static final String sql1 = "SELECT id, name,tenant_id FROM role";
    private static final String sql2 = "select * from user s where s.name='333'";
    private static final String sql3 = "select tenant_id from people where id in (select id from user s)";
    private static final String sql4 = "select tenant_id from people where id in (select id from user s) and id in (select u.id from user u)";
    private static final String sql5 = "select tenant_id from people where id " +
            "in (select id from user s) " +
            "and id in (select u.id from user u) " +
            "and tenant_id in (select u.tenant_id from user u)";
    private static final String sql6 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id) from people p";
    private static final String sql7 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id), (select s.name from user s where s.id=p.user_id) from people p";
    private static final String sql8 = "select p.tenant_id, (select u.name, (select s.name from user s where s.id=p.user_id) from user u where u.id=p.user_id) from people p";
    private static final String sql9 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql10 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    private static final String sql11 = "SELECT u.*, (select ur.name from role r where r.id=ur.role_id) as r_name FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    private static final String sql12 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r)";
    private static final String sql13 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r) AND ur.test_id in (select r.test_id from role r)";
    private static final String sql14 = "SELECT u.* FROM `user` u LEFT JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql15 = "SELECT u.* FROM `user` u RIGHT JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql16 = "SELECT COUNT( * ), id FROM `user` GROUP BY id HAVING COUNT( * ) >= 1";
    private static final String sql17 = "SELECT EXISTS ( SELECT * FROM `user` WHERE username = ?) d";
    private static final String sql18 = "SELECT EXISTS ( select tenant_id from people where id in (select id from user s)) d";
    private static final String sql19 = "SELECT username FROM `user`  union SELECT username FROM account";
    private static final String sql20 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22";
    private static final String sql21 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22 LEFT JOIN menu m on m.role_id = r.id";
    private static final String sql22 = "select a,b from (select * from table_a) temp where temp.a = 'a';";
    private static final String sql23 = "SELECT temp.* FROM (SELECT * FROM `user`) temp JOIN user_role ur ON ur.user_id=temp.id WHERE temp.a='a';";

    private static final String sql24 = "SELECT id, name,tenant_id FROM role where tenant_id=1";


    private static final String sql25 = "delete from user u where u.id = 1";
    private static final String sql26 = "delete from user where id in ( select id from user s )";
    private static final String sql27 = "DELETE FROM system_permission_data sp LEFT JOIN system_role_data_relation re ON sp.id=re.data_id LEFT JOIN system_role sr ON sr.id=re.role_id WHERE sr.id=1";

    private static final String sql28 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?)";
    private static final String sql29 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?) , (?,?,?) , (?,?,?)";

    private static final String sql30 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id =?";
    private static final String sql31 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(1,2,3)";
    private static final String sql32 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur)";
    private static final String sql33 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur where ur.id=1)";
    private static final String sql34 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 111 SET u.qr_code='1212'";
    private static final String sql35 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id in(SELECT urr.user_id from user_role urr) SET u.qr_code='1212'";
    private static final String sql36 = "update user" +
            "    set name =" +
            "    case " +
            "        when id = 1 then 'name1' " +
            "        when id = 2 then 'name2' " +
            "        when id = 3 then 'name3' " +
            "    end " +
            "    where id in (11,22,33,3);";


    private static final String sql37 = "SELECT vm.*FROM tbl_vehicle_maintenance vm JOIN tbl_vehicle v ON v.id=vm.vehicle_id AND v.plate LIKE concat('%','test111','%') WHERE vm.deleted=0 AND vm.id IN (\n" +
            "SELECT MAX(vl.id) FROM tbl_vehicle_maintenance vl INNER JOIN (\n" +
            "SELECT vehicle_id AS vid,MAX(created_time) AS time1 FROM tbl_vehicle_maintenance WHERE deleted=0 GROUP BY vehicle_id) vl1 ON vl.vehicle_id=vl1.vid AND vl.created_time=vl1.time1 WHERE vl1.vid IN ('11122','3333') GROUP BY vl.vehicle_id,vl.created_time)";


    private static final String sql38 = "SELECT t2.*FROM (\n" +
            "SELECT @r AS _id,(\n" +
            "SELECT @r :=parent_id FROM tbl_dept WHERE id=_id) AS parent_id,@s :=@s+1 AS sort FROM (\n" +
            "SELECT @r :=32,@s :=0) temp,tbl_dept WHERE @r> 0) temp1 JOIN tbl_dept t2 ON temp1._id=t2.id AND t2.cp_delete=0 ORDER BY temp1.sort DESC";

    private static final String sql39 = "SELECT id, name,tenant_id FROM role temp";

    private static final String sql40 = "SELECT *\n" +
            "        from\n" +
            "                (SELECT * from test_t1) a \n" +
            "                LEFT JOIN test_t2 pd ON a.depository_id = pd.id\n" +
            "                LEFT JOIN test_t3 pi ON a.order_no = pi.order_no\n" +
            "        ORDER BY a.part_no";

    private static final String sql41 = "SELECT *\n" +
            "        from\n" +
            "                (SELECT * from act_test_t1) a \n" +
            "                LEFT JOIN test_t2 pd ON a.depository_id = pd.id\n" +
            "                LEFT JOIN test_t3 pi ON a.order_no = pi.order_no\n" +
            "        ORDER BY a.part_no";

    private static final String sql42 = "DELETE a \n" +
            "FROM\n" +
            "\tact_test_t1 a\n" +
            "\tJOIN act_test_t2 b ON a.message_id = b.message_id \n" +
            "WHERE\n" +
            "\tb.test_id = '92fa9ed3-b0f8-11ee-9df6-f21898798332'";

    private static String sql43 = "SELECT t.* FROM (\n" +
            "\tSELECT u.username FROM `user`  u\n" +
            "\tunion \n" +
            "\tSELECT a.username FROM account a\n" +
            ") t";

    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
        TenantServiceImpl tenantService = new TenantServiceImpl();
        defaultSqlParser.setTenantService(tenantService);
        defaultSqlParser.setTenantInfoHandler(new TenantInfoHandler() {
            @Override
            public List getTenantIds() {
                Long tenantId = 1540616714700591104L;
                Long tenantId2 = 1540616731523944448L;
                List<Long> tenantIdList = new ArrayList<>();
                tenantIdList.add(tenantId);
//                tenantIdList.add(tenantId2);
                return tenantIdList;
            }

            @Override
            public DbType getDbType() {
                return JdbcConstants.MYSQL;
            }

            @Override
            public List<String> ignoreTableName() {
                return new ArrayList<>();
            }

            @Override
            public List<String> ignoreMatchTableAlias() {
                List<String> objects = new ArrayList<>();
                objects.add("temp");
                return objects;
            }

            @Override
            public List<String> ignoreTableNamePrefix() {
                List<String> objects = new ArrayList<>();
//                objects.add("act_");
                return objects;
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public List<String> ignoreDynamicDatasource() {
                return null;
            }
        });

        System.out.println(defaultSqlParser.setTenantParameter(sql1));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql2));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql3));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql4));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql5));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql6));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql7));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql8));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql9));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql10));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql11));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql12));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql13));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql14));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql15));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql16));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql17));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql18));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql19));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql20));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql21));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql22));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql23));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql24));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql25));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql26));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql27));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql28));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql29));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql30));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql31));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql32));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql33));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql34));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql35));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql36));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql37));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql38));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql39));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql40));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql41));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql42));
        System.out.println(defaultSqlParser.setTenantParameter(sql43));

    }
}
