package entity.storehouse;

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

/**
 * 库存
 */
public class DetailsWarehousing {
    private static Logger log = LogManager.getLogger(DetailsWarehousing.class.getName());

    private int goodsid;
    private double purchaseprice;
    private int number;

    public DetailsWarehousing(int goodsid, double purchaseprice, int number) {
        this.goodsid = goodsid;
        this.purchaseprice = purchaseprice;
        this.number = number;
    }

    public DetailsWarehousing() {
    }

    public boolean save(Connection conn, int id) {

        String sql =
                "select * from detailswarehousing where state <> 999 and goodsid = " + goodsid;
        log.debug("sql:{}", sql);

        try (Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {
            if (re.next()) {
                sql = "update detailswarehousing set number = number + " + number + " where goodsid = " + goodsid;

                log.debug("sql:{}", sql);

                st.executeUpdate(sql);
            } else {

                sql = "insert into detailswarehousing (goodsid ,number,purchaseprice,insocketorderid) values " +
                        "(" + goodsid + "," + number + "," + purchaseprice + "," + id + ")";
                log.debug("sql:{}", sql);

                st.executeUpdate(sql);
            }
        } catch (SQLException e) {
            log.debug("sql语句错误",e);
            return false;
        }
        return true;
    }

    /**
     * 减少库存操作
     *
     * @return
     */
    public Result update(JSONArray goodsList) {

        DBUtil dbUtil = DBUtil.getDbUtil();
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement()) {
            //开启事务
            conn.setAutoCommit(false);
            for (Object obj : goodsList) {
                int goodsid = ((JSONObject) obj).getInteger("goodsid");
                int number = ((JSONObject) obj).getInteger("number");

                String sql = "update detailswarehousing set number = number -" + number + " where " + "goodsid = " + goodsid;

                log.debug("sql:{}", sql);

                st.executeUpdate(sql);
            }
            //关闭事务
            conn.commit();
        } catch (SQLException e) {
            log.error("sql语句错误：{}", e);
            return Result.UNKNOW;
        }

        return Result.createSuccess();
    }

    /**
     * 查询商品是不是在入库单里面
     *
     * @param Goodsid
     */
    public boolean select(int Goodsid) {
        DBUtil dbUtil = DBUtil.getDbUtil();
        String sql = "select * from detailswarehousing where goodsid = " + Goodsid;
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {
            if (re.next()) {
                return true;
            }else {
                return false;
            }
        } catch (SQLException e) {
            log.error("sql语句错误", e);
            return false;
        }
    }
}
