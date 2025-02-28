package edu.ucsb.cs156.example.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomepageController {
    @GetMapping("/")
    public String index(){
        return """
                <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
                <p><ul>
                  <li><a href="/oauth2/authorization/google">To sign in, click here</a></li>
                  <li><a href="/logout">To sign out, click here</a></li>
                  <li><a href="/swagger-ui/index.html">Click here to go to Swagger</a></li>
                </ul></p>
                """;
    }

}
