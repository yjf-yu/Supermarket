package servlet.ReturnInstorage;


import entity.Goods;
import entity.returnInstorage.ReturnInstorage;
import entity.storehouse.DetailsWarehousing;
import entity.Supplier;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import servlet.Result;
import servlet.baservlet.ServletBaservlet;


import javax.servlet.annotation.WebServlet;


/**
 * @author yjf
 * @version 1.0
 * 2020/1/18
 * @date 2020/1/18 17:18
 */
@WebServlet(name = "ReturnInstorageServlet", urlPatterns = "/returninstorage")
public class ReturnInstorageServlet extends ServletBaservlet {

    @Override
    protected Result handle(JSONObject json) {

        // 判断数据是否齐全 supplierid GoodsList
        if (!json.containsKey("supplierid") && !json.containsKey("GoodsList")) {
            log.debug("supplierid或GoodsList没有");
            return Result.LACK_OF_KEY;
        }

        // 判断供应商是否有效
        int supplierid = json.getInteger("supplierid");
        Supplier supplier = Supplier.getSupplierById(supplierid);
        if (supplier == null) {
            log.debug("供应商不存在：{}", supplier);
            return Result.NON_EXISTENT;
        }

        //	生成入库退回单
        ReturnInstorage returninstorage = new ReturnInstorage();

        //	循环判断GoodsList：里面的
        JSONArray GoodsList = json.getJSONArray("GoodsList");
        for (Object goodslist : GoodsList) {
            if (!(goodslist instanceof JSONObject)) return Result.PARA_ERROR;

            //	判断Goodsid，Number是否存在
            if (!((JSONObject) goodslist).containsKey("goodsid") && !((JSONObject) goodslist).containsKey("number")) {
                log.debug("Goodsid或Number没有");
                return Result.LACK_OF_KEY;
            }

            //  判断商品是否在入库单
            DetailsWarehousing detailsWarehousing = new DetailsWarehousing();
            int Goodsid = ((JSONObject) goodslist).getInteger("goodsid");
            log.debug("得到Goodsid：{}", Goodsid);
            if (!detailsWarehousing.select(Goodsid)) {
                log.debug("入库单没有该商品", Goodsid);
                return Result.NON_EXISTENT;
            }

            //判断商品是否在库
            Goods goods = Goods.getGoodsById(Goodsid);
            if (goods == null) {
                log.debug("找不到该商品：{}", goods);
                return Result.NON_EXISTENT;
            }

            //	生成入库退回明细增加到退回单
            int number = ((JSONObject) goodslist).getInteger("number");
            returninstorage.add(Goodsid, number, supplierid);
            log.debug("Goodsid:{},number:{},supplierid:{}", Goodsid, number, supplierid);
        }

        // 仓库减库存
        DetailsWarehousing detailsWarehousing = new DetailsWarehousing();
        Result result = detailsWarehousing.update(GoodsList);
        if (!result.isSuccess()) {
            return result;
        }

        //	写入入库退回单头档数据
        result = returninstorage.save(supplierid);
        if (!result.isSuccess()) {
            return result;
        }

        // 	供应商减去相对应的退款总金额 写入数据库
        result = supplier.reduceMoney(returninstorage);

        //返回数据
        return result;
    }

}
