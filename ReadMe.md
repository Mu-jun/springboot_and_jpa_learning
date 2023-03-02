# 실전! 스프링 부트와 JPA 활용1

## 쿼리 파라미터 로그 남기기
- 외부 라이브러리
  - Spring Boot DataSource Decorator(https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
    - ```implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${version}")``` << 라이브러리 추가
    - 1.8.1 -> Spring boot 2.x
    - 1.9.0 -> Spring boot 3.x