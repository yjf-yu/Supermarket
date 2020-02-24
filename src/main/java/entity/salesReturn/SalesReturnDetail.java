package entity.salesReturn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 销售退回明细
 */
public class SalesReturnDetail {
    private static Logger log = LogManager.getLogger(SalesReturnDetail.class.getName());

    private int id;
    private int goodsid;
    private double price;
    private int number;
    private final SalesReturn salesReturn;

    public SalesReturnDetail(int goods, double price, int number,SalesReturn salesReturn) {
        this.goodsid = goods;
        this.price = price;
        this.number = number;
        this.salesReturn = salesReturn;
    }

    public boolean save(int salesslipid,Connection conn) {
        String sql =
                "select * from salesdetails where goodsid = " + goodsid + " and salesslipid =  " + salesReturn.getSaleOrderId();
        log.debug("sql:{}", sql);

        try (Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {
            while (re.next()) {
                //易购数量
                int purchasedQuantity = re.getInt("number") ;
                //已退数量
                int quantityreturned = number + re.getInt("quantityreturned");

                log.debug("purchasedquantity :{},quantityreturned:{}",purchasedQuantity,
                        quantityreturned);

                if (purchasedQuantity >= quantityreturned) {
                    sql = "insert into salesreturndetails(salesreturnid,goodsid,price,number) values " +
                            "(" + salesslipid + "," + goodsid + "," + price + "," + number + ")";
                    log.debug("sql:{}",sql);

                    Statement st2 = conn.createStatement();
                    st2.executeUpdate(sql);

                    sql = "insert into salesdetails(quantityreturned) values(" + quantityreturned + ")";
                    log.debug("sql:{}",sql);
                    st2.executeUpdate(sql);
                    st2.close();
                }else {
                    log.debug("退货的数量大于购物的数量");
                    return false;
                }
            }
        } catch (SQLException e) {
            log.error("sql语句错误", e);
            return false;
        }

        return true;
    }
}



