import com.reactive.learn.LearnApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LearnApplication.class)
public class TestFlux {
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Test
    public void testAdd(){
        IntStream.range(0, 10).forEach(i -> {
            redisTemplate.opsForList().leftPush("num", i).subscribe();
        });
    }

    @Test
    public void testSave(){
        redisTemplate.opsForValue().set("key", "word").subscribe();
    }
}
