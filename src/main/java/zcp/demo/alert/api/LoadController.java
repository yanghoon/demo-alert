package zcp.demo.alert.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadController {
    private String MSG_INVALID = "Invalid Parameter (eg. 1g or 100m--)";
    private String MSG_TOO_SMALL;

    private Resource DATA;
    private long DATA_SIZE;
    private double DATA_SIZE_MIB;

    private ArrayList<byte[]> cache = new ArrayList<>();

    private Pattern regx = Pattern.compile("(\\d+)(g|m)(i)?(--)?");
    private AtomicInteger delta = new AtomicInteger();

    @PostConstruct
    public void init() throws IOException {
        DATA = new ClassPathResource("static/mock.csv");
        DATA_SIZE = DATA.getInputStream().available();
        DATA_SIZE_MIB = toBiFloat(DATA_SIZE);

        MSG_TOO_SMALL = String.format("Too small. (min: %s)", toBi(DATA_SIZE));
    }

    @GetMapping("/load/mem/{size}")
    public String memory(@PathVariable String size) throws IOException {
        Matcher m = regx.matcher(size);
        if(!m.find()) return MSG_INVALID;

        int amount = Integer.valueOf(m.group(1));
        String unit = m.group(2);
        boolean sig = "i".equals(m.group(3));
        boolean minus = "--".equals(m.group(4));

        if(minus) amount *= -1;
        if("g".equals(unit)) amount *= 1000;
        // if(sig) amount = amount / 1024 * 1000;

        if(Math.abs(amount) < DATA_SIZE_MIB) return MSG_TOO_SMALL;

        amount = (int) (amount / DATA_SIZE_MIB);
        delta.addAndGet(amount);

        memoryLoad();
        String ret = String.format("Scale %s %s.", (minus ? "down" : "up"), toBi(DATA_SIZE * Math.abs(amount)) );
        return ret + "\n" + status();
    }

    @GetMapping("/load/mem/clear")
    public String clear() {
        cache.clear();
        System.gc();
        return status();
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
        System.out.println(status());
    }

    @GetMapping("/load/mem/status")
    public String status(){
        // https://javaslave.tistory.com/23
        long hold = cache.stream().mapToLong(a->a.length).sum();
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long commited = Runtime.getRuntime().totalMemory();
        return String.format("Memory Status : %s (used: %s, commited: %s)", toBi(hold), toBi(used), toBi(commited));
    }

    private String toBi(long bytes) {
        // https://stackoverflow.com/a/3758880
        // https://github.com/JakeWharton/byteunits
        boolean si = false;
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private double toBiFloat(long bytes) {
        boolean si = false;
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes * 1.0d;
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        return bytes / Math.pow(unit, exp);
    }

    private byte[] read() throws IOException {
        return IOUtils.toByteArray(DATA.getInputStream());
        // InputStream is = DATA.getInputStream();
        // ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        // int len;
        // byte[] buf = new byte[100 * 1024];
        // // byte[] ret = new byte[(int) DATA_SIZE];
        // while ((len = is.read(buf, 0, buf.length)) != -1) {
        //     buffer.write(buf, 0, len);
        // }

        // return buffer.toByteArray();
    }
}