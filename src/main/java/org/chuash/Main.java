package org.chuash;

import org.chuash.config.SpringApplicationContextConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(SpringApplicationContextConfig.class);
    System.out.println("Hello world!");
  }
}