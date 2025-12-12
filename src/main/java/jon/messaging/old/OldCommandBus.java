package jon.messaging.old;

import jon.messaging.old.commands.OldCommand;

public interface OldCommandBus {
    <R> R dispatch(OldCommand oldCommand);
}
