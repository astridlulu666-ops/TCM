package com.itcmdas.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itcmdas.service.FrequencyAnalysis;
import com.itcmdas.service.MedicineStrTableGeneration;
import com.itcmdas.vo.AssociationFormData;
import com.itcmdas.vo.Medicine;
import com.itcmdas.vo.MedicineFreTable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.*;

/**
 * @Classname CoreMedicineListServlet
 * @Description 核心药物组合表 Servlet
 * @Author lbt
 * @Date 2026/3/15 20:38
 * @Version 1.0
 */
@WebServlet(name = "FrequencyAnalysisServlet", urlPatterns = {"/FrequencyAnalysisServlet"})
public class FrequencyAnalysisServlet extends BasicServlet {
    //改为全局变量方便调用
    String[] medicineStrFre = null;

    //显示排序后的前几名
    int sortedNum;

    //接收是 药物频次分析 or药对频次分析 or三元组
    int medicineNum;


    /**
     * 获取前端数据，并保存到session中
     * @param req
     * @param resp
     * @throws Exception
     */
    public void saveDataToSession(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        // 防止乱码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=UTF-8");
        
        // 获取前端参数
        medicineStrFre=req.getParameterValues("medicineStrFre");
        String sortedNumStr=req.getParameter("sortedNum");
        String medicineNumStr=req.getParameter("medicineNum");
        
        // 检查参数是否为空
        if(medicineStrFre==null || sortedNumStr==null || medicineNumStr==null){
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
            sortedNum= Integer.parseInt(sortedNumStr);
            medicineNum= Integer.parseInt(medicineNumStr);
        } catch (NumberFormatException e) {
            PrintWriter out = resp.getWriter();
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "参数错误：数值类型参数格式不正确");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            out.print(obj.toJSONString());
            return;
        }

        // 保存到session
        req.getSession().setAttribute("medicineStrFre",medicineStrFre);
        req.getSession().setAttribute("sortedNum",sortedNum);
        req.getSession().setAttribute("medicineNum",medicineNum);
        
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
    public void frequencyAnalysisLayuiTableData(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //防止乱码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=UTF-8");

        //获取session中数据
        Object medicineStrFreObj = req.getSession().getAttribute("medicineStrFre");
        Object sortedNumObj = req.getSession().getAttribute("sortedNum");
        Object medicineNumObj = req.getSession().getAttribute("medicineNum");
        
        if (medicineStrFreObj == null || sortedNumObj == null || medicineNumObj == null) {
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "数据库接口异常: parameterror");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            PrintWriter out = resp.getWriter();
            out.print(obj.toJSONString());
            return;
        }
        
        medicineStrFre= (String[]) medicineStrFreObj;
        sortedNum= (int) sortedNumObj;
        medicineNum= (int) medicineNumObj;

//        out.println(medicineStrFre[0]);

//        out.println(medicineNum+"------"+sortedNum+"------"+medicineStrFre);

        /**
         * 调用 MedicineStrTableGeneration 类的 getMedicineStrTable 方法，生成二维字符串处方数组
         */
        FrequencyAnalysis.setMedicalTableStr(MedicineStrTableGeneration.getMedicineStrTable(medicineStrFre));
        List<Map.Entry<ArrayList<String>, Integer>> resList =
                FrequencyAnalysis.getFrequencyAnalysisMap(sortedNum, medicineNum);


        /*for (int i = 0; i < resList.size(); i++) {
            Map.Entry entry = resList.get(i);
            out.println(entry.getKey() + " " + entry.getValue());
        }*/

        ArrayList<MedicineFreTable> listFre=new ArrayList<>();

        for (int i = 0; i < resList.size(); i++) {
            Map.Entry entry = resList.get(i);

            //将算法实现结果保存到Java 实体类中
            MedicineFreTable medicineFreTable=new MedicineFreTable();

            medicineFreTable.setId(i+1);

            //字符串拼接
            String str="";
            ArrayList<String> strList= (ArrayList<String>) entry.getKey();
            for(int j=0;j<strList.size();j++){
                if(j==0){
                    str+=strList.get(j);
                }else{
                    str=str+","+strList.get(j);
                }
            }

            medicineFreTable.setMedicineListFre(str);
            medicineFreTable.setMedicineNumFre((Integer) entry.getValue());

            listFre.add(medicineFreTable);
        }

        int currentPage = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));
        int limit = Integer.parseInt(req.getParameter("limit") == null ? "10" : req.getParameter("limit"));

        List<MedicineFreTable> limitDataFre;
        int count=listFre.size();

        if((count-(currentPage-1)*limit)/limit==0){
            limitDataFre=new ArrayList<>(listFre.subList((currentPage-1)*limit,count));
        }else {
            limitDataFre=new ArrayList<>(listFre.subList((currentPage-1)*limit,currentPage*limit));
        }


        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "");
        obj.put("count", listFre.size());
        obj.put("data", limitDataFre);

        PrintWriter out = resp.getWriter();
        String medicineTableFre = obj.toJSONString();

        out.print(medicineTableFre);

    }

    /**
     * 为Echarts数据提供接口
     * @param req
     * @param resp
     * @throws Exception
     */
    public void frequencyAnalysisEchartsData(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //防止乱码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=UTF-8");

        //获取session中数据
        Object medicineStrFreObj = req.getSession().getAttribute("medicineStrFre");
        Object sortedNumObj = req.getSession().getAttribute("sortedNum");
        Object medicineNumObj = req.getSession().getAttribute("medicineNum");
        
        if (medicineStrFreObj == null || sortedNumObj == null || medicineNumObj == null) {
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("msg", "数据库接口异常: parameterror");
            obj.put("count", 0);
            obj.put("data", new ArrayList<>());
            PrintWriter out = resp.getWriter();
            out.print(obj.toJSONString());
            return;
        }
        
        medicineStrFre= (String[]) medicineStrFreObj;
        sortedNum= (int) sortedNumObj;
        medicineNum= (int) medicineNumObj;

        /**
         * 调用 MedicineStrTableGeneration 类的 getMedicineStrTable 方法，生成二维字符串处方数组
         */
        FrequencyAnalysis.setMedicalTableStr(MedicineStrTableGeneration.getMedicineStrTable(medicineStrFre));
        List<Map.Entry<ArrayList<String>, Integer>> resList =
                FrequencyAnalysis.getFrequencyAnalysisMap(sortedNum, medicineNum);


        ArrayList<MedicineFreTable> listFre=new ArrayList<>();

        for (int i = 0; i < resList.size(); i++) {
            Map.Entry entry = resList.get(i);

            //将算法实现结果保存到Java 实体类中
            MedicineFreTable medicineFreTable=new MedicineFreTable();

            //字符串拼接
            String str="";
            ArrayList<String> strList= (ArrayList<String>) entry.getKey();
            for(int j=0;j<strList.size();j++){
                if(j==0){
                    str+=strList.get(j);
                }else{
                    str=str+","+strList.get(j);
                }
            }
            medicineFreTable.setMedicineListFre(str);
            medicineFreTable.setMedicineNumFre((Integer) entry.getValue());

            listFre.add(medicineFreTable);
        }

        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "");
        obj.put("count", listFre.size());
        obj.put("data", listFre);

        PrintWriter out = resp.getWriter();
        String medicineTableFre = obj.toJSONString();

        out.print(medicineTableFre);
    }
}
