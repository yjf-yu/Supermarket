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

@WebServlet(name = "DeleteUnitsServlet", urlPatterns = "/deleteunits")
public class DeleteUnitsServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (DeleteUnitsServlet.class.getName ());
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        String requestStr = null;
        try {
            //接收前台请求
            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            //获取id
            Integer id = json.getInteger ("id");

            //效验
            if (id == null) {
                log.debug ("id:{}",id);

                response.getWriter ().println (Result.PARA_ERROR);//参数错误
            }

            Units units = Units.getUnitsID (id);

            if (units == null){

                response.getWriter ().println (Result.NON_EXISTENT);
                return;

            }

            Result result = units.Delete();//删除操作

            if (result.isSuccess ()) {

                response.getWriter ().println (result);

            }else {

                response.getWriter ().println (result);

            }


        }catch (JSONException e){
            log.error ("请输入正确的格式,json:{}",requestStr, e);


            response.getWriter ().println (Result.FORMAT_ERROR);//格式错误
        }

    }

}
