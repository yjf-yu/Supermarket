package servlet.AlesSlip;

import entity.salesSlip.SalesSlip;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;
import java.sql.SQLException;

/**
 * 查询销售单号
 */
@WebServlet(name = "SalesSlipServlet",urlPatterns = "/selectsalesslip")
public class SalesSlipServlet extends ServletBaservlet{
    @Override
    protected Result handle(JSONObject json) throws SQLException{
        //判断字段是否为空
        if(!json.containsKey ("OddNumbers")) return Result.PARA_ERROR;

       String OddNumbers = json.getString ("OddNumbers");
       if (OddNumbers == null || OddNumbers =="") return Result.PARA_ERROR;
       log.debug ("拿到销售单号：{}",OddNumbers);

        SalesSlip salesSlip = new SalesSlip ();
        Result result = salesSlip.select(OddNumbers);

        if (result.isSuccess ()){
            return result;
        }
        return result;
    }
}
