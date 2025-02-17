/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.selfservice.controller;

import com.selfservice.model.Template;
import com.selfservice.model.User;
import com.selfservice.servers.DbConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Actions in template.jsp
 * @author aiming
 */
public class TemplateListServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
               response.setContentType("text/html;charset=UTF-8");
        Connection con = null;
        List<Template> list = new ArrayList<>();
        request.setAttribute("templateList", list);
        PrintWriter out=response.getWriter();
        try {
            con = DbConnection.getConnection();
            User user = (User) request.getSession().getAttribute("user");
            String username = user.getUsername();
            String deleteString = request.getQueryString();
            PreparedStatement ps;
            String sql =null;
            //delete template
            if(deleteString !=null && deleteString.contains("delete")){
                
                String[] deleteNames=deleteString.replace("delete=", "").split("&");
                sql="delete from TEMPLATE where template_uuid in ";
                String names="(";
                for(int i=0; i<deleteNames.length; i++){
                    if(i== deleteNames.length-1){
                        names += "'"+deleteNames[i] + "'";
                    }else{
                         names += "'"+ deleteNames[i] + "',";
                    }
                }
                names += ")";
                sql += names;
                System.out.println("deleteTemplate sql-->" + sql);
                ps = con.prepareStatement(sql);
                ps.executeUpdate();
            }
            //TO check if salesforce_case come from Template or request
            sql = "select  temp.last_edit,  temp.template_name, temp.template_uuid  from TEMPLATE temp ";
            sql = sql + " where  temp.username='" + username + "'";

            //Search Request
            String sTxt = request.getParameter("search");
            if (sTxt != null && sTxt.length() > 0) {
                sql = sql + " and ( template_name like '%" + sTxt + "%' or last_edit like '%" + sTxt + "%' )";
            }
            sql = sql + " order by last_edit DESC";
            //add paging sql
            int page = 1; //current page;
            int recordsPerPage = 12;
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
            int beginIndex = (page - 1) * recordsPerPage;

            String pageSql = "select o.* from (" + sql + ") o limit " + beginIndex + ", " + recordsPerPage;
            ps = con.prepareStatement(pageSql);
            ResultSet rs = ps.executeQuery();
            System.out.println("templateList sql-->" + sql);

            while (rs.next()) {
                Template object = new Template();              
                object.setLast_edit(rs.getDate(1));
                object.setTemplate_name(rs.getString(2));
                //object.setSalesforce_case(rs.getString(3));
                object.setTemplate_uuid(rs.getString(3));
                list.add(object);
            }
            rs.close();
            ps.close();
            //get thet total count
            String totalSql = "select count(*) from (" + sql + ") o";
            ps = con.prepareStatement(totalSql);
            rs = ps.executeQuery();
            int total = 0;
            if (rs.next()) {
                total = rs.getInt(1);
            }
            rs.close();
            ps.close();
            //setup the paging attribute
            int noOfPages = (int) Math.ceil(total * 1.0 / recordsPerPage);
            request.setAttribute("noOfPages", noOfPages);
            request.setAttribute("currentPage", page);           
            request.getRequestDispatcher("templateList.jsp").forward(request, response);
            rs.close();
        } catch (SQLException ex) {
            String err = ex.getLocalizedMessage();
            request.setAttribute("errMessage", "<font color='red'>Delete Failed!  " + err + "</font>");
                Logger.getLogger(UserListServlet.class.getName()).log(Level.SEVERE, null, ex);
                request.getRequestDispatcher("templateList.jsp").forward(request, response);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
