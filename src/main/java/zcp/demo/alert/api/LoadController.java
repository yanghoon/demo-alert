package zcp.demo.alert.api;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {
    private static String response = "Start";

    @GetMapping("/load/mem")
    public String memory(@RequestParam(name = "l", defaultValue = "1") int loop) {
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response + " (" + loop + ")";
    }

    @GetMapping("/load/cpu")
    public String cpu() {
        return response + "!!!";
    }

    private void read() throws IOException {
        Resource data = new ClassPathResource("static/mock.csv");
        byte[] buf = Files.readAllBytes(data.getFile().toPath());
        System.out.format("Read %s bytes.", buf.length);
    }
}