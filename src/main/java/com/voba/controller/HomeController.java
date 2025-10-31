package com.voba.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller für die Startseite der Anwendung. */
@Controller
public class HomeController {

  /**
   * Zeigt die Startseite der Anwendung an.
   *
   * @return Name der Index-View
   */
  @GetMapping("/")
  public String index() {
    return "index";
  }
}
