package com.example.SearchService.controller;

import com.example.SearchService.client.ProductClient.Product;
import com.example.SearchService.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class SearchController {
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * Fetches all products from Admin_Product via ProductClient.
     *
     * @return List of all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(searchService.getAllProducts());
    }
    
    /**
     * Returns the top 3 products with the lowest prices.
     * Results are cached in Redis for improved performance.
     *
     * @return List of the 3 lowest priced products
     */
    @GetMapping("/top-three-lowest-price")
    public ResponseEntity<List<Product>> getTopThreeLowestPriceProducts() {
        return ResponseEntity.ok(searchService.getTopThreeLowestPriceProducts());
    }
    
    /**
     * Performs full-text search across name and description.
     *
     * @param keyword The search term to match against name and description
     * @return List of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(searchService.searchByKeyword(keyword));
    }
    
    /**
     * Returns products with stock level below threshold.
     *
     * @return List of products with low stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(searchService.getLowStockProducts());
    }
    
    /**
     * Filters products by category.
     *
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    @GetMapping("/filter/category")
    public ResponseEntity<List<Product>> filterByCategory(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(searchService.filterByCategory(category));
    }
    
    /**
     * Filters products by a price range.
     *
     * @param min Minimum price (inclusive)
     * @param max Maximum price (inclusive)
     * @return List of products within the price range
     */
    @GetMapping("/filter/price")
    public ResponseEntity<List<Product>> filterByPriceRange(
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max) {
        return ResponseEntity.ok(searchService.filterByPriceRange(min, max));
    }
}
