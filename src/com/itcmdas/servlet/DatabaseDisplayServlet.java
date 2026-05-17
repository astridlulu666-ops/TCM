package com.itcmdas.servlet;

import com.itcmdas.dao.MedicineDao;
import com.itcmdas.util.DBUtil;
import com.itcmdas.vo.Medicine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "DatabaseDisplayServlet", urlPatterns = {"/DatabaseDisplayServlet"})
public class DatabaseDisplayServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 获取请求参数，确定要显示的表
        String tableName = request.getParameter("table");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "chinese_medicine";
        }

        // 连接数据库并查询数据
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<List<String>> dataList = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();

            // 查询表结构（列名）
            rs = conn.getMetaData().getColumns(null, null, tableName, "%");
            while (rs.next()) {
                columnNames.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();

            // 查询表数据
            String sql = "SELECT * FROM " + tableName;
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (String columnName : columnNames) {
                    String value = rs.getString(columnName);
                    row.add(value != null ? value : "");
                }
                dataList.add(row);
            }

            // 将数据传递到JSP页面
            request.setAttribute("tableName", tableName);
            request.setAttribute("columnNames", columnNames);
            request.setAttribute("dataList", dataList);

            // 转发到JSP页面
            request.getRequestDispatcher("DatabaseDisplay.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            out.println("数据库查询失败: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}