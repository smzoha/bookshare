package com.zedapps.bookshare.repository.connection;

import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 5/3/26
 **/
@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findConnectionsByPerson1(Login person1);

    Connection findConnectionByPerson1AndPerson2(Login person1, Login person2);
}
