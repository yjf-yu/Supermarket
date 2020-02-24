package servlet.TimeQuery;

import com.alibaba.fastjson.JSONObject;
import entity.TimeQuery;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author yjf
 * @version 1.0
 * 2020/2/2
 * @date 2020/2/2 0:39
 */
@WebServlet(name = "CategoryServlet",urlPatterns = "/category")
public class CategoryServlet extends ServletBaservlet {

    @Override
    protected Result handle(JSONObject json){
        //判断是否有该key startTime endTime
        if (!json.containsKey("startTime") && !json.containsKey("endTime"))
            return Result.PARA_ERROR;


        String startTime = json.getString("startTime");
        String endTime = json.getString("endTime");
        log.debug("开始时间：{}，结束时间：{}",startTime,endTime);

        TimeQuery timeQuery = new TimeQuery(startTime,endTime);
        Result result = timeQuery.category();
        if (result.isSuccess()){
            return result;
        }
        return result;
    }
}
