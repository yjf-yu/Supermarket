package entity.salesSlip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 销售明细
 */
public class Salesdetails{
    private static Logger log = LogManager.getLogger (Salesdetails.class.getName ());
    private int goodsid;
    private double price;
    private int number;

    public Salesdetails(int goodsid, double price, int number){
        this.goodsid = goodsid;
        this.price = price;
        this.number = number;
    }


    public int getGoodsid() {
        return goodsid;
    }

    public double getPrice() {
        return price;
    }

    public int getNumber() {
        return number;
    }

    /**
     * 销售明细写入明细档
     *
     * @return
     */
    public Result save(Connection conn,int id){
        String sql =
                "insert into salesdetails (goodsid,price,number,salesslipid) values (" + goodsid +
                        "," + price + "," + number + "," + id + ")";
        log.debug ("sql:{}", sql);

        try (Statement st = conn.createStatement ()) {

            st.executeUpdate (sql);
        } catch (SQLException e) {
            log.error ("sql语句错误：{}", e);
            return Result.UNKNOW;
        }

        return Result.createSuccess ();
    }



}
