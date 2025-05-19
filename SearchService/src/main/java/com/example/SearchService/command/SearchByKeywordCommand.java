package com.example.SearchService.command;

import com.example.SearchService.client.ProductClient;
import com.example.SearchService.client.ProductClient.Product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete command implementation for searching products by keyword.
 * This command encapsulates the logic for filtering products based on a keyword in name or description.
 */
public class SearchByKeywordCommand implements Command<List<Product>> {
    
    private final List<Product> allProducts;
    private final String keyword;
    
    /**
     * Constructor for SearchByKeywordCommand.
     * 
     * @param allProducts The list of all products to search through
     * @param keyword The keyword to search for in product name and description
     */
    public SearchByKeywordCommand(List<Product> allProducts, String keyword) {
        this.allProducts = allProducts;
        this.keyword = keyword;
    }
    
    /**
     * Executes the search by keyword command.
     * 
     * @return List of products that match the keyword in name or description
     */
    @Override
    public List<Product> execute() {
        if (keyword == null || keyword.trim().isEmpty()) {
            return allProducts;
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        
        return allProducts.stream()
                .filter(product -> 
                    (product.getName() != null && product.getName().toLowerCase().contains(lowercaseKeyword)) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowercaseKeyword))
                )
                .collect(Collectors.toList());
    }
} 