package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

/**
 *
 * @author Chamika
 */
@MultipartConfig
@WebServlet(name = "UserUpdate", urlPatterns = {"/UserUpdate"})
public class UserUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("5555555555555");
        Gson gson = new Gson();
        Session session = HibernateUtil.getSessionFactory().openSession();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        String mobile = request.getParameter("mobile");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part avatarImage = request.getPart("avatarImage");

        System.out.println(mobile);
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(password);
        
        if (mobile.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Mobile Number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            responseJson.addProperty("message", "Ivalid Mobile Number");
        } else if (firstName.isEmpty()) {
            responseJson.addProperty("message", "Please Fill First Name");
        } else if (lastName.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Last Name");
        } else if (password.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Password");
        } else if (!Validations.isPasswordValid(password)) {
            responseJson.addProperty("message", "Password must need 8 characters, one Uppercase , Number and special character");
        } else {

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));

            User user = (User) criteria1.uniqueResult();

            if (user != null) {
                System.out.println("lllllllllllllll");
                // Update user details
                user.setFirst_name(firstName);
                user.setLast_name(lastName);
                if (!password.isEmpty()) {
                    user.setPassword(password);
                }

//            session.beginTransaction();
                session.update(user);
                session.beginTransaction().commit();
                System.out.println("afsgsg");
//            session.getTransaction().commit();

                if (avatarImage != null && avatarImage.getSize() > 0) {
                    System.out.println("lllllllllllllll");
                    String serverPath = request.getServletContext().getRealPath("");
                    String avatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + mobile + ".png";
                    File file = new File(avatarImagePath);
                    Files.copy(avatarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Profile updated successfully.");
            } else {
                responseJson.addProperty("message", "User not found.");

            }
            session.close();
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}
