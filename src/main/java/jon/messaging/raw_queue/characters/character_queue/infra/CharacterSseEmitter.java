package jon.messaging.raw_queue.characters.character_queue.infra;

import jon.messaging.raw_queue.characters.character_queue.CharacterQueue;
import jon.messaging.raw_queue.shared.Emitter;
import jon.messaging.raw_queue.shared.HttpSseEmitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CharacterSseEmitter implements Emitter {
    private final HttpSseEmitter httpSseEmitter;

    @Override
    public void emitUpdate(final CharacterQueue msg) {
        httpSseEmitter.sendMessageUpdated(String.valueOf(msg.getInternalId()), msg.transformFieldsToMap());
    }

    @Override
    public void emitCreation(final CharacterQueue msg) {
        httpSseEmitter.sendMessageCreated(String.valueOf(msg.getInternalId()), msg.transformFieldsToMap());
    }

    @Override
    public void emitDeletion(final CharacterQueue msg) {
        httpSseEmitter.sendMessageDeleted(String.valueOf(msg.getInternalId()), msg.transformFieldsToMap());
    }
}
