# User Feedback Form Application

Servlet + HTML form app (Jakarta Servlet 6, Java 17, Tomcat 10+).

## Build & run
    mvn clean package
    # copy target/feedback-app.war into Tomcat's webapps/ and start Tomcat
    # open http://localhost:8080/feedback-app/

## Structure
- src/main/webapp/index.html   - feedback form (POST and GET buttons)
- src/main/java/.../FeedbackServlet.java - doGet / doPost, validation, confirmation page
- src/main/webapp/css/style.css - styling

Tomcat 9 or older: use javax.servlet-api 4.0.1 and change `jakarta.servlet` imports to `javax.servlet`.
