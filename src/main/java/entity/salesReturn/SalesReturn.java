package entity.salesReturn;

import entity.Goods;

import Util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.Result;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 销售单头档
 */
public class SalesReturn {
    private static Logger log = LogManager.getLogger(SalesReturn.class.getName());

    private int id;
    private String returntime = dateTime();
    private double amountMoney;
    private int saleOrderId;

    private final List<SalesReturnDetail> returndetails = new ArrayList<>();


    public double getAmountMoney() {
        return amountMoney;
    }

    public SalesReturn(int saleOrderId){
        this.saleOrderId = saleOrderId;
    }

    int getSaleOrderId(){
        return this.saleOrderId;
    }

    public int getId() {
        return id;
    }

    public String getReturntime() {
        return returntime;
    }

    /**
     * 退货
     *
     * @param goods
     * @param number
     */
    public void add(Goods goods, int number) {
        returndetails.add(new SalesReturnDetail(goods.getId(), goods.getPrice(), number,this));
        amountMoney += goods.getPrice() * number;
    }

    /**
     * 得到当前时间
     *
     * @return
     */
    private synchronized String dateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String tiem = format.format(new Date());
        return tiem;
    }

    /**
     * @return
     */
    public Result save() {
        DBUtil dbUtil = DBUtil.getDbUtil();
        String sql =
                "insert into salesreturn (returntime,amountmoney) values ('" + returntime + "'," + amountMoney + ")";
        Connection conn = null;
        Statement st = null;
        try {
            conn = dbUtil.getConnection();
            st = conn.createStatement();
            conn.setAutoCommit(false);

            st.executeUpdate(sql);

            ResultSet set = st.executeQuery("select max(id) as id from salesreturn");

            if(set.next()){
                this.id = set.getInt(1);
                log.trace("salesreturn id :{}",this.id);
            }

            for (SalesReturnDetail detail : returndetails) {
                if(!detail.save(this.id, conn)){
                    conn.rollback();
                    return  Result.UNKNOW;
                }
            }

            conn.commit();
        } catch (SQLException e) {
            log.error("sql语句错误", e);
            return Result.UNKNOW;
        } finally {
            dbUtil.close(st,conn);
        }

        return Result.createSuccess();
    }


}
