package entity;

import Util.DBUtil;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.Result;
import servlet.SuccessResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * 商品类
 */
public class Goods{
    private static Logger log = LogManager.getLogger (Goods.class.getName ());

    private int id;
    private String name;
    private double price;

    private int classId;
    private int unitsId;
    private int supplierId;
    private int state;

    private String supperName = "";
    private String unitsName = "";
    private String className = "";


    public String getSupperName(){
        return supperName;
    }

    public String getUnitsName(){
        return unitsName;
    }

    public String getClassName(){
        return className;
    }

    public Goods(String name, double price, int classId, int unitsId,
                 int supplierId){

        this.name = name;
        this.price = price;
        this.classId = classId;
        this.unitsId = unitsId;
        this.supplierId = supplierId;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public double getPrice(){
        return price;
    }

    public int getClassId(){
        return classId;
    }

    public int getUnitsId(){
        return unitsId;
    }

    public int getSupplierId(){
        return supplierId;
    }

    public int getState(){
        return state;
    }

    public Result save(String name, double price, int classid, int unitsid,
                       int supplierid){

        DBUtil dbUtil = DBUtil.getDbUtil ();

        //查询goods这张表name对应的ID
        String sql = "select id from goods where name = '" + name + "' and" +
                " state = 0";
        try (Connection conn = dbUtil.getConnection (); //获取数据库连接
             Statement st = conn.createStatement ()) {
            //开启事务
            conn.setAutoCommit (false);

            ResultSet re = st.executeQuery (sql);
            if (re.next ()) {
                conn.rollback ();
                return Result.EXISTED;
            }

            //查找goods这张表的id ,条件：state状态999,name == 前台输入的name
            String sql1 = "select id from goods where name = '" + name + "' " +
                    "and state = 999";

            re = st.executeQuery (sql1);

            if (re.next ()) {
                id = re.getInt (1);
                //
                sql = "update goods set state = 0 where id = " + id;//查询state
                // 并把状态改为0
                //执行Sql语句
                st.executeUpdate (sql);
                conn.commit ();
                return new SuccessResult ();
            }

            //sql语句
            sql = "insert into goods(name,price,classid,unitsid,supplierid)" +
                    " values ('" + name + "'," + price + "," + classid + "," + unitsid + "," + supplierid + ")";

            log.debug ("sql:{}", sql);
            //执行sql语句
            st.executeUpdate (sql);

            sql = "select max(id) as id from goods";
            re = st.executeQuery (sql);

            while (re.next ()) {
                this.id = re.getInt ("id");
            }

            conn.commit ();
            re.close ();
        } catch (SQLException e) {
            log.error ("连接数据库失败：{}", e);

            return Result.UNKNOW;
        }

        return Result.createSuccess ();
    }

    /**
     * @param id
     * @return
     */
    public static Goods getGoodsById(int id){
        DBUtil dbUtil = DBUtil.getDbUtil ();


        String sql = "select go.*,un.name units" +
                " ,cl.name class,su.name supplier, su.phone from goods as go " +
                "inner join units un on go.unitsid = un.id " +
                "inner join class cl on go.classid = cl.id " +
                "inner join supplier su on go.supplierid = su.id " +
                "where go.id = " + id + " and go.state <> 999 ";

        log.debug ("SQL:{}", sql);
        try (Connection conn = dbUtil.getConnection ();//获取数据库连接
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) {//返回一个结果集

            if (!re.next ()) {
                return null;
            }

            Goods goods = new Goods (
                    re.getString ("name"),
                    re.getDouble ("price"),
                    re.getInt ("classid"),
                    re.getInt ("unitsid"),
                    re.getInt ("supplierid"));

            goods.id = re.getInt ("id");
            goods.supperName = re.getString ("supplier");
            goods.className = re.getString ("class");
            goods.unitsName = re.getString ("units");
            goods.state = re.getInt ("state");

            return goods;
        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);
            return null;
        }

    }


    public Result Update(String name, Double price, int unitsid, int classid,
                         int supplierid){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql2 = "select * from goods where name = '" + name + "'";

        String sql = "update goods set name = '" + name + "',price = '" + price +
                "',unitsid = " + unitsid + ",classid = " + classid + "," +
                " supplierid = " + supplierid + " where " +
                "id = " + id;

        log.debug ("sql:{}", sql);
        try (Connection conn = dbUtil.getConnection ();//连接数据库
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql2)) {

            if (re.next ()) {
                return Result.EXISTED;
            }
            //执行sql语句

            st.executeUpdate (sql);


        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;
        }

        return new SuccessResult ();
    }

    public Result Delete(){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql =
                "select state from goods where state = 999  and id = " + id;

        String sql1 = "update goods set state = 999 where id = " + id;

        try (Connection conn = dbUtil.getConnection ();//连接数据库
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) { //执行sql语句

            if (re.next ()) {

                return Result.DELETED;
            } else {

                st.executeUpdate (sql1);
            }


        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;
        }

        return new SuccessResult ();
    }

    public static Result getGoodsList(int page, int rownumber, JSONObject condition){
        Result result = Result.createSuccess ();

        JSONArray jsonArray = new JSONArray ();

        DBUtil dbUtil = DBUtil.getDbUtil ();

        String where = Goods.handleWhere (condition);

        String sql = "select go.*,un.name units" +
                " ,cl.name class,su.name supplier, su.phone from goods as go " +
                "inner join units un on go.unitsid = un.id " +
                "inner join class cl on go.classid = cl.id " +
                "inner join supplier su on go.supplierid = su.id  " + where + " order by id limit" +
                " " + rownumber + " offset " + page * (rownumber - 1);
        log.debug ("sql:{}", sql);

        String sql1 = "select count(*) as count from goods go " + where;
        log.debug ("sql:{}", sql1);

        String sql2 = "select * from goods go" + where ;


        try (Connection conn = dbUtil.getConnection ();
             Statement st = conn.createStatement ()
        ) {
            ResultSet re = st.executeQuery (sql2);
            if (!re.next ()){
                return Result.CONTENT_DOES_NOT_EXIST;
            }

             re = st.executeQuery (sql);

            while (re.next ()) {
                JSONObject json = new JSONObject ();

                json.put ("id", re.getInt ("id"));
                json.put ("name", re.getString ("name"));
                json.put ("price", re.getDouble ("price"));
//                json.put ("classid", re.getInt ("classid"));
//                json.put ("unitsid", re.getInt ("unitsid"));
//                json.put ("supplierid", re.getInt ("supplierid"));
                json.put("supperName",re.getString ("supplier"));
                json.put("className",re.getString ("class"));
                json.put ("unitsName",re.getString ("units"));
                json.put ("state", re.getInt ("state"));


                jsonArray.add (json);

            }
            log.debug ("jsonArray:{}", jsonArray);

            result.put ("unitslist", jsonArray);
            log.debug ("result:{}", result);

            re = st.executeQuery (sql1);


            while (re.next ()) {
                Integer count = re.getInt ("count");
                result.put ("count", count);
            }

            re.close ();
        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;

        }
        return result;

    }

    /**
     * 处理where
     *
     * @param condition
     * @return builder.toString ()
     */
    private static String handleWhere(JSONObject condition){
        StringBuilder builder = new StringBuilder (" where go.state < 999 ");

        if (condition == null) return builder.toString ();

        for (Map.Entry<String, Object> entry : condition.entrySet ()) {
            if (entry.getKey ().trim ().equals ("name")) {
                builder.append (" and go.name like '%")
                        .append (entry.getValue ())
                        .append ("%'");
            } else if (entry.getKey ().trim ().equals ("minPrice")) {
                builder.append (" and go.price >= ")
                        .append (entry.getValue ());
            } else if (entry.getKey ().trim ().equals ("maxPrice")) {
                builder.append (" and go.price <= ")
                        .append (entry.getValue ());
            } else {
                builder.append (" and go.")
                        .append (entry.getKey ())
                        .append (" = ")
                        .append (entry.getValue ());
            }
        }

        return builder.toString ();
    }

}

