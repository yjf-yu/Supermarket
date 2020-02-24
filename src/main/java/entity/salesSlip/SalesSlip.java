package entity.salesSlip;

import entity.Goods;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 销售单
 */
public class SalesSlip{
    private static Logger log = LogManager.getLogger (SalesSlip.class.getName ());

    private static Calendar calendar = InitializationTime ();
    private static int saveOrderNumber;

    private int id;
    private String datetime = dateTime ();
    private String oddnumber = createOrderNumber ();
    private double amountMoney; //销售总金额
    private double giveChange; //找零
    private double netreceipts; //实付

    private final List<Salesdetails> details = new ArrayList<> ();

    public SalesSlip(){
    }

    public double getNetreceipts(){
        return netreceipts;
    }

    public String getDatetim(){
        return datetime;
    }

    public List<Salesdetails> getDetails(){
        return details;
    }

    public double getAmountMoney(){
        return amountMoney;
    }

    public String getOddnumber(){
        return oddnumber;
    }

    public double getGiveChange(){
        return giveChange;
    }

    /**
     * 添加商品信息和价格到details表里面
     *
     * @param goods
     * @param number
     */
    public void add(Goods goods, int number){
        details.add (new Salesdetails (goods.getId (), goods.getPrice (), number));
        amountMoney += goods.getPrice () * number;
    }


    /**
     * 销售单写入数据库
     *
     * @return
     */
    public Result save(double netreceipts) throws SQLException{
        DBUtil dbUtil = DBUtil.getDbUtil ();
        Connection conn = null;
        Statement st = null;
        ResultSet re = null;
        try {
            conn = dbUtil.getConnection ();
            st = conn.createStatement ();
            conn.setAutoCommit (false);

            this.netreceipts = netreceipts;
            this.giveChange = netreceipts - amountMoney;

            String sql = "insert into salesslip (oddnumber,datetime,amountmoney,givechange," +
                    "netreceipts) values (" + oddnumber + ",'" + datetime + "'," + amountMoney + ","
                    + giveChange + "," + netreceipts + ")";
            log.debug ("sql:{}", sql);

            st.executeUpdate (sql);

            sql = "select max(id) as id from salesslip";
            re = st.executeQuery (sql);
            if (re.next ()) {
                this.id = re.getInt ("id");
            }
            for (Salesdetails salesdetails : details) {
                salesdetails.save (conn, this.id);
            }
            conn.commit ();
        } catch (SQLException e) {
            conn.rollback ();
            log.error ("sql语句错误:{}", e);

            return Result.UNKNOW;
        } finally {
            dbUtil.close (st, conn);
        }
        return Result.createSuccess ();
    }

    /**
     * 得到当前时间
     *
     * @return
     */
    private synchronized String dateTime(){
        SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        String tiem = format.format (new Date ());
        return tiem;
    }

    /**
     * 时间 + 流水号
     *
     * @return返回一个String类型
     */
    private String createOrderNumber(){

        SimpleDateFormat format = new SimpleDateFormat ("yyyyMMdd");
        String tiem = format.format (new Date ());
        return tiem + serialNumber ();
    }

    /**
     * 创建流水号
     *
     * @return
     */
    private synchronized String serialNumber(){
        if (System.currentTimeMillis () > calendar.getTimeInMillis ()) {
            calendar.add (Calendar.DATE, 1);
            saveOrderNumber = 0;
            log.debug ("reInit serialNumber.");
        }

        log.trace ("now serialNumber is :{}", saveOrderNumber);
        return String.format ("%03d", ++saveOrderNumber);
    }

    private static synchronized Calendar InitializationTime(){
        Calendar calendar = Calendar.getInstance ();

        calendar.set (Calendar.MILLISECOND, 0);
        calendar.set (Calendar.HOUR, 0);
        calendar.set (Calendar.MONTH, 0);
        calendar.set (Calendar.SECOND, 0);
        calendar.add (Calendar.DATE, 1);

        String sql = "select ifnull(max(oddnumber),'000') as oddnumber from salesslip";
        log.debug ("sql:{}", sql);
        DBUtil dbUtil = DBUtil.getDbUtil ();
        try (Connection conn = dbUtil.getConnection ();
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) {
            if (re.next ()) {
                String oddnumber = re.getString ("oddnumber");
                saveOrderNumber = Integer.parseInt (oddnumber.substring (oddnumber.length () - 3));
            }

        } catch (SQLException e) {
            log.debug ("连接失败。", e);
        }
        return calendar;
    }

    /**
     * 添加销售明细到销售头档里面
     *
     * @param goods
     * @param number
     */
    public void addSalesdetails(Goods goods, int number){
        Salesdetails salesdetails = new Salesdetails (goods.getId (), goods.getPrice (), number);
        this.details.add (salesdetails);
    }

    /**
     * 根据销售单号查询销售记录
     *
     * @param OddNumbers
     */
    public Result select(String OddNumbers){
        DBUtil dbUtil = DBUtil.getDbUtil ();
        Result result = Result.createSuccess ();

        String sql = "select sa.id, sa.oddnumber, sa.datetime,sa.amountmoney ,go.name,go.price,n" +
                ".name,c.name " +
                " from salesslip sa " +
                " inner join salesdetails sal on sa.id = sal.salesslipid " +
                " inner join goods go on go.id = sal.goodsid " +
                " inner join units n on go.unitsid = n.id " +
                " inner join class c on go.classid = c.id " +
                " where sa.oddnumber = " + OddNumbers + " and sa.state <> 999 ";
        log.debug ("sql:{}", sql);


        try (Connection conn = dbUtil.getConnection ();
             Statement st = conn.createStatement ();
             ResultSet re = st.executeQuery (sql)) {

            JSONObject json = new JSONObject ();
            JSONArray jsonArray = new JSONArray ();
            result.put ("SaleSslip", json);
            json.put ("detail", jsonArray);


            while (re.next ()) {

                JSONObject detailed = new JSONObject ();


                json.put ("id", re.getInt ("sa.id"));
                json.put ("oddnumber", re.getString ("sa.oddnumber"));
                json.put ("datetime", re.getString ("sa.datetime"));
                json.put ("amountmoney", re.getDouble ("sa.amountmoney"));


                jsonArray.add (detailed);
                detailed.put ("name", re.getString ("go.name"));
                detailed.put ("price", re.getDouble ("go.price"));
                detailed.put ("units", re.getString ("n.name"));
                detailed.put ("class", re.getString ("c.name"));
            }

            return result;
        } catch (SQLException e) {
            log.error ("sql语句错误。", e);
            return Result.UNKNOW;
        }
    }


}
