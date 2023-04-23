package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    QOrder order = QOrder.order;
    QMember member = QMember.member;



    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {

        return jpaQueryFactory
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(
                        nameLike(orderSearch.getMemberName()),
                        statusEq(orderSearch.getOrderStatus())
                )
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String memberName) {
        return memberName == null ? null : order.member.name.contains(memberName);
    }

    private BooleanExpression statusEq(OrderStatus orderStatus) {
        return orderStatus == null ? null : order.status.eq(orderStatus);
    }
}
