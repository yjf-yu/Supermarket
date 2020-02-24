package entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import Util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.SuccessResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 单位类
 */
public class Units{
    private static Logger log = LogManager.getLogger (Units.class.getName ());
    private int id;
    private String name;
    private int state;

    public Units(String name){
        this.name = name;
    }

    public Units(){
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getState(){
        return state;
    }


    /**
     * 插入一条数据到数据库
     *
     * @throws SQLException
     */
    public Result save() throws SQLException{
        DBUtil dbUtil = DBUtil.getDbUtil ();
        Connection conn = null;
        Statement st = null;
        ResultSet re = null;
        try {
            //获取数据库连接
            conn = dbUtil.getConnection ();
            //开启事务
            conn.setAutoCommit (false);
            //获取sql执行对象
            st = conn.createStatement ();

            //查询units这张表name对应的ID
            String sql1 = "select id from units where name = '" + name + "' and state = 0" ;

            re = st.executeQuery (sql1);
            if (re.next ()) {
                conn.rollback ();
                return Result.EXISTED;
            }


            String sql = "select id from units where name = '" + name + "' and state = 999";

            re = st.executeQuery (sql);
            if (re.next ()) {
                id = re.getInt (1);
                //
                sql = "update units set state = 0 where id = " + id;
                //执行Sql语句
                st.executeUpdate (sql);
                conn.commit ();
                return new SuccessResult ();
            }

            st = conn.createStatement ();
            //sql语句
            sql = "insert into units(name) values('" + name + "')";//单位
            log.debug ("sql:{}", sql);

            //执行sql语句
            st.executeUpdate (sql);

            //sql语句
            sql = "select max(id) as id from units";
            re = st.executeQuery (sql);
            while (re.next ()) {
                this.id = re.getInt ("id");
            }

            //提交事务
            conn.commit ();
            log.debug ("unit,id :{},name:{},status:{}", id, name, state);
        } catch (SQLException e) {
            log.error ("连接数据库失败！", e);

            if (conn != null) conn.rollback ();
            return Result.UNKNOW;
        } finally {
            //释放资源
            dbUtil.close (re, st, conn);
        }

        return new SuccessResult ();
    }

    /**
     * 得到ID并返回查询到的对应数据
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public static Units getUnitsID(Integer id){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql = "select * from units where id =  " + id + "  and state <> 999 " ;

        try (Connection conn = dbUtil.getConnection ();//获取数据库连接
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) {//返回一个结果集

            if (!re.next ()) {

                return null;

            }else {

                Units units = new Units (re.getString ("name"));

                units.id = re.getInt ("id");

                return units;
            }

        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return null;
        }
    }

    /**
     * 修改某字段name
     *
     * @param
     * @param name
     * @throws SQLException
     */
    public Result Update(String name){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql2 = "select * from units where name = '" + name + "'";

        //修改units表中的name字段,条件：id = id（前台传出的ID）
        String sql = "update units set name = '" + name + "' where id = " + id;

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

    /**
     * 逻辑删除，修改state字段状态
     * @return
     */
    public Result Delete(){
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql = "select state from units where state = 999  and id = " + id ;

        String sql1 = "update units set state = 999 where id = " + id;
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

    /**
     * List列表查询
     * @param page
     * @param rownumber
     * @return
     */
    public static Result listUnits(int page , int rownumber){
        Result result = Result.createSuccess ();

        JSONArray jsonArray = new JSONArray ();
        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql = "select * from units where state <> 999 order by id limit " + rownumber + " offset " + page * (rownumber - 1);
        log.debug ("sql:{}",sql);

        String sql1 = "select count(*) as count from units where state <> 999  ";
        log.debug ("sql:{}",sql1);

        try (Connection conn = dbUtil.getConnection ();
            Statement st = conn.createStatement ();
            ){

            ResultSet re = st.executeQuery (sql);

            while (re.next ()){
                JSONObject json = new JSONObject ();

                json.put ("id",re.getInt ("id"));
                json.put ("name",re.getString ("name"));
                json.put ("state",re.getInt ("state"));

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

            re.close();//释放资源

        } catch (SQLException e) {
            log.error ("连接数据库失败！:{}", e);

            return Result.UNKNOW;//未知异常

        }
        return result;
    }


}
