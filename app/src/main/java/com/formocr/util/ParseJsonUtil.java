package com.formocr.util;

import com.formocr.model.ThreeTurple;
import com.formocr.model.TurpleXSort;
import com.formocr.model.TurpleYSort;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 识别结果特定格式解析类
 * <p>
 * Created by ZhangGuanQun on 2017/7/2.
 */

public class ParseJsonUtil {
    /**
     * 解析返回的json,生成需要的结果
     *
     * @param response 返回的json
     * @return 解析后的中间元组代码集合(itemstring, x, y)
     */
    private static ArrayList<ThreeTurple> parseJsonToTurple(JSONObject response) {
        JSONObject json = JSONObject.fromObject(response);
        JSONArray items = json.getJSONArray("items");
        ArrayList<ThreeTurple> lst = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            JSONObject element = items.getJSONObject(i);
            JSONObject itemcoord = element.getJSONObject("itemcoord");
            String itemString = element.get("itemstring").toString();
            int itemCoordX = Integer.parseInt(itemcoord.get("x").toString());
            int itemCoordY = Integer.parseInt(itemcoord.get("y").toString());
            ThreeTurple<String, Integer, Integer> tup = new ThreeTurple<>(itemString, itemCoordX, itemCoordY);
            lst.add(tup);
        }
        return lst;
    }

    /**
     * 对结果坐标排序,按行顺序整理
     *
     * @param lst json解析结果
     * @return 行顺序列表
     */
    private static ArrayList<ArrayList<ThreeTurple>> rowSort(ArrayList<ThreeTurple> lst) {
        /* 按Y排序 */
        Collections.sort(lst, new TurpleYSort());
        /* 按照Y的间距200进行类别划分,其中每一个对象代表一个列 */
        ArrayList<ArrayList<ThreeTurple>> res = ClassifyUtil.classifyByY(lst);
        /* 行内按X排序 */
        for (int i = 0; i < res.size(); i++) {
            ArrayList<ThreeTurple> row = res.get(i);
            Collections.sort(row, new TurpleXSort());
        }
        return res;
    }

    /**
     * 生成标准格式的结果
     *
     * @param res 行顺序列表
     * @return 标准格式结果
     */
    private static String generateStr(ArrayList<ArrayList<ThreeTurple>> res) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < res.size(); i++) {
            ArrayList<ThreeTurple> row = res.get(i);
            for (int j = 0; j < row.size(); j++) {
                ThreeTurple rowItem = row.get(j);
                sb.append(rowItem.first + ",");
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 对外提供json解析以及按行顺序整理后的接口
     *
     * @param response 返回的json
     * @return 最终结果
     */
    public static String getResultStr(org.json.JSONObject response) {
        JSONObject wrapperResponse = JSONObject.fromObject(response.toString());
        return generateStr(rowSort(parseJsonToTurple(wrapperResponse)));
    }
}

