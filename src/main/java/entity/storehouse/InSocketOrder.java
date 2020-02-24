package entity.storehouse;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 入库单
 */
public class InSocketOrder{
    private static Logger log = LogManager.getLogger (DetailsWarehousing.class.getName ());
    private static Calendar calendar = InitializationTime();
    private int id ;
    private double amountMoney = 0.00; //总金额
    private String orderNumber;//订单号
    private int saveOrderNumber;
    private String datetime = dateTime();

    private final List<DetailsWarehousing> details = new ArrayList<>();

    /**
     * 得到总金额
     * @param goods
     * @param number
     * @param purchaseprice
     */
    public void add(Goods goods, int number,double purchaseprice){
        details.add (new DetailsWarehousing(goods.getId (), purchaseprice, number));
        amountMoney += purchaseprice * number;
        this.orderNumber = createOrderNumber ();
    }

    public InSocketOrder() {
    }


    public Result save(int supplierid ){

        DBUtil dbUtil = DBUtil.getDbUtil ();

        String sql = "insert into insocketorder (supplierid,datetime,ordernumber,amountmoney) " +
                "values (" + supplierid +",'"+ datetime + "','"+ orderNumber +"'," + amountMoney + ")";

        log.debug ("sql:{}",sql);
        try (Connection conn = dbUtil.getConnection ();
            Statement st = conn.createStatement ();
            ){
            st.executeUpdate (sql);
            sql = "select max(id) as id  from insocketorder";
            ResultSet re = st.executeQuery(sql);
            if (re.next()){
                this.id = re.getInt("id");
            }
            for (DetailsWarehousing detail :details){
                if(!detail.save(conn,this.id)){
                    conn.rollback();
                    return  Result.UNKNOW;
                }
            }

            re.close();
        } catch (SQLException e) {
            log.debug ("sql语句错误：{}",e);
            return Result.UNKNOW;
        }
        return Result.createSuccess ();
    }

    private static synchronized Calendar InitializationTime(){
        Calendar calendar = Calendar.getInstance ();

        calendar.set (Calendar.MILLISECOND,0);
        calendar.set (Calendar.HOUR,0);
        calendar.set (Calendar.MONTH,0);
        calendar.set (Calendar.SECOND,0);
        calendar.add (Calendar.DATE,1);

        String sql = "select ifnull(max(ordernumber),'000') as ordernumber from insocketorder";
        DBUtil dbUtil = DBUtil.getDbUtil ();
        try(Connection conn = dbUtil.getConnection ();
            Statement st = conn.createStatement ();
            ResultSet re = st.executeQuery (sql)){

            if (re.next ()){
                String ordernumber = re.getString ("ordernumber");
                ordernumber = ordernumber.substring(ordernumber.length ()-3);
            }

        } catch (SQLException e) {
            log.debug ("连接失败：{}",e);
        }
        return calendar;
    }

    /**
     * 创建订单号
     *
     * @return
     */
    private String createOrderNumber(){

        SimpleDateFormat format = new SimpleDateFormat ("yyyyMMdd");
        String tiem = format.format (new Date ());
        return tiem + saveOrderNumber;
    }
    private String dateTime(){
        SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        String tiem = format.format (new Date ());
        return tiem;
    }

    /**
     * 创建流水号
     *
     * @return
     */
    private synchronized String serialNumber(){

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DATE, 1);
            saveOrderNumber = 0;
        }

        return String.format ("%03d",++saveOrderNumber );
    }

    public double getAmountMoney(){
        return amountMoney;
    }

    public int getId(){
        return id;
    }
}
