<%@ page import="java.util.*" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.sourcegraph.demo.bigbadmonolith.dao.*" %>
<%@ page import="com.sourcegraph.demo.bigbadmonolith.entity.*" %>
<%@ page import="com.sourcegraph.demo.bigbadmonolith.util.HtmlUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    CustomerDAO customerDAO = new CustomerDAO();
    UserDAO userDAO = new UserDAO();
    BillableHourDAO billableHourDAO = new BillableHourDAO();
    BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    
    String reportType = request.getParameter("reportType");
    String customerId = request.getParameter("customerId");
    String month = request.getParameter("month");
    String year = request.getParameter("year");
    
    DecimalFormat df = new DecimalFormat("#,##0.00");
%>
<html>
<head>
    <title>Reports - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .content { background: white; padding: 20px; margin: 20px 0; border: 1px solid #ddd; border-radius: 5px; }
        .form-group { margin-bottom: 15px; display: inline-block; margin-right: 20px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group select, .form-group input { padding: 8px; border: 1px solid #ccc; border-radius: 3px; }
        .btn { background: #007acc; color: white; padding: 10px 15px; border: none; border-radius: 3px; cursor: pointer; }
        .btn:hover { background: #005a9e; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; }
        .report-section { margin: 30px 0; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
        .summary-box { background: #e7f3ff; padding: 15px; margin: 15px 0; border-radius: 5px; }
        .text-right { text-align: right; }
        .text-center { text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Billing Reports</h1>
    </div>
    
    <div class="nav">
        <a href="index.jsp">Dashboard</a>
        <a href="customers.jsp">Customers</a>
        <a href="users.jsp">Users</a>
        <a href="categories.jsp">Billing Categories</a>
        <a href="hours.jsp">Log Hours</a>
        <a href="reports.jsp">Reports</a>
    </div>
    
    <div class="content">
        <h2>Generate Reports</h2>
        <form method="get" action="reports.jsp">
            <div class="form-group">
                <label for="reportType">Report Type:</label>
                <select id="reportType" name="reportType">
                    <option value="">Select Report Type</option>
                    <option value="customer" <%= "customer".equals(reportType) ? "selected" : "" %>>Customer Bill</option>
                    <option value="monthly" <%= "monthly".equals(reportType) ? "selected" : "" %>>Monthly Summary</option>
                    <option value="revenue" <%= "revenue".equals(reportType) ? "selected" : "" %>>Revenue Summary</option>
                </select>
            </div>
            
            <% if ("customer".equals(reportType)) { %>
            <div class="form-group">
                <label for="customerId">Customer:</label>
                <select id="customerId" name="customerId">
                    <option value="">Select Customer</option>
                    <%
                        try {
                            List<Customer> customers = customerDAO.findAll();
                            for (Customer customer : customers) {
                                String selected = customer.getId().toString().equals(customerId) ? "selected" : "";
                                out.println("<option value='" + customer.getId() + "' " + selected + ">" + 
                                          HtmlUtils.htmlEscape(customer.getName()) + "</option>");
                            }
                        } catch (Exception e) {
                            out.println("<option value=''>Error loading customers</option>");
                        }
                    %>
                </select>
            </div>
            <% } %>
            
            <% if ("monthly".equals(reportType)) { %>
            <div class="form-group">
                <label for="year">Year:</label>
                <input type="number" id="year" name="year" value="<%= year != null ? year : "2024" %>" min="2020" max="2030">
            </div>
            <div class="form-group">
                <label for="month">Month:</label>
                <select id="month" name="month">
                    <% 
                        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
                        String[] monthNames = {"January", "February", "March", "April", "May", "June", 
                                             "July", "August", "September", "October", "November", "December"};
                        for (int i = 0; i < months.length; i++) {
                            String selected = months[i].equals(month) ? "selected" : "";
                            out.println("<option value='" + months[i] + "' " + selected + ">" + monthNames[i] + "</option>");
                        }
                    %>
                </select>
            </div>
            <% } %>
            
            <button type="submit" class="btn">Generate Report</button>
        </form>
        
        <%
            if ("customer".equals(reportType) && customerId != null && !customerId.trim().isEmpty()) {
        %>
        <div class="report-section">
            <h2>Customer Bill Report</h2>
            <%
                String customerName = "";
                String customerEmail = "";
                double totalAmount = 0.0;
                double totalHours = 0.0;
                
                try {
                    Long custId = Long.parseLong(customerId);
                    Customer reportCustomer = customerDAO.findById(custId);
                    
                    if (reportCustomer != null) {
                        customerName = reportCustomer.getName();
                        customerEmail = reportCustomer.getEmail();
                    }
                    
                    List<BillableHour> custHours = billableHourDAO.findByCustomerId(custId);
                    List<User> allUsers = userDAO.findAll();
                    List<BillingCategory> allCategories = categoryDAO.findAll();
                    
                    Map<Long, User> userMap = new HashMap<>();
                    for (User u : allUsers) {
                        userMap.put(u.getId(), u);
                    }
                    Map<Long, BillingCategory> catMap = new HashMap<>();
                    for (BillingCategory bc : allCategories) {
                        catMap.put(bc.getId(), bc);
                    }
            %>
            
            <div class="summary-box">
                <h3>Bill To:</h3>
                <p><strong><%= HtmlUtils.htmlEscape(customerName) %></strong><br>
                Email: <%= HtmlUtils.htmlEscape(customerEmail) %></p>
            </div>
            
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>User</th>
                        <th>Category</th>
                        <th>Hours</th>
                        <th>Rate</th>
                        <th>Amount</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (BillableHour bh : custHours) {
                            User bhUser = userMap.get(bh.getUserId());
                            BillingCategory bhCat = catMap.get(bh.getCategoryId());
                            if (bhUser != null && bhCat != null) {
                                double lineTotal = bh.getHours().doubleValue() * bhCat.getHourlyRate().doubleValue();
                                double hours = bh.getHours().doubleValue();
                                totalAmount += lineTotal;
                                totalHours += hours;
                    %>
                        <tr>
                            <td><%= bh.getDateLogged().toString() %></td>
                            <td><%= HtmlUtils.htmlEscape(bhUser.getName()) %></td>
                            <td><%= HtmlUtils.htmlEscape(bhCat.getName()) %></td>
                            <td class="text-right"><%= df.format(hours) %></td>
                            <td class="text-right">$<%= df.format(bhCat.getHourlyRate()) %></td>
                            <td class="text-right">$<%= df.format(lineTotal) %></td>
                            <td><%= HtmlUtils.htmlEscape(bh.getNote() != null ? bh.getNote() : "") %></td>
                        </tr>
                    <%
                            }
                        }
                    %>
                    <tr style="background-color: #f8f9fa; font-weight: bold;">
                        <td colspan="3">TOTAL</td>
                        <td class="text-right"><%= df.format(totalHours) %></td>
                        <td></td>
                        <td class="text-right">$<%= df.format(totalAmount) %></td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
            
            <%
                } catch (Exception e) {
                    out.println("<p>Error generating customer report: " + HtmlUtils.htmlEscape(e.getMessage()) + "</p>");
                }
            %>
        </div>
        <%
            } else if ("monthly".equals(reportType) && year != null && month != null) {
        %>
        <div class="report-section">
            <h2>Monthly Summary - <%= HtmlUtils.htmlEscape(month) %>/<%= HtmlUtils.htmlEscape(year) %></h2>
            <%
                try {
                    List<BillableHour> allHours = billableHourDAO.findAll();
                    List<Customer> allCustomers = customerDAO.findAll();
                    List<BillingCategory> allCategories = categoryDAO.findAll();
                    
                    int targetYear = Integer.parseInt(year);
                    int targetMonth = Integer.parseInt(month);
                    
                    Map<Long, Customer> custMap = new HashMap<>();
                    for (Customer c : allCustomers) { custMap.put(c.getId(), c); }
                    Map<Long, BillingCategory> catMap = new HashMap<>();
                    for (BillingCategory bc : allCategories) { catMap.put(bc.getId(), bc); }
                    
                    // Group by customer
                    Map<String, double[]> customerTotals = new LinkedHashMap<>();
                    for (BillableHour bh : allHours) {
                        LocalDate dl = bh.getDateLogged();
                        if (dl.getYear() == targetYear && dl.getMonthValue() == targetMonth) {
                            Customer c = custMap.get(bh.getCustomerId());
                            BillingCategory bc = catMap.get(bh.getCategoryId());
                            if (c != null && bc != null) {
                                String cName = c.getName();
                                double[] totals = customerTotals.getOrDefault(cName, new double[]{0.0, 0.0});
                                totals[0] += bh.getHours().doubleValue();
                                totals[1] += bh.getHours().doubleValue() * bc.getHourlyRate().doubleValue();
                                customerTotals.put(cName, totals);
                            }
                        }
                    }
            %>
            
            <table>
                <thead>
                    <tr>
                        <th>Customer</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        double monthlyTotal = 0.0;
                        double monthlyHours = 0.0;
                        
                        for (Map.Entry<String, double[]> entry : customerTotals.entrySet()) {
                            double customerHours = entry.getValue()[0];
                            double customerTotal = entry.getValue()[1];
                            monthlyTotal += customerTotal;
                            monthlyHours += customerHours;
                    %>
                        <tr>
                            <td><%= HtmlUtils.htmlEscape(entry.getKey()) %></td>
                            <td class="text-right"><%= df.format(customerHours) %></td>
                            <td class="text-right">$<%= df.format(customerTotal) %></td>
                        </tr>
                    <%
                        }
                    %>
                    <tr style="background-color: #f8f9fa; font-weight: bold;">
                        <td>MONTHLY TOTAL</td>
                        <td class="text-right"><%= df.format(monthlyHours) %></td>
                        <td class="text-right">$<%= df.format(monthlyTotal) %></td>
                    </tr>
                </tbody>
            </table>
            
            <%
                } catch (Exception e) {
                    out.println("<p>Error generating monthly report: " + HtmlUtils.htmlEscape(e.getMessage()) + "</p>");
                }
            %>
        </div>
        <%
            } else if ("revenue".equals(reportType)) {
        %>
        <div class="report-section">
            <h2>Revenue Summary</h2>
            <%
                try {
                    List<BillableHour> allHours = billableHourDAO.findAll();
                    List<Customer> allCustomers = customerDAO.findAll();
                    List<BillingCategory> allCategories = categoryDAO.findAll();
                    
                    Map<Long, Customer> custMap = new HashMap<>();
                    for (Customer c : allCustomers) { custMap.put(c.getId(), c); }
                    Map<Long, BillingCategory> catMap = new HashMap<>();
                    for (BillingCategory bc : allCategories) { catMap.put(bc.getId(), bc); }
            %>
            
            <h3>By Customer</h3>
            <table>
                <thead>
                    <tr>
                        <th>Customer</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                        <th>Average Rate</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        // Revenue by customer
                        Map<String, double[]> custRevenue = new LinkedHashMap<>();
                        Map<String, List<Double>> custRates = new HashMap<>();
                        for (Customer c : allCustomers) {
                            custRevenue.put(c.getName(), new double[]{0.0, 0.0});
                            custRates.put(c.getName(), new ArrayList<>());
                        }
                        for (BillableHour bh : allHours) {
                            Customer c = custMap.get(bh.getCustomerId());
                            BillingCategory bc = catMap.get(bh.getCategoryId());
                            if (c != null && bc != null) {
                                double[] totals = custRevenue.get(c.getName());
                                totals[0] += bh.getHours().doubleValue();
                                totals[1] += bh.getHours().doubleValue() * bc.getHourlyRate().doubleValue();
                                custRates.get(c.getName()).add(bc.getHourlyRate().doubleValue());
                            }
                        }
                        
                        for (Map.Entry<String, double[]> entry : custRevenue.entrySet()) {
                            double revTotalHours = entry.getValue()[0];
                            double revTotalRevenue = entry.getValue()[1];
                            List<Double> rates = custRates.get(entry.getKey());
                            double avgRate = rates.isEmpty() ? 0.0 : rates.stream().mapToDouble(d -> d).average().orElse(0.0);
                    %>
                        <tr>
                            <td><%= HtmlUtils.htmlEscape(entry.getKey()) %></td>
                            <td class="text-right"><%= df.format(revTotalHours) %></td>
                            <td class="text-right">$<%= df.format(revTotalRevenue) %></td>
                            <td class="text-right">$<%= df.format(avgRate) %></td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            
            <h3>By Category</h3>
            <table>
                <thead>
                    <tr>
                        <th>Category</th>
                        <th>Hourly Rate</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        // Revenue by category — single pass over hours, grouped by category ID
                        Map<Long, double[]> catTotals = new LinkedHashMap<>();
                        for (BillingCategory bc : allCategories) {
                            catTotals.put(bc.getId(), new double[]{0.0, 0.0});
                        }
                        for (BillableHour bh : allHours) {
                            BillingCategory bc = catMap.get(bh.getCategoryId());
                            if (bc != null) {
                                double[] totals = catTotals.get(bc.getId());
                                if (totals != null) {
                                    totals[0] += bh.getHours().doubleValue();
                                    totals[1] += bh.getHours().doubleValue() * bc.getHourlyRate().doubleValue();
                                }
                            }
                        }

                        for (BillingCategory bc : allCategories) {
                            double[] totals = catTotals.get(bc.getId());
                            double catTotalHours = totals[0];
                            double catTotalRevenue = totals[1];
                    %>
                        <tr>
                            <td><%= HtmlUtils.htmlEscape(bc.getName()) %></td>
                            <td class="text-right">$<%= df.format(bc.getHourlyRate()) %></td>
                            <td class="text-right"><%= df.format(catTotalHours) %></td>
                            <td class="text-right">$<%= df.format(catTotalRevenue) %></td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            
            <%
                } catch (Exception e) {
                    out.println("<div class='error'>Error generating revenue summary: " + HtmlUtils.htmlEscape(e.getMessage()) + "</div>");
                }
            %>
        </div>
        <%
            }
        %>
        

    </div>
</body>
</html>
