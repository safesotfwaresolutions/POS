package com.sciencebot.pos.products.internal.mappers;

import com.sciencebot.pos.categories.CategoryFacade;
import com.sciencebot.pos.products.ProductDto;
import com.sciencebot.pos.products.internal.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final CategoryFacade categoryFacade;

    public ProductMapper(@org.springframework.context.annotation.Lazy CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }


    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        String categoryName = categoryFacade.getById(product.getCategoryId())
                .map(category -> category.name())
                .orElse("Sin Categoría");

        return new ProductDto(
                product.getId(),
                product.getInternalCode(),
                product.getBarcode(),
                product.getName(),
                categoryName,
                product.getPurchasePrice(),
                product.getSalePrice(),
                product.getQuantityAvailable(),
                product.getMinStock(),
                product.isActive(),
                product.getImageUrl()
        );
    }
}
