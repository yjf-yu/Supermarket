package entity;

import Util.DBUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.Result;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author yjf
 * @version 1.0
 * 2020/1/20
 * @date 2020/1/20 19:50
 */
public class TimeQuery {
    private static Logger log = LogManager.getLogger(TimeQuery.class.getName());
    private int id;
    private String startTime;
    private String endTime;
    private int state;

    public TimeQuery(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 根据时间查询24小时内的价格
     *
     * @param
     * @param
     * @return
     */
    public Result select() {
        DBUtil dbUtil = DBUtil.getDbUtil();
        Result result = Result.createSuccess();
        String sql = "select tq.time_slot,ifnull(t.amountMoney,0)as amountMoney from timequery tq left join" +
                "(select " +
                "hour(s.datetime) as kk,sum(amountMoney)as amountMoney " +
                "from salesslip s " +
                "where s.datetime between '" + startTime + "'and '" + endTime + "' " +
                "group by kk) t on t.kk = tq.time_slot " +
                "order by tq.time_slot ";
        log.debug("sql:{}", sql);

        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {

            JSONArray jsonArray = new JSONArray();
            while (re.next()) {
                JSONObject json = new JSONObject();

                json.put("time", re.getString("tq.time_slot"));
                json.put("amountMoney", re.getString("amountMoney"));

                jsonArray.add(json);
            }
            result.put("result", jsonArray);
        } catch (SQLException e) {
            log.debug("sql语句错误", e);
            return Result.UNKNOW;
        }
        return result;
    }

    /**
     * 查找 总金额和数量排行 前三
     *
     * @return
     */
    public Result totalAmountRanking() {
        DBUtil dbUtil = DBUtil.getDbUtil();
        Result result = Result.createSuccess();

        // 查询数量总数销售前三名
        String sql = "select sa.goodsid,sum(sa.number) number " +
                " from salesslip s " +
                " inner join salesdetails sa on sa.salesslipid = s.id " +
                " where s.datetime between '" + startTime + "'and '" + endTime + "' " +
                " group by sa.goodsid " +
                " order by number desc " +
                " limit 3 ";
        log.debug("sql:{}", sql);

        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {

            List list = new JSONArray();
            while (re.next()) {
                JSONObject json = new JSONObject();

                json.put("goodsid", re.getString("goodsid"));
                json.put("number", re.getString("number"));
                list.add(json);

            }
            result.put("SaleNumber", list);

            sql = " select sa.goodsid,sum(sa.price) as price " +
                    " from salesslip s " +
                    " inner join salesdetails sa on sa.salesslipid =s.id " +
                    " where s.datetime between '" + startTime + "' and '" + endTime + "' " +
                    " group by sa.goodsid " +
                    " order by price desc " +
                    " limit 3 ";

            Statement sta = conn.createStatement();
            ResultSet res = sta.executeQuery(sql);
            List jsonArray = new JSONArray();
            while (res.next()) {
                JSONObject json = new JSONObject();

                json.put("goodsid", res.getString("goodsid"));
                json.put("price", res.getString("price"));
                jsonArray.add(json);

            }
            result.put("SalePrice", jsonArray);


        } catch (SQLException e) {
            log.debug("sql语句错误", e);
            return Result.UNKNOW;
        }
        return result;
    }

    /**
     * 每个商品类别的销售额
     */
    public Result category() {
        DBUtil dbUtil = DBUtil.getDbUtil();
        Result result = Result.createSuccess();

        String sql = " select g.classid,sum(sal.price) as price " +
                " from salesslip sa " +
                " inner join salesdetails sal on sal.salesslipid = sa.id " +
                " inner join goods g on g.id = sal.goodsid " +
                " where sa.datetime between '" + startTime + "'and '" + endTime + "' " +
                " group by g.classid ";
        log.debug("sql:{}",sql);

        JSONArray jsonArray = new JSONArray();
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {
            while (re.next()) {
                JSONObject json = new JSONObject();

                json.put("class", re.getInt("g.classid"));
                json.put("amountMoney", re.getDouble("price"));

                jsonArray.add(json);
            }
            result.put("result", jsonArray);
        } catch (SQLException e) {
            log.debug("sql语句错误", e);
            return Result.UNKNOW;
        }
        return result;
    }
}
