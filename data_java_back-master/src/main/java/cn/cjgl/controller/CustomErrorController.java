package cn.cjgl.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public Map<String, Object> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            // Returns different error messages based on status codes
            // This is a simple process that can be expanded to meet your needs.
            String errorMessage = "An error occurred: " + HttpStatus.valueOf(statusCode).getReasonPhrase();
            Map<String, Object> errorAttributes = new HashMap<>();
            errorAttributes.put("status", statusCode);
            errorAttributes.put("message", errorMessage);
            // You can add more detailed error information
            return errorAttributes;
        }

        // By default, return a general error message
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorAttributes.put("message", "An unknown error occurred.");
        return errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
