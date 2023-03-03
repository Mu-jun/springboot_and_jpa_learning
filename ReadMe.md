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