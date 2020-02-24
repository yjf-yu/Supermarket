package servlet.SalesReturn;

import entity.Goods;
import entity.salesSlip.SalesSlip;
import entity.salesReturn.SalesReturn;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;
import java.sql.SQLException;

@WebServlet(name = "Servlet", urlPatterns = "/salesreturn")
public class SalesReturnServlet extends ServletBaservlet {

    @Override
    protected Result handle(JSONObject json) throws SQLException {
        //效验数据是否俱全
        if (!json.containsKey("salesslipid") &&
                !json.containsKey("goodsList")) {
            log.debug("参数错误：{}：{}", json.containsKey("salesslipid"), json.containsKey("goodsList"));
            return Result.PARA_ERROR;
        }

        //得到销售单id
        int salesslipid = json.getInteger("salesslipid");
        SalesSlip salesSlip = new SalesSlip();
//        salesSlip.select();


        //生成销售退回单
        SalesReturn salesReturn = new SalesReturn(salesslipid);

        JSONArray goodsList = (JSONArray) json.get("goodsList");
        log.debug("拿到：{}列表", goodsList);
        for (Object sales : goodsList) {

            if (!(sales instanceof JSONObject)) return Result.PARA_ERROR;

            //判断goodsid number键存不存在
            if (!((JSONObject) sales).containsKey("goodsid") &&
                    ((JSONObject) sales).containsKey("number")) {
                log.debug("goodsid键或number不存在");
                return Result.PARA_ERROR;
            }

            int goodsid = ((JSONObject) sales).getInteger("goodsid");
            int number = ((JSONObject) sales).getInteger("number");
            log.debug("拿到商品的ID：{}，数量：{}", goodsid, number);

            //判断数量不能小于或等于零
            if (number < 0) {
                log.debug("这个商品：{}的数量：{}小于或等于零", goodsid, number);
                return Result.PARA_ERROR;
            }

            Goods goods = Goods.getGoodsById(goodsid);
            //销售退回单增加销售退回单明细
            salesReturn.add(goods, number);
        }

        //销售退回单写入头档
        Result result = salesReturn.save();
        if (!result.isSuccess()) {
            return result;
        }

//        //仓库增加库存
//        DetailsWarehousing detailsWarehousing = DetailsWarehousing.getIns();
//        result = detailsWarehousing.save(goodsList);
//        if (result.isSuccess()) {
//            return result;
//        }

        return result;
    }
}
