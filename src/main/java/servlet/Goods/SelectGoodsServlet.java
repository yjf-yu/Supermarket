package servlet.Goods;

import entity.Goods;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SelectGoodsServlet",urlPatterns = "/selectgoods")
public class SelectGoodsServlet extends ServletBaservlet{


    @Override
    protected Result handle(JSONObject json){
        JSONObject jsongoods = new JSONObject ();
        //获取json里的ID
        Integer id = json.getInteger ("id");
        //效验数据
        if (id == null) {
            log.debug ("id:{}",id);

           //参数错误
            return Result.PARA_ERROR;
        }


        Goods goods = Goods.getGoodsById (id);

        if (goods == null) {
            log.debug ("goods:{}",goods);

            //id不存在
            return Result.NON_EXISTENT;
        }
        Result result = Result.createSuccess ();
        if (result.isSuccess ()){

            result.put ("goods",jsongoods);

        }

        jsongoods.put ("id",goods.getId ());
        jsongoods.put ("name",goods.getName ());
        jsongoods.put ("price",goods.getPrice ());
        jsongoods.put ("units",goods.getUnitsName ());
        jsongoods.put ("class",goods.getClassName ());
        jsongoods.put ("supplier",goods.getSupperName ());
        jsongoods.put ("state",goods.getState ());

        return result;
    }


}
