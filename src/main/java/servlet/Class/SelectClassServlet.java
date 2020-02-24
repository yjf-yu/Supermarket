package servlet.Class;

import entity.Class;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SelectClassServlet",urlPatterns = "/selectclass")
public class SelectClassServlet extends ServletBaservlet{


    @Override
    protected Result handle(JSONObject json){

        //获取json里的ID
        Integer id = json.getInteger ("id");
        //效验数据
        if (id == null) {
            log.debug ("id:{}",id);

            //参数错误
            return Result.PARA_ERROR;
        }

        Class aclass = Class.getClassById (id);

        if (aclass == null) {
            log.debug ("supplier:{}",aclass);

            //id不存在
            return Result.NON_EXISTENT;
        }
        Result result = Result.createSuccess ();

        result.put ("id",aclass.getId ());
        result.put ("name",aclass.getName ());

        return result;
    }
}
