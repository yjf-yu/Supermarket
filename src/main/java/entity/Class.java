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

/**
 * 商品类型
 */
public class Class {
    private static Logger log = LogManager.getLogger(Class.class.getName());

    private int id;
    private String name;
    private int state;

    public Class(String name) {
        this.name = name;

    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 插入一条数据到数据库
     *
     * @param name
     * @return
     */
    public Result save(String name) {
        DBUtil dbUtil = DBUtil.getDbUtil();

        //查询class这张表name对应的ID
        String sql = "select id from class where name = '" + name + "'";
        try (Connection conn = dbUtil.getConnection(); //获取数据库连接
             Statement st = conn.createStatement()) {
            //开启事务
            conn.setAutoCommit(false);

            ResultSet re = st.executeQuery(sql);
            if (re.next()) {
                conn.rollback();
                return Result.EXISTED;//内容已存在
            }

            //查找class这张表的id ,条件：state状态999,name == 前台输入的name
            String sql1 = "select id from class where name = '" + name + "' and state = 999";

            re = st.executeQuery(sql1);

            if (re.next()) {
                id = re.getInt(1);
                //
                sql = "update class set state = 0 where id = " + id;//查询state并把状态改为0
                //执行Sql语句
                st.executeUpdate(sql);
                conn.commit();
                return new SuccessResult();
            }

            //sql语句
            sql = "insert into class(name) values ('" + name + "')";//写入数据库

            log.debug("sql:{}", sql);
            //执行sql语句
            st.executeUpdate(sql);

            sql = "select max(id) as id from class";//查出最大的id返回前台
            re = st.executeQuery(sql);

            while (re.next()) {
                this.id = re.getInt("id");
            }

            conn.commit();//提交事务

            re.close();//释放资源

        } catch (SQLException e) {
            log.error("连接数据库失败：{}", e);

            return Result.UNKNOW;//未知异常
        }

        return Result.createSuccess();//返回成功
    }

    /**
     * 得到ID
     *
     * @param id
     * @return
     */
    public static Class getClassById(int id) {
        DBUtil dbUtil = DBUtil.getDbUtil();

        String sql = "select id,name from class where id = " + id + " and state <> 999 ";

        try (Connection conn = dbUtil.getConnection();//获取数据库连接
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {//返回一个结果集

            if (!re.next()) {

                return null;

            } else {

                Class aClass = new Class(re.getString("name"));

                aClass.id = re.getInt("id");

                return aClass;
            }

        } catch (SQLException e) {
            log.error("连接数据库失败！:{}", e);

            return null;
        }

    }

    /**
     * 修改商品类型
     *
     * @param name
     * @return
     */
    public Result Update(String name) {

        DBUtil dbUtil = DBUtil.getDbUtil();

        String sql2 = "select * from class where name = '" + name + "'";
        //修改units表中的name字段,条件：id = id（前台传出的ID）
        String sql = "update class set name = '" + name + "' where id = " + id;

        log.debug("sql:{}", sql);
        try (Connection conn = dbUtil.getConnection();//连接数据库
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql2)) {

            if (re.next()) {
                return Result.EXISTED;
            }
            //执行sql语句

            st.executeUpdate(sql);


        } catch (SQLException e) {
            log.error("连接数据库失败！:{}", e);

            return Result.UNKNOW;
        }

        return new SuccessResult();
    }

    /**
     * 逻辑删除，改动state 状态
     *
     * @return
     */
    public Result Delete() {

        DBUtil dbUtil = DBUtil.getDbUtil();

        String sql = "select state from class where state = 999  and id = " + id;

        String sql1 = "update class set state = 999 where id = " + id;

        try (Connection conn = dbUtil.getConnection();//连接数据库
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) { //执行sql语句

            if (re.next()) {

                return Result.DELETED;
            } else {

                st.executeUpdate(sql1);
            }


        } catch (SQLException e) {
            log.error("连接数据库失败！:{}", e);

            return Result.UNKNOW;
        }

        return new SuccessResult();
    }

    /**
     * 查询表里所有类型
     *
     * @param page
     * @param rownumber
     * @return
     */
    public static Result listClass(int page, int rownumber) {
        Result result = Result.createSuccess();

        JSONArray jsonArray = new JSONArray();
        DBUtil dbUtil = DBUtil.getDbUtil();

        String sql = "select * from class where state <> 999 order by id limit " + rownumber + " offset " + page * (rownumber - 1);
        log.debug("sql:{}", sql);

        String sql1 = "select count(*) as count from class where state <> 999  ";
        log.debug("sql:{}", sql1);

        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
        ) {
            ResultSet re = st.executeQuery(sql);

            while (re.next()) {
                JSONObject json = new JSONObject();

                json.put("id", re.getInt("id"));
                json.put("name", re.getString("name"));

                jsonArray.add(json);

            }
            log.debug("jsonArray:{}", jsonArray);

            result.put("unitslist", jsonArray);
            log.debug("result:{}", result);

            re = st.executeQuery(sql1);


            while (re.next()) {
                Integer count = re.getInt("count");
                result.put("count", count);
            }
            re.close();

        } catch (SQLException e) {
            log.error("连接数据库失败！:{}", e);

            return Result.UNKNOW;

        }
        return result;
    }


}
