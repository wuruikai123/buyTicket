# Requirements Document

## Introduction

本功能为展览购票系统用户端（frontend-a）实现完整的购物车和商城订单结算流程。目前系统已有购物车API和商品列表，但缺少购物车页面和结算功能，用户无法完成商城购物的完整流程。

## Glossary

- **Shopping_Cart_System**: 购物车系统，管理用户添加的商品及数量
- **Cart_Item**: 购物车项，包含商品信息和购买数量
- **Checkout_Flow**: 结算流程，从购物车到创建订单并支付的完整过程
- **User**: C端用户，使用移动端购物的消费者
- **Product**: 文创商品，商城中销售的商品
- **Mall_Order**: 商城订单，用户购买商品后生成的订单记录

## Requirements

### Requirement 1

**User Story:** As a User, I want to view my shopping cart, so that I can see all products I've added and their quantities.

#### Acceptance Criteria

1. WHEN a User navigates to the cart page THEN the Shopping_Cart_System SHALL display all Cart_Items with product image, name, price, and quantity
2. WHEN the cart is empty THEN the Shopping_Cart_System SHALL display an empty state message with a link to the mall page
3. WHEN a Cart_Item is displayed THEN the Shopping_Cart_System SHALL show the subtotal (price × quantity) for each item
4. WHEN the cart page loads THEN the Shopping_Cart_System SHALL display the total amount of all Cart_Items at the bottom

### Requirement 2

**User Story:** As a User, I want to modify cart item quantities, so that I can adjust how many of each product I want to buy.

#### Acceptance Criteria

1. WHEN a User increases the quantity of a Cart_Item THEN the Shopping_Cart_System SHALL update the quantity and recalculate the subtotal
2. WHEN a User decreases the quantity to zero THEN the Shopping_Cart_System SHALL remove the Cart_Item from the cart
3. WHEN a User clicks the delete button on a Cart_Item THEN the Shopping_Cart_System SHALL remove that item from the cart
4. WHEN quantity changes occur THEN the Shopping_Cart_System SHALL update the total amount immediately

### Requirement 3

**User Story:** As a User, I want to select items for checkout, so that I can choose which products to purchase now.

#### Acceptance Criteria

1. WHEN viewing the cart THEN the Shopping_Cart_System SHALL provide a checkbox for each Cart_Item to select for checkout
2. WHEN a User toggles the "select all" checkbox THEN the Shopping_Cart_System SHALL select or deselect all Cart_Items
3. WHEN items are selected THEN the Shopping_Cart_System SHALL display the selected items count and their total amount
4. WHEN no items are selected THEN the Shopping_Cart_System SHALL disable the checkout button

### Requirement 4

**User Story:** As a User, I want to proceed to checkout, so that I can complete my purchase.

#### Acceptance Criteria

1. WHEN a User clicks the checkout button with selected items THEN the Checkout_Flow SHALL navigate to the order confirmation page
2. WHEN on the order confirmation page THEN the Checkout_Flow SHALL display selected products, quantities, and total amount
3. WHEN on the order confirmation page THEN the Checkout_Flow SHALL require the User to enter or select a shipping address
4. WHEN on the order confirmation page THEN the Checkout_Flow SHALL require the User to enter contact name and phone number

### Requirement 5

**User Story:** As a User, I want to submit my order and pay, so that I can complete the purchase transaction.

#### Acceptance Criteria

1. WHEN a User submits the order with valid information THEN the Checkout_Flow SHALL create a Mall_Order via the backend API
2. WHEN the Mall_Order is created successfully THEN the Checkout_Flow SHALL proceed to payment
3. WHEN payment is successful THEN the Checkout_Flow SHALL display a success message and navigate to the order list
4. WHEN payment fails THEN the Checkout_Flow SHALL display an error message and allow retry
5. IF the User has not filled required fields THEN the Checkout_Flow SHALL display validation errors and prevent submission

### Requirement 6

**User Story:** As a User, I want to access my cart from the mall page, so that I can easily navigate to checkout.

#### Acceptance Criteria

1. WHEN a User clicks the cart icon on the mall page THEN the Shopping_Cart_System SHALL navigate to the cart page
2. WHEN items exist in the cart THEN the Shopping_Cart_System SHALL display a badge showing the total item count on the cart icon
