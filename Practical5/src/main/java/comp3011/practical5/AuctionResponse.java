package comp3011.practical5;

public record AuctionResponse(
		String name,
		int currentPrice,
		long deadlineEpochMillis) {
}