package servlet.SalesSlip;

import entity.Goods;
import entity.storehouse.InSocketOrder;
import entity.Supplier;
import com.alibaba.fastjson.JSONArray;

import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;

import javax.servlet.annotation.WebServlet;


@WebServlet(name = "WarehousingServlet", urlPatterns = "/warehousing")
public class WarehousingServlet extends ServletBaservlet {

    @Override
    protected Result handle(JSONObject json) {

        JSONArray goodsList = json.getJSONArray("goodsList");
        if (goodsList == null) return Result.UNKNOW;
        log.debug("拿到json里面的数据:{}", goodsList);


        if (json.getInteger("supplierid") == null) return Result.PARA_ERROR;


        //判断供应商是否有效
        int supplierid = json.getInteger("supplierid");
        log.debug("供应商的ID：{}", supplierid);
        Supplier supplier = Supplier.getSupplierById(supplierid);
        if (supplier == null) {
            log.debug("supplierid找不到");
            return Result.NON_EXISTENT;
        }

        // 生成入库单
        InSocketOrder inSocketOrder = new InSocketOrder();

        for (Object obj : goodsList) {
            JSONObject entity;
            if (obj instanceof JSONObject) {
                entity = (JSONObject) obj;
            } else {
                return Result.PARA_ERROR;
            }

            //同时判断 goodsid number purchaseprice 不能为空
            if (entity.getInteger("goodsid") == null ||
                    entity.getInteger("number") == null ||
                    entity.getDouble("purchaseprice") == null) {
                return Result.PARA_ERROR;
            }

            //取出需要的数据
            int id = entity.getInteger("goodsid");

            //判断商品是否在商品库
            Goods goods = Goods.getGoodsById(id);
            if (goods == null) {
                return Result.NON_EXISTENT;
            }

            int number = ((JSONObject) obj).getInteger("number");
            double purchaseprice = ((JSONObject) obj).getDouble("purchaseprice");
            inSocketOrder.add(goods, number, purchaseprice);
        }


        //入库动作
        Result result = inSocketOrder.save(supplierid);
        if (!result.isSuccess()) {
            return result;
        }

        //给供应商加钱
        result = supplier.addMoney(inSocketOrder);
        if (!result.isSuccess()) {
            return result;
        }

        return result;
    }
}
