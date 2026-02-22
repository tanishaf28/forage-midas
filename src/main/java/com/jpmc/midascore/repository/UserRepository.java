package com.jpmc.midascore.repository;  //This is the repository layer. It is responsible for interacting with the database. It uses Spring Data JPA to provide CRUD operations for the UserRecord entity. It also defines a custom query method findById to find a user by their id.

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserRecord, Long> {
    UserRecord findById(long id);
}
