package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responsJson = new JsonObject();
        responsJson.addProperty("success", false);
        
        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        String mobile = requestJson.get("mobile").getAsString();
        String password = requestJson.get("password").getAsString();

        if (mobile.isEmpty()) {
            responsJson.addProperty("message", "Please Fill Mobile Number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            responsJson.addProperty("message", "Ivalid Mobile Number");
        } else if (password.isEmpty()) {
            responsJson.addProperty("message", "Please Fill Password");
        } else if (!Validations.isPasswordValid(password)) {
            responsJson.addProperty("message", "Password must need 8 characters, one Uppercase , Number and special character");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            //search mobile number
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));
            criteria1.add(Restrictions.eq("password", password));
            if (!criteria1.list().isEmpty()) {
                //mobile number found
//                responsJson.addProperty("message", "Mobile Number Already Used");
                 User user = (User) criteria1.uniqueResult();
                 
                 responsJson.addProperty("success", true);
                 responsJson.addProperty("success", "Sign IN Success");
                 responsJson.add("user", gson.toJsonTree(user));
                 
            } else {
                //user not found
                
                responsJson.addProperty("success", "Registration Complete");

            }
            session.close();
        }

//        responsJson.addProperty("message", "Server:Hello!");

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responsJson));
    }

}
