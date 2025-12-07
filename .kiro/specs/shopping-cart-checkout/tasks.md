# Implementation Plan

- [x] 1. Set up routing and utility functions


  - [x] 1.1 Add cart and checkout routes to router/index.ts


    - Add `/cart` route pointing to Cart.vue
    - Add `/mall-checkout` route pointing to MallCheckout.vue
    - _Requirements: 4.1, 6.1_
  - [x] 1.2 Create cart calculation utility functions


    - Create `src/utils/cartUtils.ts` with calculateSubtotal, calculateTotal, calculateSelectedTotal functions
    - _Requirements: 1.3, 1.4, 3.3_
  - [ ]* 1.3 Write property tests for cart calculation utilities
    - **Property 1: Subtotal calculation correctness**
    - **Property 2: Total amount calculation correctness**
    - **Validates: Requirements 1.3, 1.4, 2.4**

- [x] 2. Implement Cart page


  - [x] 2.1 Create Cart.vue basic structure


    - Header with back button and title
    - Cart list container
    - Bottom bar with select all, total, checkout button
    - _Requirements: 1.1, 1.4_

  - [ ] 2.2 Implement cart item display
    - Display product image, name, price, quantity
    - Display subtotal for each item
    - Handle empty cart state with link to mall

    - _Requirements: 1.1, 1.2, 1.3_
  - [ ] 2.3 Implement quantity modification
    - Add increase/decrease buttons
    - Call API to update quantity
    - Remove item when quantity reaches zero
    - _Requirements: 2.1, 2.2_
  - [x]* 2.4 Write property test for quantity increase

    - **Property 3: Quantity increase updates state correctly**
    - **Validates: Requirements 2.1**
  - [ ] 2.5 Implement delete functionality
    - Add delete button for each item
    - Call API to remove item
    - Update cart list after deletion
    - _Requirements: 2.3_

  - [ ]* 2.6 Write property test for delete operation
    - **Property 4: Delete removes item from cart**
    - **Validates: Requirements 2.3**
  - [ ] 2.7 Implement item selection
    - Add checkbox for each item
    - Implement select all toggle
    - Calculate and display selected count and total
    - Disable checkout button when nothing selected
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  - [ ]* 2.8 Write property tests for selection logic
    - **Property 5: Select all toggles all items**
    - **Property 6: Selected items calculation correctness**
    - **Validates: Requirements 3.2, 3.3**

- [x] 3. Checkpoint - Ensure cart page tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement MallCheckout page


  - [x] 4.1 Create MallCheckout.vue basic structure


    - Header with back button and title
    - Contact info section
    - Address section
    - Order items display
    - Bottom bar with total and pay button
    - _Requirements: 4.2, 4.3, 4.4_
  - [x] 4.2 Implement form validation

    - Validate contactName is not empty
    - Validate contactPhone is not empty
    - Validate address is not empty
    - Show validation errors
    - Disable submit when invalid
    - _Requirements: 4.3, 4.4, 5.5_
  - [ ]* 4.3 Write property test for form validation
    - **Property 7: Form validation prevents incomplete submission**
    - **Validates: Requirements 4.3, 4.4, 5.5**
  - [x] 4.4 Implement order submission and payment

    - Create order via API
    - Process payment
    - Handle success (navigate to profile/orders)
    - Handle failure (show error, allow retry)
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 5. Update Mall page with cart badge


  - [x] 5.1 Add cart item count badge to cart icon


    - Fetch cart count on page load
    - Display badge with item count
    - Update badge after adding to cart
    - _Requirements: 6.2_
  - [ ]* 5.2 Write property test for badge count
    - **Property 8: Cart badge count matches item count**
    - **Validates: Requirements 6.2**

- [x] 6. Final Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.
