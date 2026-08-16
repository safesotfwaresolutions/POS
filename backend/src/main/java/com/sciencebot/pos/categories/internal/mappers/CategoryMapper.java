package com.sciencebot.pos.categories.internal.mappers;

import com.sciencebot.pos.categories.CategoryDto;
import com.sciencebot.pos.categories.CategoryProductCountProvider;
import com.sciencebot.pos.categories.internal.entities.Category;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    private final List<CategoryProductCountProvider> countProviders;

    public CategoryMapper(@Lazy List<CategoryProductCountProvider> countProviders) {
        this.countProviders = countProviders;
    }

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        int productCount = 0;
        if (countProviders != null) {
            for (CategoryProductCountProvider provider : countProviders) {
                productCount += provider.getProductCount(category.getId());
            }
        }

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                productCount
        );
    }
}
