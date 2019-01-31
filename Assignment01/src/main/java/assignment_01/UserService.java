package assignment_01;

import ch.qos.logback.core.joran.conditional.ThenAction;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


//@Controller    // This means that this class is a Controller
//@RequestMapping(path="/demo") // This means URL's sta
@RestController
public class UserService {
    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @GetMapping("/api/add/") // Map ONLY GET Requests
    public @ResponseBody
    String addNewUser(@RequestParam String password
            , @RequestParam String email) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setPassword(password);
        n.setEmail(email);
        userRepository.save(n);
        return "{ \n  \"code\":\"201 Created.\"\n  \"reason\":\"Saved.\"\n}";
    }

    @GetMapping("/api/all/")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }


    //get for assignment
    @GetMapping("/api/") // Map ONLY GET Requests
    public @ResponseBody
    String authentiction(@RequestParam String auth) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request


        ArrayList<User> list = (ArrayList<User>) getAllUsers();

//        User newUser = new User();
//        newUser.setToken(auth);

        for (User user : list) {
            if (user.getToken().equals(auth)) {
                return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

//              return String.valueOf((new Date()).getTime());
            }

            //else return "{ \"you are not login.\"}";
        }

        return "{ \n  \"code\":\"404 Not Found.\"\n  \"reason\":\"You are not logged in.\"\n}";

    }


    //post for assignment
    @PostMapping("/api/user/register")
    public @ResponseBody
    String

    register(@RequestBody User newUser) {

        if (
                newUser.getEmail().matches("[\\w\\-]+@[a-zA-Z0-9]+(\\.[A-Za-z]{2,3}){1,2}")
        ) {
            //get users from database
            ArrayList<User> list = (ArrayList<User>) getAllUsers();

            if (list.size() == 0) {
                if (
                        newUser.getPassword().matches(".*[a-zA-Z].*") &&
                                newUser.getPassword().matches(".*[0-9].*") &&
                                newUser.getPassword().length() >= 8 &&
                                newUser.getPassword().length() <= 20) {


                    // BCrypt
                    String password = newUser.getPassword();
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                    newUser.setPassword(hashed);
                    //create token
                    String token = newUser.getEmail() + ":" + hashed;

                    Base64 base64 = new Base64();
                    String result = base64.encodeToString(token.getBytes());

                    newUser.setToken(result);

                    // the format of the password is correct and make it into Bcrypt token then save the user
                    userRepository.save(newUser);

                   // return result + "\n" + "{\"Sucessfully Registered\"}";
                      return "{ \n  \"code\":\"201 Created.\"\n  \"reason\":\"Successfully Registered.\"\n}";

                } else {

                   // return "{\"password invalid, The password must containing letters and numbers\"}";
                    return "{ \n  \"code\":\"406 Not Acceptable.\"\n  \"reason\":\"Invalid Password. The password must containing letters and numbers.\"\n}";

                }
            } else {


                //Bug here! 

                for (int i = 0; i < list.size(); i++) {
                    User user = list.get(i);
                    if (user.getEmail().equalsIgnoreCase(newUser.getEmail())) {
                        //return "{\"result\":\"exist\"}";
                          return "{ \n  \"code\":\"403 Not Forbidden.\"\n  \"reason\":\"The account already exists.\"\n}";

                    } else {
                        if(i == list.size() - 1) {
                            if (
                                    newUser.getPassword().matches(".*[a-zA-Z].*") &&
                                            newUser.getPassword().matches(".*[0-9].*") &&
                                            newUser.getPassword().length() >= 8 &&
                                            newUser.getPassword().length() <= 20) {


                                // BCrypt
                                String password = newUser.getPassword();
                                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                                newUser.setPassword(hashed);
                                //create token
                                String token = newUser.getEmail() + ":" + hashed;

                                Base64 base64 = new Base64();
                                String result = base64.encodeToString(token.getBytes());

                                newUser.setToken(result);
//            //Bcrypt
//            String password = newUser.getPassword();
//            String hashed = BCrypt.hashpw(password);


                                // the format of the password is correct and make it into Bcrypt token then save the user
                                userRepository.save(newUser);


                                // return the token and tell user successfully registered
                                //return result+" " + System.currentTimeMillis();

                               // return result + "\n" + "{\"Sucessfully Registered\"}";

 
                                  return "{ \n  \"code\":\"201 Created.\"\n  \"reason\":\"Successfully Registered.\"\n}";
                                //return "{\"Valid Password\"}";
                                  
                                 

                            }


//        else if(newUser.getPassword().equals("Fang")) {
//
//
////            //Bcrypt
////
//
//        }
//
                            else {


                                //return "{\"email\":\""+newUser.getEmail()+"\", \"name\":\""+newUser.getPassword()+"\"}";


                                //return "{\"password invalid, The password must containing letters and numbers\"}";

                               return "{ \n  \"code\":\"406 Not Acceptable.\"\n  \"reason\":\"Invalid Password. The password must containing letters and numbers.\"\n}";


                            }
                        } else {
                            continue;
                        }
                }
                }
            }
            //return null;
        } else {
           // return "{\"result\":\"email invalid, Please input the right format of email to create an account\"}";

          return "{ \n  \"code\":\"406 Not Acceptable.\"\n  \"reason\":\"Invalid Email. Please input the right format of email to create an account.\"\n}";

        }

        return null;
    }
}
