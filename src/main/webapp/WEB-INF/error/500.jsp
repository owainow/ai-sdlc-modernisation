<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<html>
<head>
    <title>Server Error - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; display: flex; justify-content: center; align-items: center; height: 100vh; }
        .error-container { text-align: center; background: white; padding: 40px; border-radius: 5px; border: 1px solid #ddd; max-width: 500px; }
        h1 { color: #dc3545; font-size: 72px; margin: 0; }
        h2 { color: #333; }
        p { color: #666; }
        a { color: #007acc; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>500</h1>
        <h2>Internal Server Error</h2>
        <p>Something went wrong. Please try again later.</p>
        <p><a href="<%= request.getContextPath() %>/index.jsp">Return to Dashboard</a></p>
    </div>
</body>
</html>
