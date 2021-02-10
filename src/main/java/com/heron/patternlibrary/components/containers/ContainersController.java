package com.heron.patternlibrary.components.containers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/containers")
public class ContainersController {

  @GetMapping("")
  public String bootstrap() {
    return "index";
  }

  @GetMapping("/foo")
  public String foo() {
    return "components/containers/foo";
  }
}
