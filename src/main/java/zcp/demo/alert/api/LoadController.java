package zcp.demo.alert.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {
    private static String response = "Start";

    @GetMapping("/load/mem")
    public String memory() {
        return response;
    }

    @GetMapping("/load/cpu")
    public String cpu() {
        return response + "!!!";
    }
}