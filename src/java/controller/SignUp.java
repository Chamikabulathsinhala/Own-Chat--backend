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

@MultipartConfig
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responsJson = new JsonObject();
        responsJson.addProperty("success", false);
        //JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        String mobile = request.getParameter("mobile");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part avatarImage = request.getPart("avatarImage");
        

        if (mobile.isEmpty()) {
            responsJson.addProperty("message", "Please Fill Mobile Number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            responsJson.addProperty("message", "Ivalid Mobile Number");
        } else if (firstName.isEmpty()) {
            responsJson.addProperty("message", "Please Fill First Name");
        } else if (lastName.isEmpty()) {
            responsJson.addProperty("message", "Please Fill Last Name");
        } else if (password.isEmpty()) {
            responsJson.addProperty("message", "Please Fill Password");
        } else if (!Validations.isPasswordValid(password)) {
            responsJson.addProperty("message", "Password must need 8 characters, one Uppercase , Number and special character");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            //search mobile number
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));

            if (!criteria1.list().isEmpty()) {
                //mobile number found
                responsJson.addProperty("message", "Mobile Number Already Used");
            } else {
                //mobile number not found
                User user = new User();
                user.setFirst_name(firstName);
                user.setLast_name(lastName);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegistered_date_time(new Date());

                //get user status 2 = offline
                User_Status user_status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_status(user_status);

                session.save(user);
                session.beginTransaction().commit();

                //check uploaded image
                if (avatarImage != null) {
                    //image selected

                    String serverPath = request.getServletContext().getRealPath("");
                    String avatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + mobile + ".png";
                    File file = new File(avatarImagePath);
                    Files.copy(avatarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                }
                responsJson.addProperty("success", true);
                responsJson.addProperty("message", "Registration Complete");
            }
            session.close();
        }

        //responsJson.addProperty("message", "Server:Hello!");

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responsJson));
    }

}
