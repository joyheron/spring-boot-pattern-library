package com.heron.patternlibrary.components.bootstrap.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Pagination {
  private final Previous previous;
  private final Next next;
  private final List<PageLink> pages;

  private Pagination(Previous previous, Next next, List<PageLink> pages) {
    this.previous = previous;
    this.next = next;
    this.pages = pages;
  }

  public Previous getPrevious() {
    return previous;
  }

  public Next getNext() {
    return next;
  }

  public List<PageLink> getPages() {
    return pages;
  }

  public static Pagination create(int currentIndex, int pageSize, int totalItems, Function<Integer, String> linkCreator) {
    int numberPages = totalItems / pageSize;
    if (totalItems % pageSize != 0) {
      numberPages++;
    }

    return new Pagination(Previous.create(currentIndex, linkCreator),
        Next.create(currentIndex, numberPages, linkCreator),
        PageLink.createPages(currentIndex, numberPages, linkCreator));
  }

  public static class Previous {
    public static final Previous NONE = new Previous(null, true);

    private final String href;
    private final boolean disabled;

    public Previous(String href, boolean disabled) {
      this.href = href;
      this.disabled = disabled;
    }

    public String getHref() {
      return href;
    }

    public boolean isDisabled() {
      return disabled;
    }

    public static Previous create(int currentIndex, Function<Integer, String> linkCreator) {
      if (currentIndex == 0) {
        return NONE;
      }
      return new Previous(linkCreator.apply(currentIndex - 1), false);
    }
  }

  public static class Next {
    public static final Next NONE = new Next(null, true);

    private final String href;
    private final boolean disabled;

    public Next(String href, boolean disabled) {
      this.href = href;
      this.disabled = disabled;
    }

    public String getHref() {
      return href;
    }

    public boolean isDisabled() {
      return disabled;
    }

    public static Next create(int currentIndex, int numberPages, Function<Integer, String> linkCreator) {
      int nextPage = currentIndex + 1;
      if (nextPage == numberPages) {
        return NONE;
      }
      return new Next(linkCreator.apply(nextPage), false);
    }
  }

  public static class PageLink {
    private final String href;
    private final boolean current;

    public PageLink(String href, boolean current) {
      this.href = href;
      this.current = current;
    }

    public String getHref() {
      return href;
    }

    public boolean isCurrent() {
      return current;
    }

    public static List<PageLink> createPages(int currentIndex, int numberPages, Function<Integer, String> linkCreator) {
      List<PageLink> links = new ArrayList<>();

      for (int i = 0; i < numberPages; i++) {
        links.add(new PageLink(linkCreator.apply(i), i == currentIndex));
      }

      return links;
    }
  }
}
