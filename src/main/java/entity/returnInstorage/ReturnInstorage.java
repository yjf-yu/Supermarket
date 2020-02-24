package entity.returnInstorage;

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
 * @author yjf
 * @version 1.0
 * 2020/1/18
 * @date 2020/1/18 17:15
 */
public class ReturnInstorage {
    private static Logger log = LogManager.getLogger(ReturnInstorage.class.getName());
    private static int saveOrderNumber;
    private static Calendar calendar = InitializationTime();

    private final List<ReturnInstoragedetails> detail = new ArrayList<>();
    private int id;
    private String ordernumber = createOrderNumber();
    private String returntime = dateTime();
    private double amountMoney;
    private double purchaseprice;

    public String getOrdernumber() {
        return ordernumber;
    }

    public String getReturntime() {
        return returntime;
    }

    public double getPurchaseprice() {
        return purchaseprice;
    }

    public double getAmountMoney() {
        return amountMoney;
    }

    /**
     * 生成入库退回明细增加到退回单
     *
     * @param Goodsid
     * @param number
     * @param supplierid
     * @return
     */
    public Result add(int Goodsid, int number, int supplierid) {

        DBUtil dbUtil = DBUtil.getDbUtil();
        String sql = "select de.number,de.purchaseprice,i.datetime,de.goodsid ,de.quantityreturned " +
                "from detailswarehousing de  " +
                " inner join insocketorder i on i.id = de.insocketorderid  " +
                " where de.goodsid = " + Goodsid + " " +
                " and de.number >=  de.quantityreturned and i.supplierid = " + supplierid + "" +
                " order by  datetime ";
        log.debug("sql:{}", sql);

        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {

            while (re.next()) {

                if (re.getInt("number") < number) {
                    detail.add(new ReturnInstoragedetails(
                            re.getInt("de.goodsid"),
                            re.getDouble("purchaseprice"),
                            re.getInt("number")));
                    sql = "update detailswarehousing set quantityreturned = quantityreturned" + number;
                    number = number - re.getInt("number");
                } else {
                    this.purchaseprice = re.getDouble("purchaseprice");
                    detail.add(new ReturnInstoragedetails(
                            re.getInt("de.goodsid"),
                            re.getDouble("purchaseprice"),
                            number));
                    sql = "update detailswarehousing set quantityreturned = quantityreturned" + number;
                    number = 0;
                    break;
                }

                this.amountMoney += purchaseprice * number;
                log.debug("purchaseprice:{},number:{}", purchaseprice, number);
            }

            st.executeUpdate(sql);
            if (number != 0) return Result.UNKNOW;

        } catch (SQLException e) {
            log.error("sql语句错误", e);
            return Result.UNKNOW;
        }

        return Result.createSuccess();
    }

    /**
     * 写入入库退回单头档数据
     *
     * @return
     */
    public Result save(int supplierid) {
        DBUtil dbUtil = DBUtil.getDbUtil();
        String sql = "insert into returninstorage(ordernumber,returntime,amountmoney,supplierid)" +
                "values ('" + ordernumber + "','" + returntime + "'," + amountMoney + "," + supplierid + ")";
        log.debug("sql:{}", sql);
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
        ) {
            // 开启事务
            conn.setAutoCommit(false);
            st.executeUpdate(sql);

            sql = "select max(id) as id from returninstorage";
            ResultSet re = st.executeQuery(sql);
            if (re.next()) {
                this.id = re.getInt("id");
            }

            for (ReturnInstoragedetails details : detail) {
                if (!details.save(this.id, conn)) {
                    conn.rollback();
                    return Result.UNKNOW;
                }
            }
            re.close();
            conn.commit();
        } catch (SQLException e) {
            log.error("sql语句错误", e);
            return Result.UNKNOW;
        }
        return Result.createSuccess();
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
     * 创建流水号
     *
     * @return
     */
    private synchronized String serialNumber() {
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DATE, 1);
            saveOrderNumber = 0;
            log.debug("reInit serialNumber.");
        }

        log.trace("now serialNumber is :{}", saveOrderNumber);
        return String.format("%03d", ++saveOrderNumber);
    }

    /**
     * 时间 + 流水号
     *
     * @return返回一个String类型
     */
    private String createOrderNumber() {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String tiem = format.format(new Date());
        return tiem + serialNumber();
    }

    private static synchronized Calendar InitializationTime() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, 1);

        String sql = "select ifnull(max(oddnumber),'000') as oddnumber from salesslip";
        log.debug("sql:{}", sql);
        DBUtil dbUtil = DBUtil.getDbUtil();
        try (Connection conn = dbUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet re = st.executeQuery(sql)) {
            if (re.next()) {
                String oddnumber = re.getString("oddnumber");
                saveOrderNumber = Integer.parseInt(oddnumber.substring(oddnumber.length() - 3));
            }

        } catch (SQLException e) {
            log.debug("连接失败。", e);
        }
        return calendar;
    }

}
