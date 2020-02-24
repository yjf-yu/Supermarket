package entity.returnInstorage;

import entity.salesReturn.SalesReturn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yjf
 * @version 1.0
 * 2020/1/18
 * @date 2020/1/18 17:16
 */
public class ReturnInstoragedetails {
    private static Logger log = LogManager.getLogger(ReturnInstoragedetails.class.getName());

    private int id;
    private int returninstorageid;
    private int goodsid;
    private double purchaseprice;
    private double number;

    public ReturnInstoragedetails(int goodsid, double purchaseprice, int number){
        this.goodsid = goodsid;
        this.purchaseprice = purchaseprice;
        this.number = number;
    }

    /**
     * 	写入明细档
     * @param id
     * @param conn
     * @return
     */
    public boolean save(int id, Connection conn) {
        String sql = "insert into returninstoragedetails(returninstorageid,goodsid,number," +
                "purchaseprice) values (" + id + "," + goodsid + "," + number + "," + purchaseprice + ")";
        log.debug("sql:{}",sql);
        try (Statement st = conn.createStatement()){
            st.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("sql语句错误",e);
            return false;
        }

        return true;
    }
}
