package com.heron.patternlibrary.components.bootstrap;

import com.heron.patternlibrary.annotations.PatternLibraryComponents;
import com.heron.patternlibrary.components.bootstrap.models.Pagination;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@PatternLibraryComponents(docs = "bootstrap.md", order = 1)
@RequestMapping("/bootstrap")
public class BootstrapController {

  @GetMapping("/alerts")
  public String alerts() {
    return "components/bootstrap/alerts/alerts";
  }

  @GetMapping("/badges")
  public String badges() {
    return "components/bootstrap/badges/badges";
  }

  @GetMapping("/buttons")
  public String buttons() {
    return "components/bootstrap/buttons/buttons";
  }

  @GetMapping("/pagination")
  public ModelAndView pagination() {
    return new ModelAndView("components/bootstrap/pagination/pagination", Map.of("pagination",Pagination.create(1, 4, 12, p -> "")));
  }

  @GetMapping("/pagination--first-page")
  public ModelAndView pagination_firstPage() {
    return new ModelAndView("components/bootstrap/pagination/pagination", Map.of("pagination",Pagination.create(0, 4, 12, p -> "")));
  }

  @GetMapping("/pagination--last-page")
  public ModelAndView pagination_lastPage() {
    return new ModelAndView("components/bootstrap/pagination/pagination", Map.of("pagination",Pagination.create(2, 4, 12, p -> "")));
  }
}
