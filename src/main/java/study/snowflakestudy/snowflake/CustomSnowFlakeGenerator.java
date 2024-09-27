package study.snowflakestudy.snowflake;


import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomSnowFlakeGenerator implements IdentifierGenerator {

    // 각각 4비트, 9비트, 10비트로 구성됨  =  23비트
    // 여기 CASE_ONE 이나 CASE_TWO를 내가 원하는 무언가로 바꾸면 될듯!?
    // 찾아보니 case_one이나 case_two에 머신ID나 근로자ID같은걸 넣어주고 거기의 ID규칙에 맞게 비트를 조정함.
    // 우린 딱히 없으니 걍 지움
//    private static final int CASE_ONE_BITS = 4;
//    private static final int CASE_TWO_BITS = 9;
    private static final int SEQUENCE_BITS = 10;

    // 시퀸스는 중복방지로 같은 시간에 들어올경우 구분해주기 위해서 있는거임.
    // 시퀸스의 최댓값을 미리 지정해놓음. 2^10 = 1024 - 1 = 1023.
    private static final int maxSequence = (int) (Math.pow(2, SEQUENCE_BITS) - 1);

    // 원래 1970년부터 시간이 시작되는데 2015년 1월1일을 기준으로 에포크를 새로 지정. 그래야 41비트내로 쳐낼 수 있음.
    private static final long CUSTOM_EPOCH = 1420070400000L;

    // volatile로 선언하여 멀티 쓰레드에서 가시성 보장. 이러면 메인메모리에 올라가니까!
    private volatile long sequence = 0L;
    // ID 생성에 사용되는 고유값 <- 적당한 값으로 넣어주면 됨.
    private int case_one = 10;
    private int case_two = 0;
    // 마지막에 ID를 생성한 시간
    private volatile long lastTimestamp = -1L;

    // @GeneratedValue에서 이걸 호출해서 Id를 생성함
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return nextId();
    }

    // 타임스탬프 값을 줄여서 41비트내로 표현이 가능하도록 바꿔주기
    private static long timestamp() {
        // 기존 1970년부터는 좀 빡셈 그래서 2015년부터인애를 빼서 맞추는거
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;
        return makeId(currentTimestamp);
    }

    private Long makeId(long currentTimestamp) {
        long id = 0;

        // 41비트짜리 timeStamp를 23비트 옆으로 이동해서 64비트로 맞춤.  110...1 + 23비트 (xxx)..
        // 우리는 timestamp랑 sequence만 있으면 될거같아서 다 지움
//        id |= (currentTimestamp << CASE_ONE_BITS + CASE_TWO_BITS + SEQUENCE_BITS);
        id |= (currentTimestamp << SEQUENCE_BITS);
        // 자릿수 맞춰서 넣어주기 위해서.. 뒤에 애들 비트수만큼 옆으로 떙기는거.
//        id |= (case_one << CASE_TWO_BITS + SEQUENCE_BITS);
//        id |= (case_two << SEQUENCE_BITS);
        id |= sequence;

        return id;
    }

    // 동일시간에 들어온 요청에 대해서 sequence값을 증가시켜서 중복을 방지함.
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }
}
