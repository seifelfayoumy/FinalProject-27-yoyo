package com.example.SearchService.service;

import com.example.SearchService.client.ProductClient;
import com.example.SearchService.client.ProductClient.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    
    private final ProductClient productClient;
    
    public SearchService(ProductClient productClient) {
        this.productClient = productClient;
    }
    
    /**
     * Fetches all products from Admin_Product via ProductClient.
     *
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productClient.getAllProducts();
    }
    
    /**
     * Returns the top 3 products with the lowest prices.
     * Results are cached in Redis for improved performance.
     *
     * @return List of the 3 lowest priced products
     */
    @Cacheable(value = "topThreeLowestPriceProducts")
    public List<Product> getTopThreeLowestPriceProducts() {
        return getAllProducts().stream()
                .sorted(Comparator.comparing(Product::getPrice))
                .limit(3)
                .collect(Collectors.toList());
    }
    
    /**
     * Scheduled task to refresh the cached top 3 lowest priced products every 30 minutes.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void refreshTopThreeCache() {
        // This will trigger a cache refresh
        getTopThreeLowestPriceProducts();
    }
    
    /**
     * Returns products where keyword matches name or description (case-insensitive).
     *
     * @param keyword The search term to match against name and description
     * @return List of matching products
     */
    public List<Product> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        
        return getAllProducts().stream()
                .filter(product -> 
                    (product.getName() != null && product.getName().toLowerCase().contains(lowercaseKeyword)) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowercaseKeyword))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Filters products by exact match on category (e.g., "Electronics", "Clothing").
     *
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    public List<Product> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllProducts();
        }
        
        return getAllProducts().stream()
                .filter(product -> 
                    product.getCategory() != null && 
                    product.getCategory().equalsIgnoreCase(category)
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Returns products with stock level below threshold.
     *
     * @return List of products with low stock
     */
    public List<Product> getLowStockProducts() {
        return getAllProducts().stream()
                .filter(product -> product.getQuantity() <= product.getStockThreshold())
                .collect(Collectors.toList());
    }
    
    /**
     * Returns products within the given price range (inclusive).
     *
     * @param min Minimum price (inclusive)
     * @param max Maximum price (inclusive)
     * @return List of products within the price range
     */
    public List<Product> filterByPriceRange(Double min, Double max) {
        List<Product> allProducts = getAllProducts();
        
        if (min == null && max == null) {
            return allProducts;
        }
        
        return allProducts.stream()
                .filter(product -> {
                    double price = product.getPrice();
                    
                    boolean aboveMin = min == null || price >= min;
                    boolean belowMax = max == null || price <= max;
                    
                    return aboveMin && belowMax;
                })
                .collect(Collectors.toList());
    }
}
