package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.User;

import java.util.Optional;
//                                                    entity, type of the id
public interface UserRepository extends JpaRepository<User, Long> {
/*
    save(S entity): Saves an entity.
            saveAll(Iterable<S> entities): Saves multiple entities.
            findById(ID id): Finds an entity by ID, returns Optional.
    existsById(ID id): Checks if an entity exists by ID.
            findAll(): Finds all entities.
            findAllById(Iterable<ID> ids): Finds entities by a list of IDs.
            count(): Counts all entities.
            deleteById(ID id): Deletes an entity by ID.
            delete(T entity): Deletes an entity.
            deleteAllById(Iterable<ID> ids): Deletes entities by a list of IDs.
            deleteAll(Iterable<T> entities): Deletes multiple entities.
            deleteAll(): Deletes all entities.
    From PagingAndSortingRepository:
    findAll(Sort sort): Finds all entities with sorting.
            findAll(Pageable pageable): Finds entities with pagination and sorting.
    Additional JpaRepository methods:
    flush(): Flushes pending changes to the database.
    saveAndFlush(S entity): Saves and flushes an entity.
            deleteInBatch(Iterable<T> entities): Deletes entities in a batch.
            deleteAllInBatch(): Deletes all entities in a batch.
    getOne(ID id): Deprecated; use findById instead.
            getById(ID id): Finds an entity by ID (throws exception if not found).
*/

    Optional<User> findByEmail(String email);
//    Optional<User> findByNomContaining(String email);
    boolean existsByEmail(String email);

}
