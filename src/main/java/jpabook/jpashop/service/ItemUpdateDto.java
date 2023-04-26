package jpabook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemUpdateDto {

    private int price;
    private int stockQuantity;
}
