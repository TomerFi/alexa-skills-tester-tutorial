---
title: Alexa Skills Testing
published: false
description: Test your Java Alexa skills with real requests.
tags: ["java", "alexa", "test", "bdd"]
cover_image: https://source.unsplash.com/k1osF_h2fzA
---

## Test your Java Alexa skills with real requests

Writing an Alexa skill in Java is pretty simple using the [Alexa Skills Kit SDK for Java][0]. :smiley: </br>

Testing your code, however, can be quite troublesome.</br>

You can, and should, write the required unit test cases to verify the integrity of your code.</br>
But...</br>
If you want to test your overall skill interaction...
Well, this can be tricky and messy to accomplish using your standard testing tools. :dizzy_face:</br>

There are a couple of solutions for testing skills, but the ones I found (for Java), require the skill to be deployed/hosted and accessible for the test cases to run.

I was looking for a tool that can help me write automated test cases for my Alexa skills, a tool that can be used in my unit/integration tests to **fire real-life JSON requests at my code and verify the multi-turn interactions with my skill, without having to deploy or host it first.**</br>

When I didn't find such a tool, I decided to build one.
My [alexa-skills-tester][1] :grinning:</br>

- [Sources on Github][1]
- [Deployed to Maven Central][2]

The sources for this post can be found [here][3].</br>

To use the [alexa-skills-tester][2], just add the following to your project:</br>
(replace _VERSION_ with the [current release][4]).

```xml
<dependency>
  <groupId>info.tomfi.alexa</groupId>
  <artifactId>alexa-skills-tester</artifactId>
  <version>VERSION</version>
</dependency>
```

## Example usage

Using a basic `Nice to meet you` custom skill:</br>
When launching the skill, it asks for the user name and then replies with a `Nice to meet you #name` prompt, ending the session.</br>

Let's take a look at the code for the skill.

### A launch request handler

```java
public final class LaunchRequestHandlerImpl implements LaunchRequestHandler {
  @Override
  public boolean canHandle(final HandlerInput input, final LaunchRequest request) {
    return true;
  }

  @Override
  public Optional<Response> handle(final HandlerInput input, final LaunchRequest request) {
    return input.getResponseBuilder()
        .withSpeech("What is your name?")
        .withReprompt("Please tell me your name.")
        .withShouldEndSession(false)
        .build();
  }
}
```

### An intent request handler handling `MyNameIntent` with a `nameSlot`

```java
public final class MyNameIntentRequestHandler implements IntentRequestHandler {
  @Override
  public boolean canHandle(final HandlerInput input, final IntentRequest request) {
    return request.getIntent().getName().equals("MyNameIntent");
  }

  @Override
  public Optional<Response> handle(final HandlerInput input, final IntentRequest request) {
    var name = request.getIntent().getSlots().get("nameSlot").getValue();
    return input.getResponseBuilder()
        .withSpeech(String.format("Nice to meet you %s!", name))
        .withShouldEndSession(true)
        .build();
  }
}
```

### A session ended request handler

```java
public final class SessionEndedRequestHandlerImpl implements SessionEndedRequestHandler {
  @Override
  public boolean canHandle(final HandlerInput input, final SessionEndedRequest request) {
    return true;
  }

  @Override
  public Optional<Response> handle(final HandlerInput input, final SessionEndedRequest request) {
    return Optional.empty();
  }
}
```

### A utility class for constructing the skill

```java
class NiceToMeetYouSkill {
  public static Skill getSkill() {
    return Skills.standard()
        .addRequestHandler(new LaunchRequestHandlerImpl())
        .addRequestHandler(new MyNameIntentRequestHandler())
        .addRequestHandler(new SessionEndedRequestHandlerImpl())
        .build();
  }
}
```

### Now, let's test the skill

> You should get familiarized with [Alexa's Request and Response JSON Reference][5]. This is of course should be considered common knowledge for skill developers.</br>

Let's create a couple of fake request JSON files to use in our tests.

#### launch_request.json

```json
{
  "version": "1.0",
  "session": {
    "new": true
  },
  "context": {
    "System": {}
  },
  "request": {
    "type": "LaunchRequest",
    "requestId": "amzn1.echo-api.request.fake-request-id",
    "timestamp": "2021-02-11T15:30:00Z",
    "locale": "en-US"
  }
}

```

#### my_name_intent.json

```json
{
  "version": "1.0",
  "session": {
    "new": false
  },
  "context": {
    "System": {}
  },
  "request": {
    "type": "IntentRequest",
    "requestId": "amzn1.echo-api.request.fake-request-id",
    "timestamp": "2021-02-11T15:31:00Z",
    "locale": "en-US",
    "intent": {
      "name": "MyNameIntent",
      "slots": {
        "nameSlot": {
          "name": "nameSlot",
          "value": "master"
        }
      }
    }
  }
}
```

#### session_ended.json

```json
{
    "version": "1.0",
    "session": {
        "new": false
    },
    "context": {
        "System": {}
    },
    "request": {
        "type": "SessionEndedRequest",
        "requestId": "amzn1.echo-api.request.fake-request-id",
        "timestamp": "2021-02-11T15:32:00Z",
        "locale": "en-US"
    }
}
```

Now, let's create some test cases leveraging the above JSON files to verify the interaction with the skill.

#### Test cases testing the `Nice to meet you` skill

```java
final class SkillInteractionTest {
  @Test
  void verify_greeting_for_my_name_intent_as_a_followup_to_a_launch_request() throws IOException {
    givenSkill(NiceToMeetYouSkill.getSkill())
        .whenRequestIs(getClass().getClassLoader().getResourceAsStream("launch_request.json").readAllBytes())
        .thenResponseShould()
            .waitForFollowup()
            .haveOutputSpeechOf("What is your name?")
            .haveRepromptSpeechOf("Please tell me your name.")
        .followingUpWith(getClass().getClassLoader().getResourceAsStream("my_name_intent.json").readAllBytes())
        .thenResponseShould()
            .haveOutputSpeechOf("Nice to meet you master!")
            .and()
            .notWaitForFollowup();
  }

  @Test
  void verify_empty_response_for_session_ended_requests() throws IOException {
    givenSkill(NiceToMeetYouSkill.getSkill())
      .whenRequestIs(getClass().getClassLoader().getResourceAsStream("launch_request.json").readAllBytes())
      .thenResponseShould()
          .waitForFollowup()
          .haveOutputSpeechOf("What is your name?")
          .haveRepromptSpeechOf("Please tell me your name.")
      .followingUpWith(getClass().getClassLoader().getResourceAsStream("session_ended.json").readAllBytes())
      .thenResponseShould().beEmpty();
  }
}
```

That's it!</br>
Hope you'll find this tool useful.</br>

Please note, I don't develop skills regularly, and this tool was written based on my own limited requirements.</br>
There are a lot of assertion methods and features that can be added to this tool.
Please feel free to open a [feature request issue][6], describing the feature you would like to see, or push a pull request if you want to contribute the feature yourself. :sunglasses:

You can check out the code for this part of the tutorial in [Github][3].

**:wave: See you in the next tutorial :wave:**

[0]: https://developer.amazon.com/en-US/docs/alexa/alexa-skills-kit-sdk-for-java/overview.html
[1]: https://github.com/TomerFi/alexa-skills-tester
[2]: https://search.maven.org/artifact/info.tomfi.alexa/alexa-skills-tester
[3]: https://github.com/TomerFi/alexa-skills-tester-tutorial
[4]: https://github.com/TomerFi/alexa-skills-tester/releases
[5]: https://developer.amazon.com/en-US/docs/alexa/custom-skills/request-and-response-json-reference.html
[6]: https://github.com/TomerFi/alexa-skills-tester/issues/new/choose
