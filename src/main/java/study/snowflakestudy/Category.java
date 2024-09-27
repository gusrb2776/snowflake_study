package study.snowflakestudy;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import study.snowflakestudy.snowflake.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Category{

    @Id
    // name : GeneratedValue 에서 사용할 이름, strategy : 어떻게 생성할껀지. (IdentifierGenerator 구현한 친구)
    @GenericGenerator(name ="snowflake_id", strategy = "study.snowflakestudy.snowflake.CustomSnowFlakeGenerator")
    // 바로위에서 이름 정한애로 사용하는거임.
    @GeneratedValue(generator = "snowflake_id")
    private Long id;

    private Long test;
}
