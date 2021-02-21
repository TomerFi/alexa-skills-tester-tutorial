package info.tomfi.tutorials;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import java.util.Optional;

/**
 * LaunchRequest handler returning 'What is your name?' and 'Please tell me your name.' as a
 * repromopt, not ending the session.
 */
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
