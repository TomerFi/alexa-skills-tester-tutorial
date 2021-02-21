package info.tomfi.tutorials;

import static info.tomfi.alexa.skillstester.SkillsTester.givenSkill;

import java.io.IOException;
import org.junit.jupiter.api.Test;

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
