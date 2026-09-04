package comp3011.practical5;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentMap;

@Service
public class LocalKeyValueStore {
	
	private final DB db;
	private final ConcurrentMap <String, Integer> map;
	
	public LocalKeyValueStore() {
		this.db = DBMaker
				.fileDB("local.db")
				.fileMmapEnableIfSupported()
				.make();
		
		this.map = db
				.hashMap("store", Serializer.STRING, Serializer.INTEGER)
				.createOrOpen();
	}
	
	public void put(String key, Integer value) {
		map.put(key, value);
		db.commit();
	}
	
	public Integer get(String key) {
		return map.get(key);
	}
	
	public void delete(String key) {
		map.remove(key);
		db.commit();
	}
	
	public boolean contains(String key) {
		return map.containsKey(key);
	}
	
	@PreDestroy
	public void close() {
		db.close();
	}
}











