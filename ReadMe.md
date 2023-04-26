# [실전! 스프링 부트와 JPA 활용1](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1/dashboard)
<hr>

## 쿼리 파라미터 로그 남기기
- 외부 라이브러리
  - Spring Boot DataSource Decorator(https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
    - ```implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${version}")``` << 라이브러리 추가
    - 1.8.1 -> Spring boot 2.x
    - 1.9.0 -> Spring boot 3.x
<hr>

## 요구사항 분석
### 회원 기능
- 회원 가입
- 회원 조회

### 상품 기능
- 상품 등록
- 상품 수정
- 상품 조회

### 주문 기능
- 상품 주문
- 주문 내역 조회
- 주문 취소

### 기타 요구사항
- 상품은 제고 관리가 필요하다.
- 상품의 종류는 도서, 음반, 영화가 있다.
- 상품을 카테고리로 구분할 수 잇다.
- 상품 주문시 배송 정보를 입력할 수 있다.

### 예제를 단순화 하기 위해 다음 기능은 구현 X
- 로그인과 권한 관리X
- 파라미터 검증과 예외 처리 단순화
- 상품은 도서만 사용
- 카테고리 사용X
- 배송 정보는 사용X

<hr>

## 도메인 모델과 테이블 설계
- 회원 : 주문 = 1 : N
- 주문 : 상품 = N : N => 주문상품
- 주문 : 배송 = 1 : 1
- 상품 <|- 도서, 음반, 영화
- 상품 : 상품 카테고리 = N : N
### 회원(Member) 엔티티
- id : Long
- name : String
- address : Address
- orders : List <- 양방향 연관관계 안좋음, 풍성한 예제를 위해 들어갔음.
#### << Value Type>>
  - 주소(Address)
    - city
    - street
    - zipcode
### 주문(Order) 엔티티
- id
- member : Member
- orderIdems : List
- delivery : Delivery
- orderDate : Date
- status : OrderStatus
### 배송(Delivery) 엔티티
- id
- order : Order  <- 양방향 연관관계 안좋음, 풍성한 예제를 위해 들어갔음.
- address : Address
- status : DeliveryStatus
### 주문상품(OrderItem) 엔티티
- id
- item : Item
- order : Order
- orderPrice
- count
### 상품(Item) 엔티티
- id
- name
- price : int
- stockQuantity
- categories: List
#### 음반(Album)
- artist
- etc
#### 도서(Book)
- author
- isbn
#### 영화(Movie)
- director
- actor
### 상품 카테고리(Category) 엔티티
- id
- name
- items : List
- parent : Category
- child : List
#### 참고
- 카테고리와 상품은 ```@ManyToMany```를 사용해서 매핑한다.(실무에서 ```@ManyToMany```는 사용하지 말자. 여기서는 다대다 관계를 예제로 보여주기 위해 추가했을 뿐이다.)
<br>

### 회원 테이블(MEMBER)
- MEMBER_ID(PK)
- CITY
- STREET
- ZIPCODE
### 주문 테이블(ORDERS)
- ORDER_ID(PK)
- MEMBER_ID(FK)
- DELIVERY_ID(FK)
- ORDERDATE
- STATUS
### 배송 테이블(DELIVERY)
- DELIVERY_ID(PK)
- STATUS
- CITY
- STREET
- ZIPCODE
### 주문 상품 테이블(ORDER_ITEM)
- ORDER_ITEM_ID(PK)
- ORDER_ID(FK)
- ITEM_ID(FK)
- ORDERPRICE
- COUNT
### 상품 테이블(ITEM)
- ITEM_ID(PK)
- NAME
- PRICE
- STOCKQUANTITY
- DTYPE
- ARTIST
- ETC
- AUTHOR
- ISBN
- DIRECTOR
- ACTOR
### 상품 카테고리 테이블(CATEGORY)
- CATEGORY_ID(PK)
- PARENT_ID(FK)
- NAME
### 카테고리 상품 테이블(CATEGORY_ITEM)
- CATEGORY_ID(PK/FK)
- ITEM_ID(PK/FK)

## 엔티티 클래스 개발 시 주의점
- Setter를 막 열어두면 가까운 미래에 엔티티가 도대체 왜 변경되는지 추적하기 힘들어진다.
- 따라서, 엔티티를 변경할 때는 Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.

## 엔티티 설계시 주의점
### 엔티티에는 가급적 Setter를 사용하지 말자.
- Setter가 모두 열려있으면, 변경 포인트가 너무 많아서 유지보수가 어렵다.
### 모든 연관관계는 지연로딩(Lazy)으로 설정.
- 즉시로딩(Eager)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다.
- 특히 JPQL을 실행할 때 N+1문제가 자주 발생한다.
- 따라서 실무에서 모든 연관관계는 지연로딩(Layz)으로 설정해야 한다.
- 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
- ```@XToOne(OneToOne, ManyToOne)```관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.
### 컬렉션은 필드에서 초기화 하자.
- null 문제에서 안전하다.
- 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다.
```
Member member = new Member();
System.out.println(member.getOrders().getClass());
em.persist(member);
System.out.println(member.getOrders().getClass());

//출력 결과
class java.util.ArrayList
class org.hibernate.collection.internal.PersistentBag
```
- 만약 ```getOrders()```처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.
- 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.
### 테이블, 컬럼명 생성 전략
- 스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
- 카멜 케이스 -> 스네이크 케이스
- .(점) -> _(언더스코어)
- 대문자 -> 소문자
- https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#naming
- https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#howto-configure-hibernate-naming-strategy
- 적용2단계
  1. 논리명 생성: 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ```ImplicitNamingStrategy``` 사용
    ```spring.jpa.hibernate.naming.implicit-strategy``` : 테이블이나, 컬럼명을 명시하지 않을 때 논리명
    적용,
  2. 물리명 적용:
     ```spring.jpa.hibernate.naming.physical-strategy``` : 모든 논리명에 적용됨, 실제 테이블에 적용
     (username -> usernm 등으로 회사 룰로 바꿀 수 있음)

## 애플리케이션 아키텍처
### 계층형 구조 사용
- controller, web : 웹 계층
- service : 비즈니스 로직, 트랜잭션 처리
- repository : JPA를 직접 사용하는 계층, 엔티티 매니저 사용
- domain : 엔티티가 모여 있는 계층, 모든 계층에서 사용

### 패키지 구조
- jpabook.jpashop
  - domain
  - exception
  - repository
  - service
  - web

### 개발 순서
1. 서비스, 리포지토리 계층을 개발
2. 테스트 케이스를 작성해서 검증
3. 마지막에 웹 계층 적용

### cascade = CascadeType.ALL 의 범위
- persist 라이프 사이클이 완전히 똑같은 경우 사용 O
- 다른 것들이 참조할 수 없는 private owner인 경우 사용 O
- 여러 클래스에서 참조하는 클래스를 참조한다면 cascade = CascadeType.ALL 를 함부로 사용하면 안된다.

## JPA 변경감지(dirty checking)와 병합(merge)
### 준영속(상태) 엔티티
- 데이터가 존재하지만 영속성 컨텍스트(Entity Manager, JPA)가 관리하지 않는 엔티티
- 생성자를 통해 생성된 객체가 데이터베이스에 존재하는 식별자를 가지게 된다면 영속성 컨텍스트는 식별자를 통해서 엔티티를 구분하므로, 데이터베이스에 데이터가 존재하지만 영속성 컨텍스트가 관리하지 않으므로 준영속(상태) 엔티티로 볼 수 있다.
- ex) ```Member member = new Member(); member.setId(id);```
- 직접적으로 준영속상태로 변환시키려면 ```Member detachMember = em.detach(member)```
- 영속성 컨텍스트가 관리하는 모든 엔티티를 준영속 상태로 만들려면 ```em.clear()```

### 영속(상태) 엔티티
- 영속성 컨텍스트(Entity Manager, JPA)가 관리하는 엔티티
- ```Member savedMember = em.persist(member)```
- ```Member mergedMember = em.merge(member)```
- ```Member findMember = em.find(Member.class, id)```
- 위와 같이 반환된 객체는 영속성 컨텍스트가 관리한다.

### 변경감지(dirty checking)
- 영속 상태의 엔티티의 값을 바꾸어 트랜잭션 커밋 시점에 데이터를 수정하는 방법
- 트랜잭션 커밋 시점에 영속성 컨텍스트가 관리하는 엔티티의 변경이 감지되면 영속성 컨텍스트가 데이터베이스에 UPDATE SQL을 실행한다.

### 병합(merge)
- 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다.
- 병합 작동 방식
  1. ```em.merge(entity)```를 실행한다.
  2. parameter 로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
     - 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 조회한 엔티티를 1차 캐시에 저장한다.
  3. 조회한 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 치환(교체)한다.
  4. 값이 치환된 영속성 엔티티를 반환한다.

### 변경감지와 병합의 차이점
- 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이
  변경된다.
- __<U>**병합은 모든 필드를 교체하므로, 병합시 값이 없으면 ```null```로 업데이트 할 위험도 있다.**</U>__

### 결론 및 요약
- 실무에서는 보통 업데이트하는 값이 매우 제한적이고, 변경가능한 데이터만 노출하기 때문에, 병합을
  사용하는 것은 매우 번거롭다.
- **따라서, 엔티티를 변경할 때는 항상 변경 감지를 사용하는 것이 좋다.**
- 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
  - 갑자기 DB에서 데이터 속성값이 ```null```로 변경될 수 있음.
- 트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
- 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.