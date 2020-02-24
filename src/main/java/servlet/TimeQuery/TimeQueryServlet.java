package servlet.TimeQuery;

import com.alibaba.fastjson.JSONObject;
import entity.TimeQuery;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

/**
 * @author yjf
 * @version 1.0
 * 2020/1/20
 * @date 2020/1/20 19:59
 */
@WebServlet(name = "TimeQuery", urlPatterns = "/timequery")
public class TimeQueryServlet extends ServletBaservlet {

    @Override
    protected Result handle(JSONObject json) {
        //判断是否有该key startTime endTime
        if (!json.containsKey("startTime") && !json.containsKey("endTime"))
            return Result.PARA_ERROR;


        String startTime = json.getString("startTime");
        String endTime = json.getString("endTime");
        log.debug("开始时间：{}，结束时间：{}",startTime,endTime);

        TimeQuery timeQuery = new TimeQuery(startTime,endTime);
        Result result = timeQuery.select();
        if (result.isSuccess()){
            return result;
        }

        return result;
    }

}
