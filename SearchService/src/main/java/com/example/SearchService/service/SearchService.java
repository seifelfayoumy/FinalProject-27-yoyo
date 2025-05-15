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
     * Returns the top 10 products with the lowest prices.
     * Results are cached in Redis for improved performance.
     *
     * @return List of the 10 lowest priced products
     */
    @Cacheable(value = "topTenLowestPriceProducts")
    public List<Product> getTopTenLowestPriceProducts() {
        return getAllProducts().stream()
                .filter(product -> product.getPrice() != null)
                .sorted(Comparator.comparing(Product::getPrice))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Scheduled task to refresh the cached top 10 lowest priced products every 30 minutes.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void refreshTopTenCache() {
        // This will trigger a cache refresh
        getTopTenLowestPriceProducts();
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
     * Filters by exact match on category (e.g., "Electronics").
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
                    Double price = product.getPrice();
                    if (price == null) {
                        return false;
                    }
                    
                    boolean aboveMin = min == null || price >= min;
                    boolean belowMax = max == null || price <= max;
                    
                    return aboveMin && belowMax;
                })
                .collect(Collectors.toList());
    }
}
