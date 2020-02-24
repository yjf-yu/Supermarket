package servlet.Supplier;

import entity.Supplier;
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

@WebServlet(name = "UpdateSupplierServlet",urlPatterns = "/updatesupplier")
public class UpdateSupplierServlet extends HttpServlet{
    private static Logger log = LogManager.getLogger (UpdateSupplierServlet.class.getName ());
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        response.setCharacterEncoding ("utf-8");
        response.setHeader ("Content-Type", "application/json;charset=utf-8");

        JSONObject resultJson = new JSONObject ();
        String requestStr = "";

        try {

            //调用方法接收post请求
            requestStr = Utils.getRequestPostStr (request);
            JSONObject json = JSON.parseObject (requestStr);

            //得到 ID , name
            Integer id = json.getInteger ("id");
            String name = json.getString ("name");
            Long phone = json.getLong ("phone");
            log.debug ("id:{},name:{},phone：{}",id,name,phone);

            //效验数据
            if (id == null && name == null||name.trim ().isEmpty ()){

                response.getWriter ().println (Result.PARA_ERROR);//参数错误
                return;
            }

            Supplier supplier =Supplier.getSupplierById (id);

            if(supplier==null){

                response.getWriter ().println (Result.NON_EXISTENT);//id不存在
                return;
            }
            //调用修改方法
            Result result = supplier.Update (name,phone);


            if (result.isSuccess ()) {

                response.getWriter ().println (result);

            }else {

                response.getWriter ().println (result);

            }

        }catch (Exception e){
            log.error ("sql:{}", e);

            response.getWriter ().println (Result.UNKNOW);//未知异常
        }



    }
}
