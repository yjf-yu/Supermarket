package servlet.Class;

import entity.Class;

import com.alibaba.fastjson.JSONObject;

import servlet.Result;
import servlet.baservlet.ServletBaservlet;


import javax.servlet.annotation.WebServlet;


@WebServlet(name = "DeleteClassServlet",urlPatterns = "/deleteclass")
public class DeleteClassServlet extends ServletBaservlet{

    @Override
    protected Result handle(JSONObject json){
        //获取id
        Integer id = json.getInteger ("id");

        //效验
        if (id == null) {
            log.debug ("id:{}",id);

            return Result.PARA_ERROR;//参数错误
        }

        Class aClass = Class.getClassById (id);

        //判断supplier是否为null
        if (aClass == null){

            //id不存在
            return Result.NON_EXISTENT;

        }

        Result result = aClass.Delete();//删除操作

        if (result.isSuccess ()) {

            //返回成功
           return Result.createSuccess ();
        }

        return result;
    }






}
