package univ.StockManger.StockManger.service;

import org.springframework.data.jpa.domain.Specification;
import univ.StockManger.StockManger.entity.Bon;

public class BonSpecification {

    public static Specification<Bon> search(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("id").as(String.class), pattern),
                    criteriaBuilder.like(root.get("type").as(String.class), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("magasinier").get("nom")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("pdfPath")), pattern)
            );
        };
    }
}
