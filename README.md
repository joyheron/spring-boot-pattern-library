# Spring Boot Pattern Library

The goal of this project is to create a proof-of-concept about how we can
create a pattern-library using [Spring Boot] as the engine and [Thymeleaf] as
the templating engine.

Thymeleaf is a powerful templating engine, and can do most of what we need to
develop good reusable components for Java projects. However, most of the
tooling for doing frontend development is only available in the Node universe,
so this project intends to provide some tooling for developing frontend
components when you know that you want to use Thymeleaf for your rendering
engine, and you also know that all of the applications which are going to be
using your components will also be running on the JVM and will therefore be
able to use these same templates.

Why develop something completely new instead of just trying to integrate
Thymeleaf with existing tooling?

There is a [port of Thymeleaf for Node JS], but that is not complete.
Additionally, there is no support for the [Thymeleaf Layout Dialect], which I
believe is crucial for being able to create reusable layout containers (using
[layout:replace] or [layout:insert]). There is also not support for all of the
helper classes in the Node version of Thymeleaf.

Writing the whole pattern library in Spring Boot would also have the benefit
that you could share a lot more things between projects if you publish your
pattern library as a Java library. This means that you could theoretically
share the following between all services:

* Any messages.properties files which are needed to render/translate text
  within a component.
* Model objects which directly correspond to the component definitions that you
  are creating. This Model would then act as a contract between the pattern
  library and any services which use the component.
* Theoretically, we could publish executable test contracts for Selenium (or at
  the very lease publish Java classes which encode the contracts for finding a
  component within a page via a CSS selector) so that we can easily write UI
  tests for all of our applications.

Currently, this project has absolutely no aspirations to be a new framework or
to actually be used as a dependency in another project. This is intended as a
proof-of-concept for the idea, and theoretically as a starter for other
projects (so to copy-paste and then fix everything which doesn't work).

[Spring Boot]: https://spring.io/projects/spring-boot
[Thymeleaf]: https://www.thymeleaf.org/
[port of Thymeleaf for Node JS]: https://github.com/ultraq/thymeleafjs
[Thymeleaf Layout Dialect]: https://ultraq.github.io/thymeleaf-layout-dialect/
[layout:replace]: https://ultraq.github.io/thymeleaf-layout-dialect/processors/replace/
[layout:insert]: https://ultraq.github.io/thymeleaf-layout-dialect/processors/insert/

## Running the Application

### Prerequisites

* Java 9 or greater
* Gradle
* Node & npm

### Starting the application

Here we need to start both the pattern library (a Java application) as well as
the asset pipeline (a Node application) which will run in the background. We can
do this with the following two commands:

#### Asset Pipeline

    npm start

#### Pattern Library Application

    gradle run

## What currently works

The application assumes that all of your component examples are grouped within
a Spring Boot Controller. This Controller will then need to be annotated with
the `@PatternLibraryComponents` annotation to let the application know that it
has interesting components which should be added to the navigation.

The application (currently) also assumes that all of the methods that provide
`GET` requests can be called without any arguments (we need to retrieve view
information via reflection). It also expects either a `String` or a
`ModelAndView` as a return type.

### An example component controller (minus imports)

    @Controller
    @PatternLibraryComponents
    public class ComponentController {
        @GetMapping("/component1")
        public ModelAndView component1() {
            return new ModelAndView("components/component1", Map.of("item", "Foo"));
        }
        
        @GetMapping("/component2")
        public String component2() {
            return "components/component2";
        }
    }

The components "Component 1" and "Component 2" would then appear in the
automatically generated pattern library navigation.

### Documentating a group of components

Documentation is really important for a pattern library. In the
`@PatternLibraryComponents` annotation you should also pass in the path to a
markdown file with documentation about the components that that Controller
provides. By default, the application will be look in the
`src/main/resources/docs` file to find the documentation file.

    @PatternLibraryComponents(docs = "components.md")

### Ordering the groups of components in the navigation of the pattern library

We can specify the `order` for a `@PatternLibraryComponents` controller to
determine in which order the group will appear within the navigation of the
pattern library. Otherwise, the order is random and subject to change.

    @PatternLibraryComponents(docs = "components.md", order = 1)

### Providing different variants of a single component

One important task that pattern libraries provide is the ability to show
different variants of a component that is possible. In this implementation,
we can create a variant of a specific component by creating an identical
a method which has the same URI prefix, but appends a `--` and a variant
description in the UI. The application will then show all of the variants
together on the same page in the generated pattern library (and will generate
the name of the variant in the UI based on the suffix of the method name).

    @GetMapping("/lightbulb")
    public String lightbulb() {
        return "components/lightbulb";
    }

    @GetMapping("/lightbulb--on")
    public String lightbulb_on() {
        return new ModelAndView("components/lightbulb", Map.of("state", "on"));
    }

    @GetMapping("/lightbulb--off")
    public String lightbulb_off() {
        return new ModelAndView("components/lightbulb", Map.of("state", "off"));
    }

### Documentation for a variant

Documentation for our component should be front and center when we view it in
our pattern library. In order to document our component, we can add a
`README.md` in the same directory as our Thymeleaf template. Then the
application will find this documentation and show it in the pattern library
before the example components or variants.

### Asset Pipeline

This app uses the [faucet-pipeline] to generate the custom CSS and JavaScript
files for our components. We recommend having the actual CSS & JS file for a
component in the same directory as the template because they are part of the
same contract that we are defining. The asset pipeline can be configured in
the `faucet.config.js` file, and you will have to modify the
`component-layout.html` template (which is used for all of the component
examples) if you add new asset files, in order for those files to take effect.

[faucet-pipeline]: https://www.faucet-pipeline.org/


## What is not (yet) supported

### Live Reload

There is currently no live reloading of components when we modify the template
file or the asset files. Currently, if you modify any asset files, you need to
reload the browser to see any changes. If you modify a template file, you will
also need to rebuild your project (if you are using an IDE). This would be
CMD+F9 _MacOS_ or CTRL+F9 _Windows_. 

This is definitely not ideal, and I'm open for suggestions about how to make
this easier in the future.

### Pattern Library Features

This pattern library currently uses the CSS and JS components from [aiur] for
the implementation of the frontend. aiur itself is still considered somewhat
experimental, but in this instance I considered it a decent tradeoff, because
the actual styles/implementation that we are using is so small that we can
easily fork the repository should the aiur maintainers decide to take the tool
in a different direction in the future.

However, there are a few missing features that would be nice to have for a
pattern library tool (e.g. a link to open the examples in a new browser tab, or
updating the iframes showing the result so that their _height_ and not only the
width also changes when the browser window is resized), but I think those small
things should be able to be fixed within aiur itself and could be easily ported
here as well.

[aiur]: https://github.com/moonglum/aiur/