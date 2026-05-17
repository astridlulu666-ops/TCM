package com.itcmdas.servlet;

import com.alibaba.fastjson.JSONObject;
import com.itcmdas.service.ComplexEntropyClustering;
import com.itcmdas.service.FrequencyAnalysis;
import com.itcmdas.service.MedicineStrTableGeneration;

import com.itcmdas.vo.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lbt
 * @description
 * @data 2025/1/6
 */
@WebServlet("/FlusterAnalysisServlet")
public class FlusterAnalysisServlet extends BasicServlet {
    //接收前端传入的excel文件 转化为的string数组
    static String[] medicineStr = null;

    //接收前端传入的最小关联度阈值
    double minCorrelationCoefficient;


    /**
     * 获取前端数据，并保存到session中
     *
     * @param req
     * @param resp
     */
    public void saveDataToSession(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 防止乱码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=UTF-8");
        
        // 获取前端参数
        medicineStr = req.getParameterValues("medicineStr");
        String minCorrelationCoefficientStr = req.getParameter("minCorrelationCoefficient");
        
        // 检查参数是否为空
        if(medicineStr == null || minCorrelationCoefficientStr == null) {
            PrintWriter out = resp.getWriter();
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "参数错误：缺少必要参数");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            out.print(obj.toJSONString());
            return;
        }
        
        // 转换参数类型
        try {
            minCorrelationCoefficient = Double.parseDouble(minCorrelationCoefficientStr);
        } catch (NumberFormatException e) {
            PrintWriter out = resp.getWriter();
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "参数错误：最小关联度阈值格式不正确");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            out.print(obj.toJSONString());
            return;
        }

        // 保存到session
        req.getSession().setAttribute("medicineStr", medicineStr);
        req.getSession().setAttribute("minCorrelationCoefficient", minCorrelationCoefficient);
        
        // 返回成功响应
        PrintWriter out = resp.getWriter();
        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "数据保存成功");
        obj.put("count", 0);
        obj.put("data", new ArrayList<>());
        out.print(obj.toJSONString());

    }

    /**
     * 从session中获取数据，以便第二次ajax操作
     * @param req
     * @param resp
     * @throws Exception
     */
    public void correlationCoefficientTableData(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=utf-8");

        //获取前端数据
        Object medicineStrObj = req.getSession().getAttribute("medicineStr");
        Object minCorrelationCoefficientObj = req.getSession().getAttribute("minCorrelationCoefficient");
        
        if (medicineStrObj == null || minCorrelationCoefficientObj == null) {
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "数据库接口异常: parameterror");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            PrintWriter out = resp.getWriter();
            out.print(obj.toJSONString());
            return;
        }
        
        medicineStr = (String[]) medicineStrObj;
        minCorrelationCoefficient = (double) minCorrelationCoefficientObj;

        /**
         * 调用 MedicineStrTableGeneration 类的 getMedicineStrTable 方法，生成二维字符串处方数组
         */
        ComplexEntropyClustering.setMedicalTableStr(MedicineStrTableGeneration.getMedicineStrTable(medicineStr));

        //类初始化，获取采用独热编码的处方表，以及处方总数，药方数
        ComplexEntropyClustering.init();

        //调用 getMedicineMapForMinCorrelationCoefficient 获取关联度药物系数排名表
        ArrayList<TwoMedicineMapForClustering> correlationCoefficientList =
                ComplexEntropyClustering.getMedicineMapForMinCorrelationCoefficient(minCorrelationCoefficient);

        //将结果进行封装成前端所需格式
        ArrayList<CorrelationCoefficientListData> correlationCoefficientListDatas=new ArrayList<>();

        for(int i=0;i<correlationCoefficientList.size();i++){
            CorrelationCoefficientListData correlationCoefficientListData=new CorrelationCoefficientListData();
            correlationCoefficientListData.setId(i+1);
            correlationCoefficientListData.setMedicine1(correlationCoefficientList.get(i).getMedicine1());
            correlationCoefficientListData.setMedicine2(correlationCoefficientList.get(i).getMedicine2());

            //将浮点数精度变小，传给前端
            int num= (int) (correlationCoefficientList.get(i).getCorrelationCoefficient()*100);
            double correlationCoefficient_num=num*1.0/100;


            correlationCoefficientListData.setCorrelationCoefficient(correlationCoefficient_num);

            correlationCoefficientListDatas.add(correlationCoefficientListData);
        }

        int currentPage = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));
        int limit = Integer.parseInt(req.getParameter("limit") == null ? "10" : req.getParameter("limit"));

        List<CorrelationCoefficientListData> limitCorrelationCoefficientListDatas;
//        List<TwoMedicineMapForClustering> limitCorrelationCoefficientListDatas;

        int count=correlationCoefficientListDatas.size();


        if((count-(currentPage-1)*limit)/limit==0){
            limitCorrelationCoefficientListDatas=new ArrayList<>(correlationCoefficientListDatas.subList((currentPage-1)*limit,count));
        }else {
            limitCorrelationCoefficientListDatas=new ArrayList<>(correlationCoefficientListDatas.subList((currentPage-1)*limit,currentPage*limit));
        }

        PrintWriter out = resp.getWriter();

        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "");
        obj.put("count", correlationCoefficientListDatas.size());
        obj.put("data", limitCorrelationCoefficientListDatas);

        String correlationCoefficientListJson = obj.toJSONString();

        out.print(correlationCoefficientListJson);
    }

    /**
     * Echarts图数据接口
     * @param req
     * @param resp
     * @throws IOException
     */
    public void correlationCoefficientEchartsData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=utf-8");

        //获取前端数据
        Object medicineStrObj = req.getSession().getAttribute("medicineStr");
        Object minCorrelationCoefficientObj = req.getSession().getAttribute("minCorrelationCoefficient");
        
        if (medicineStrObj == null || minCorrelationCoefficientObj == null) {
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "数据库接口异常: parameterror");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            PrintWriter out = resp.getWriter();
            out.print(obj.toJSONString());
            return;
        }
        
        medicineStr = (String[]) medicineStrObj;
        minCorrelationCoefficient = (double) minCorrelationCoefficientObj;

        /**
         * 调用 MedicineStrTableGeneration 类的 getMedicineStrTable 方法，生成二维字符串处方数组
         */
        ComplexEntropyClustering.setMedicalTableStr(MedicineStrTableGeneration.getMedicineStrTable(medicineStr));

        //类初始化，获取采用独热编码的处方表，以及处方总数，药方数
        ComplexEntropyClustering.init();

        //调用 getMedicineMapForMinCorrelationCoefficient 获取关联度药物系数排名表
        ArrayList<TwoMedicineMapForClustering> correlationCoefficientList =
                ComplexEntropyClustering.getMedicineMapForMinCorrelationCoefficient(minCorrelationCoefficient);

        ArrayList<CorrelationCoefficientListData> correlationCoefficientListDatas=new ArrayList<>();

        for(int i=0;i<correlationCoefficientList.size();i++){
            CorrelationCoefficientListData correlationCoefficientListData=new CorrelationCoefficientListData();
            correlationCoefficientListData.setId(i+1);
            correlationCoefficientListData.setMedicine1(correlationCoefficientList.get(i).getMedicine1());
            correlationCoefficientListData.setMedicine2(correlationCoefficientList.get(i).getMedicine2());

            //将浮点数精度变小，传给前端
            int num= (int) (correlationCoefficientList.get(i).getCorrelationCoefficient()*100);
            double correlationCoefficient_num=num*1.0/100;


            correlationCoefficientListData.setCorrelationCoefficient(correlationCoefficient_num);

            correlationCoefficientListDatas.add(correlationCoefficientListData);
        }

        PrintWriter out = resp.getWriter();

        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "");
        obj.put("count", correlationCoefficientListDatas.size());
        obj.put("data", correlationCoefficientListDatas);

        String correlationCoefficientListJson = obj.toJSONString();

        out.print(correlationCoefficientListJson);
    }

}