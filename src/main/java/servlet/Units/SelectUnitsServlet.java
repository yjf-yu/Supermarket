package servlet.Units;

import entity.Units;
import Util.Utils;
import com.alibaba.fastjson.JSON;
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

@WebServlet(name = "SelectUnitsServlet",urlPatterns = "/selectunits")
public class SelectUnitsServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (SelectUnitsServlet.class.getName ());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        JSONObject resultJson = new JSONObject ();
        JSONObject result = new JSONObject ();
        String requestStr = "";
        try {
            //获取到前台的数据
            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            //获取json里的ID
            Integer id = json.getInteger ("id");
            //效验数据
            if (id == null) {
                log.debug ("id:{}",id);
                response.getWriter ().println (Result.PARA_ERROR);//参数错误
                return;
            }



            Units units = Units.getUnitsID (id);

            if (units == null) {
                log.debug ("units:{}",units);
                response.getWriter ().println (Result.NON_EXISTENT);//id不存在
                return;
            }

            resultJson.put ("code",0);
            resultJson.put ("message",result);

            result.put ("id",units.getId ());
            result.put ("name",units.getName ());
            response.getWriter ().println (resultJson);


        } catch (Exception e){
            log.error ("sql:{}", e);

            response.getWriter ().println (Result.UNKNOW);//未知异常
        }

    }

}
