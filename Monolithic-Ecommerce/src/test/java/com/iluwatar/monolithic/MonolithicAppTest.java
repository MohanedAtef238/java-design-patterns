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
package com.iluwatar.monolithic;

import com.iluwatar.monolithic.controller.OrderCon;
import com.iluwatar.monolithic.controller.ProductCon;
import com.iluwatar.monolithic.controller.UserCon;
import com.iluwatar.monolithic.exceptions.InsufficientStockException;
import com.iluwatar.monolithic.exceptions.NonExistentProductException;
import com.iluwatar.monolithic.exceptions.NonExistentUserException;
import com.iluwatar.monolithic.model.Products;
import com.iluwatar.monolithic.model.Orders;
import com.iluwatar.monolithic.model.User;
import com.iluwatar.monolithic.repository.OrderRepo;
import com.iluwatar.monolithic.repository.ProductRepo;
import com.iluwatar.monolithic.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;


import static org.mockito.Mockito.*;

class MonolithicAppTest {

  @Mock
  private UserCon userService;

  @Mock
  private ProductCon productService;

  @Mock
  private OrderCon orderService;

  private EcommerceApp ecommerceApp;

  private ByteArrayOutputStream outputStream;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ecommerceApp = new EcommerceApp(userService, productService, orderService);
    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
  }

  @Test
  void testRegisterUser() {
    String simulatedInput = "John Doe\njohn@example.com\npassword123\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

    ecommerceApp.registerUser(new Scanner(System.in, StandardCharsets.UTF_8));

    verify(userService, times(1)).registerUser(any(User.class));
    assertTrue(outputStream.toString().contains("User registered successfully!"));
  }

  @Test
  void testPlaceOrderUserNotFound() {
    UserRepo mockUserRepo = mock(UserRepo.class);
    ProductRepo mockProductRepo = mock(ProductRepo.class);
    OrderRepo mockOrderRepo = mock(OrderRepo.class);

    when(mockUserRepo.findById(1L)).thenReturn(Optional.empty());

    OrderCon orderCon = new OrderCon(mockOrderRepo, mockUserRepo, mockProductRepo);

    Exception exception = assertThrows(NonExistentUserException.class, () -> {
        orderCon.placeOrder(1L, 1L, 5);
    });

    assertEquals("User with ID 1 not found", exception.getMessage());
  }

    @Test
  void testPlaceOrderProductNotFound() {
    UserRepo mockUserRepo = mock(UserRepo.class);
    ProductRepo mockProductRepo = mock(ProductRepo.class);
    OrderRepo mockOrderRepo = mock(OrderRepo.class);

    User mockUser = new User(1L, "John Doe", "john@example.com", "password123");
    when(mockUserRepo.findById(1L)).thenReturn(Optional.of(mockUser));

    when(mockProductRepo.findById(1L)).thenReturn(Optional.empty());

    OrderCon orderCon = new OrderCon(mockOrderRepo, mockUserRepo, mockProductRepo);

    Exception exception = assertThrows(NonExistentProductException.class, () -> {
        orderCon.placeOrder(1L, 1L, 5);
    });

    assertEquals("Product with ID 1 not found", exception.getMessage());
  }



  @Test
  void testOrderConstructor(){
    OrderRepo mockOrderRepo = mock(OrderRepo.class);
    UserRepo mockUserRepo = mock(UserRepo.class);
    ProductRepo mockProductRepo = mock(ProductRepo.class);

    OrderCon orderCon = new OrderCon(mockOrderRepo, mockUserRepo, mockProductRepo);

    assertNotNull(orderCon);
  }

  @Test
  void testAddProduct() {
    String simulatedInput = "Laptop\nGaming Laptop\n1200.50\n10\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

    ecommerceApp.addProduct(new Scanner(System.in, StandardCharsets.UTF_8));

    verify(productService, times(1)).addProduct(any(Products.class));
    assertTrue(outputStream.toString().contains("Product added successfully!"));
  }

  @Test
  void testPlaceOrderSuccess() {
    String simulatedInput = "1\n2\n3\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

    Orders mockOrder = new Orders();
    doReturn(mockOrder).when(orderService).placeOrder(anyLong(), anyLong(), anyInt());

    ecommerceApp.placeOrder(new Scanner(System.in, StandardCharsets.UTF_8));

    verify(orderService, times(1)).placeOrder(anyLong(), anyLong(), anyInt());
    assertTrue(outputStream.toString().contains("Order placed successfully!"));
  }

  @Test
  void testPlaceOrderFailure() {
    String simulatedInput = "1\n2\n3\n";
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

    doThrow(new RuntimeException("Product out of stock"))
        .when(orderService).placeOrder(anyLong(), anyLong(), anyInt());

    ecommerceApp.placeOrder(new Scanner(System.in, StandardCharsets.UTF_8));

    verify(orderService, times(1)).placeOrder(anyLong(), anyLong(), anyInt());
    assertTrue(outputStream.toString().contains("Error placing order: Product out of stock"));
  }
  @Test
  void testPlaceOrderInsufficientStock() {
    UserRepo mockUserRepo = mock(UserRepo.class);
    ProductRepo mockProductRepo = mock(ProductRepo.class);
    OrderRepo mockOrderRepo = mock(OrderRepo.class);

    User mockUser = new User(1L, "John Doe", "john@example.com", "password123");
    when(mockUserRepo.findById(1L)).thenReturn(Optional.of(mockUser));
    Products mockProduct = new Products(1L, "Laptop", "High-end gaming laptop", 1500.00, 2); // Only 2 in stock
    when(mockProductRepo.findById(1L)).thenReturn(Optional.of(mockProduct));

    OrderCon orderCon = new OrderCon(mockOrderRepo, mockUserRepo, mockProductRepo);

    Exception exception = assertThrows(InsufficientStockException.class, () -> {
        orderCon.placeOrder(1L, 1L, 5);
    });
    assertEquals("Not enough stock for product 1", exception.getMessage());
}
  @Test
  void testProductConAddProduct() {
    ProductRepo mockProductRepo = mock(ProductRepo.class);

    Products mockProduct = new Products(1L, "Smartphone", "High-end smartphone", 1000.00, 20);

    when(mockProductRepo.save(any(Products.class))).thenReturn(mockProduct);

    ProductCon productCon = new ProductCon(mockProductRepo);

    Products savedProduct = productCon.addProduct(mockProduct);

    verify(mockProductRepo, times(1)).save(any(Products.class));

    assertNotNull(savedProduct);
    assertEquals("Smartphone", savedProduct.getName());
    assertEquals("High-end smartphone", savedProduct.getDescription());
    assertEquals(1000.00, savedProduct.getPrice());
    assertEquals(20, savedProduct.getStock());
  }

  @Test
  void testRun() {
    String simulatedInput = """
        1
        John Doe
        john@example.com
        password123
        2
        Laptop
        Gaming Laptop
        1200.50
        10
        3
        1
        1
        2
        4
        """;                                          // Exit
    System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

    ByteArrayOutputStream outputTest = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputTest, true, StandardCharsets.UTF_8));

    when(userService.registerUser(any(User.class))).thenReturn(new User(1L, "John Doe", "john@example.com", "password123"));
    when(productService.addProduct(any(Products.class))).thenReturn(new Products(1L, "Laptop", "Gaming Laptop", 1200.50, 10));
    when(orderService.placeOrder(anyLong(), anyLong(), anyInt())).thenReturn(new Orders(1L, new User(1L, "John Doe", "john@example.com","password123" ), new Products(1L, "Laptop", "Gaming Laptop", 1200.50, 10), 5, 6002.50));

    ecommerceApp.run();

    verify(userService, times(1)).registerUser(any(User.class));
    verify(productService, times(1)).addProduct(any(Products.class));
    verify(orderService, times(1)).placeOrder(anyLong(), anyLong(), anyInt());

    String output = outputTest.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("Welcome to the Monolithic E-commerce CLI!"));
    assertTrue(output.contains("Choose an option:"));
    assertTrue(output.contains("Register User"));
    assertTrue(output.contains("Add Product"));
    assertTrue(output.contains("Place Order"));
    assertTrue(output.contains("Exiting the application. Goodbye!"));
}




}