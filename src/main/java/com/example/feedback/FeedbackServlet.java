package com.example.feedback;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handles feedback submitted from index.html using either GET or POST
 * and renders a confirmation page with the submitted data.
 */
@WebServlet(name = "FeedbackServlet", urlPatterns = "/feedback")
public class FeedbackServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        try {
            Database.initialize();
        } catch (Exception e) {
            throw new ServletException("Database initialization failed", e);
        }
    }

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Opening /feedback directly (no data) -> send the user back to the form
        if (req.getParameter("name") == null && req.getParameter("email") == null
                && req.getParameter("feedback") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        handle(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        handle(req, resp, "POST");
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, String method)
            throws IOException {
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String feedback = trim(req.getParameter("feedback"));

        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String error = validate(name, email, feedback);
        if (error != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderError(out, req.getContextPath(), error);
            return;
        }
          String refId = "FB-" + UUID.randomUUID()
             .toString().substring(0, 8).toUpperCase();

          try {
          Database.save(refId, name, email, feedback);
          } catch (Exception e) {
         getServletContext().log("Failed to save feedback", e);
         resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         renderError(out, req.getContextPath(),
            "We could not save your feedback. Please try again later.");
    return;
}

renderSuccess(out, req.getContextPath(), method,
        refId, name, email, feedback);
        
    }

    private String validate(String name, String email, String feedback) {
        if (name.isEmpty() || email.isEmpty() || feedback.isEmpty()) {
            return "All fields are required. Please fill in your name, email and feedback.";
        }
        if (name.length() > 80) return "Name must be 80 characters or fewer.";
        if (!EMAIL.matcher(email).matches()) return "Please enter a valid email address.";
        if (feedback.length() < 10) return "Feedback must be at least 10 characters long.";
        if (feedback.length() > 1000) return "Feedback must be 1000 characters or fewer.";
        return null;
    }

    private void renderSuccess(PrintWriter out, String ctx, String method, String refId,
                               String name, String email, String feedback) {
        head(out, ctx, "Thank you for your feedback");
        out.println("<main class=\"card\">");
        out.println("  <div class=\"icon success\">&#10003;</div>");
        out.println("  <h1>Thank you, " + esc(firstName(name)) + "!</h1>");
        out.println("  <p class=\"subtitle\">Your feedback has been received successfully.</p>");
        out.println("  <div class=\"alert alert-success\">Confirmation: your submission was processed using the <strong>"
                + method + "</strong> method.</div>");
        out.println("  <dl class=\"summary\">");
        out.println("    <div><dt>Reference ID</dt><dd>" + refId + "</dd></div>");
        out.println("    <div><dt>Submitted on</dt><dd>" + LocalDateTime.now().format(TS) + "</dd></div>");
        out.println("    <div><dt>Name</dt><dd>" + esc(name) + "</dd></div>");
        out.println("    <div><dt>Email</dt><dd>" + esc(email) + "</dd></div>");
        out.println("    <div><dt>Feedback</dt><dd class=\"multiline\">" + esc(feedback) + "</dd></div>");
        out.println("  </dl>");
        out.println("  <a class=\"btn\" href=\"" + ctx + "/index.html\">Submit another response</a>");
        out.println("</main>");
        foot(out);
    }

    private void renderError(PrintWriter out, String ctx, String message) {
        head(out, ctx, "Submission error");
        out.println("<main class=\"card\">");
        out.println("  <div class=\"icon error\">!</div>");
        out.println("  <h1>We couldn&rsquo;t submit your feedback</h1>");
        out.println("  <div class=\"alert alert-error\">" + esc(message) + "</div>");
        out.println("  <a class=\"btn\" href=\"javascript:history.back()\">Go back and fix</a>");
        out.println("</main>");
        foot(out);
    }

    private void head(PrintWriter out, String ctx, String title) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\"><head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        out.println("<title>" + esc(title) + "</title>");
        out.println("<link rel=\"stylesheet\" href=\"" + ctx + "/css/style.css\">");
        out.println("</head><body>");
    }

    private void foot(PrintWriter out) {
        out.println("<footer class=\"footer\">&copy; 2026 Feedback Portal</footer>");
        out.println("</body></html>");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String firstName(String name) {
        int i = name.indexOf(' ');
        return i > 0 ? name.substring(0, i) : name;
    }

    /** Escapes HTML special characters to prevent XSS. */
    private static String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
