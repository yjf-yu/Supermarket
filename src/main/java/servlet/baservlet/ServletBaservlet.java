package servlet.baservlet;

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

@WebServlet(name = "ServletBaservlet")
public abstract class ServletBaservlet extends HttpServlet{
    protected static Logger log = LogManager.getLogger (ServletBaservlet.class.getName ());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        String requestStr = null;//初始化

        try {

            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            if (json == null)return;

            Result result= handle (json);
            log.debug ("result:{}",result);
            response.getWriter ().println (result);
        }catch (SQLException e){
            log.debug ("sql语句错误：{}",e);
            response.getWriter ().println (Result.PARA_ERROR);//格式错误
        } catch (JSONException e){
            log.error ("请输入正确的格式,json:{}", e);

            response.getWriter ().println (Result.FORMAT_ERROR);//格式错误
        } catch (Exception e){
            log.error ("UNKNOW ERROR:{}", e);

            response.getWriter ().println (Result.UNKNOW);//未知异常
        }

    }
    protected abstract Result handle(JSONObject json) throws SQLException;

}
