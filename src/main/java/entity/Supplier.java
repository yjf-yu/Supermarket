package entity;

import entity.returnInstorage.ReturnInstorage;
import entity.storehouse.InSocketOrder;
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

/**
 * 供应商
 */
public class Supplier{
    private static Logger log = LogManager.getLogger (Supplier.class.getName ());

    private int id;
    private String name;
    private double money;

    private int phone;
    private int state;


    public Supplier(String name){
        this.name = name;
    }

    public Supplier() {
    }

    public int getId(){
        return id;
    }


    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public double getMoney(){
        return money;
    }

    public int getPhone(){
        return phone;
    }


    public int getState(){
        return state;
    }


    public Result save(String name , Long phone){
        DBUtil dbUtil = DBUtil.getDbUtil ();

       //查询supplier这张表name对应的ID
        String sql =  "select id from supplier where name = '" + name + "' and state = 0";
        try(Connection conn = dbUtil.getConnection (); //获取数据库连接
            Statement st = conn.createStatement ()) {
            //开启事务
            conn.setAutoCommit (false);

            ResultSet re = st.executeQuery (sql);
            if (re.next ()){
                conn.rollback ();
                return Result.EXISTED;
            }

            //查找supplier这张表的id ,条件：state状态999,name == 前台输入的name
            String sql1 = "select id from supplier where name = '" + name + "' and state = 999";

            re = st.executeQuery (sql1);

            if (re.next ()) {
                id = re.getInt (1);
                //
                sql = "update supplier set state = 0 where id = " + id;//查询state并把状态改为0
                //执行Sql语句
                st.executeUpdate (sql);
                conn.commit ();
                return new SuccessResult ();
            }

            //sql语句
            sql = "insert into supplier(name,phone) values ('" + name + "',"+ phone +")";

            log.debug ("sql:{}",sql);
            //执行sql语句
            st.executeUpdate (sql);

            sql = "select max(id) as id from supplier";
            re = st.executeQuery (sql);

            while (re.next ()) {
                this.id = re.getInt ("id");
            }

            conn.commit ();
            re.close ();

        } catch (SQLException e) {
            log.error ("连接数据库失败：{}",e);

            return Result.UNKNOW;
        }

        return Result.createSuccess ();
    }

    public static Supplier getSupplierById(int id){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql = "select id,name,phone from supplier where id = " + id + " and state <> 999 "  ;

        try (Connection conn = dbUtil.getConnection ();//获取数据库连接
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) {//返回一个结果集

            if (!re.next ()) {

                return null;

            }else {

                Supplier supplier = new Supplier (re.getString ("name"));

                supplier.id = re.getInt ("id");

                return supplier;
            }

        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return null;
        }
    }


    public Result Update(String name,Long phone){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql2 = "select * from supplier where name = '" + name + "'";
        //修改units表中的name字段,条件：id = id（前台传出的ID）
        String sql = "update supplier set name = '" + name + "',phone = '" + phone + "' where id = " + id;

        log.debug ("sql:{}",sql);
        try (Connection conn = dbUtil.getConnection ();//连接数据库
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql2)) {

            if (re.next ()){
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

        String sql = "select state from supplier where state = 999  and id = " + id ;

        String sql1 = "update supplier set state = 999 where id = " + id;

        try (Connection conn = dbUtil.getConnection ();//连接数据库
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql) ) { //执行sql语句

            if (re.next ()){

                return Result.DELETED;
            }else {

                st.executeUpdate(sql1);
            }




        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;
        }

        return new SuccessResult ();
    }

    public static Result listSupplier(int page , int rownumber){
        Result result = Result.createSuccess ();

        JSONArray jsonArray = new JSONArray ();
        DBUtil dbUtil = DBUtil.getDbUtil ();
        String sql = "select * from supplier where state <> 999 order by id limit " + rownumber + " offset " + page * (rownumber - 1);
        log.debug ("sql:{}",sql);
        String sql1 = "select count(*) as count from supplier where state <> 999  ";
        log.debug ("sql:{}",sql1);
        try (Connection conn = dbUtil.getConnection ();
             Statement st = conn.createStatement ();
        ){
            ResultSet re = st.executeQuery (sql);

            while (re.next ()){
                JSONObject json = new JSONObject ();

                json.put ("id",re.getInt ("id"));
                json.put ("name",re.getString ("name"));

                jsonArray.add(json);

            }
            log.debug ("jsonArray:{}",jsonArray);

            result.put ("unitslist",jsonArray);
            log.debug ("result:{}",result);

            re = st.executeQuery (sql1);


            while (re.next ()){
                Integer count = re.getInt("count");
                result.put ("count",count);
            }
            re.close();
        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;

        }
        return result;
    }

    /**
     * 给供应商加钱动作
     * @param inSocketOrder
     * @return
     */
    public Result addMoney(InSocketOrder inSocketOrder) {
        this.money = inSocketOrder.getAmountMoney();
//        this.money += money;
     // update database
        DBUtil dbUtil = DBUtil.getDbUtil ();

        try(Connection conn = dbUtil.getConnection ();
            Statement st = conn.createStatement ()) {
            String sql = "update supplier set money = money + " + this.money;

            log.debug ("sql:{}",sql);
            st.executeUpdate (sql);

        } catch (SQLException e) {
            log.debug ("sql错误:{}",e);

            return Result.UNKNOW;
        }

        return Result.createSuccess ();
    }

    /**
     * 供应商减钱
     * @param returnInstorage
     * @return
     */
    public Result reduceMoney(ReturnInstorage returnInstorage) {
        this.money = returnInstorage.getAmountMoney();

        DBUtil dbUtil = DBUtil.getDbUtil ();

        try(Connection conn = dbUtil.getConnection ();
            Statement st = conn.createStatement ()) {
            String sql = "update supplier set money = money - " + this.money;

            log.debug ("sql:{}",sql);
            st.executeUpdate (sql);

        } catch (SQLException e) {
            log.debug ("sql错误:{}",e);

            return Result.UNKNOW;
        }

        return Result.createSuccess ();
    }
}
