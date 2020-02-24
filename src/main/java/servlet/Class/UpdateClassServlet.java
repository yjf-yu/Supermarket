package servlet.Class;

import entity.Class;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "UpdateClassServlet",urlPatterns = "/updateclass")
public class UpdateClassServlet extends ServletBaservlet{

    @Override
    protected Result handle(JSONObject json){

        //得到 ID , name
        Integer id = json.getInteger ("id");
        String name = json.getString ("name");
        log.debug ("id:{},name:{},phone：{}",id,name);

        //效验数据
        if (id == null && name == null||name.trim ().isEmpty ()){

            //参数错误
            return Result.PARA_ERROR;
        }

        Class aClass =Class.getClassById (id);

        if(aClass==null){

             //id不存在
            return Result.NON_EXISTENT;
        }
        //调用修改方法
        Result result = aClass.Update (name);


        if (result.isSuccess ()) {

            return Result.createSuccess ();

        }

        return result;
    }

}
