package com.heron.patternlibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class PatternLibraryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternLibraryController.class);

  private static final String COMPONENT_PACKAGE_PREFIX = "com.heron.patternlibrary.components";

  private static List<PatternLibraryGroup> patternLibraryGroups;

  @Autowired
  private RequestMappingHandlerMapping requestHandlerMapping;

  @Autowired
  private ResourceLoader resourceLoader;

  @Value( "${spring.thymeleaf.prefix:/templates/}" )
  private String thymeleafPrefix;

  @GetMapping("/")
  public String index() {

    System.out.println(getPatternLibraryGroups());

    return "index";
  }

  private List<PatternLibraryGroup> getPatternLibraryGroups() {
    if (patternLibraryGroups != null) {
      return patternLibraryGroups;
    }

    Map<? extends Class<?>, List<Map.Entry<RequestMappingInfo, HandlerMethod>>> collect = getComponentEndpoints()
        .collect(Collectors.groupingBy(entry -> entry.getValue().getBeanType()));

    patternLibraryGroups = collect.entrySet()
        .stream()
        .map(entry -> {
          // Remove "Controller" from class name to get name for category
          String name = entry.getKey().getSimpleName().replaceAll("Controller", "");

          String uri = entry.getValue()
              .stream()
              .map(e -> e.getKey().getPatternsCondition().getPatterns())
              .flatMap(Set::stream)
              .min(Comparator.comparing(String::length))
              .orElseThrow(() -> new RuntimeException("Could not find a base URI for Controller: " + entry.getKey()));

          Map<String, PatternLibraryExample> examples = entry.getValue()
              .stream()
              .map(e -> Map.entry(e.getKey().getPatternsCondition().getPatterns().stream().findFirst().orElse(""), e.getValue()))
              .filter(e -> !e.getKey().equals(uri))
              .collect(Collectors.toMap(Map.Entry::getKey,
                  e -> new PatternLibraryExample(null, e.getKey(),
                      extractTemplatePath(e.getValue()).orElse(null))));

          Map<String, PatternLibraryEntry> entries = examples.entrySet()
              .stream()
              .filter(e -> !e.getKey().contains("--")) // Variants of components will have -- in the URI
              .collect(Collectors.toMap(Map.Entry::getKey,
                  e -> new PatternLibraryEntry(e.getValue(), new ArrayList<>(),
                      extractComponentDir(e.getValue().templatePath()))));

          System.out.println(entries);
          System.out.println(entry);


          return new PatternLibraryGroup(uri, name, Collections.emptyList());
        }).collect(Collectors.toList());
    System.out.println(patternLibraryGroups);

    return patternLibraryGroups;
  }

  private Stream<Map.Entry<RequestMappingInfo, HandlerMethod>> getComponentEndpoints() {
    return this.requestHandlerMapping.getHandlerMethods()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getBeanType().getPackageName().startsWith(COMPONENT_PACKAGE_PREFIX));
  }

  record PatternLibraryGroup(String uri, String name, List<PatternLibraryEntry> entries) { }

  public record PatternLibraryEntry(PatternLibraryExample mainExample, List<PatternLibraryExample> examples, Optional<File> componentDir) {
    public Optional<String> getDocumentation() {
      return componentDir
          .map(file -> file.listFiles((dir, name) -> name.equals("README.md")))
          .filter(files -> files.length > 0)
          .flatMap(files -> extractFile(files[0]));
    }
  }

  public record PatternLibraryExample(String name, String uri, String templatePath) {}

  public Optional<String> extractTemplatePath(HandlerMethod handlerMethod) {
    Method method = handlerMethod.getMethod();
    try {
      // Expects Controller to have a no-args constructor
      Object object = handlerMethod.getBeanType().getDeclaredConstructor().newInstance();

      // Expects controller for a pattern library endpoint to have no parameters
      Object view = method.invoke(object);

      // XXX: hardcodes template path. Preferable to retrieve from properties?
      String templatePath = thymeleafPrefix;

      if (view instanceof String) {
        templatePath += view;
      } else if (view instanceof ModelAndView) {
        templatePath += ((ModelAndView) view).getViewName();
      } else {
        return Optional.empty();
      }

      templatePath += ".html";

      return Optional.of(templatePath);
    } catch (Exception e) {
      LOGGER.info("Could not retrieve template path from method={}", method);
      return Optional.empty();
    }
  }

  public Optional<File> extractComponentDir(String templatePath) {
    if (templatePath == null) {
      return Optional.empty();
    }

    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(templatePath);

    try {
      return Optional.ofNullable(resource.getFile().getParentFile());
    } catch (IOException e) {
      LOGGER.info("Could not find documentation for template={}", templatePath);
      return Optional.empty();
    }
  }

  public static Optional<String> extractResource(String resourcePath) {
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(resourcePath);
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
      return Optional.of(FileCopyUtils.copyToString(reader));
    } catch (IOException e) {
      LOGGER.info("Could not extract resource={}", resourcePath);
      return Optional.empty();
    }
  }

  public static Optional<String> extractFile(File file) {
    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
      return Optional.of(FileCopyUtils.copyToString(reader));
    } catch (IOException e) {
      LOGGER.info("Could not extract file={}", file);
      return Optional.empty();
    }
  }
}
