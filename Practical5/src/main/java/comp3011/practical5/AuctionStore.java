package comp3011.practical5;

public interface AuctionStore {
	
	Integer get(String name);
	
	void put(String name, int price);
	
	void delete(String name);
	
	boolean contains(String name);
}