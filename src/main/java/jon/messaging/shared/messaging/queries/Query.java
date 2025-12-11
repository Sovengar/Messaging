package jon.messaging.shared.messaging.queries;

public sealed interface Query<R> permits GetOrderByIdQuery, GetOrdersSearchCriteria {}

