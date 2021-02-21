package info.tomfi.tutorials;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import java.util.Optional;

/**
 * `MyNameIntent` handler returning 'Nice to meet you' concatenated with the `nameSlot` slot value
 * and ending the session.
 */
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
