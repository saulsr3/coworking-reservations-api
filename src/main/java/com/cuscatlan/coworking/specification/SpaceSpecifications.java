package com.cuscatlan.coworking.specification;

import com.cuscatlan.coworking.entity.Space;
import com.cuscatlan.coworking.entity.SpaceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construye la query de filtrado de espacios de forma dinámica: solo agrega
 * al WHERE los criterios que el cliente realmente envió.
 */
public class SpaceSpecifications {

    private SpaceSpecifications() {
    }

    public static Specification<Space> withFilters(SpaceType type, Integer minCapacity, String location) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}