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

@WebServlet(name = "ListUnitsServlet", urlPatterns = "/listunits")
public class ListUnitsServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (ListUnitsServlet.class.getName ());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        String requestStr = "";
        try {
            //获取前台数据
            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            int page = json.getInteger ("page");
            int rownumber = json.getInteger ("rownumber");

            Result result = Units.listUnits (page,rownumber);

            if (result.isSuccess ()) {

                response.getWriter ().println (result);

            }else {

                response.getWriter ().println (result);
            }


        }catch (JSONException e) {
            log.error ("请输入正确的格式,json:{}", requestStr, e);

            response.getWriter ().println (Result.FORMAT_ERROR);//格式错误
        }

    }

}
