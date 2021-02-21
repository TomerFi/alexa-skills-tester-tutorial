package info.tomfi.tutorials;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;

class NiceToMeetYouSkill {
  private NiceToMeetYouSkill() {
    //
  }

  public static Skill getSkill() {
    return Skills.standard()
        .addRequestHandler(new LaunchRequestHandlerImpl())
        .addRequestHandler(new MyNameIntentRequestHandler())
        .addRequestHandler(new SessionEndedRequestHandlerImpl())
        .build();
  }
}
