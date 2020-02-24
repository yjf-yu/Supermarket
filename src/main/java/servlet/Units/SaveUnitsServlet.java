package servlet.Units;

import entity.Units;
import Util.Utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlet.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "SavaUnitsServlet", urlPatterns = "/savaunits")
public class SaveUnitsServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (SaveUnitsServlet.class.getName ());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        String requestStr = "";
        try {
            //获取到前台的数据
            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            //获取需要的数据
            String name = json.getString ("name");
            log.debug ("name:{}", name);
            //效验数据
            if (name == null || name.trim ().isEmpty ()) {

                response.getWriter ().println (Result.PARA_ERROR);//参数错误
                return;
            }
            if (name != null || name.trim ().isEmpty ()){

                response.getWriter ().println (Result.PARA_ERROR);//参数错误
                return;

            }

            Units units = new Units (name);

            Result result = units.save ();

            //成功返回一个ID
            if (result.isSuccess ()) {
                result.put ("id", units.getId ());
            }

            response.getWriter ().println (result);
        } catch (JSONException e) {
            log.error ("请输入正确的格式,json:{}", requestStr, e);

            response.getWriter ().println (Result.FORMAT_ERROR);//格式错误
        } catch (SQLException e) {
            log.error ("sql错误，{}", e);

            response.getWriter ().println (Result.UNKNOW);//未知异常
        } catch (Exception e) {
            log.error ("sql:{}", e);

            response.getWriter ().println (Result.UNKNOW);//未知异常
        }
    }
}
