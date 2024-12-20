/*
 * This project is licensed under the MIT license. Module model-view-viewmodel is using ZK framework licensed under LGPL (see lgpl-3.0.txt).
 *
 * The MIT License
 * Copyright © 2014-2022 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.monolithic.controller;
import com.iluwatar.monolithic.exceptions.InsufficientStockException;
import com.iluwatar.monolithic.exceptions.NonExistentProductException;
import com.iluwatar.monolithic.exceptions.NonExistentUserException;
import com.iluwatar.monolithic.model.Orders;
import com.iluwatar.monolithic.model.Products;
import com.iluwatar.monolithic.model.User;
import com.iluwatar.monolithic.repository.OrderRepo;
import com.iluwatar.monolithic.repository.ProductRepo;
import com.iluwatar.monolithic.repository.UserRepo;
import org.springframework.stereotype.Service;
/**
 * OrderCon is a controller class for managing Order operations.
 * */
@Service
public class OrderCon {
  private final OrderRepo orderRepository;
  private final UserRepo userRepository;
  private final ProductRepo productRepository;
  /**
   * This function handles the initializing of the controller.
   * */
  public OrderCon(OrderRepo orderRepository, UserRepo userRepository, ProductRepo productRepository) {
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
  }
  /**
   * This function handles placing orders with all of its cases.
   * */
  public Orders placeOrder(Long userId, Long productId, Integer quantity) {
    final User user = userRepository.findById(userId).orElseThrow(() -> new NonExistentUserException("User with ID " + userId + " not found"));

    final Products product = productRepository.findById(productId).orElseThrow(() -> new NonExistentProductException("Product with ID " + productId + " not found"));
    
    if (product.getStock() < quantity) {
      throw new InsufficientStockException("Not enough stock for product " + productId);
    }

    product.setStock(product.getStock() - quantity);
    productRepository.save(product);

    final Orders order = new Orders(null, user, product, quantity, product.getPrice() * quantity);
    return orderRepository.save(order);
  }
}