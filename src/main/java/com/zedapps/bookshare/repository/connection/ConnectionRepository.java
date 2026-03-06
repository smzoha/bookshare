package com.zedapps.bookshare.repository.connection;

import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 5/3/26
 **/
@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findConnectionsByPerson1(Login person1);

    @Modifying
    @Query(value = """
            INSERT INTO connection (id, person1_id, person2_id) VALUES
            (nextval('connection_seq'), :person1Id, :person2Id),
            (nextval('connection_seq'), :person2Id, :person1Id)
            """, nativeQuery = true)
    void saveConnection(Long person1Id, Long person2Id);

    @Modifying
    @Query(value = """
            DELETE FROM connection
            WHERE person1_id = :person1Id AND person2_id = :person2Id
            """, nativeQuery = true)
    void deleteConnection(Long person1Id, Long person2Id);
}
