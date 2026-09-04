package comp3011.practical5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import PreDestroy;

@Service
public class AuctionService {
	
	private static final long AUCTION_WINDOW_MILLIS = 10_000L;
	
	private final AuctionStore store;
	private final Clock clock;

	private final ConcurrentMap<String, Long> deadlines =
			new ConcurrentHashMap<>();

	private final ConcurrentMap<String, String> auctionIds =
			new ConcurrentHashMap<>();

	private final ConcurrentMap<String, ReentrantLock> locks =
			new ConcurrentHashMap<>();

	private final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(2);

	public AuctionService(AuctionStore store) {
		this(store, Clock.systemUTC());
	}

	AuctionService(AuctionStore store, Clock clock) {
		this.store = store;
		this.clock = clock;
	}

	private ReentrantLock lockFor(String name) {
		return locks.computeIfAbsent(
				name,
				ignored -> new ReentrantLock(true));
	}
	
	public enum CreateResult {
		CREATED,
		CONFLICT,
		INVALID
	}
	
	public enum BidResult {
		ACCEPTED,
		TOO_LOW,
		NOT_FOUND
	}
	
	public CreateResult createAuction(
			String name,
			int reservePrice) {
		
	}
		if (name == null || name.isBlank() || reservePrice < 0) {
			return CreateResult.INVALID;
		}
		
		ReentrantLock lock = lockFor(name);
		lock.lock();
		
		try {
			expireIfNeededLocked(name);
			
			if (store.contains(name)) {
				return CreateResult.CONFLICT;
			}
			
			store.put(name, reservePrice);
			
			long deadline =
				clock.millis() + AUCTION_WINDOW_MILLIS;
			
			String auctionId = UUID.randomUUID().toString();
			
			deadlines.put(name, deadline);
			auctionIds.put(name, auctionId);
				
			scheduleExpiry(name, auctionId, deadline);
				
			return CreateResult.CREATED;
				
		} finally {
			lock.unlock();
		}
		
		public AuctionResponse getAuction(String name) {
			ReentrantLock lock = lockFor(name);
			lock.lock();
			
			try {
			expireIfNeededLocked(name);
			Integer price = store.get(name);
			Long deadline = deadlines.get(name);
			if (price == null || deadline == null) {
				return null;
			}
				
			return new AuctionResponse(
					name,
					price,
					deadline);
			
		} finally {
			lock.unlock();
		}
		}
		
		private void expireIfNeededLocked(String name) {
			Long deadline = deadlines.get(name);
			
			if (deadline == null) {
				return;
			}
			
			if (clock.millis() >= deadline) {
				Integer soldPrice = store.get(name);
			
				if (soldPrice != null) {
					store.delete(name);
					System.out.println(
							"Sold " + name
							+ " at price " + soldPrice);
			}
				
			deadlines.remove(name);
			auctionIds.remove(name);
		}
	}
		
		public BidResult placeBid(String name, int amount) {
			ReentrantLock lock = lockFor(name);
			lock.lock();
			
			try {
				expireIfNeededLocked(name);
			
				Integer currentPrice = store.get(name);
			
				if (currentPrice == null) {
					return BidResult.NOT_FOUND;
				}
			
				if (amount <= currentPrice) {
					return BidResult.TOO_LOW;
				}
			
				store.put(name, amount);
			
				long newDeadline =
						clock.millis() + AUCTION_WINDOW_MILLIS;
			
				deadlines.put(name, newDeadline);
			
				String auctionId = auctionIds.get(name);
				scheduleExpiry(name, auctionId, newDeadline);
			
				return BidResult.ACCEPTED;
			
			} finally {
				lock.unlock();
			}
		}
		
		public boolean deleteAuction(String name) {
			ReentrantLock lock = lockFor(name);
			lock.lock();
			
			try {
				expireIfNeededLocked(name);
			
				if (!store.contains(name)) {
					return false;
				}
			
				store.delete(name);
				deadlines.remove(name);
				auctionIds.remove(name);
			
				return true;
			
			} finally {
				lock.unlock();
			}
		}
		private void scheduleExpiry(
				String name,
				String expectedAuctionId,
				long expectedDeadline) {
				
			long delay = Math.max(
					0L,
					expectedDeadline - clock.millis());
				
			scheduler.schedule(() -> {
				ReentrantLock lock = lockFor(name);
				lock.lock();
				
			try {
				String currentAuctionId = auctionIds.get(name);
				Long currentDeadline = deadlines.get(name);
				
				if (currentAuctionId == null
					|| !currentAuctionId.equals(expectedAuctionId)) {
				return;
				}
				
				if (currentDeadline == null
					|| currentDeadline.longValue() != expectedDeadline) {
				return;
				}
				
				if (clock.millis() < expectedDeadline) {
					scheduleExpiry(
							name,
							expectedAuctionId,
							expectedDeadline);
					return;
				}
				
				Integer soldPrice = store.get(name);
				
				if (soldPrice != null) {
					store.delete(name);
					System.out.println(
							"Sold " + name
							+ " at price " + soldPrice);
				}
				
				deadlines.remove(name);
				auctionIds.remove(name);
				
			} finally {
					lock.unlock();
			}
		}, delay, TimeUnit.MILLISECONDS);
	}
		
		@PreDestroy
		public void shutdownScheduler() {
			scheduler.shutdownNow();
		}
}




@SpringBootApplication
public class Practical5Application {

	public static void main(String[] args) {
		SpringApplication.run(Practical5Application.class, args);
	}

}
