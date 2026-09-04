package comp3011.practical5;

import URI;

import ResponseEntity;
import DeleteMapping;
import GetMapping;
import PathVariable;
import PostMapping;
import PutMapping;
import RequestBody;
import RequestMapping;
import RestController;


@RestController
@RequestMapping("/auctions")
public class AuctionController {

	private final AuctionService service;

	public AuctionController(AuctionService service) {
		this.service = service;
	}

	@PutMapping("/{name}")
	public ResponseEntity<AuctionResponse> create(
			@PathVariable String name,
			@RequestBody CreateAuctionRequest request) {
		
		AuctionService.CreateResult result =
				service.createAuction(
						name,
						request.reservePrice());
		
		return switch (result) {
			case CREATED -> ResponseEntity
				.created(URI.create("/auctions/" + name))
				.body(service.getAuction(name));
			case CONFLICT -> ResponseEntity
				.status(409)
				.build();
			case INVALID -> ResponseEntity
				.badRequest()
				.build();
		};
	}
	
	@GetMapping("/{name}")
	public ResponseEntity<AuctionResponse> get(
			@PathVariable String name) {
		
		AuctionResponse auction = service.getAuction(name);
		
		if (auction == null) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.ok(auction);
	}
	@PostMapping("/{name}/bids")
	public ResponseEntity<AuctionResponse> bid(
			@PathVariable String name,
			@RequestBody BidRequest request) {
		
		AuctionService.BidResult result =
				service.placeBid(name, request.amount());
		
		return switch (result) {
			case ACCEPTED -> ResponseEntity.ok(
					service.getAuction(name));
			case TOO_LOW -> ResponseEntity
					.unprocessableContent()
					.build();
			case NOT_FOUND -> org.springframework.http.ResponseEntity<T>
					.notFound()
					.build();
		};
	}
	
	@DeleteMapping("/{name}")
	public ResponseEntity<void> delete(
			@PathVariable String name){
		
		if (!service.deleteAuction(name)) {
			return org.springframework.http.ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.noContent().build()
	}
}
