package jon.messaging.bus.old;

import jon.messaging.bus.old.commands.OldCommand;

public interface OldCommandBus {
    <R> R dispatch(OldCommand oldCommand);
}
