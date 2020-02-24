package servlet.AlesSlip;

import entity.*;
import entity.salesSlip.SalesSlip;
import entity.storehouse.DetailsWarehousing;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;
import java.sql.SQLException;

/**
 * 销售单
 */
@WebServlet(name = "AlesSlipServlet", urlPatterns = "/alesslip")
public class AlesSlipServlet extends ServletBaservlet{
    @Override
    protected Result handle(JSONObject json) throws SQLException{
        //判断字段是否为空
        if (!json.containsKey ("goodsList")
                || !json.containsKey ("netreceipts")
                || json.getJSONArray ("goodsList") == null
                || json.getDouble ("netreceipts") == null) {

            log.debug ("goodsList:{}错误 或者netreceipts：{}错误",json.containsKey ("goodsList"),json.containsKey ("netreceipts"));
            return Result.PARA_ERROR;
        }

        //判断商品是否在商品表里
        JSONArray goodsList = (JSONArray) json.get ("goodsList");

        //生成销售单
        SalesSlip salesSlip = new SalesSlip ();

        for (Object goodsobj : goodsList) {
            if (!(goodsobj instanceof JSONObject)) return Result.PARA_ERROR;

            /*if (!((JSONObject) goodsobj).containsKey ("goodsid") ||
                    ((JSONObject) goodsobj).containsKey ("number") ||
                    ((JSONObject) goodsobj).getInteger ("id") == null ||
                    ((JSONObject) goodsobj).getInteger ("number") == null ) {
                log.debug ("goodsobj:{}", goodsobj);
                return Result.PARA_ERROR;
            }*/

            //循环拿到商品id和数量
            int goodsid = ((JSONObject) goodsobj).getInteger ("goodsid");
            int number = ((JSONObject) goodsobj).getInteger ("number");

            //判断数量不能小于或等于零
            if (number <= 0) {
                log.debug ("这个商品：{}的数量：{}小于或等于零", goodsid, number);
                return Result.PARA_ERROR;
            }

            //判断商品是否在库
            Goods goods = Goods.getGoodsById (goodsid);
            if (goods == null) {
                log.debug ("没找到此ID：{}",goods);
                return Result.NON_EXISTENT;
            }

            //销售单增加销售单明细
            salesSlip.add (goods, number);


        }

        DetailsWarehousing DetailsWarehousing = new DetailsWarehousing();
        //仓库减少库存
        Result result = DetailsWarehousing.update (goodsList);
        if (!result.isSuccess ()) return result;
        log.debug ("result:{}",result);

        //销售单写入数据库0
        double netreceipts = json.getDouble ("netreceipts");
        log.debug ("拿到实付价格:{}",netreceipts);

        result = salesSlip.save (netreceipts);
        if (!result.isSuccess ()) return result;

        //返回数据
        result.put ("Oddnumber", salesSlip.getOddnumber ());
        result.put ("Date", salesSlip.getDatetim());
        result.put ("Netreceipts", salesSlip.getNetreceipts ());
        result.put ("GiveChange", salesSlip.getGiveChange ());
        result.put ("AmountMoney", salesSlip.getAmountMoney ());

        return result;
    }

}
