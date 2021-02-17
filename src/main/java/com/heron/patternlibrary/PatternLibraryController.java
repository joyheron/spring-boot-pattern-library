package com.heron.patternlibrary;

import com.heron.patternlibrary.annotations.PatternLibraryComponents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class PatternLibraryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternLibraryController.class);

  private static List<PatternLibraryGroup> patternLibraryGroups;

  @Autowired
  private RequestMappingHandlerMapping requestHandlerMapping;

  @Autowired
  private ResourceLoader resourceLoader;

  @Value( "${spring.thymeleaf.prefix:/templates/}" )
  private String thymeleafPrefix;

  @GetMapping("/")
  public ModelAndView index(@RequestParam(required = false) String uri) {

    System.out.println(getPatternLibraryGroups());

    return new ModelAndView("pattern-library",
        Map.of("groups", getPatternLibraryGroups(),
            "uri", uri));
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
                  e -> new PatternLibraryExample(extractNameFromMethod(e.getValue().getMethod().getName()), e.getKey(),
                      extractTemplatePath(e.getValue()).orElse(null))));

          Map<String, PatternLibraryEntry> entries = examples.entrySet()
              .stream()
              .filter(e -> !e.getKey().contains("--")) // Variants of components will have -- in the URI
              .collect(Collectors.toMap(Map.Entry::getKey,
                  e -> new PatternLibraryEntry(e.getValue(), new ArrayList<>(),
                      extractComponentDir(e.getValue().getTemplatePath()))));

          examples.entrySet()
              .stream()
              .map(e -> {
                String[] parts = e.getKey().split("--");
                return new AbstractMap.SimpleEntry<>(parts, e.getValue());
              })
              .filter(e -> e.getKey().length > 1 && entries.containsKey(e.getKey()[0]))
              .forEach(e -> {
                PatternLibraryEntry mainEntry = entries.get(e.getKey()[0]);
                mainEntry.examples.add(e.getValue());
              });

          return new PatternLibraryGroup(uri, name, entries.values().stream()
              .sorted(Comparator.comparing(e -> e.getMainExample().name))
              .collect(Collectors.toList()));
        }).collect(Collectors.toList());
    System.out.println(patternLibraryGroups);

    return patternLibraryGroups;
  }

  private Stream<Map.Entry<RequestMappingInfo, HandlerMethod>> getComponentEndpoints() {
    return this.requestHandlerMapping.getHandlerMethods()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getBeanType().getAnnotation(PatternLibraryComponents.class) != null);
  }

  public static class PatternLibraryGroup {
    private final String uri;
    private final String name;
    private final List<PatternLibraryEntry> entries;

    public PatternLibraryGroup(String uri, String name, List<PatternLibraryEntry> entries) {
      this.uri = uri;
      this.name = name;
      this.entries = entries;
    }

    public String getUri() {
      return uri;
    }

    public String getName() {
      return name;
    }

    public List<PatternLibraryEntry> getEntries() {
      return entries;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("PatternLibraryGroup{");
      sb.append("uri='").append(uri).append('\'');
      sb.append(", name='").append(name).append('\'');
      sb.append(", entries=").append(entries);
      sb.append('}');
      return sb.toString();
    }
  }

  public static class PatternLibraryEntry {
    private final PatternLibraryExample mainExample;
    private final List<PatternLibraryExample> examples;
    private final Optional<File> componentDir;

    public PatternLibraryEntry(PatternLibraryExample mainExample, List<PatternLibraryExample> examples, Optional<File> componentDir) {
      this.mainExample = mainExample;
      this.examples = examples;
      this.componentDir = componentDir;
    }

    public PatternLibraryExample getMainExample() {
      return mainExample;
    }

    public List<PatternLibraryExample> getExamples() {
      return examples;
    }

    public Optional<File> getComponentDir() {
      return componentDir;
    }

    public Optional<String> getDocumentation() {
      return componentDir
          .map(file -> file.listFiles((dir, name) -> name.equals("README.md")))
          .filter(files -> files.length > 0)
          .flatMap(files -> extractFile(files[0]));
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("PatternLibraryEntry{");
      sb.append("mainExample=").append(mainExample);
      sb.append(", examples=").append(examples);
      sb.append(", componentDir=").append(componentDir);
      sb.append('}');
      return sb.toString();
    }
  }

  public static class PatternLibraryExample {
    private final String name;
    private final String uri;
    private final String templatePath;

    public PatternLibraryExample(String name, String uri, String templatePath) {
      this.name = name;
      this.uri = uri;
      this.templatePath = templatePath;
    }

    public String getName() {
      return name;
    }

    public String getUri() {
      return uri;
    }

    public String getTemplatePath() {
      return templatePath;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("PatternLibraryExample{");
      sb.append("name='").append(name).append('\'');
      sb.append(", uri='").append(uri).append('\'');
      sb.append(", templatePath='").append(templatePath).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }

  public static String extractNameFromMethod(String name) {
    String[] split = name.split("_");
    if (split.length > 1) {
      name = split[1];
    }

    Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(name);
    String result = m.replaceAll(match -> " " + match.group());
    return StringUtils.capitalize(result);
  }

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
