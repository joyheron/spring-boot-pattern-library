package com.heron.patternlibrary.components.containers;

import com.heron.patternlibrary.annotations.PatternLibraryComponents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PatternLibraryComponents(order = 2, docs = "containers.md")
@RequestMapping("/containers")
public class ContainersController {

  @GetMapping("/foo")
  public String foo() {
    return "components/containers/foo";
  }
}
