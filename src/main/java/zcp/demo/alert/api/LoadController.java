package zcp.demo.alert.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {
    private static String response = "Start";

    private ArrayList<byte[]> cache = new ArrayList<>();

    private Pattern regx = Pattern.compile("(\\d+)(g|m)(i)?(-)?");
    private AtomicInteger delta = new AtomicInteger();

    @GetMapping("/load/mem/{size}")
    public String memory(@PathVariable String size) {
        Matcher m = regx.matcher(size);
        if(!m.find()) return "Invalid Parameter";

        long amount = Long.parseLong(m.group(1));
        String unit = m.group(2);
        boolean sig = "i".equals(m.group(3));
        boolean minus = "-".equals(m.group(4));

        if(minus) amount *= -1;
        if("g".equals(unit)) amount *= 1000;
        if(sig) amount = amount / 1024 * 1000;

        delta.addAndGet((int) amount/10);

        return response + " " + (minus ? "" : "+") + amount + unit.toUpperCase() + (sig ? "i" : "");
    }

    @GetMapping("/load/cpu")
    public String cpu() {
        return response + "!!!";
    }

    @Scheduled(fixedRate = 3000)
    private void memoryLoad() throws IOException {
        final int delta = this.delta.getAndSet(0);
        if(delta == 0) return;
        
        final int direction = delta < 0 ? -1 : 1;
        for(int i = delta; i != 0; i -= direction){
            if (0 < direction) {
                cache.add(read());
            } else {
                if(cache.isEmpty()) break;
                cache.remove(cache.size() - 1);
            }
        }

        if(direction == -1) System.gc();

        long heap = Runtime.getRuntime().totalMemory();
        System.out.format("Memory Status : %s mb (heap: %d mb)\n", cache.size() * 10, heap/1024/1024);
    }

    private byte[] read() throws IOException {
        Resource data = new ClassPathResource("static/mock.csv");

        // byte[] buf = Files.readAllBytes(data.getFile().toPath());

        InputStream is = data.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int len;
        byte[] buf = new byte[100 * 1024];
        while ((len = is.read(buf, 0, buf.length)) != -1) {
            buffer.write(buf, 0, len);
        }

        buf = buffer.toByteArray();
        // System.out.format("Read %s bytes.\n", buf.length);

        return buf;
    }
}