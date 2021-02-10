package com.heron.patternlibrary.components.bootstrap.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {
  @Test
  void create_forFirstPage() {
    // e.g. for List.of(1, 2, 3, 4, 5, 6, 7, 8);

    Pagination pagination = Pagination.create(0, 4, 8, index -> Integer.toString(index));

    assertThat(pagination.getPrevious().getHref()).isEqualTo(null);
    assertThat(pagination.getPrevious().isDisabled()).isTrue();

    assertThat(pagination.getNext().getHref()).isEqualTo("1");
    assertThat(pagination.getNext().isDisabled()).isFalse();

    assertThat(pagination.getPages()).hasSize(2);

    Pagination.PageLink page1 = pagination.getPages().get(0);
    assertThat(page1.getHref()).isEqualTo("0");
    assertThat(page1.isCurrent()).isTrue();

    Pagination.PageLink page2 = pagination.getPages().get(1);
    assertThat(page2.getHref()).isEqualTo("1");
    assertThat(page2.isCurrent()).isFalse();
  }

  @Test
  void create_forLastPage() {
    // e.g. for List.of(1, 2, 3, 4, 5, 6, 7, 8);

    Pagination pagination = Pagination.create(1, 4, 8, index -> Integer.toString(index));

    assertThat(pagination.getPrevious().getHref()).isEqualTo("0");
    assertThat(pagination.getPrevious().isDisabled()).isFalse();

    assertThat(pagination.getNext().getHref()).isEqualTo(null);
    assertThat(pagination.getNext().isDisabled()).isTrue();

    assertThat(pagination.getPages()).hasSize(2);

    Pagination.PageLink page1 = pagination.getPages().get(0);
    assertThat(page1.getHref()).isEqualTo("0");
    assertThat(page1.isCurrent()).isFalse();

    Pagination.PageLink page2 = pagination.getPages().get(1);
    assertThat(page2.getHref()).isEqualTo("1");
    assertThat(page2.isCurrent()).isTrue();
  }

  @Test
  void create_forMiddlePage() {
    // e.g. for List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

    Pagination pagination = Pagination.create(1, 3, 9, index -> Integer.toString(index));
    assertThat(pagination.getPrevious().getHref()).isEqualTo("0");
    assertThat(pagination.getPrevious().isDisabled()).isFalse();

    assertThat(pagination.getNext().getHref()).isEqualTo("2");
    assertThat(pagination.getNext().isDisabled()).isFalse();

    assertThat(pagination.getPages()).hasSize(3);

    Pagination.PageLink page1 = pagination.getPages().get(0);
    assertThat(page1.getHref()).isEqualTo("0");
    assertThat(page1.isCurrent()).isFalse();

    Pagination.PageLink page2 = pagination.getPages().get(1);
    assertThat(page2.getHref()).isEqualTo("1");
    assertThat(page2.isCurrent()).isTrue();

    Pagination.PageLink page3 = pagination.getPages().get(2);
    assertThat(page3.getHref()).isEqualTo("2");
    assertThat(page3.isCurrent()).isFalse();
  }

  @Test
  void create_forRaggedPageNotDivisibleByPageSize() {
    // e.g. for List.of(1, 2, 3, 4, 5, 6, 7, 8);

    Pagination pagination = Pagination.create(1, 3, 8, index -> Integer.toString(index));
    assertThat(pagination.getPrevious().getHref()).isEqualTo("0");
    assertThat(pagination.getPrevious().isDisabled()).isFalse();

    assertThat(pagination.getNext().getHref()).isEqualTo("2");
    assertThat(pagination.getNext().isDisabled()).isFalse();

    assertThat(pagination.getPages()).hasSize(3);

    Pagination.PageLink page1 = pagination.getPages().get(0);
    assertThat(page1.getHref()).isEqualTo("0");
    assertThat(page1.isCurrent()).isFalse();

    Pagination.PageLink page2 = pagination.getPages().get(1);
    assertThat(page2.getHref()).isEqualTo("1");
    assertThat(page2.isCurrent()).isTrue();

    Pagination.PageLink page3 = pagination.getPages().get(2);
    assertThat(page3.getHref()).isEqualTo("2");
    assertThat(page3.isCurrent()).isFalse();
  }
}