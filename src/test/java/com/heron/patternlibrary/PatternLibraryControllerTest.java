package com.heron.patternlibrary;

import org.junit.jupiter.api.Test;

import static com.heron.patternlibrary.PatternLibraryController.extractNameFromMethod;
import static org.assertj.core.api.Assertions.assertThat;

class PatternLibraryControllerTest {

  @Test
  void extractNameFromMethod_simpleName() {
    assertThat(extractNameFromMethod("pagination")).isEqualTo("Pagination");
  }

  @Test
  void extractNameFromMethod_camelCase() {
    assertThat(extractNameFromMethod("paginationComplicatedComponent")).isEqualTo("Pagination Complicated Component");
  }

  @Test
  void extractNameFromMethod_variant() {
    assertThat(extractNameFromMethod("paginationComponent_first")).isEqualTo("First");
  }

  @Test
  void extractNameFromMethod_variantCamelCase() {
    assertThat(extractNameFromMethod("paginationComponent_firstName")).isEqualTo("First Name");
  }
}
