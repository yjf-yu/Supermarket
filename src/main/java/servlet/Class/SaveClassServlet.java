package servlet.Class;

import entity.Class;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SaveClassServlet",urlPatterns = "/saveclass")
public class SaveClassServlet extends ServletBaservlet{

    @Override
    protected Result handle(JSONObject json){
        //拿到需要的数据
        String name  = json.getString ("name");

        //效验数据
        if (name == null || name.trim ().isEmpty ()) {

           //参数错误
            return  Result.PARA_ERROR;
        }

        Class aClass = new Class (name);
        Result result = aClass.save (name);

        if (result == null){

            //参数错误
            return Result.PARA_ERROR ;
        }

        //成功返回一个ID
        if (result.isSuccess ()) {
            result.put ("id", aClass.getId ());
        }

        return result;
    }
}
