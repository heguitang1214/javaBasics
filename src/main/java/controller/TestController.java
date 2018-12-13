package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
//@RestController
@RequestMapping("/")
public class TestController {


    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public String test(){
        return "hello11122223333";
    }



}
