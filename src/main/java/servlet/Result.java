package servlet;


import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Result{

    private final Logger log = LogManager.getLogger (Result.class.getName ());

    /**
     * 内容已存在
     */
    public static Result EXISTED = new Result (4, "内容已存在");

    /**
     * 未知异常
     */
    public static Result UNKNOW = new Result (3, "未知异常");

    /**
     * 参数错误
     */
    public static Result PARA_ERROR = new Result (1, "参数错误");

    /**
     * 格式错误
     */
    public static Result FORMAT_ERROR = new Result (2, "请输入正确的格式");

    /**
     * id不存在
     */
    public static Result NON_EXISTENT = new Result (5,"id不存在");

    /**
     * 已被删除请勿重复操作
     */
    public static Result DELETED = new Result (6,"已被删除请勿重复操作");

    public  static Result CONTENT_DOES_NOT_EXIST = new Result (7,"内容不存在");

    /**
     * 单号不存在
     */
    public static Result NO_DOES_NOT_EXIST = new Result (8,"单号不存在");

    public static Result LACK_OF_KEY = new Result (9,"缺少key值");


    public static Result createSuccess(){
        return new Result (0, "成功");
    }

    private final JSONObject json = new JSONObject ();

    public Result(int code, String message){
        json.put ("code", code);
        json.put ("message", message);
    }

    public int getCode(){
        return json.getInteger ("code");
    }

    public String getMessage(){
        return json.getString ("message");
    }

    public boolean isSuccess(){
        return 0 == json.getInteger ("code");
    }

    public Result put(String name, Object value){
        json.put (name, value);
        return this;
    }

    @Override
    public String toString(){
        log.debug ("result string:{}", json);
        return json.toString ();
    }
    public <T> T get(String name){
        return (T) json.get(name);
    }

}
