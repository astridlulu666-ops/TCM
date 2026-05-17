package com.itcmdas.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    /**
     *连接数据库，获取连接对象
     * @return
     */
    public static Connection getConnection(){
        try {
            //加载驱动程序
            Class.forName("com.mysql.cj.jdbc.Driver");

            //连接数据库字符串
            String dbUrl="jdbc:mysql://localhost:3306/chinese_medicine?characterEncoding=utf8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";

            //数据库角色&密码
            String user="root";
            String password="123456";

            //创建Connection对象
            Connection connection= DriverManager.getConnection(dbUrl,user,password);

            return  connection;

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }
}
