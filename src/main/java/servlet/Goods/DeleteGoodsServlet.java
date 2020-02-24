package servlet.Goods;

import entity.Goods;
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

@WebServlet(name = "DeleteGoodsServlet",urlPatterns = "/deletegoods")
public class DeleteGoodsServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (DeleteGoodsServlet.class.getName ());
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        String requestStr = null;//初始化
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

            Goods goods = Goods.getGoodsById (id);

            //判断supplier是否为null
            if (goods == null){

                response.getWriter ().println (Result.NON_EXISTENT);//id不存在
                return;

            }

            Result result = goods.Delete();//删除操作

            if (result.isSuccess ()) {

                //返回成功
                response.getWriter ().println (result);

            }else {

                //返回失败
                response.getWriter ().println (result);

            }


        }catch (JSONException e){
            log.error ("请输入正确的格式,json:{}",requestStr, e);


            response.getWriter ().println (Result.FORMAT_ERROR);//格式错误
        }


    }

}
