package comp3011.practical5;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ReentrantLock;

import org.springframework.beans.factory,annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class AuctionService {
	
	private static final long AUCTION_WINDOW_MILLIS = 10_000L;
	
	private final AuctionStore store;
	private final Clock clock;
	
	private final ConcurrentMap<String, Long> deadlines =
			new ConcurrentHashMap<>();
	
	private final ConcurrentMap<String, String> autionIds = 
			new ConcurrentHashMap<>();
	
	private final ConcurrentMap<String, java.util.concurrent.locks.ReentrantLock> locks = 
			new ConcurrentHashMap<>();
	
	private final ScheduledExecutorService Scheduler =
			Executors.newScheduledThreadPool(2);
	
	@Autowired
	public AutionService(AuctionStore store) {
		this(store, Clock.systemUTC());
	}
	
	AuctionService(AuctionStore store, Clock clock) {
		this.store = store;
		this.clock = clock;
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
	
	private ReentrantLock lockFor(String name) {
		return locks.computeIfAbsent(
				name, 
				ignored -> new ReentrantLock(true));
	}
	
	public CreateResult createAuction(
			String name,
			int reservePrice) {
		
		if (name == null || name.isBlank() || reservePrice < 0) {
			return CreateResult.INVALID;
		}
		
		java.util.concurrent.locks.ReentrantLock lock = lockFor(name);
		lock.lock();
		
		try {
			expireIfNeededLocked(name);
			
			if (store.contains(name)) {
				return CreateResult.CONFLICT;
			}
			
			store.put(name, reservePrice);
			
			long deadline = clock.millis() + AUCTION_WINDOWS_MILLIS;
			
			String auctionId = UUID.randomUUID().toString();
			
			deadlines.put(name, deadline);
			autionIds.put(name, auctionId);
			
			scheduleExpiry(name, auctionId, deadline);
			
			return CreateResult.CREATED;
			
		} finally {
			lock.unlock();
		}
	}
	
	public AuctionResponse getAuction(String name) {
		java.util.concurrent.locks.ReentrantLock lock = lockFor(name);
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
		
		if (deadline = null) {
			return;
		}
		
		if (clock.millis() < deadline) {
			return;
		}
		
		Integer price = store.get(name);
		
		if (price != null) {
			System.out.println(
					"Sold " + name
					+  " at price " + price);
			store.delete(name);
		}
		
		deadlines.remove(name);
		auctionIds.remove(name);
	}
	
	public BidResult placeBid(String name, int amount) {
		java.util.concurrent.locks.ReentrantLock lock = lockFor(name);
		lock.lock();
		
		try {
			expireIfNeededLocked(name);
			
			Integer currentPrice = store.get(name);
			Long currentDeadline = deadlines.get(name);
			String auctionId = auctionIds.get(name);
			
			if (currentPrice == null
					|| currentDeadline == null
					|| auctionId == null) {
				return BidResult.NOT_FOUND;
			}
			
			if (amount <= currentPrice) {
				return BidResult.TOO_LOW;
			}
			
			store.put(name, amount);
			
			long newDeadline = 
					clock.millis() + AUCTION_WINDOW_MILLIS;
			
			deadlines.put(name, newDeadline);
			scheduleExpiry(name, auctionId, newDeadline);
			
			return BidResult.ACCEPTED;
			
		} finally {
			lock.unlock();
		}
	}
	
	public boolean deleteAuction(String name) {
		java.util.concurrent.locks.ReentrantLock lock = lockFor(name);
		lock.lock();
		
		try {
			expireIfNeededLocked(name);
			
			if(!store.contains(name)) {
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
			
					if (!expectedAuctionId.equals(currentAuctionId)) {
						return;
					}
					
					if (currentDeadline == null
							|| currentDeadline.longValue()
							!= expectedDeadline) {
						return;
					}
					
					if (clock.millis() < expectedDeadline) {
						scheduleExpiry(
								name,
								expectedAuctionId,
								expectedDeadline);
						return;
					}
			
					Integer price = store.get(name);
			
					if (price != null) {
						System.out.println(
								"Sold " + name
								+ " at price " + price);
						store.delete(name);
					}
			
					deadlines.remove(name, expectedDeadline);
					auctionIds.remove(name, expectedAuctionId);
				} finally {
					lock.unlock();
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	public void shutdownScheduler() {
		scheduler.shutdown();
	}
}