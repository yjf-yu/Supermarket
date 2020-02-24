package servlet.Class;

import entity.Class;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ListClassServlet",urlPatterns = "/listclass")
public class ListClassServlet extends ServletBaservlet{

    @Override
    protected Result handle(JSONObject json){

        int page = json.getInteger ("page");
        int rownumber = json.getInteger ("rownumber");

        Result result = Class.listClass (page,rownumber);

        if (result.isSuccess ()) {

            return Result.createSuccess ();

        }
        return result;
    }

}
