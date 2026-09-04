package comp3011.practical5;

import org.springframework.stereotype.Service;

@Service
public class LocalKeyValueAuctionStore implements AuctionStore {
	
	private final LocalKeyValueStore suppliedStore;
	
	public LocalKeyValueAuctionStore(
			LocalKeyValueStore suppliedStore) {
		this.suppliedStore = suppliedStore;
	}
	
	@Override
	public Integer get(String name) {
		return suppliedStore.get(name);
	}
	
	@Override
	public void put(String name, int price) {
		suppliedStore.put(name,  price);
	}
	
	@Override
	public void delete(String name) {
		suppliedStore.delete(name);
	}
	
	@Override
	public boolean contains(String name) {
		return suppliedStore.contains(name);
	}
}